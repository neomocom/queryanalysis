package com.searchgears.queryanalysis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Config {
    private List<Rule> rules;

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

    public List<Rule> getRules() {
        return new ArrayList<>(rules);
    }

    public void setRules(List<Rule> pRules) {
        rules = new ArrayList<>(pRules);
    }


}
