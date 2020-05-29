package com.searchgears.queryanalysis.rule;

import com.searchgears.queryanalysis.config.Rule;

import java.util.Optional;

public interface RankingRuleMatcher {
    Optional<Rule> matchRankingRule(String ruleQuery);
}
