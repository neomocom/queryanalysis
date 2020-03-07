package com.searchgears.queryanalysis.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.IOException;

public class Config {
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
    public Rules getRules() {
        return null;
    }
}
