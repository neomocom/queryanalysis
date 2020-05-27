package com.searchgears.queryanalysis.matching;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.synonym.SynonymGraphFilterFactory;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.ResourceLoader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
        initSynonymGraphFilter(resourceLoader);
    }

    private Map<String, String> getSynonymGraphSettings(String synFile) {
        //Case needs to be respected in order to keep the output intact, will be handled "outside"
        Map<String, String> args = Maps.newHashMap();
        args.put("ignoreCase", "false");
        args.put("synonyms", synFile);
        return args;
    }

    private void initSynonymGraphFilter(ResourceLoader resourceLoader) {
        try {
            synonymGraphFilterFactory.inform(resourceLoader);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to initialize synonymGraphFilterFactory", e);
        }
    }

    public String rewrite(String s) {
        TokenStream tokenStream = synonymGraphFilterFactory.create(new SimpleAnalyzer().tokenStream("", s));
        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
        List<String> terms = new ArrayList<>();

        try {
            tokenStream.reset();
            while (tokenStream.incrementToken()) {
                terms.add(charTermAttribute.toString());
            }
            tokenStream.end();
            tokenStream.close();
        } catch (IOException e) {
            e.printStackTrace();
            return s;
        }
        return Joiner.on(' ').join(terms);
    }
}
