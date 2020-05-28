package com.searchgears.queryanalysis.matching;

import com.google.common.collect.Maps;
import org.apache.lucene.analysis.synonym.SynonymGraphFilterFactory;
import org.apache.lucene.analysis.util.ResourceLoader;

import java.io.IOException;
import java.util.Map;


/**
 * DictionaryRewriter finds occurences of strings as defined in a synonym file in the input text
 * after applying tokenization and lowercasing.
 * It returns the normalized input text with all matching tokens replaced.
 */
public class DictionaryRewriter {
    private final SynonymGraphFilterFactory synonymGraphFilterFactory;

    DictionaryRewriter(String synFile, ResourceLoader resourceLoader) {
        synonymGraphFilterFactory = new SynonymGraphFilterFactory(getSynonymGraphSettings(synFile));
        informSynonymGraphFilterFactory(resourceLoader);
    }

    private Map<String, String> getSynonymGraphSettings(String synFile) {
        //Case needs to be respected in order to keep the output intact, will be handled "outside"
        Map<String, String> args = Maps.newHashMap();
        args.put("ignoreCase", "false");
        args.put("synonyms", synFile);
        return args;
    }

    private void informSynonymGraphFilterFactory(ResourceLoader resourceLoader) {
        try {
            synonymGraphFilterFactory.inform(resourceLoader);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to initialize synonymGraphFilterFactory", e);
        }
    }

    public String rewrite(String query) {
        return TokenStreamProcessor.process(query, synonymGraphFilterFactory::create);
    }

}
