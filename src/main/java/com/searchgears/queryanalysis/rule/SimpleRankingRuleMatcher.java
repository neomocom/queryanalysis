package com.searchgears.queryanalysis.rule;

import com.google.common.base.Splitter;
import com.searchgears.queryanalysis.config.Rule;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Optional;

public class SimpleRankingRuleMatcher implements RankingRuleMatcher {

    private final List<Rule> rules;

    public SimpleRankingRuleMatcher(List<Rule> rules) {
        this.rules = rules;
    }

    @Override
    public Optional<Rule> matchRankingRule(String ruleQuery) {
        if (StringUtils.isBlank(ruleQuery)) {
            return Optional.empty();
        }

        List<String> ruleMatchers = Splitter.on(' ').splitToList(ruleQuery);
        return rules.stream()
            .filter(rule -> rule.getMatchers().equals(ruleMatchers))
            .findFirst();
    }
}
