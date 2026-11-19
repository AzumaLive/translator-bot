package translatorbot.dictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PersonalDictionary {

    private final DictionaryStorage storage;
    private final long ownerId;
    private final List<DictionaryEntry> entries;

    public PersonalDictionary(DictionaryStorage storage, long ownerId) {
        this.storage = storage;
        this.ownerId = ownerId;
        this.entries = new ArrayList<>(storage.load(ownerId));
    }

    public void add(DictionaryEntry entry) {
        entries.add(entry);
        save();
    }

    public boolean remove(int number) {
        int index = number - 1;
        if (index < 0 || index >= entries.size()) {
            return false;
        }
        entries.remove(index);
        save();
        return true;
    }

    public boolean contains(DictionaryEntry entry) {
        return entries.contains(entry);
    }

    public List<DictionaryEntry> getAll() {
        return Collections.unmodifiableList(entries);
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    private void save() {
        storage.save(ownerId, entries);
    }
}
