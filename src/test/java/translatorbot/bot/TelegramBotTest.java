package translatorbot.bot;

import org.junit.jupiter.api.Test;
import translatorbot.core.BotBrain;
import translatorbot.translation.HttpGateway;
import translatorbot.translation.Language;
import translatorbot.translation.TranslationResult;
import translatorbot.translation.Translator;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TelegramBotTest {

    private static final String UPDATES_JSON =
            "{\"ok\":true,\"result\":["
                    + "{\"update_id\":100,\"message\":{\"message_id\":10,\"chat\":{\"id\":42},\"text\":\"/start\"}},"
                    + "{\"update_id\":101,\"message\":{\"message_id\":11,\"chat\":{\"id\":42},\"text\":\"/lang de\"}}"
                    + "]}";

    @Test
    void parseUpdatesExtractsUpdateIdChatIdAndText() {
        TelegramBot bot = new TelegramBot("token", new FakeGateway(), new BotBrain(new EchoTranslator()));

        List<TelegramBot.IncomingUpdate> updates = bot.parseUpdates(UPDATES_JSON);

        assertEquals(2, updates.size());
        assertEquals(100, updates.getFirst().updateId());
        assertEquals(42L, updates.getFirst().chatId());
        assertEquals("/start", updates.getFirst().text());
        assertEquals(101, updates.get(1).updateId());
        assertEquals("/lang de", updates.get(1).text());
    }

    @Test
    void parseUpdatesSkipsNonTextMessages() {
        String json = "{\"ok\":true,\"result\":["
                + "{\"update_id\":5,\"message\":{\"message_id\":1,\"chat\":{\"id\":1}}}"
                + "]}";
        TelegramBot bot = new TelegramBot("token", new FakeGateway(), new BotBrain(new EchoTranslator()));

        List<TelegramBot.IncomingUpdate> updates = bot.parseUpdates(json);

        assertEquals(1, updates.size());
        assertNull(updates.getFirst().chatId());
        assertNull(updates.getFirst().text());
    }

    @Test
    void buildGetUpdatesUrlContainsTokenAndOffset() {
        TelegramBot bot = new TelegramBot("123:ABC", new FakeGateway(), new BotBrain(new EchoTranslator()));

        String url = bot.buildGetUpdatesUrl(99);

        assertTrue(url.startsWith("https://api.telegram.org/bot123:ABC/getUpdates"));
        assertTrue(url.contains("offset=99"));
    }

    @Test
    void buildSendMessageUrlEncodesText() {
        TelegramBot bot = new TelegramBot("123:ABC", new FakeGateway(), new BotBrain(new EchoTranslator()));

        String url = bot.buildSendMessageUrl(42, "Привет, как дела?");

        assertTrue(url.contains("chat_id=42"));
        assertTrue(url.contains("text=%D0%9F%D1%80%D0%B8%D0%B2%D0%B5%D1%82%2C+%D0%BA%D0%B0%D0%BA+%D0%B4%D0%B5%D0%BB%D0%B0%3F"));
    }

    @Test
    void handleUpdateSendsReplyThroughGateway() {
        FakeGateway gateway = new FakeGateway();
        TelegramBot bot = new TelegramBot("token", gateway, new BotBrain(new EchoTranslator()));

        bot.handleUpdate(new TelegramBot.IncomingUpdate(7, 42L, "привет"));

        assertEquals(1, gateway.urls.size());
        assertTrue(gateway.urls.getFirst().contains("sendMessage"));
        assertTrue(gateway.urls.getFirst().contains("chat_id=42"));
    }

    private static class FakeGateway implements HttpGateway {
        final List<String> urls = new ArrayList<>();

        @Override
        public String get(String url) {
            urls.add(url);
            return "{\"ok\":true,\"result\":[]}";
        }
    }

    private static class EchoTranslator implements Translator {
        @Override
        public TranslationResult translate(String text, Language source, Language target) {
            return new TranslationResult(text, "[" + target.getCode() + "]" + text, source, target);
        }
    }
}
