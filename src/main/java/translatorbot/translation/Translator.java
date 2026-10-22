package translatorbot.translation;

public interface Translator {

    TranslationResult translate(String text, Language source, Language target);
}
