package com.searchgears.queryanalysis.rule;

import com.google.common.collect.ImmutableList;
import com.searchgears.queryanalysis.config.Rule;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleRankingRuleMatcherTest {

    private Rule publisherRule = Rule.builder().withMatcher("publisher").withMatcher("author").withParam("qf", "author^2").build();
    private Rule isbnRule = Rule.builder().withMatcher("isbn").withParam("qf", "isbn^100").build();
    private SimpleRankingRuleMatcher ruleMatcher = new SimpleRankingRuleMatcher(ImmutableList.of(publisherRule, isbnRule));

    @Test
    void exactRuleQueryMatchesConfiguredRule() {
        Optional<Rule> matchedRule = ruleMatcher.matchRankingRule("publisher author");
        assertThat(matchedRule).isPresent().hasValue(publisherRule);
    }

    @Test
    void partialRuleQueryWontMatch() {
        Optional<Rule> matchedRule = ruleMatcher.matchRankingRule("author Some Title");
        assertThat(matchedRule).isNotPresent();
    }

    @Test
    void ruleQueryWithOutOfOrderMatchersWontWork() {
        Optional<Rule> matchedRule = ruleMatcher.matchRankingRule("author publisher");
        assertThat(matchedRule).isNotPresent();
    }

    @Test
    void nullRuleQueryParamIsHandledGracefully() {
        Optional<Rule> matchedRule = ruleMatcher.matchRankingRule(null);
        assertThat(matchedRule).isNotPresent();
    }

    @Test
    void emptyRuleQueryParamIsHandledGracefully() {
        Optional<Rule> matchedRule = ruleMatcher.matchRankingRule("");
        assertThat(matchedRule).isNotPresent();
    }

}
