import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.*;
import java.nio.file.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigFileProcessor1 {

    private static final Path INPUT_FILE_PATH = Paths.get("path/to/your/input/config.txt");
    private static final Path OUTPUT_FILE_PATH = Paths.get("path/to/your/output/result.txt");
    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile("^\\s*([\\w\\s]+)\\s*:\\s*(\"[^\"]*\"|[^\\s]+)\\s*$");

    public static void processFile() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        StringBuilder output = new StringBuilder();
        StringBuilder jsonOutput = new StringBuilder("// JSON //\n");
        StringBuilder keyValueOutput = new StringBuilder("// KEY PAIR //\n");
        StringBuilder untouchedOutput = new StringBuilder("// UNTOUCHED //\n");

        try (BufferedReader reader = Files.newBufferedReader(INPUT_FILE_PATH);
             BufferedWriter writer = Files.newBufferedWriter(OUTPUT_FILE_PATH, StandardOpenOption.CREATE)) {

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("Processing line: " + line);  // Debug output

                boolean handled = false;

                // Handle NACHA lines
                if (line.length() > 1 && Character.isDigit(line.charAt(0))) {
                    System.out.println("Detected possible NACHA record");
                    handled = handleNACHA(line, jsonOutput);
                }

                // Try to parse as JSON
                if (!handled) {
                    try {
                        JsonElement jsonElement = JsonParser.parseString(line);
                        if (jsonElement.isJsonObject() || jsonElement.isJsonArray()) {
                            jsonOutput.append(gson.toJson(jsonElement)).append("\n");
                            handled = true;
                            System.out.println("Processed as JSON");
                        }
                    } catch (JsonSyntaxException e) {
                        System.out.println("JSON parsing failed: " + e.getMessage());
                    }
                }

                // Check for key-value pairs
                if (!handled) {
                    Matcher matcher = KEY_VALUE_PATTERN.matcher(line);
                    if (matcher.matches()) {
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty(matcher.group(1).trim(), matcher.group(2).replace("\"", ""));
                        keyValueOutput.append(gson.toJson(jsonObject)).append("\n");
                        handled = true;
                        System.out.println("Processed as Key-Value Pair");
                    }
                }

                // If no processing was successful, keep the line as original
                if (!handled) {
                    untouchedOutput.append(line).append("\n");
                    System.out.println("Keeping original line as is");
                }
            }

            // Write outputs with separators
            writer.write(jsonOutput.toString());
            writer.write(keyValueOutput.toString());
            writer.write(untouchedOutput.toString());
        }
    }

    private static boolean handleNACHA(String line, StringBuilder jsonOutput) {
        char recordType = line.charAt(0);
        JsonObject json = new JsonObject();
        boolean handled = false;

        switch (recordType) {
            case '1': // File Header
                json.addProperty("Type", "File Header");
                handled = true;
                break;
            case '5': // Batch Header
                json.addProperty("Type", "Batch Header");
                handled = true;
                break;
            case '6': // Entry Detail
                json.addProperty("Type", "Entry Detail");
                handled = true;
                break;
            case '8': // Batch Control
                json.addProperty("Type", "Batch Control");
                handled = true;
                break;
            case '9': // File Control
                json.addProperty("Type", "File Control");
                handled = true;
                break;
        }

        if (handled) {
            jsonOutput.append(json.toString()).append("\n");
        }
        return handled;
    }

    public static void main(String[] args) {
        try {
            processFile();
            System.out.println("Processing completed. Output written to " + OUTPUT_FILE_PATH);
        } catch (IOException e) {
            System.err.println("Failed to process the file: " + e.getMessage());
        }
    }
}
