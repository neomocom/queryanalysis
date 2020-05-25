package com.searchgears.queryanalysis.matching;

import com.google.common.annotations.VisibleForTesting;
import com.searchgears.queryanalysis.config.Config;
import com.searchgears.queryanalysis.config.Matcher;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;
import org.apache.solr.core.SolrResourceLoader;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * DictionaryRewriterHatcher translates between the query analysis dictionaries and the synonym files
 * as need by the DictionaryRewriter.
 * The synonym file format is just there for code re-use and is an implementation detail, so it should never be
 * created directly except for testing.
 * It is not a factory because it needs to go through the solr resource loader dance before it can actually
 * create the DictionaryRewriter.
 * TODO rename to DictionaryRewriterHolder
 */
public class DictionaryRewriterHatcher implements ResourceLoaderAware {
    private static final String DEFAULT_SYN_FILENAME = "synonyms.txt";

    private final String configFilename;
    private final String synFilename;

    private DictionaryRewriter rewriter = null;
    private ResourceLoader loader;

    public DictionaryRewriterHatcher(String configFilename) {
        this(configFilename, DEFAULT_SYN_FILENAME);
    }

    public DictionaryRewriterHatcher(String configFilename, String synFilename) {
        this.configFilename = configFilename;
        this.synFilename = synFilename;
    }

    @Override
    public void inform(ResourceLoader loader) throws IOException {
        this.loader = loader;
        Path synFilepath = resolvePathToSynonymsFile();
        createSynFileForRewriter(synFilepath);
        createDictionaryRewriter();

        // TODO delete synFile after rewriter init
        //deleteDictionaryRewriterInputFile();
    }

    private Path resolvePathToSynonymsFile() {
        if (loader instanceof SolrResourceLoader) {
            SolrResourceLoader solrResourceLoader = ((SolrResourceLoader) loader);
            return solrResourceLoader.getInstancePath().resolve("conf").resolve(synFilename);
        } else {
            throw new IllegalStateException("Solr resource loader is required to resolve path to solr core config dir");
        }
    }

    private void createSynFileForRewriter(Path synFilepath) {
        try (InputStream inputStream = loader.openResource(configFilename)) {
            Config config = Config.fromInputStream(inputStream);
            createSynFileFromMatchers(config.getMatchers(), synFilepath);
        } catch (IOException e) {
            throw new IllegalArgumentException("Config file could not be loaded");
        }
    }

    @VisibleForTesting
    void createSynFileFromMatchers(Map<String, Matcher> matchers, Path outputFile) {
        try (BufferedWriter output = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8)) {
            for (Map.Entry<String, Matcher> matcher : matchers.entrySet()) {
                writeDictionaryContents(matcher.getValue().getDictionary(), matcher.getKey(), output);
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not write to output file " + outputFile + ". ");
        }
    }

    private void writeDictionaryContents(String dictionary, String matcherRule, BufferedWriter output) {
        try (InputStream inputStream = loader.openResource(dictionary)) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
            String line;
            while ((line = reader.readLine()) != null) {
                output.write(line + " => " + matcherRule);
                output.newLine();
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Dictionary " + dictionary + " could not be opened. ");
        }
    }

    private void createDictionaryRewriter() throws IOException {
        this.rewriter = new DictionaryRewriter(synFilename);
        this.rewriter.inform(loader);
    }

    public DictionaryRewriter getRewriter() {
        if (rewriter == null) {
            throw new UnsupportedOperationException("DictionaryRewriter is only available after ResourceLoaderAware#inform");
        }
        return rewriter;
    }

}
