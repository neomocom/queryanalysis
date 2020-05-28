package com.searchgears.queryanalysis.matching;

import com.google.common.collect.ImmutableSet;
import com.searchgears.queryanalysis.SolrCoreAwareTest;
import com.searchgears.queryanalysis.config.Config;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.hamcrest.CoreMatchers.*;

public class DictionaryRewriterHatcherTest extends SolrCoreAwareTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();
    private DictionaryRewriterHatcher hatcher;

    @Before
    public void setUpTestSubject() {
        Config config = Config.fromCorePath(getResourceLoader(), "queryanalysis.yml");
        hatcher = new DictionaryRewriterHatcher(config.getMatchers(), getResourceLoader());
    }

    @Test
    public void createSynFileUponInit() {
        assertTrue(resolveSynFilepath().toFile().exists());
    }

    @Test
    public void synFileNameIsSound() {
        String synFilename = resolveSynFilepath().getFileName().toString();
        assertThat(synFilename, startsWith("synonyms"));
        assertThat(synFilename, endsWith(".txt"));
    }

    @Test
    public void synFileContentIsCreatedForAllMatcherDictionaries() throws IOException {
        Set<String> expected = ImmutableSet.of(
            "peter suhrkamp => publisher", "beck => publisher", "hänssler => publisher",
            "verlag gmbh => publisherMarker", "e v => publisherMarker"
        );
        Set<String> actual = ImmutableSet.copyOf(Files.readAllLines(resolveSynFilepath()));
        assertEquals(expected, actual);
    }

    @Test
    public void illegalDictionaryThrowsException() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(containsString("invalidDictionary.dic"));
        Config invalidConfig = Config.fromCorePath(getResourceLoader(), "queryanalysis-invalid-dic.yml");
        new DictionaryRewriterHatcher(invalidConfig.getMatchers(), getResourceLoader());
    }

    @Test
    public void instanceOfRewriterIsCreated() {
        assertNotNull(hatcher.getRewriter());
    }

    private Path resolveSynFilepath() {
        return hatcher.getSynFilepath();
    }

}
