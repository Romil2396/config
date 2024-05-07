import com.google.gson.*;

import java.io.*;
import java.nio.file.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigFileProcessor2 {

    private static final Path INPUT_FILE_PATH = Paths.get("path/to/your/input/config.txt");
    private static final Path OUTPUT_FILE_PATH = Paths.get("path/to/your/output/result.txt");
    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile("^\\s*([\\w\\s]+)\\s*:\\s*(\"[^\"]*\"|[^\\s]+)\\s*$");

    public static void processConfigFile() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        StringBuilder processedOutput = new StringBuilder();
        StringBuilder untouchedOutput = new StringBuilder();

        try (BufferedReader reader = Files.newBufferedReader(INPUT_FILE_PATH);
             BufferedWriter writer = Files.newBufferedWriter(OUTPUT_FILE_PATH, StandardOpenOption.CREATE)) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.trim().startsWith("//") || line.trim().startsWith("#")) {
                    untouchedOutput.append(line).append("\n"); // Preserve comments and empty lines as is
                    continue; // Skip processing for empty or commented lines
                }

                boolean handled = false;

                // Try to correct and parse as JSON
                try {
                    JsonElement jsonElement = JsonParser.parseString(correctMalformedJson(line));
                    if (jsonElement.isJsonObject() || jsonElement.isJsonArray()) {
                        processedOutput.append(gson.toJson(jsonElement)).append("\n");
                        handled = true;
                    }
                } catch (JsonSyntaxException ignored) {}

                // Check for key-value pairs
                if (!handled) {
                    Matcher matcher = KEY_VALUE_PATTERN.matcher(line);
                    if (matcher.matches()) {
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty(matcher.group(1).trim(), formatValue(matcher.group(2)));
                        processedOutput.append(gson.toJson(jsonObject)).append("\n");
                        handled = true;
                    }
                }

                // If no processing was successful, keep the line as original
                if (!handled) {
                    untouchedOutput.append(line).append("\n");
                }
            }

            // Write processed and untouched outputs
            writer.write(processedOutput.toString());
            writer.write(untouchedOutput.toString()); // Untouched lines are written last
        }
    }

    private static String correctMalformedJson(String json) {
        if (!json.trim().startsWith("{") && !json.trim().startsWith("[")) {
            json = "{" + json + "}";
        }
        return json.replaceAll("([^\\\"]\\s*:\\s*)([^\\\"\\{\\[]+)(\\s*[,\\}])", "$1\"$2\"$3"); // Add quotes around bare words
    }

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
