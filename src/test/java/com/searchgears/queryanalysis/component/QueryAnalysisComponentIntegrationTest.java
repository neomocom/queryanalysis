package com.searchgears.queryanalysis.component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.lucene.util.IOUtils;
import org.apache.solr.SolrTestCaseJ4;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.core.CoreContainer;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class QueryAnalysisComponentIntegrationTest extends SolrTestCaseJ4 {
    private static List<SolrInputDocument> docs;
    private static String CORE_NAME = "testcore";
    private EmbeddedSolrServer server;

    @BeforeClass
    public static void initSolr() throws Exception {
        readDocuments();
        initCore(SolrResources.getPathToCoreConfig(CORE_NAME), SolrResources.getPathToCoreSchema(CORE_NAME),
                SolrResources.getPathToSolrCores(), CORE_NAME);
    }


    private static void readDocuments() throws java.io.IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JsonResult result = mapper.readValue(QueryAnalysisComponentIntegrationTest.class.getClassLoader().getResourceAsStream("" +
                        "./solr_docs.json"),
                JsonResult.class);
        docs = new ArrayList<>();
        for(Map<String, Object> doc: result.response.docs) {
            SolrInputDocument solrDocument = new SolrInputDocument();
            for (Map.Entry<String, Object> field: doc.entrySet()) {
                solrDocument.addField(field.getKey(), field.getValue());
            }
            docs.add(solrDocument);
        }
    }


    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        CoreContainer container = h.getCoreContainer();
        assertNotNull(h);
        server = new EmbeddedSolrServer(container, CORE_NAME);
        //server.add(docs);
        server.commit(true, true);
    }


    @AfterClass
    public static void shutdownSolr() throws Exception {
        IOUtils.rm(initAndGetDataDir().toPath());
    }


    @Test
    public void authorTitleIsRecognizedAndReturnsResults() throws SolrServerException, IOException {
        QueryResponse resp = query("Scholl-Latour Fluch der bösen Tat");
        assertMoreThanZeroHits(resp);
    }


    @Test
    public void titleIsRecognizedAndReturnsResults() throws SolrServerException, IOException {
        QueryResponse resp = query("Fluch der bösen Tat");
        assertMoreThanZeroHits(resp);
    }


    @Test
    public void authorIsRecognizedAndReturnsResults() throws SolrServerException, IOException {
        QueryResponse resp = query("Lutz Seiler");
        assertMoreThanZeroHits(resp);
    }


    @Test
    public void queryNotAnalyzedAndReturnsHitsWithDefaultRanking() throws SolrServerException, IOException {
        QueryResponse resp = query("9783518424476");
        assertMoreThanZeroHits(resp);
    }


    @Test
    public void queryNotAnalyzedAndReturnsNoHitsWithDefaultRanking() throws SolrServerException, IOException {
        QueryResponse resp = query("Diogenes");
        assertZeroHits(resp);
    }


    @Test
    public void queryAnalysisIsDescribedInResult() throws SolrServerException, IOException {
        QueryResponse resp = query("Scholl-Latour Fluch der bösen Tat");
        assertQueryAnalysisDescription(resp, "test");
    }


    private void assertQueryAnalysisDescription(QueryResponse resp, String types) {
        assertEquals(types, resp.getHeader().get(QueryAnalysisComponent.HEADER_NAME));
    }


    private QueryResponse query(String queryString) throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery(queryString);
        query.setParam("qa", true);
        return server.query(query);
    }


    private void assertMoreThanZeroHits(QueryResponse resp) {
        assertTrue(resp.getResults().getNumFound() > 0);
    }


    private void assertZeroHits(QueryResponse resp) {
        assertTrue(resp.getResults().getNumFound() == 0);
    }

}
