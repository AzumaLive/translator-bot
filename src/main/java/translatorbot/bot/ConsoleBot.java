package translatorbot.bot;

import translatorbot.core.BotBrain;
import translatorbot.core.UserSession;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;

public class ConsoleBot implements Bot {

    private final BotBrain brain;
    private final InputStream input;
    private final PrintStream output;

    public ConsoleBot(BotBrain brain) {
        this(brain, System.in, System.out);
    }

    public ConsoleBot(BotBrain brain, InputStream input, PrintStream output) {
        this.brain = brain;
        this.input = input;
        this.output = output;
    }

    @Override
    public void run() {
        UserSession session = new UserSession();
        output.println(brain.handle("/start", session));
        output.println();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line;
            output.print("> ");
            while ((line = reader.readLine()) != null) {
                if (isExit(line)) {
                    break;
                }
                output.println(brain.handle(line, session));
                output.println();
                output.print("> ");
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Ошибка чтения из консоли", e);
        }
    }

    private boolean isExit(String line) {
        String trimmed = line.trim();
        return trimmed.equalsIgnoreCase("/exit") || trimmed.equalsIgnoreCase("/quit");
    }
}
