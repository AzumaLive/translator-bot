package translatorbot.dictionary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InMemoryDictionaryStorage implements DictionaryStorage {

    private final Map<Long, List<DictionaryEntry>> entries = new HashMap<>();

    @Override
    public List<DictionaryEntry> load(long ownerId) {
        return new ArrayList<>(entries.getOrDefault(ownerId, List.of()));
    }

    @Override
    public void save(long ownerId, List<DictionaryEntry> newEntries) {
        entries.put(ownerId, new ArrayList<>(newEntries));
    }
}
