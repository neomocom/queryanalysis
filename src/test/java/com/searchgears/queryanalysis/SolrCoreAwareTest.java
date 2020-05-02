package com.searchgears.queryanalysis;

import com.carrotsearch.randomizedtesting.RandomizedRunner;
import com.searchgears.queryanalysis.component.SolrResources;
import org.apache.solr.SolrTestCaseJ4;
import org.apache.solr.core.SolrResourceLoader;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

@RunWith(RandomizedRunner.class)
public class SolrCoreAwareTest extends SolrTestCaseJ4 {
    private static String CORE_NAME = "testcore";

    @BeforeClass
    public static void initSolr() throws Exception {
        initCore(SolrResources.getPathToCoreConfig(CORE_NAME), SolrResources.getPathToCoreSchema(CORE_NAME),
                SolrResources.getPathToSolrCores(), CORE_NAME);
    }

    protected SolrResourceLoader getResourceLoader() {
        return h.getCore().getResourceLoader();
    }
}
