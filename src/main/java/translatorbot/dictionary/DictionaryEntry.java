package translatorbot.dictionary;

import translatorbot.translation.Language;

public record DictionaryEntry(
        String originalText,
        String translatedText,
        Language source,
        Language target) {
}
