package com.searchgears.queryanalysis.matching;

import com.searchgears.queryanalysis.config.Matcher;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;

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
 */
public class DictionaryRewriterHatcher implements ResourceLoaderAware {
    private DictionaryRewriter rewriter = null;
    private ResourceLoader loader;

    public DictionaryRewriter getRewriter() {
        if (rewriter == null) {
            throw new UnsupportedOperationException("DictionaryRewriter is only available after ResourceLoaderAware#inform");
        }
        return rewriter;
    }

    @Override
    public void inform(ResourceLoader pLoader) throws IOException {
        loader = pLoader;
        //createDictionaryRewriterInputFile();
        //createDictionaryRewriter();
        //informDictionaryRewriter();
        //deleteDictionaryRewriterInputFile();
    }

    public void createSynFileFromMatchers(Map<String, Matcher> matchers, Path outputFile) {
        try {
            BufferedWriter output = Files.newBufferedWriter(outputFile, StandardCharsets.UTF_8);
            for (Map.Entry<String, Matcher> matcher: matchers.entrySet()) {
                String dictionary = matcher.getValue().getDictionary();
                try {
                    InputStream inputStream = loader.openResource(dictionary);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.write(line + " => " + matcher.getKey());
                        output.newLine();
                    }
                } catch (IOException e) {
                    throw new IllegalArgumentException("Dictionary " + dictionary + " could not be opened. ");
                }
            }
            output.close();
        } catch (IOException e) {
            throw new IllegalStateException("Could not write to  output file " + outputFile + ". ");
        }
    }
}
