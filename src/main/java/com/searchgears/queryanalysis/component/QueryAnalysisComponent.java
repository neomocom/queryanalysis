package com.searchgears.queryanalysis.component;

import com.searchgears.queryanalysis.config.Config;
import com.searchgears.queryanalysis.config.Rule;
import com.searchgears.queryanalysis.matching.DictionaryRewriterHolder;
import com.searchgears.queryanalysis.rule.RankingRuleMatcher;
import com.searchgears.queryanalysis.rule.SimpleRankingRuleMatcher;
import org.apache.solr.common.params.CommonParams;
import org.apache.solr.common.params.MapSolrParams;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.core.SolrCore;
import org.apache.solr.handler.component.QueryComponent;
import org.apache.solr.handler.component.ResponseBuilder;
import org.apache.solr.search.QParserPlugin;
import org.apache.solr.search.QueryParsing;
import org.apache.solr.util.plugin.SolrCoreAware;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.profiler.Profiler;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

public class QueryAnalysisComponent extends QueryComponent implements SolrCoreAware {
    private static final Logger LOGGER = LoggerFactory.getLogger(QueryAnalysisComponent.class);
    public static final String HEADER_NAME = "query_analysis";

    private String configFileName;
    private DictionaryRewriterHolder dictionaryRewriterHolder;
    private RankingRuleMatcher rankingRuleMatcher;

    @Override
    public void init(NamedList args) {
        super.init(args);
        configFileName = args.get("configFile") != null ? args.get("configFile").toString() : "queryanalysis.yml";
        LOGGER.info("Query Analysis initialized");
    }

    @Override
    public void inform(SolrCore core) {
        profile("Initialization", profiler -> {
            Config config = Config.fromCorePath(core.getResourceLoader(), configFileName);
            dictionaryRewriterHolder = new DictionaryRewriterHolder(config.getMatchers(), core.getResourceLoader());
            rankingRuleMatcher = new SimpleRankingRuleMatcher(config.getRules());
        });
    }

    @Override
    public void prepare(ResponseBuilder rb) throws IOException {
        SolrParams params = getSolrParams(rb);
        if (isExtendedDisMaxQuery(params) && analyzeQueryEnabled(params)) {
            analyzeQuery(rb);
        } else {
            LOGGER.debug("No edismax query or 'qa'-Parameter not set, not touching query \"{}\".", rb.getQueryString());
        }
        super.prepare(rb);
    }

    private SolrParams getSolrParams(ResponseBuilder rb) {
        return rb.req.getParams();
    }

    private boolean isExtendedDisMaxQuery(SolrParams params) {
        String defType = params.get(QueryParsing.DEFTYPE, QParserPlugin.DEFAULT_QTYPE);
        return defType.equals("edismax");
    }

    private boolean analyzeQueryEnabled(SolrParams params) {
        return params.getBool("qa", false);
    }

    private void analyzeQuery(ResponseBuilder rb) {
        profile("Analyze Query", profiler -> {
            profiler.start("Rewrite Query");
            String ruleQuery = rewriteToRuleQuery(rb);

            profiler.start("Match Rule");
            Optional<Rule> matchedRule = findMatchingRankingRule(ruleQuery);

            profiler.start("Adjust Query");
            if (matchedRule.isPresent()) {
                addQueryParamsFromRule(rb, matchedRule.get());
            } else {
                addNoMatchResponseHeader(rb, ruleQuery);
            }
        });
    }

    private String rewriteToRuleQuery(ResponseBuilder rb) {
        String query = getSolrParams(rb).get(CommonParams.Q);
        LOGGER.debug("Processing query \"{}\". ", query);
        String ruleQuery = dictionaryRewriterHolder.getRewriter().rewrite(query);
        LOGGER.debug("Rewritten query for rule matching to \"{}\"", ruleQuery);
        return ruleQuery;
    }

    private Optional<Rule> findMatchingRankingRule(String ruleQuery) {
        return rankingRuleMatcher.matchRankingRule(ruleQuery);
    }

    private void addQueryParamsFromRule(ResponseBuilder rb, Rule matchedRule) {
        LOGGER.debug("Found ranking rule: {}", matchedRule);
        addAdditionalSolrParamsToResponseBuilder(rb, new MapSolrParams(matchedRule.getParams()));
        addHeaderToResponseBuilder(rb, "rule-match");
    }

    private void addNoMatchResponseHeader(ResponseBuilder rb, String ruleQuery) {
        LOGGER.debug("No ranking rule found for ruleQuery: \"{}\"", ruleQuery);
        addHeaderToResponseBuilder(rb, "no-match");
    }

    private void addAdditionalSolrParamsToResponseBuilder(ResponseBuilder rb, SolrParams additionalQueryParams) {
        SolrParams originalParams = getSolrParams(rb);
        LOGGER.debug("Org Solr parameters: {}", originalParams);
        ModifiableSolrParams finalParams = new ModifiableSolrParams();
        finalParams.add(originalParams);
        finalParams.add(additionalQueryParams);
        rb.req.setParams(finalParams);
        LOGGER.debug("New Solr parameters: {}", getSolrParams(rb));
    }

    private void addHeaderToResponseBuilder(ResponseBuilder rb, String info) {
        rb.rsp.getResponseHeader().add(HEADER_NAME, info);
    }


    private void profile(String profilerName, Consumer<Profiler> profilerConsumer) {
        Profiler profiler = new Profiler(profilerName);
        profiler.setLogger(LOGGER);
        profilerConsumer.accept(profiler);
        profiler.stop().log();
    }
}
