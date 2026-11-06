package translatorbot.core;

import translatorbot.translation.Language;

public class UserSession {

    private Language sourceLanguage = Language.RUSSIAN;
    private Language targetLanguage = Language.ENGLISH;

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
}
