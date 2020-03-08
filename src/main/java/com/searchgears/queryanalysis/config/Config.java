package com.searchgears.queryanalysis.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Config {
    private List<Rule> rules;
    private Map<String, Matcher> matchers;

    public static Config fromFile(String fileName) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        Config config = null;
        try {
            config = mapper.readValue(new File(fileName), Config.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not open file. ", e);
        }
        return config;
    }

    public Config() {}

    @JsonCreator
    public Config(@JsonProperty("rules") List<Rule> pRules,
                  @JsonProperty("matchers") Map<String, Matcher> pMatchers) {
        rules = new ArrayList<>(pRules);
        matchers = new HashMap<>(pMatchers);
    }

    public List<Rule> getRules() {
        return new ArrayList<>(rules);
    }

    public Map<String, Matcher> getMatchers() {
        return Collections.unmodifiableMap(matchers);
    }
}
