package com.searchgears.queryanalysis.matching;

import com.google.common.collect.ImmutableMap;
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
        hatcher = new DictionaryRewriterHatcher("queryanalysis.yml");
    }

    @Test
    public void informCreatesSynonymsFile() throws IOException {
        hatcher.inform(getResourceLoader());
        assertTrue(resolveSynFilepath().toFile().exists());
    }

    @Test
    public void synFileNameIsSound() throws IOException {
        hatcher.inform(getResourceLoader());
        String synFilename = resolveSynFilepath().getFileName().toString();
        assertThat(synFilename, startsWith("synonyms"));
        assertThat(synFilename, endsWith(".txt"));
    }

    @Test
    public void synonymFileContentIsCreatedForAllMatcherDictionaries() throws IOException {
        hatcher.inform(getResourceLoader());
        Set<String> expected = ImmutableSet.of(
                "Verlag GmbH => publisherMarker", "e. V. => publisherMarker",
                "Peter Suhrkamp => publisher", "Beck => publisher", "Hänssler => publisher"
        );
        Set<String> actual = ImmutableSet.copyOf(Files.readAllLines(resolveSynFilepath()));
        assertEquals(expected, actual);
    }

    @Test
    public void illegalDictionaryThrowsException() throws IOException {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(containsString("invalidDictionary.dic"));
        new DictionaryRewriterHatcher("queryanalysis-invalid-dic.yml").inform(getResourceLoader());
    }

    @Test
    public void invalidConfigFileLocationThrowsException() throws IOException {
        exception.expect(IllegalArgumentException.class);
        exception.expectMessage(containsString("Config file could not be loaded"));
        new DictionaryRewriterHatcher("invalid-location").inform(getResourceLoader());
    }

    @Test
    public void retrievalOfRewriterBeforeInformThrowsException() {
        exception.expect(UnsupportedOperationException.class);
        exception.expectMessage(containsString("only available after ResourceLoaderAware#inform"));
        hatcher.getRewriter();
    }

    @Test
    public void informCreatesInstanceOfRewriter() throws IOException {
        hatcher.inform(getResourceLoader());
        assertNotNull(hatcher.getRewriter());
    }

    private Path resolveSynFilepath() {
        return hatcher.getSynFilepath();
    }

}
