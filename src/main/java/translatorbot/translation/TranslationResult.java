package translatorbot.translation;

public record TranslationResult(String originalText,
                                String translatedText,
                                Language source,
                                Language target,
                                String detectedLanguage) {

    public TranslationResult(String originalText, String translatedText, Language source, Language target) {
        this(originalText, translatedText, source, target, null);
    }
}
