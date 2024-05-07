import com.google.gson.*;

import java.io.*;
import java.nio.file.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigFileProcessor1 {

    private static final Path INPUT_FILE_PATH = Paths.get("path/to/your/input/config.txt");
    private static final Path OUTPUT_FILE_PATH = Paths.get("path/to/your/output/result.txt");
    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile("^\\s*([\\w\\s]+)\\s*:\\s*(\"[^\"]*\"|[^\\s]+)\\s*$");

    public static void processConfigFile() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        StringBuilder jsonOutput = new StringBuilder("// JSON //\n");
        StringBuilder keyValueOutput = new StringBuilder("// KEY PAIR //\n");
        StringBuilder untouchedOutput = new StringBuilder("// UNTOUCHED //\n");

        try (BufferedReader reader = Files.newBufferedReader(INPUT_FILE_PATH);
             BufferedWriter writer = Files.newBufferedWriter(OUTPUT_FILE_PATH, StandardOpenOption.CREATE)) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.trim().startsWith("//") || line.trim().startsWith("#")) {
                    continue; // Skip empty or commented lines
                }

                boolean handled = false;

                // Try to correct and parse as JSON
                try {
                    JsonElement jsonElement = JsonParser.parseString(correctMalformedJson(line));
                    if (jsonElement.isJsonObject() || jsonElement.isJsonArray()) {
                        jsonOutput.append(gson.toJson(jsonElement)).append("\n");
                        handled = true;
                    }
                } catch (JsonSyntaxException ignored) {}

                // Check for key-value pairs
                if (!handled) {
                    Matcher matcher = KEY_VALUE_PATTERN.matcher(line);
                    if (matcher.matches()) {
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty(matcher.group(1).trim(), formatValue(matcher.group(2)));
                        keyValueOutput.append(gson.toJson(jsonObject)).append("\n");
                        handled = true;
                    }
                }

                // Attempt to parse as ACH record
                if (!handled) {
                    JsonObject achRecord = tryParseACHRecord(line);
                    if (achRecord != null) {
                        untouchedOutput.append(gson.toJson(achRecord)).append("\n");
                        handled = true;
                    }
                }

                // If no processing was successful, keep the line as original
                if (!handled) {
                    untouchedOutput.append(line).append("\n");
                }
            }

            // Write outputs with separators
            writer.write(jsonOutput.toString());
            writer.write(keyValueOutput.toString());
            writer.write(untouchedOutput.toString());
        }
    }

    private static JsonObject tryParseACHRecord(String line) {
        // Here you can add logic specific to parsing ACH records
        // For now, this is a placeholder and should be adjusted based on actual ACH format
        if (line.length() > 1 && Character.isDigit(line.charAt(0))) {
            JsonObject json = new JsonObject();
            json.addProperty("RecordType", "ACH Record");
            json.addProperty("Content", line);
            return json;
        }
        return null;
    }

    // Correct common JSON formatting issues
    private static String correctMalformedJson(String json) {
        if (!json.trim().startsWith("{") && !json.trim().startsWith("[")) {
            json = "{" + json + "}";
        }
        return json.replaceAll("([^\\\"]\\s*:\\s*)([^\\\"\\{\\[]+)(\\s*[,\\}])", "$1\"$2\"$3"); // Attempt to add quotes around bare words
    }

    // Ensure values are properly formatted as JSON values
    private static String formatValue(String value) {
        if (!value.startsWith("\"")) {
            value = "\"" + value + "\"";
        }
        return value;
    }

    public static void main(String[] args) {
        try {
            processConfigFile();
            System.out.println("Processing completed. Output written to " + OUTPUT_FILE_PATH);
        } catch (IOException e) {
            System.err.println("Failed to process the config file: " + e.getMessage());
        }
    }
}
