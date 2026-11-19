package translatorbot.core;

import translatorbot.dictionary.DictionaryEntry;
import translatorbot.dictionary.DictionaryStorage;
import translatorbot.dictionary.InMemoryDictionaryStorage;
import translatorbot.dictionary.PersonalDictionary;
import translatorbot.translation.Language;

public class UserSession {

    private final PersonalDictionary dictionary;

    private Language sourceLanguage = Language.RUSSIAN;
    private Language targetLanguage = Language.ENGLISH;
    private DictionaryEntry lastTranslation;

    public UserSession() {
        this(new InMemoryDictionaryStorage(), 0L);
    }

    public UserSession(DictionaryStorage dictionaryStorage, long ownerId) {
        this.dictionary = new PersonalDictionary(dictionaryStorage, ownerId);
    }

    public Language getSourceLanguage() {
        return sourceLanguage;
    }

    public void setSourceLanguage(Language sourceLanguage) {
        this.sourceLanguage = sourceLanguage;
    }

    public Language getTargetLanguage() {
        return targetLanguage;
    }

    public void setTargetLanguage(Language targetLanguage) {
        this.targetLanguage = targetLanguage;
    }

    public DictionaryEntry getLastTranslation() {
        return lastTranslation;
    }

    public void setLastTranslation(DictionaryEntry lastTranslation) {
        this.lastTranslation = lastTranslation;
    }

    public PersonalDictionary getDictionary() {
        return dictionary;
    }
}
