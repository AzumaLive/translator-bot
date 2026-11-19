package translatorbot.dictionary;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import translatorbot.translation.Language;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersonalDictionaryTest {

    private InMemoryDictionaryStorage storage;
    private PersonalDictionary dictionary;

    @BeforeEach
    void setUp() {
        storage = new InMemoryDictionaryStorage();
        dictionary = new PersonalDictionary(storage, 1L);
    }

    @Test
    void newDictionaryIsEmpty() {
        assertTrue(dictionary.isEmpty());
        assertTrue(dictionary.getAll().isEmpty());
    }

    @Test
    void addStoresEntry() {
        DictionaryEntry entry = entry("привет", "hello");

        dictionary.add(entry);

        assertFalse(dictionary.isEmpty());
        assertEquals(1, dictionary.getAll().size());
        assertEquals(entry, dictionary.getAll().getFirst());
    }

    @Test
    void addSavesToStorage() {
        dictionary.add(entry("привет", "hello"));

        assertEquals(1, storage.load(1L).size());
    }

    @Test
    void containsFindsAddedEntry() {
        DictionaryEntry entry = entry("привет", "hello");
        dictionary.add(entry);

        assertTrue(dictionary.contains(entry));
        assertFalse(dictionary.contains(entry("пока", "bye")));
    }

    @Test
    void removeDeletesEntryByNumber() {
        DictionaryEntry first = entry("привет", "hello");
        DictionaryEntry second = entry("пока", "bye");
        dictionary.add(first);
        dictionary.add(second);

        assertTrue(dictionary.remove(1));

        assertEquals(1, dictionary.getAll().size());
        assertEquals(second, dictionary.getAll().getFirst());
    }

    @Test
    void removePersistsChanges() {
        dictionary.add(entry("привет", "hello"));

        dictionary.remove(1);

        assertTrue(storage.load(1L).isEmpty());
    }

    @Test
    void removeRejectsOutOfRangeNumber() {
        dictionary.add(entry("привет", "hello"));

        assertFalse(dictionary.remove(0));
        assertFalse(dictionary.remove(2));
        assertEquals(1, dictionary.getAll().size());
    }

    @Test
    void entriesAreLoadedFromStorageOnCreation() {
        DictionaryEntry entry = entry("привет", "hello");
        storage.save(1L, java.util.List.of(entry));

        PersonalDictionary loaded = new PersonalDictionary(storage, 1L);

        assertEquals(1, loaded.getAll().size());
        assertEquals(entry, loaded.getAll().getFirst());
    }

    @Test
    void getAllIsUnmodifiable() {
        dictionary.add(entry("привет", "hello"));

        org.junit.jupiter.api.Assertions.assertThrows(
                UnsupportedOperationException.class,
                () -> dictionary.getAll().clear());
    }

    private DictionaryEntry entry(String original, String translated) {
        return new DictionaryEntry(original, translated, Language.RUSSIAN, Language.ENGLISH);
    }
}
