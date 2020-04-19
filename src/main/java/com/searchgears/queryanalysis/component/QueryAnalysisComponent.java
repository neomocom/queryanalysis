package com.searchgears.queryanalysis.component;

import com.google.common.base.Joiner;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.component.QueryComponent;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.schema.FieldType;
import org.apache.solr.schema.IndexSchema;
import org.apache.solr.search.QParserPlugin;
import org.apache.solr.search.QueryParsing;
import org.apache.solr.spelling.QueryConverter;
import org.apache.solr.spelling.SpellingQueryConverter;
import org.apache.solr.util.plugin.SolrCoreAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class QueryAnalysisComponent extends QueryComponent implements SolrCoreAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryAnalysisComponent.class);
    public static final String HEADER_NAME = "query_analysis";

    private String fieldTypeName;
    private String configFileName;
    private String indexFileName;
    private String indexConfigFileName;

    private QueryConverter queryConverter;



    @Override
    public void prepare(ResponseBuilder rb) throws IOException {
        SolrParams params = rb.req.getParams();
        String defType = params.get(QueryParsing.DEFTYPE, QParserPlugin.DEFAULT_QTYPE);
        if (defType.equals("edismax") && params.getBool("qa", false)) {
            LOGGER.trace("'qa'-Parameter set, processing query \"{}\". ", params.get(CommonParams.Q));
            rewriteQuery(rb);
        } else {
            LOGGER.trace("No edismax or 'qa'-Parameter not set, not touching query \"{}\".", rb.getQueryString());
        }
        super.prepare(rb);
    }


    private void rewriteQuery(ResponseBuilder rb) {
        Profiler profiler = createProfiler("Rewrite Query");
        profiler.start("Parse");
        //Convert query to tokens
        LOGGER.trace("Tokens after parsing: \"{}\"", "");
        profiler.start("Analyze");
        //Analyzse query
        LOGGER.trace("Analysis result: {}", "");
        profiler.start("Select ranking");
        addHeaderToResponseBuilder(rb, "test");
        //selectRanking(rb, result);
        profiler.stop().log();
    }


    private void selectRanking(ResponseBuilder rb, String[] result) {
        LOGGER.trace(Joiner.on(" // ").join(result));
        /** RulesResult rulesResult = rankingRules.applyRules(result);
        if (rulesResult.getSolrParams() != null) {
            addAdditionalSolrParamsToResponseBuilder(rb, rulesResult.getSolrParams());
            addHeaderToResponseBuilder(rb, rulesResult.getTypesDescription());
        }*/
    }


    private void addAdditionalSolrParamsToResponseBuilder(ResponseBuilder rb, SolrParams additionalQueryParams) {
        SolrParams originalParams = rb.req.getParams();
        LOGGER.trace("Org Solr parameters: {}. ", originalParams);
        ModifiableSolrParams finalParams = new ModifiableSolrParams();
        finalParams.add(originalParams);
        finalParams.add(additionalQueryParams);
        rb.req.setParams(finalParams);
        LOGGER.trace("New Solr parameters: {}. ", rb.req.getParams());
    }


    private void addHeaderToResponseBuilder(ResponseBuilder rb, String info) {
        rb.rsp.getResponseHeader().add(HEADER_NAME, info);
    }


/*
    private List<Token> parseQuery(ResponseBuilder rb) {
        String queryString = rb.req.getParams().get( CommonParams.Q );;
        Collection<org.apache.lucene.analysis.Token> luceneTokens = queryConverter.convert(queryString);
        List<Token> tokens = Lists.newArrayList();
        for (org.apache.lucene.analysis.Token token: luceneTokens) {
            tokens.add(new Token(token.startOffset(), token.toString(), queryString.substring(token.startOffset(),
                    token.endOffset())));
        }
        return tokens;
    }
*/

    @Override
    public void init(NamedList args) {
        super.init(args);
        fieldTypeName = args.get("analyzerFieldType") != null ? args.get("analyzerFieldType").toString(): "<empty>";
        configFileName = args.get("configFile") != null ? args.get("configFile").toString(): "<empty>";
        indexFileName = args.get("indexFile") != null ? args.get("indexFile").toString(): "<empty>";
        indexConfigFileName = args.get("indexConfigFile") != null ? args.get("indexConfigFile").toString(): "<empty>";

    }


    @Override
    public void inform(SolrCore core) {
        Profiler profiler = createProfiler("Initialization");
        profiler.start("Query Converter");
        createQueryConverter(core);
        profiler.start("Rules");
        configureRules(core);
        profiler.start("Query Analyzer");
        createQueryAnalyzer(core);
        profiler.stop().log();
        addCloseHook(core);
    }


    private Profiler createProfiler(String profilerName) {
        Profiler profiler = new Profiler(profilerName);
        profiler.setLogger(LOGGER);
        return profiler;
    }


    private void createQueryAnalyzer(SolrCore core) {
        LOGGER.info("Reading approx index from {} and {}. ", indexConfigFileName, indexFileName);
        File indexFile = findConfigFile(core, indexFileName);
        File indexConfigFile = findConfigFile(core, indexConfigFileName);

    }


    private void configureRules(SolrCore core) {
        File configFile = findConfigFile(core, configFileName);
        LOGGER.info("Reading rules config file from {}. ", configFile);
        /*try {
            Config config = new Config(null, null, new InputSource(new FileInputStream(configFile)), "/ltquery");
            config.getNode("config", true);
            rankingRules = RankingRules.of(config);
        } catch (ParserConfigurationException | SAXException |IOException e) {
            throw new SolrException(SolrException.ErrorCode.SERVER_ERROR, e.getMessage());
        }*/
    }


    private void addCloseHook(SolrCore core) {
        //delete temporary config file for synonyms?
       //core.addCloseHook(new QueryAnalyzerCloseHook(queryAnalyzer));
    }



    private File findConfigFile(SolrCore core, String fileName) {
        File configDirectory = new File(core.getConfigResource()).getParentFile();

        File configFile = new File(configDirectory, fileName);
        if (!configFile.canRead()) {
            throw new SolrException(SolrException.ErrorCode.NOT_FOUND, String.format(Locale.GERMANY,
                    "Config file %s does not exist. ", configFile));
        }
        return configFile;
    }


    private void createQueryConverter(SolrCore core) {
        Map<String, QueryConverter> queryConverters = new HashMap<>();
        core.initPlugins(queryConverters, QueryConverter.class);

        if (queryConverters.isEmpty()) {
            LOGGER.info("No queryConverter defined, using default converter");
            queryConverters.put("queryConverter", new SpellingQueryConverter());
        }

        queryConverter = queryConverters.values().iterator().next();
        IndexSchema schema = core.getLatestSchema();
        FieldType fieldType = schema.getFieldTypes().get(fieldTypeName);
        Analyzer analyzer = (fieldType == null) ? new WhitespaceAnalyzer() : fieldType.getQueryAnalyzer();
        queryConverter.setAnalyzer(analyzer);
    }
}
