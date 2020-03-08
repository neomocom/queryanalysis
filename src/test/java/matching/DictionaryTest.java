package matching;

import com.searchgears.queryanalysis.matching.DictionaryMatcher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class DictionaryTest {
    @Test
    public void nonExistingConfigFileThrows() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
           new DictionaryMatcher("non-existing");
        });
        assertEquals("Error reading file \"non-existing\". ", exception.getMessage());
    }
}
