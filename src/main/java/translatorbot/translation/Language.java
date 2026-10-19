package translatorbot.translation;

import java.util.Optional;

public enum Language {

    ENGLISH("en", "английский"),
    GERMAN("de", "немецкий"),
    FRENCH("fr", "французский"),
    SPANISH("es", "испанский"),
    ITALIAN("it", "итальянский"),
    CHINESE("zh-CN", "китайский"),
    JAPANESE("ja", "японский"),
    TURKISH("tr", "турецкий"),
    UKRAINIAN("uk", "украинский"),
    KAZAKH("kk", "казахский"),
    RUSSIAN("ru", "русский");

    private final String code;
    private final String russianName;

    Language(String code, String russianName) {
        this.code = code;
        this.russianName = russianName;
    }

    public String getCode() {
        return code;
    }

    public String getRussianName() {
        return russianName;
    }

    public static Optional<Language> fromCode(String code) {
        for (Language language : values()) {
            if (language.code.equalsIgnoreCase(code)) {
                return Optional.of(language);
            }
        }
        return Optional.empty();
    }

    public static Optional<Language> fromName(String name) {
        for (Language language : values()) {
            if (language.russianName.equalsIgnoreCase(name)) {
                return Optional.of(language);
            }
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return russianName + " (" + code + ")";
    }
}
