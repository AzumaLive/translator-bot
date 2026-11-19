package translatorbot.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import translatorbot.dictionary.DictionaryEntry;
import translatorbot.dictionary.InMemoryDictionaryStorage;
import translatorbot.translation.Language;
import translatorbot.translation.TranslationResult;
import translatorbot.translation.Translator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BotBrainTest {

    private FakeTranslator translator;
    private BotBrain brain;
    private UserSession session;

    @BeforeEach
    void setUp() {
        translator = new FakeTranslator();
        brain = new BotBrain(translator);
        session = new UserSession();
    }

    @Test
    void startReturnsGreeting() {
        String reply = brain.handle("/start", session);
        assertTrue(reply.contains("Привет"));
    }

    @Test
    void helpListsCommands() {
        String reply = brain.handle("/help", session);
        assertTrue(reply.contains("/start"));
        assertTrue(reply.contains("/help"));
        assertTrue(reply.contains("/lang"));
    }

    @Test
    void langWithoutArgumentShowsCurrentAndList() {
        String reply = brain.handle("/lang", session);
        assertTrue(reply.contains("английский"));
        assertTrue(reply.contains("немецкий"));
    }

    @Test
    void langSetsTargetLanguage() {
        String reply = brain.handle("/lang de", session);

        assertEquals(Language.GERMAN, session.getTargetLanguage());
        assertTrue(reply.contains("немецкий"));
    }

    @Test
    void langAcceptsRussianName() {
        brain.handle("/lang французский", session);
        assertEquals(Language.FRENCH, session.getTargetLanguage());
    }

    @Test
    void langRejectsUnknownLanguage() {
        String reply = brain.handle("/lang xx", session);

        assertEquals(Language.ENGLISH, session.getTargetLanguage());
        assertTrue(reply.contains("не найден"));
    }

    @Test
    void langRejectsRussianAsTarget() {
        brain.handle("/lang ru", session);
        assertEquals(Language.ENGLISH, session.getTargetLanguage());
    }

    @Test
    void sourceSetsSourceLanguage() {
        String reply = brain.handle("/source de", session);

        assertEquals(Language.GERMAN, session.getSourceLanguage());
        assertTrue(reply.contains("немецкий"));
    }

    @Test
    void sourceAutoEnablesAutoDetect() {
        String reply = brain.handle("/source auto", session);

        assertEquals(Language.AUTO, session.getSourceLanguage());
        assertTrue(reply.contains("Автоопределение"));
    }

    @Test
    void sourceWithoutArgumentShowsCurrent() {
        String reply = brain.handle("/source", session);

        assertTrue(reply.contains("русский"));
        assertTrue(reply.contains("auto"));
    }

    @Test
    void translateUsesSelectedSource() {
        brain.handle("/source it", session);

        String reply = brain.handle("привет", session);

        assertEquals(Language.ITALIAN, translator.lastSource);
        assertEquals("[en]привет", reply);
    }

    @Test
    void translateAutoShowsDetectedLanguage() {
        brain.handle("/source auto", session);
        translator.detectedLanguage = Language.FRENCH;

        String reply = brain.handle("bonjour", session);

        assertEquals(Language.AUTO, translator.lastSource);
        assertTrue(reply.contains("fr"));
    }

    @Test
    void translateUsesRussianAsSourceAndSelectedTarget() {
        brain.handle("/lang it", session);

        String reply = brain.handle("привет", session);

        assertEquals("привет", translator.lastText);
        assertEquals(Language.RUSSIAN, translator.lastSource);
        assertEquals(Language.ITALIAN, translator.lastTarget);
        assertEquals("[it]привет", reply);
    }

    @Test
    void plainTextIsTranslated() {
        String reply = brain.handle("привет, мир", session);

        assertEquals("привет, мир", translator.lastText);
        assertEquals(Language.RUSSIAN, translator.lastSource);
        assertEquals(Language.ENGLISH, translator.lastTarget);
        assertEquals("[en]привет, мир", reply);
    }

    @Test
    void unknownCommandShowsHelpHint() {
        String reply = brain.handle("/blabla", session);
        assertTrue(reply.contains("/help"));
    }

    @Test
    void helpListsDictionaryCommands() {
        String reply = brain.handle("/help", session);

        assertTrue(reply.contains("/save"));
        assertTrue(reply.contains("/dictionary"));
        assertTrue(reply.contains("/delete"));
    }

    @Test
    void saveWithoutTranslationExplainsWhatToDo() {
        String reply = brain.handle("/save", session);

        assertTrue(reply.contains("/save"));
        assertTrue(session.getDictionary().isEmpty());
    }

    @Test
    void saveStoresLastTranslation() {
        brain.handle("привет", session);

        String reply = brain.handle("/save", session);

        assertTrue(reply.contains("Сохранено"));
        assertTrue(reply.contains("привет"));
        assertEquals(1, session.getDictionary().getAll().size());
    }

    @Test
    void saveDoesNotDuplicateEntry() {
        brain.handle("привет", session);
        brain.handle("/save", session);

        String reply = brain.handle("/save", session);

        assertTrue(reply.contains("уже есть"));
        assertEquals(1, session.getDictionary().getAll().size());
    }

    @Test
    void dictionaryShowsSavedEntries() {
        brain.handle("привет", session);
        brain.handle("/save", session);

        String reply = brain.handle("/dictionary", session);

        assertTrue(reply.contains("1. привет"));
        assertTrue(reply.contains("ru → en"));
    }

    @Test
    void dictionaryIsEmptyAtStart() {
        String reply = brain.handle("/dictionary", session);

        assertTrue(reply.contains("пуст"));
    }

    @Test
    void deleteRemovesEntryByNumber() {
        brain.handle("привет", session);
        brain.handle("/save", session);
        brain.handle("пока", session);
        brain.handle("/save", session);

        String reply = brain.handle("/delete 1", session);

        assertTrue(reply.contains("удалена"));
        assertEquals(1, session.getDictionary().getAll().size());
        assertTrue(session.getDictionary().getAll().getFirst().originalText().equals("пока"));
    }

    @Test
    void deleteRejectsInvalidNumber() {
        brain.handle("привет", session);
        brain.handle("/save", session);

        String reply = brain.handle("/delete 5", session);

        assertTrue(reply.contains("нет"));
        assertEquals(1, session.getDictionary().getAll().size());
    }

    @Test
    void deleteRejectsNonNumericArgument() {
        brain.handle("привет", session);
        brain.handle("/save", session);

        String reply = brain.handle("/delete abc", session);

        assertTrue(reply.contains("номер"));
        assertEquals(1, session.getDictionary().getAll().size());
    }

    @Test
    void deleteOnEmptyDictionaryExplainsWhy() {
        String reply = brain.handle("/delete 1", session);

        assertTrue(reply.contains("пуст"));
    }

    @Test
    void savedEntryUsesDetectedLanguageForAutoSource() {
        brain.handle("/source auto", session);
        translator.detectedLanguage = Language.FRENCH;

        brain.handle("bonjour", session);
        brain.handle("/save", session);

        String reply = brain.handle("/dictionary", session);
        assertTrue(reply.contains("fr → en"));
    }

    @Test
    void dictionarySurvivesNewSessionWithSameStorage() {
        InMemoryDictionaryStorage storage = new InMemoryDictionaryStorage();
        UserSession first = new UserSession(storage, 42L);
        first.setLastTranslation(new DictionaryEntry("кот", "cat", Language.RUSSIAN, Language.ENGLISH));
        brain.handle("/save", first);

        UserSession second = new UserSession(storage, 42L);

        assertEquals(1, second.getDictionary().getAll().size());
        assertEquals("кот", second.getDictionary().getAll().getFirst().originalText());
    }

    private static class FakeTranslator implements Translator {
        String lastText;
        Language lastSource;
        Language lastTarget;
        Language detectedLanguage;

        @Override
        public TranslationResult translate(String text, Language source, Language target) {
            this.lastText = text;
            this.lastSource = source;
            this.lastTarget = target;
            return new TranslationResult(text, "[" + target.getCode() + "]" + text, source, target, detectedLanguage);
        }
    }
}
