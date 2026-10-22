package translatorbot.translation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MyMemoryTranslatorTest {

    private static final String OK_JSON =
            "{\"responseData\":{\"translatedText\":\"hello\",\"match\":1},\"responseStatus\":200}";

    private final FakeHttpGateway gateway = new FakeHttpGateway();
    private final MyMemoryTranslator translator = new MyMemoryTranslator(gateway);

    @Test
    void translatesAndParsesResponse() {
        gateway.response = OK_JSON;

        TranslationResult result = translator.translate("привет", Language.RUSSIAN, Language.ENGLISH);

        assertEquals("hello", result.translatedText());
        assertEquals(Language.RUSSIAN, result.source());
        assertEquals(Language.ENGLISH, result.target());
    }

    @Test
    void buildsUrlWithEncodedTextAndLangPair() {
        gateway.response = OK_JSON;

        translator.translate("привет", Language.RUSSIAN, Language.GERMAN);

        assertTrue(gateway.lastUrl.contains("langpair=ru%7Cde"));
        assertTrue(gateway.lastUrl.contains("q=%D0%BF%D1%80%D0%B8%D0%B2%D0%B5%D1%82"));
    }

    @Test
    void throwsOnErrorStatus() {
        gateway.response = "{\"responseStatus\":403,\"responseDetails\":\"INVALID SOURCE LANGUAGE\"}";

        assertThrows(TranslationException.class,
                () -> translator.translate("hi", Language.RUSSIAN, Language.ENGLISH));
    }

    @Test
    void throwsOnQuotaWarning() {
        gateway.response = "{\"responseData\":{\"translatedText\":\"MYMEMORY WARNING: YOU USED ALL AVAILABLE FREE TRANSLATIONS\"},\"responseStatus\":200}";

        assertThrows(TranslationException.class,
                () -> translator.translate("hi", Language.RUSSIAN, Language.ENGLISH));
    }

    @Test
    void throwsOnEmptyTranslation() {
        gateway.response = "{\"responseData\":{\"translatedText\":\"\"},\"responseStatus\":200}";

        assertThrows(TranslationException.class,
                () -> translator.translate("hi", Language.RUSSIAN, Language.ENGLISH));
    }

    @Test
    void throwsOnInvalidJson() {
        gateway.response = "<html>not json</html>";

        assertThrows(TranslationException.class,
                () -> translator.translate("hi", Language.RUSSIAN, Language.ENGLISH));
    }

    private static class FakeHttpGateway implements HttpGateway {
        String lastUrl;
        String response;

        @Override
        public String get(String url) {
            this.lastUrl = url;
            return response;
        }
    }
}
