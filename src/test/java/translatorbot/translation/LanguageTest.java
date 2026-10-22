package translatorbot.translation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LanguageTest {

    @Test
    void fromCodeIsCaseInsensitive() {
        assertEquals(Language.ENGLISH, Language.fromCode("en").orElseThrow());
        assertEquals(Language.ENGLISH, Language.fromCode("EN").orElseThrow());
        assertEquals(Language.CHINESE, Language.fromCode("zh-CN").orElseThrow());
    }

    @Test
    void fromCodeUnknownReturnsEmpty() {
        assertTrue(Language.fromCode("xx").isEmpty());
    }

    @Test
    void fromNameIsCaseInsensitive() {
        assertEquals(Language.ENGLISH, Language.fromName("английский").orElseThrow());
        assertEquals(Language.GERMAN, Language.fromName("Немецкий").orElseThrow());
    }

    @Test
    void fromNameUnknownReturnsEmpty() {
        assertTrue(Language.fromName("марсианский").isEmpty());
    }
}
