package translatorbot.core;

import translatorbot.translation.Language;

public class UserSession {

    private Language targetLanguage = Language.ENGLISH;

    public Language getTargetLanguage() {
        return targetLanguage;
    }

    public void setTargetLanguage(Language targetLanguage) {
        this.targetLanguage = targetLanguage;
    }
}
