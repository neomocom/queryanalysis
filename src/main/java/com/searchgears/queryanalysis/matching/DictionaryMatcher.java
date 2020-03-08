package com.searchgears.queryanalysis.matching;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DictionaryMatcher {
    public DictionaryMatcher(String fileName) {
        try {
            Files.readAllLines(Path.of(fileName));
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading file \"" + fileName + "\". ");
        }
    }
}
