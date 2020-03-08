package com.searchgears.queryanalysis.config;

import com.searchgears.queryanalysis.config.Config;
import com.searchgears.queryanalysis.config.Matcher;
import com.searchgears.queryanalysis.config.Rule;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.Parameterized;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConfigTest {
    private Config config;
    private List<Rule> rules;
    private Map<String, Matcher> matchers;

    @Before
    public void readConfig() {
        config = parseConfig("queryanalysis.yml");
        rules = config.getRules();
        matchers = config.getMatchers();
    }

    @Test
    public void rulesMatchersAreReadCorrectly() {
        assertTrue(rules.size() == 1);
        Rule rule = rules.get(0);
        assertArrayEquals(new String[]{"publisher", "publisher_marker"}, rule.getMatchers().toArray());
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
        assertEquals(Set.of("publisher", "publisher_marker"), matchers.keySet());
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

    private Config parseConfig(String fileName) {
        String file = ClassLoader.getSystemClassLoader()
                .getResource(fileName).getFile();
        Config config = Config.fromFile(file);
        return config;
    }

}
