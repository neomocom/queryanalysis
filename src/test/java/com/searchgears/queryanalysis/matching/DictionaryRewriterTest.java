package com.searchgears.queryanalysis.matching;

import com.searchgears.queryanalysis.SolrCoreAwareTest;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;


public class DictionaryRewriterTest extends SolrCoreAwareTest {
    private DictionaryRewriter rewriter;


    @Before
    public void initalizeRewriter() throws IOException {
        rewriter = new DictionaryRewriter("synonyms.txt");
        rewriter.inform(getResourceLoader());
    }

    @Test
    public void nonMatchingTextIsReturnedAsIs() {
        assertEquals("i don t match", rewriter.rewrite("I don't match."));
    }

    @Test
    public void fullyMatchingTextIsRewritten() {
        assertEquals("publisher publisherMarker", rewriter.rewrite("Suhrkamp Verlag"));
    }

    @Test
    public void textContainingMatchIsPartiallyRewritten() {
        assertEquals("this is returned as is publisher publisherMarker so is this",
                rewriter.rewrite("this is returned as is, Suhrkamp Verlag so is this"));
    }

    @Test
    public void caseIsIgnored() {
        assertEquals("publisher publisherMarker",
                rewriter.rewrite("sUhrkamp VERLAG"));
    }
}