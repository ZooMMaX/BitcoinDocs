package ru.zoommax.bitcoindocs.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Blocks {
    private String description;
    private Result result;
    private List<String> examples;

    public Blocks(String raw) {
        String[] lines = raw.split("\n");
        String[] parts = {"","",""};
        int resultIndex = 0;
        int exampleIndex = 0;
        for (int i = 0; i < lines.length; i++) {
            if (lines[i].startsWith("Result") && resultIndex == 0) {
                resultIndex = i-1;
            }
            if (lines[i].startsWith("Example")) {
                exampleIndex = i;
            }
        }

        for (int i = 0; i < lines.length; i++) {
            if (i < resultIndex) {
                parts[0] = parts[0] + lines[i]+ "\n";
            }
            if (i > resultIndex && i < exampleIndex) {
                parts[1] = parts[1] + lines[i]+ "\n";
            }
            if (i > exampleIndex) {
                parts[2] = parts[2] + lines[i]+ "\n";
            }
        }

        this.description = parts[0].trim();
        this.result = new Result(parts[1]);
        this.examples = new ArrayList<>(Arrays.asList(parts[2].split(">")));
        this.examples.replaceAll(String::trim);
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
