package com.searchgears.queryanalysis;

import com.searchgears.queryanalysis.config.Config;
import com.searchgears.queryanalysis.config.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

@RunWith(JUnit4.class)
public class ConfigParserTest {

    @Test
    public void matchersAreReadCorrectly() {
        Config config = parseConfig("queryanalysis.yml");
        List<Rule> rules = config.getRules();

    }

    private Config parseConfig(String fileName) {
        String file = ClassLoader.getSystemClassLoader()
                .getResource(fileName).getFile();
        Config config = Config.fromFile(file);
        return config;
    }

}
