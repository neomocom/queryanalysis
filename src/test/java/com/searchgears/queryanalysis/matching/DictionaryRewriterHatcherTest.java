package com.searchgears.queryanalysis.matching;

import com.google.common.collect.ImmutableSet;
import com.searchgears.queryanalysis.SolrCoreAwareTest;
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
        hatcher = new DictionaryRewriterHatcher("queryanalysis.yml", getResourceLoader());
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
                "Verlag GmbH => publisherMarker", "e. V. => publisherMarker",
                "Peter Suhrkamp => publisher", "Beck => publisher", "Hänssler => publisher"
        );
        Set<String> actual = ImmutableSet.copyOf(Files.readAllLines(resolveSynFilepath()));
        assertEquals(expected, actual);
    }

    @Test
    public void illegalDictionaryThrowsException() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(containsString("invalidDictionary.dic"));
        new DictionaryRewriterHatcher("queryanalysis-invalid-dic.yml", getResourceLoader());
    }

    @Test
    public void invalidConfigFileLocationThrowsException() {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(containsString("Config file could not be loaded"));
        new DictionaryRewriterHatcher("invalid-location", getResourceLoader());
    }

    @Test
    public void instanceOfRewriterIsCreated() {
        assertNotNull(hatcher.getRewriter());
    }

    private Path resolveSynFilepath() {
        return hatcher.getSynFilepath();
    }

}
