package com.searchgears.queryanalysis.config;

import java.util.*;

public class Rule {
    private List<String> matchers;
    private Map<String, String> params;

    public Rule() {}

    public Rule(List<String> pMatchers, Map<String, String> pParams) {
        matchers = new ArrayList<>(pMatchers);
        params = new HashMap<>(pParams);
    }

    public List<String> getMatchers() {
        return Collections.unmodifiableList(matchers);
    }

    public Map<String, String> getParams() {
        return Collections.unmodifiableMap(params);
    }
}
