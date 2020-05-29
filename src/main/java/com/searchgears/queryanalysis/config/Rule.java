package com.searchgears.queryanalysis.config;

import java.util.*;

public class Rule {
    private List<String> matchers;
    private Map<String, String> params;

    public Rule() {
    }

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

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<String> matchers = new ArrayList<>();
        private Map<String, String> params = new HashMap<>();

        public Builder withMatcher(String matcher) {
            this.matchers.add(matcher);
            return this;
        }

        public Builder withParam(String param, String value) {
            this.params.put(param, value);
            return this;
        }

        public Rule build() {
            return new Rule(matchers, params);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Rule)) return false;
        Rule rule = (Rule) o;
        return matchers.equals(rule.matchers) &&
            params.equals(rule.params);
    }

    @Override
    public int hashCode() {
        return Objects.hash(matchers, params);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Rule.class.getSimpleName() + "[", "]")
            .add("matchers=" + matchers)
            .add("params=" + params)
            .toString();
    }
}
