package com.searchgears.queryanalysis.config;

import java.util.*;

public class Rule {
    private List<String> matchers;
    private Map<String, String> params;

    public Map<String, String> getParams() {
        return new HashMap<>(params);
    }

    public void setParams(Map<String, String> params) {
        this.params = new HashMap<>(params);
    }

    public List<String> getMatchers() {
        return new ArrayList<>(matchers);
    }

    public void setMatchers(List<String> pMatchters) {
        matchers = new ArrayList<>(pMatchters);
    }

}
