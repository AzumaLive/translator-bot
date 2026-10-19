package translatorbot.bot;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import translatorbot.core.BotBrain;
import translatorbot.core.UserSession;
import translatorbot.translation.HttpGateway;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TelegramBot implements Bot {

    private static final String API_BASE = "https://api.telegram.org/bot";
    private static final long POLL_TIMEOUT_SECONDS = 10;

    private final String token;
    private final HttpGateway gateway;
    private final BotBrain brain;
    private final Map<Long, UserSession> sessions = new ConcurrentHashMap<>();

    public TelegramBot(String token, HttpGateway gateway, BotBrain brain) {
        this.token = token;
        this.gateway = gateway;
        this.brain = brain;
    }

    @Override
    public void run() {
        long offset = 0;
        while (true) {
            try {
                String json = gateway.get(buildGetUpdatesUrl(offset));
                for (IncomingUpdate update : parseUpdates(json)) {
                    offset = Math.max(offset, update.updateId() + 1);
                    handleUpdate(update);
                }
            } catch (IOException e) {
                System.err.println("Ошибка сети при опросе Telegram: " + e.getMessage());
            } catch (RuntimeException e) {
                System.err.println("Ошибка обработки обновлений: " + e.getMessage());
            }
        }
    }

    String buildGetUpdatesUrl(long offset) {
        return API_BASE + token + "/getUpdates?timeout=" + POLL_TIMEOUT_SECONDS + "&offset=" + offset;
    }

    String buildSendMessageUrl(long chatId, String text) {
        String encodedText = URLEncoder.encode(text, StandardCharsets.UTF_8);
        return API_BASE + token + "/sendMessage?chat_id=" + chatId + "&text=" + encodedText;
    }

    List<IncomingUpdate> parseUpdates(String json) {
        List<IncomingUpdate> result = new ArrayList<>();
        JsonObject root = JsonParser.parseString(json).getAsJsonObject();
        if (!root.has("result") || root.get("result").isJsonNull()) {
            return result;
        }

        JsonArray array = root.getAsJsonArray("result");
        for (JsonElement element : array) {
            JsonObject update = element.getAsJsonObject();
            long updateId = update.get("update_id").getAsLong();

            Long chatId = null;
            String text = null;
            if (update.has("message") && !update.get("message").isJsonNull()) {
                JsonObject message = update.getAsJsonObject("message");
                if (message.has("text") && !message.get("text").isJsonNull()) {
                    chatId = message.getAsJsonObject("chat").get("id").getAsLong();
                    text = message.get("text").getAsString();
                }
            }
            result.add(new IncomingUpdate(updateId, chatId, text));
        }
        return result;
    }

    void handleUpdate(IncomingUpdate update) {
        if (update.chatId() == null || update.text() == null) {
            return;
        }
        UserSession session = sessions.computeIfAbsent(update.chatId(), id -> new UserSession());
        String reply = brain.handle(update.text(), session);
        try {
            gateway.get(buildSendMessageUrl(update.chatId(), reply));
        } catch (IOException e) {
            System.err.println("Не удалось отправить сообщение: " + e.getMessage());
        }
    }

    record IncomingUpdate(long updateId, Long chatId, String text) {
    }
}
