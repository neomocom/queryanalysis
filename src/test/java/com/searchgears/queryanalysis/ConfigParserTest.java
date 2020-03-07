package com.searchgears.queryanalysis;

import com.searchgears.queryanalysis.config.Config;
import com.searchgears.queryanalysis.config.Rules;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ConfigParserTest {

    @Test
    public void matchersAreReadCorrectly() {
        Config config = parseConfig("queryanalysis.yml");
        Rules rules = config.getRules();

    }

    private Config parseConfig(String fileName) {
        Config config = Config.fromFile(fileName);
        return config;
    }

}
