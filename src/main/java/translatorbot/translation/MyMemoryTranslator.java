package translatorbot.translation;

import com.google.gson.Gson;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class MyMemoryTranslator implements Translator {

    private static final String API_URL = "https://api.mymemory.translated.net/get";
    private static final String WARNING_PREFIX = "MYMEMORY WARNING";

    private final HttpGateway httpGateway;
    private final Gson gson;

    public MyMemoryTranslator(HttpGateway httpGateway) {
        this.httpGateway = httpGateway;
        this.gson = new Gson();
    }

    @Override
    public TranslationResult translate(String text, Language source, Language target) {
        String url = buildUrl(text, source, target);
        String json;
        try {
            json = httpGateway.get(url);
        } catch (IOException e) {
            throw new TranslationException("Ошибка сети при обращении к сервису перевода", e);
        }
        return parseResponse(text, source, target, json);
    }

    String buildUrl(String text, Language source, Language target) {
        String encoded = URLEncoder.encode(text, StandardCharsets.UTF_8);
        String langPair = URLEncoder.encode(source.getCode() + "|" + target.getCode(), StandardCharsets.UTF_8);
        return API_URL + "?q=" + encoded + "&langpair=" + langPair;
    }

    private TranslationResult parseResponse(String text, Language source, Language target, String json) {
        MyMemoryResponse response;
        try {
            response = gson.fromJson(json, MyMemoryResponse.class);
        } catch (RuntimeException e) {
            throw new TranslationException("Некорректный ответ сервиса перевода", e);
        }

        if (response == null || response.responseData == null) {
            String details = response == null ? "пустой ответ" : response.responseDetails;
            throw new TranslationException("Сервис перевода вернул ошибку: " + details);
        }

        String translatedText = response.responseData.translatedText;
        if (response.responseStatus != 0 && response.responseStatus != 200) {
            throw new TranslationException("Сервис перевода вернул ошибку: " + response.responseDetails);
        }
        if (translatedText == null || translatedText.isBlank()) {
            throw new TranslationException("Сервис перевода вернул пустой перевод");
        }
        if (translatedText.startsWith(WARNING_PREFIX)) {
            throw new TranslationException(translatedText);
        }

        return new TranslationResult(text, translatedText, source, target);
    }

    private static class MyMemoryResponse {
        ResponseData responseData;
        int responseStatus;
        String responseDetails;
    }

    private static class ResponseData {
        String translatedText;
    }
}
