package com.searchgears.queryanalysis.matching;

import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.searchgears.queryanalysis.component.SolrResources;
import org.apache.solr.SolrTestCaseJ4;
import org.junit.Test;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

import java.io.IOException;

@RunWith(RandomizedRunner.class)
public class DictionaryRewriterTest extends SolrTestCaseJ4 {
    private DictionaryRewriter rewriter;
    private static String CORE_NAME = "testcore";


    @BeforeClass
    public static void initSolr() throws Exception {
        initCore(SolrResources.getPathToCoreConfig(CORE_NAME), SolrResources.getPathToCoreSchema(CORE_NAME),
                SolrResources.getPathToSolrCores(), CORE_NAME);
    }

    @Before
    public void initalizeRewriter() throws IOException {
        rewriter = new DictionaryRewriter("synonyms.txt");
        rewriter.inform(h.getCore().getResourceLoader());
    }

    @Test
    public void nonMatchingTextIsReturnedAsIs() {
        assertEquals("I don't match.", rewriter.rewrite("I don't match."));
    }

    @Test
    public void fullyMatchingTextIsRewritten() {
        assertEquals("publisher publisherMarker", rewriter.rewrite("Suhrkamp Verlag"));
    }

}