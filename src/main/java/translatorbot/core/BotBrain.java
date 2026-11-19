package translatorbot.core;

import translatorbot.dictionary.DictionaryEntry;
import translatorbot.dictionary.PersonalDictionary;
import translatorbot.translation.Language;
import translatorbot.translation.TranslationException;
import translatorbot.translation.TranslationResult;
import translatorbot.translation.Translator;

import java.util.List;
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
            case "/source" -> handleSource(args, session);
            case "/save" -> handleSave(session);
            case "/dictionary" -> handleDictionary(session);
            case "/delete" -> handleDelete(args, session);
            default -> "Неизвестная команда. Введите /help для списка команд.";
        };
    }

    private String greeting() {
        return """
                Привет! Я бот-переводчик.
                Перевожу текст с одного языка на другой.
                
                Просто отправьте текст — я его переведу.
                Введите /help, чтобы увидеть список команд.""";
    }

    private String helpText() {
        return """
                Доступные команды:
                /start - приветствие
                /help - список команд
                /lang - текущий язык перевода и список языков
                /lang <код> - выбрать язык перевода (например: /lang en)
                /source - текущий язык оригинала
                /source <код> - выбрать язык оригинала (например: /source de)
                /source auto - автоопределение языка оригинала
                /save - сохранить последний перевод в словарик
                /dictionary - показать сохранённые переводы
                /delete <номер> - удалить запись из словарика
                
                Просто отправьте текст — и он будет переведён на выбранный язык.""";
    }

    private String handleLang(String arg, UserSession session) {
        if (arg.isEmpty()) {
            Language current = session.getTargetLanguage();
            return "Текущий язык перевода: " + current + "\n\n" + availableTargetLanguagesText();
        }

        Optional<Language> found = Language.fromCode(arg).or(() -> Language.fromName(arg));
        if (found.isEmpty() || found.get() == Language.RUSSIAN || found.get() == Language.AUTO) {
            return "Язык не найден.\n\n" + availableTargetLanguagesText();
        }

        session.setTargetLanguage(found.get());
        return "Язык перевода изменён на: " + found.get();
    }

    private String handleSource(String arg, UserSession session) {
        if (arg.isEmpty()) {
            Language current = session.getSourceLanguage();
            return "Язык оригинала: " + current + "\n\n" + availableSourceLanguagesText();
        }

        if (arg.equalsIgnoreCase("auto")) {
            session.setSourceLanguage(Language.AUTO);
            return "Автоопределение языка оригинала включено.";
        }

        Optional<Language> found = Language.fromCode(arg).or(() -> Language.fromName(arg));
        if (found.isEmpty() || found.get() == Language.AUTO) {
            return "Язык не найден.\n\n" + availableSourceLanguagesText();
        }

        session.setSourceLanguage(found.get());
        return "Язык оригинала изменён на: " + found.get();
    }

    private String translate(String text, UserSession session) {
        try {
            Language source = session.getSourceLanguage();
            TranslationResult result = translator.translate(text, source, session.getTargetLanguage());
            Language entrySource = source == Language.AUTO && result.detectedLanguage() != null
                    ? result.detectedLanguage()
                    : source;
            session.setLastTranslation(new DictionaryEntry(
                    result.originalText(),
                    result.translatedText(),
                    entrySource,
                    result.target()));
            if (source == Language.AUTO && result.detectedLanguage() != null) {
                return result.translatedText() + "\n\n(определён язык: " + result.detectedLanguage() + ")";
            }
            return result.translatedText();
        } catch (TranslationException e) {
            return "Не удалось перевести текст: " + e.getMessage();
        }
    }

    private String handleSave(UserSession session) {
        DictionaryEntry last = session.getLastTranslation();
        if (last == null) {
            return "Сначала переведите текст, а затем сохраните его командой /save.";
        }

        PersonalDictionary dictionary = session.getDictionary();
        if (dictionary.contains(last)) {
            return "Эта пара уже есть в словарике.";
        }

        dictionary.add(last);
        return "Сохранено в словарик: " + last.originalText() + " → " + last.translatedText()
                + " [" + last.source().getCode() + " → " + last.target().getCode() + "]";
    }

    private String handleDictionary(UserSession session) {
        List<DictionaryEntry> entries = session.getDictionary().getAll();
        if (entries.isEmpty()) {
            return "Словарик пуст. Переведите текст и сохраните его командой /save.";
        }

        StringBuilder sb = new StringBuilder("Твой словарик:\n");
        for (int i = 0; i < entries.size(); i++) {
            DictionaryEntry entry = entries.get(i);
            sb.append(i + 1).append(". ")
                    .append(entry.originalText()).append(" → ").append(entry.translatedText())
                    .append(" [").append(entry.source().getCode()).append(" → ")
                    .append(entry.target().getCode()).append("]\n");
        }
        return sb.toString().stripTrailing();
    }

    private String handleDelete(String arg, UserSession session) {
        PersonalDictionary dictionary = session.getDictionary();
        if (dictionary.isEmpty()) {
            return "Словарик пуст, удалять нечего.";
        }

        int number;
        try {
            number = Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            return "Укажите номер записи: /delete <номер>. Посмотреть словарик: /dictionary";
        }

        if (!dictionary.remove(number)) {
            return "Записи с номером " + number + " нет. Посмотреть словарик: /dictionary";
        }
        return "Запись удалена.";
    }

    private String availableTargetLanguagesText() {
        StringBuilder sb = new StringBuilder("Доступные языки перевода:\n");
        for (Language language : Language.values()) {
            if (language == Language.RUSSIAN || language == Language.AUTO) {
                continue;
            }
            sb.append(language.getCode()).append(" - ").append(language.getRussianName()).append('\n');
        }
        return sb.toString().stripTrailing();
    }

    private String availableSourceLanguagesText() {
        StringBuilder sb = new StringBuilder("Доступные языки оригинала:\n");
        sb.append("auto - автоопределение\n");
        for (Language language : Language.values()) {
            if (language == Language.AUTO) {
                continue;
            }
            sb.append(language.getCode()).append(" - ").append(language.getRussianName()).append('\n');
        }
        return sb.toString().stripTrailing();
    }
}
