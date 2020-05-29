package com.searchgears.queryanalysis.matching;

import com.google.common.annotations.VisibleForTesting;
import com.searchgears.queryanalysis.config.Matcher;
import org.apache.solr.core.SolrResourceLoader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * DictionaryRewriterHolder translates between the query analysis dictionaries and the synonym files
 * as need by the DictionaryRewriter.
 * The synonym file format is just there for code re-use and is an implementation detail, so it should never be
 * created directly except for testing.
 * It is not a factory because it needs to go through the solr resource loader dance before it can actually
 * create the DictionaryRewriter.
 */
public class DictionaryRewriterHolder {

    private final Map<String, Matcher> matchers;

    private Path synFilepath;
    private DictionaryRewriter rewriter;
    private SolrResourceLoader loader;

    public DictionaryRewriterHolder(Map<String, Matcher> matchers, SolrResourceLoader resourceLoader) {
        this.matchers = matchers;
        this.loader = resourceLoader;
        init();
    }

    private void init() {
        createTempSynFile();
        writeSynFileFromMatcherDictionaries();
        createDictionaryRewriter();
        // TODO delete synFile after rewriter init
        //deleteTempSynFile();
    }

    private void createTempSynFile() {
        try {
            this.synFilepath = Files.createTempFile(loader.getInstancePath().resolve("conf"), "synonyms-", ".txt");
        } catch (IOException e) {
            throw new IllegalStateException("Temporary synonyms file could not be created", e);
        }
    }

    private void writeSynFileFromMatcherDictionaries() {
        try (BufferedWriter output = Files.newBufferedWriter(synFilepath, StandardCharsets.UTF_8)) {
            for (Map.Entry<String, Matcher> matcher : matchers.entrySet()) {
                writeDictionaryContents(matcher.getValue().getDictionary(), matcher.getKey(), output);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not write to output file " + synFilepath + ". ");
        }
    }

    private void writeDictionaryContents(String dictionary, String matcherRule, BufferedWriter output) {
        try (InputStream inputStream = loader.openResource(dictionary)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                output.write(TokenStreamProcessor.process(line) + " => " + matcherRule);
                output.newLine();
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Dictionary " + dictionary + " could not be opened. ");
        }
    }

    private void createDictionaryRewriter() {
        this.rewriter = new DictionaryRewriter(synFilepath.getFileName().toString(), loader);
    }

    public DictionaryRewriter getRewriter() {
        if (rewriter == null) {
            throw new UnsupportedOperationException("DictionaryRewriter is only available after ResourceLoaderAware#inform");
        }
        return rewriter;
    }


    @VisibleForTesting
    Path getSynFilepath() {
        return synFilepath;
    }

}