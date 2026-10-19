package translatorbot;

import translatorbot.bot.ConsoleBot;
import translatorbot.bot.TelegramBot;
import translatorbot.config.Config;
import translatorbot.core.BotBrain;
import translatorbot.translation.JavaNetHttpGateway;
import translatorbot.translation.MyMemoryTranslator;
import translatorbot.translation.Translator;

public class Main {

    public static void main(String[] args) {
        Translator translator = new MyMemoryTranslator(new JavaNetHttpGateway());
        BotBrain brain = new BotBrain(translator);

        String mode = args.length > 0 ? args[0] : "console";
        switch (mode) {
            case "console" -> new ConsoleBot(brain).run();
            case "telegram" -> runTelegram(brain);
            default -> {
                System.err.println("Неизвестный режим: " + mode + ". Используйте console или telegram.");
                System.exit(1);
            }
        }
    }

    private static void runTelegram(BotBrain brain) {
        String token = new Config().getTelegramToken();
        if (token == null || token.isBlank()) {
            throw new IllegalStateException("Задайте токен в config.properties (telegram.bot.token)");
        }

        TelegramBot bot = new TelegramBot(token, new JavaNetHttpGateway(), brain);
        bot.run();
    }
}
