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

    private final String configFilename;

    private Path synFilepath;
    private DictionaryRewriter rewriter;
    private ResourceLoader loader;

    public DictionaryRewriterHatcher(String configFilename) {
        this.configFilename = configFilename;
    }

    @Override
    public void inform(ResourceLoader loader) throws IOException {
        this.loader = loader;
        createTempSynFile();
        writeSynFileForRewriter();
        createDictionaryRewriter();

        // TODO delete synFile after rewriter init
        //deleteTempSynFile();
    }

    private void createTempSynFile() throws IOException {
        if (loader instanceof SolrResourceLoader) {
            SolrResourceLoader solrResourceLoader = ((SolrResourceLoader) loader);
            this.synFilepath = Files.createTempFile(solrResourceLoader.getInstancePath().resolve("conf"), "synonyms-", ".txt");
        } else {
            throw new IllegalStateException("Solr resource loader is required to resolve path to solr core config dir");
        }
    }

    private void writeSynFileForRewriter() {
        Config config = loadConfig();
        writeSynFileFromMatchers(config.getMatchers());
    }

    private Config loadConfig() {
        try (InputStream inputStream = loader.openResource(configFilename)) {
            return Config.fromInputStream(inputStream);
        } catch (IOException e) {
            throw new IllegalArgumentException("Config file could not be loaded");
        }
    }

    private void writeSynFileFromMatchers(Map<String, Matcher> matchers) {
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
                output.write(line + " => " + matcherRule);
                output.newLine();
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Dictionary " + dictionary + " could not be opened. ");
        }
    }

    private void createDictionaryRewriter() throws IOException {
        this.rewriter = new DictionaryRewriter(synFilepath.getFileName().toString());
        this.rewriter.inform(loader);
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
