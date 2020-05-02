package com.searchgears.queryanalysis.matching;

import com.searchgears.queryanalysis.SolrCoreAwareTest;
import com.searchgears.queryanalysis.config.Config;
import org.junit.Test;
import org.junit.Before;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

public class DictionaryRewriterHatcherTest extends SolrCoreAwareTest {
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
        Path outputFile = Files.createTempFile(null, null);
        outputFile.toFile().deleteOnExit();
        hatcher.createSynFileFromMatchers(config.getMatchers(), outputFile);
        Set<String> expected = Set.of(
                "Verlag GmbH => publisherMarker", "e. V. => publisherMarker",
                "Peter Suhrkamp => publisher", "Beck => publisher", "Hänssler => publisher"
        );
        Set<String> actual = Set.copyOf(Files.readAllLines(outputFile));
        assertEquals(expected, actual);
    }


    @Test
    public void testAllExceptionsAndThenRefactor() {
        assertTrue(false);
    }

    private Config parseConfig(String fileName) {
        String file = ClassLoader.getSystemClassLoader()
                .getResource(fileName).getFile();
        Config config = Config.fromFile(file);
        return config;
    }
}
