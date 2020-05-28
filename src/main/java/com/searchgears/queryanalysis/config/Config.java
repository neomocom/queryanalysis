package com.searchgears.queryanalysis.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.lucene.analysis.util.ResourceLoader;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class Config {
    private List<Rule> rules;
    private Map<String, Matcher> matchers;


    public static Config fromCorePath(ResourceLoader resourceLoader, String filename) {
        try (InputStream inputStream = resourceLoader.openResource(filename)) {
            return Config.fromInputStream(inputStream);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading file \"" + filename + "\". ");
        }
    }

    public static Config fromClasspath(String name) {
        return fromInputStream(ClassLoader.getSystemClassLoader().getResourceAsStream(name));
    }

    public static Config fromInputStream(InputStream inputStream) {
        try {
            return getObjectMapper().readValue(inputStream, Config.class).validate();
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading config from input stream.", e);
        }
    }

    private static ObjectMapper getObjectMapper() {
        return new ObjectMapper(new YAMLFactory());
    }

    public Config() {
    }

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

    private Config validate() {
        for (Rule rule : rules) {
            for (String matcher : rule.getMatchers()) {
                Matcher matcherDefintion = matchers.get(matcher);
                if (matcherDefintion == null) {
                    throw new IllegalArgumentException("No definition for matcher " + matcher + ". ");
                }
            }
        }
        return this;
    }
}
