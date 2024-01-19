package ru.zoommax.bitcoindocs.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Blocks {
    private String description;
    private Result result;
    private List<String> examples;

    public Blocks(String raw) {
        String[] parts = raw.split("Result|Examples:");
        this.description = parts[0].trim();

        if (parts.length > 1) {
            this.result = new Result(parts[1]);
        }

        if (parts.length > 2) {
            this.examples = new ArrayList<>(Arrays.asList(parts[2].split("\n>")));
            this.examples.replaceAll(String::trim);
        }
    }

    public String getDescription() {
        return description;
    }

    public Result getResult() {
        return result;
    }

    public List<String> getExamples() {
        return examples;
    }
}
