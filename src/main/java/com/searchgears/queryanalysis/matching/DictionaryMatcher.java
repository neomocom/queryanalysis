package com.searchgears.queryanalysis.matching;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DictionaryMatcher {
    private final Set<String> terms;

    public static DictionaryMatcher fromPath(Path path) {
        try {
            return new DictionaryMatcher(Files.readAllLines(path));
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading file \"" + path + "\". ");
        }
    }

    public DictionaryMatcher(List<String> entries) {
        terms = new HashSet<>(entries);
    }

    public boolean matches(String input) {
        return terms.contains(input);
    }
}
