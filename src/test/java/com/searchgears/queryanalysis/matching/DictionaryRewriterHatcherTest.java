package com.searchgears.queryanalysis.matching;

import com.searchgears.queryanalysis.SolrCoreAwareTest;
import com.searchgears.queryanalysis.config.Config;
import com.searchgears.queryanalysis.config.Matcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.Before;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.containsString;

public class DictionaryRewriterHatcherTest extends SolrCoreAwareTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();
    private Config config;
    private DictionaryRewriterHatcher hatcher;

    @Before
    public void readConfig() throws IOException {
        config = parseConfig("queryanalysis.yml");
        hatcher = new DictionaryRewriterHatcher();
        hatcher.inform(getResourceLoader());
    }

    @Test
    public void synonymFileIsCreatedForAllMatchers() throws IOException {
        Path outputFile = createTempOutputFile();
        hatcher.createSynFileFromMatchers(config.getMatchers(), outputFile);
        Set<String> expected = Set.of(
                "Verlag GmbH => publisherMarker", "e. V. => publisherMarker",
                "Peter Suhrkamp => publisher", "Beck => publisher", "Hänssler => publisher"
        );
        Set<String> actual = Set.copyOf(Files.readAllLines(outputFile));
        assertEquals(expected, actual);
    }

    @Test
    public void illegalDictionaryThrowsException() {
        String invalidDictionary = "invalidDictionary.dic";
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(containsString(invalidDictionary));
        Map<String, Matcher> matchers = Map.of("publisher", new Matcher(invalidDictionary));
        hatcher.createSynFileFromMatchers(matchers, createTempOutputFile());
    }

    @Test
    public void nonWritableOutputFileThrowsException() {
        exception.expect(IllegalStateException.class);
        exception.expectMessage(containsString("Could not write to output file"));
        hatcher.createSynFileFromMatchers(config.getMatchers(), createReadOnlyOutputFile());
    }



    private Config parseConfig(String fileName) {
        String file = ClassLoader.getSystemClassLoader()
                .getResource(fileName).getFile();
        Config config = Config.fromFile(file);
        return config;
    }

    private Path createTempOutputFile() {
        try {
            Path outputFile = Files.createTempFile(null, null);
            outputFile.toFile().deleteOnExit();
            return outputFile;
        } catch (IOException exception) {
            throw new IllegalStateException(exception);
        }
    }

    private Path createReadOnlyOutputFile() {
        Path outputFile = createTempOutputFile();
        outputFile.toFile().setReadOnly();
        return outputFile;
    }
}
