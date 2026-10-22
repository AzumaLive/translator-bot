package translatorbot.bot;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import translatorbot.core.BotBrain;
import translatorbot.translation.Language;
import translatorbot.translation.TranslationResult;
import translatorbot.translation.Translator;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsoleBotTest {

    private BotBrain brain;
    private ByteArrayOutputStream out;

    @BeforeEach
    void setUp() {
        brain = new BotBrain(new EchoTranslator());
        out = new ByteArrayOutputStream();
    }

    @Test
    void printsGreetingOnStart() {
        runBot("/exit\n");

        String output = output();
        assertTrue(output.contains("Привет"));
        assertTrue(output.contains("/help"));
    }

    @Test
    void respondsToCommand() {
        runBot("/help\n/exit\n");

        String output = output();
        assertTrue(output.contains("/start"));
        assertTrue(output.contains("/lang"));
    }

    @Test
    void sessionKeepsStateBetweenCommands() {
        runBot("/lang de\nпривет\n/exit\n");

        String output = output();
        assertTrue(output.contains("немецкий"));
        assertTrue(output.contains("[de]привет"));
    }

    @Test
    void exitStopsBot() {
        runBot("/exit\n/help\n");

        String output = output();
        assertFalse(output.contains("/lang"));
    }

    private void runBot(String input) {
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        PrintStream ps = new PrintStream(out, true, StandardCharsets.UTF_8);
        new ConsoleBot(brain, in, ps).run();
    }

    private String output() {
        return out.toString(StandardCharsets.UTF_8);
    }

    private static class EchoTranslator implements Translator {
        @Override
        public TranslationResult translate(String text, Language source, Language target) {
            return new TranslationResult(text, "[" + target.getCode() + "]" + text, source, target);
        }
    }
}
