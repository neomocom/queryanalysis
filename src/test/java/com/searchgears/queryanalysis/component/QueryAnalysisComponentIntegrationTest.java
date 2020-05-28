package com.searchgears.queryanalysis.component;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.searchgears.queryanalysis.SolrCoreAwareTest;
import org.apache.lucene.util.IOUtils;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.embedded.EmbeddedSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrInputDocument;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


public class QueryAnalysisComponentIntegrationTest extends SolrCoreAwareTest {
    private static EmbeddedSolrServer server;

    @BeforeClass
    public static void initEmbeddedSolrServer() throws Exception {
        server = new EmbeddedSolrServer(h.getCoreContainer(), h.coreName);
        server.add(readDocuments());
        server.commit(true, true);
    }

    private static List<SolrInputDocument> readDocuments() throws java.io.IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        InputStream docStream = Objects.requireNonNull(QueryAnalysisComponentIntegrationTest.class.getClassLoader().getResourceAsStream("solr_docs.json"));
        SolrTestDocuments result = mapper.readValue(docStream, SolrTestDocuments.class);
        List<SolrInputDocument> docs = new ArrayList<>();
        for(Map<String, Object> doc: result.docs) {
            SolrInputDocument solrDocument = new SolrInputDocument();
            for (Map.Entry<String, Object> field: doc.entrySet()) {
                solrDocument.addField(field.getKey(), field.getValue());
            }
            docs.add(solrDocument);
        }
        return docs;
    }


    @AfterClass
    public static void shutdownSolr() throws Exception {
        IOUtils.rm(initAndGetDataDir().toPath());
    }

    @Test
    public void authorIsRecognizedAndReturnsResults() throws SolrServerException, IOException {
        QueryResponse resp = query("Peter Scholl-Latour");
        assertQueryAnalysisDescription(resp, "rule-match");
        assertMoreThanZeroHits(resp);
    }

    @Test
    public void titleIsRecognizedAndReturnsResults() throws SolrServerException, IOException {
        QueryResponse resp = query("Kruso");
        assertQueryAnalysisDescription(resp, "rule-match");
        assertMoreThanZeroHits(resp);
    }

    @Test
    public void queryNotAnalyzedAndReturnsNoHitsWithDefaultRanking() throws SolrServerException, IOException {
        QueryResponse resp = query("Rocko Schamoni");
        assertQueryAnalysisDescription(resp, "no-match");
        assertZeroHits(resp);
    }

    private QueryResponse query(String queryString) throws SolrServerException, IOException {
        SolrQuery query = new SolrQuery(queryString);
        query.setParam("qa", true);
        return server.query(query);
    }

    private void assertQueryAnalysisDescription(QueryResponse resp, String types) {
        assertEquals(types, resp.getHeader().get(QueryAnalysisComponent.HEADER_NAME));
    }


    private void assertMoreThanZeroHits(QueryResponse resp) {
        assertTrue(resp.getResults().getNumFound() > 0);
    }


    private void assertZeroHits(QueryResponse resp) {
        assertTrue(resp.getResults().getNumFound() == 0);
    }

    private static class SolrTestDocuments {
        public List<Map<String, Object>> docs = new ArrayList<>();
    }
}
