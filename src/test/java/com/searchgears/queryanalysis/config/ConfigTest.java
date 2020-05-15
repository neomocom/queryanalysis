package com.searchgears.queryanalysis.config;

import com.google.common.collect.ImmutableSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigTest {
    private Config config;
    private List<Rule> rules;
    private Map<String, Matcher> matchers;

    @BeforeEach
    public void readConfig() {
        config = parseConfig("queryanalysis.yml");
        rules = config.getRules();
        matchers = config.getMatchers();
    }

    @Test
    public void rulesMatchersAreReadCorrectly() {
        assertTrue(rules.size() == 1);
        Rule rule = rules.get(0);
        assertArrayEquals(new String[]{"publisher", "publisherMarker"}, rule.getMatchers().toArray());
    }

    @Test
    public void rulesParamsAreReadCorrectly() {
        Rule rule = rules.get(0);
        Map<String, String> params = rule.getParams();
        assertTrue(params.size() == 1);
        assertEquals(params.get("qf"), "authors^1000");
    }

    @Test
    public void matchersAreReadCorrectly() {
        assertTrue(matchers.size() == 2);
        assertEquals(ImmutableSet.of("publisher", "publisherMarker"), matchers.keySet());
    }

    @Test
    public void matchersHaveCorrectDictionary() {
        assertEquals("publisher.dic", matchers.get("publisher").getDictionary());
    }

    @Test
    public void matcherWithoutDefinitionThrows() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            parseConfig("queryanalysis-invalid.yml");
        });
        assertEquals("No definition for matcher foobasel. ", exception.getMessage());
    }

    @Test
    public void nonExistingConfigFileThrows() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            Config.fromFile("non-existing");
        });
        assertEquals("Error reading file \"non-existing\". ", exception.getMessage());
    }


    private Config parseConfig(String fileName) {
        String file = ClassLoader.getSystemClassLoader()
                .getResource(fileName).getFile();
        Config config = Config.fromFile(file);
        return config;
    }

}
