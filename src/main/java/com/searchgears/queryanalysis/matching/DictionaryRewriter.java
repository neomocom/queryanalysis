package com.searchgears.queryanalysis.matching;

import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.synonym.SynonymGraphFilterFactory;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.util.ResourceLoader;
import org.apache.lucene.analysis.util.ResourceLoaderAware;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DictionaryRewriter implements ResourceLoaderAware {
    private final SynonymGraphFilterFactory synonymGraphFilterFactory;

    public DictionaryRewriter(String synFile) {
        Map<String, String> args = Maps.newHashMap(Map.of("ignoreCase", "false", "synonyms", synFile));
        synonymGraphFilterFactory = new SynonymGraphFilterFactory(args);
    }

    public String rewrite(String s) {
        TokenStream tokenStream = synonymGraphFilterFactory.create(new WhitespaceAnalyzer().tokenStream("", s));
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

    @Override
    public void inform(ResourceLoader loader) throws IOException {
        synonymGraphFilterFactory.inform(loader);
    }
}
