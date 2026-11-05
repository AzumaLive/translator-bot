package translatorbot.translation;

import com.google.gson.annotations.SerializedName;

import java.util.Optional;

public enum Language {

    @SerializedName("en")
    ENGLISH("en", "английский"),
    @SerializedName("de")
    GERMAN("de", "немецкий"),
    @SerializedName("fr")
    FRENCH("fr", "французский"),
    @SerializedName("es")
    SPANISH("es", "испанский"),
    @SerializedName("it")
    ITALIAN("it", "итальянский"),
    @SerializedName("zh-CN")
    CHINESE("zh-CN", "китайский"),
    @SerializedName("ja")
    JAPANESE("ja", "японский"),
    @SerializedName("tr")
    TURKISH("tr", "турецкий"),
    @SerializedName("uk")
    UKRAINIAN("uk", "украинский"),
    @SerializedName("kk")
    KAZAKH("kk", "казахский"),
    @SerializedName("ru")
    RUSSIAN("ru", "русский"),
    @SerializedName("Autodetect")
    AUTO("Autodetect", "автоопределение");

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
