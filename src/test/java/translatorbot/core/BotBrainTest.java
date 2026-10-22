package translatorbot.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

    private static class FakeTranslator implements Translator {
        String lastText;
        Language lastSource;
        Language lastTarget;

        @Override
        public TranslationResult translate(String text, Language source, Language target) {
            this.lastText = text;
            this.lastSource = source;
            this.lastTarget = target;
            return new TranslationResult(text, "[" + target.getCode() + "]" + text, source, target);
        }
    }
}
