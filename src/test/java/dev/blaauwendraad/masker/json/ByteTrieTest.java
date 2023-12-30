package dev.blaauwendraad.masker.json;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.annotation.ParametersAreNonnullByDefault;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

@ParametersAreNonnullByDefault
final class ByteTrieTest {
    private static final Set<String> someKeys = Set.of("maskMe", "maskme", "\u000F\u0017\u0017\u000Bs\b\u0014X");

    @Test
    void caseInsensitiveInsertAndSearch() {
        ByteTrie trie = new ByteTrie(true);
        for (String someKey : someKeys) {
            trie.insert(someKey);
        }
        for (String someKey : someKeys) {
            Assertions.assertTrue(transformToBytesAndSearch(trie, someKey));
            Assertions.assertTrue(transformToBytesWithPaddingAndSearch(trie, someKey));
        }
        for (String someKey : someKeys) {
            Assertions.assertTrue(transformToBytesAndSearch(trie, someKey.toLowerCase(Locale.ROOT)));
            Assertions.assertTrue(transformToBytesWithPaddingAndSearch(trie, someKey.toLowerCase(Locale.ROOT)));
        }
        for (String someKey : someKeys) {
            Assertions.assertTrue(transformToBytesAndSearch(trie, someKey.toUpperCase(Locale.ROOT)));
            Assertions.assertTrue(transformToBytesWithPaddingAndSearch(trie, someKey.toUpperCase(Locale.ROOT)));
        }
        Assertions.assertFalse(transformToBytesAndSearch(trie, "notAKey"));
        Assertions.assertFalse(transformToBytesWithPaddingAndSearch(trie, "notAKey"));
    }

    @Test
    void caseSensitiveInsertAndSearch() {
        ByteTrie trie = new ByteTrie(false);
        for (String someKey : someKeys) {
            trie.insert(someKey);
        }
        for (String someKey : someKeys) {
            Assertions.assertTrue(transformToBytesAndSearch(trie, someKey));
            Assertions.assertTrue(transformToBytesWithPaddingAndSearch(trie, someKey));
        }
        for (String someKey : someKeys) {
            Assertions.assertFalse(transformToBytesAndSearch(trie, someKey.toUpperCase(Locale.ROOT)));
            Assertions.assertFalse(transformToBytesWithPaddingAndSearch(trie, someKey.toUpperCase(Locale.ROOT)));
        }
        Assertions.assertFalse(transformToBytesAndSearch(trie, "notAKey"));
        Assertions.assertFalse(transformToBytesWithPaddingAndSearch(trie, "notAKey"));
    }

    private boolean transformToBytesAndSearch(ByteTrie trie, String key) {
        byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
        return trie.search(bytes, 0, bytes.length);
    }

    private boolean transformToBytesWithPaddingAndSearch(ByteTrie trie, String key) {
        // searching by offsets
        byte[] bytes = ("{\"" + key + "\": \"some value\"}\"").getBytes(StandardCharsets.UTF_8);
        return trie.search(bytes, 2, key.getBytes(StandardCharsets.UTF_8).length);
    }
}