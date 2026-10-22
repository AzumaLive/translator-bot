package translatorbot.translation;

public record TranslationResult(String originalText,
                                String translatedText,
                                Language source,
                                Language target) {
}
