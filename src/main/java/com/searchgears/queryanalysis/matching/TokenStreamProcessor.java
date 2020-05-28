package com.searchgears.queryanalysis.matching;

import com.google.common.base.Joiner;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

interface TokenStreamProcessor {

    static String process(String input) {
        return process(input, UnaryOperator.identity());
    }

    static String process(String input, UnaryOperator<TokenStream> processor) {
        try (TokenStream tokenStream = new StandardAnalyzer().tokenStream("", input)) {
            return consume(processor.apply(tokenStream));
        } catch (IOException e) {
            e.printStackTrace();
            return input;
        }
    }

    static String consume(TokenStream tokenStream) throws IOException {
        CharTermAttribute charTermAttribute = tokenStream.addAttribute(CharTermAttribute.class);
        List<String> terms = new ArrayList<>();

        tokenStream.reset();
        while (tokenStream.incrementToken()) {
            terms.add(charTermAttribute.toString());
        }
        tokenStream.end();
        tokenStream.close();

        return Joiner.on(' ').join(terms);
    }
}
