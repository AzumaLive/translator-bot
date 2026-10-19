package translatorbot.core;

import translatorbot.translation.Language;
import translatorbot.translation.TranslationException;
import translatorbot.translation.TranslationResult;
import translatorbot.translation.Translator;

import java.util.Optional;

public class BotBrain {

    private final Translator translator;

    public BotBrain(Translator translator) {
        this.translator = translator;
    }

    public String handle(String message, UserSession session) {
        String command = message.trim();
        if (command.isEmpty()) {
            return "Введите текст для перевода или /help для списка команд.";
        }

        if (!command.startsWith("/")) {
            return translate(command, session);
        }

        String[] parts = command.split("\\s+", 2);
        String name = parts[0];
        String args = parts.length > 1 ? parts[1].trim() : "";

        return switch (name) {
            case "/start" -> greeting();
            case "/help" -> helpText();
            case "/lang" -> handleLang(args, session);
            default -> "Неизвестная команда. Введите /help для списка команд.";
        };
    }

    private String greeting() {
        return """
                Привет! Я бот-переводчик с русского языка.
                Перевожу текст на выбранный вами язык.
                
                Просто отправьте текст — я его переведу.
                Введите /help, чтобы увидеть список команд.""";
    }

    private String helpText() {
        return """
                Доступные команды:
                /start - приветствие
                /help - список команд
                /lang - текущий язык и список доступных языков
                /lang <код> - выбрать язык перевода (например: /lang en)
                
                Просто отправьте текст — и он будет переведён с русского на выбранный язык.""";
    }

    private String handleLang(String arg, UserSession session) {
        if (arg.isEmpty()) {
            Language current = session.getTargetLanguage();
            return "Текущий язык перевода: " + current + "\n\n" + availableLanguagesText();
        }

        Optional<Language> found = Language.fromCode(arg).or(() -> Language.fromName(arg));
        if (found.isEmpty() || found.get() == Language.RUSSIAN) {
            return "Язык не найден. Переводим только с русского.\n\n" + availableLanguagesText();
        }

        session.setTargetLanguage(found.get());
        return "Язык перевода изменён на: " + found.get();
    }

    private String translate(String text, UserSession session) {
        try {
            TranslationResult result = translator.translate(text, Language.RUSSIAN, session.getTargetLanguage());
            return result.translatedText();
        } catch (TranslationException e) {
            return "Не удалось перевести текст: " + e.getMessage();
        }
    }

    private String availableLanguagesText() {
        StringBuilder sb = new StringBuilder("Доступные языки:\n");
        for (Language language : Language.values()) {
            if (language == Language.RUSSIAN) {
                continue;
            }
            sb.append(language.getCode()).append(" - ").append(language.getRussianName()).append('\n');
        }
        return sb.toString().stripTrailing();
    }
}
