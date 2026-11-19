package translatorbot.dictionary;

import java.util.List;

public interface DictionaryStorage {

    List<DictionaryEntry> load(long ownerId);

    void save(long ownerId, List<DictionaryEntry> entries);
}
