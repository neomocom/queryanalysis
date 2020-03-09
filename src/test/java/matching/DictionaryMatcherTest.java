package matching;

import com.searchgears.queryanalysis.matching.DictionaryMatcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DictionaryMatcherTest {
    private static String STANDARD_DICTIONARY = "publisher.dic";

    private DictionaryMatcher matcher;


    @Test
    public void nonExistingConfigFileThrows() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            DictionaryMatcher.fromPath(Path.of("non-existing"));
        });
        assertEquals("Error reading file \"non-existing\". ", exception.getMessage());
    }

    @Test
    public void allTermsInDictionaryAreMatched() throws Exception {
        List<String> lines = Files.readAllLines(getFileFromClasspath(STANDARD_DICTIONARY));
        for (String line: lines) {
            assertTrue(matcher.matches(line));
        }
    }


    @Test
    public void termsNotInDictionaryAreNotMatched() {
        String[] notInDictionary = new String[] {"blu", "bla", "foo"};
        for (String line: notInDictionary) {
            assertFalse(matcher.matches(line));
        }
    }

    @BeforeEach
    public void createMatcher() {
        Path path = getFileFromClasspath(STANDARD_DICTIONARY);
        matcher = DictionaryMatcher.fromPath(path);
    }

    private Path getFileFromClasspath(String fileName) {
        try {
            return Path.of(ClassLoader.getSystemClassLoader()
                        .getResource(fileName).toURI());
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("No such file. ");
        }
    }
}

