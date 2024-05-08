import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.io.*;
import java.nio.file.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigFileProcessor {

    private static final Path INPUT_FILE_PATH = Paths.get("path/to/your/input/config.txt");
    private static final Path OUTPUT_FILE_PATH = Paths.get("path/to/your/output/result.txt");
    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile("^\\s*([\\w\\s]+)\\s*:\\s*(\"[^\"]*\"|[^\\s]+)\\s*$");

    public static void processConfigFile() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        StringBuilder jsonOutput = new StringBuilder();
        StringBuilder keyValueOutput = new StringBuilder();
        StringBuilder untouchedOutput = new StringBuilder();

        try (BufferedReader reader = Files.newBufferedReader(INPUT_FILE_PATH);
             BufferedWriter writer = Files.newBufferedWriter(OUTPUT_FILE_PATH, StandardOpenOption.CREATE)) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.trim().startsWith("//") || line.trim().startsWith("#")) {
                    continue; // Skip empty or commented lines
                }

                boolean handled = false;

                // Try to parse as JSON
                try {
                    JsonElement jsonElement = JsonParser.parseString(line);
                    if (jsonElement.isJsonObject() || jsonElement.isJsonArray()) {
                        if (!jsonElement.toString().equals("{}")) { // Ensure not writing empty objects
                            jsonOutput.append(gson.toJson(jsonElement)).append("\n");
                            handled = true;
                        }
                    }
                } catch (JsonSyntaxException e) {
                    // Not JSON or malformed, continue to try other formats
                }

                // Check for key-value pairs
                if (!handled) {
                    Matcher matcher = KEY_VALUE_PATTERN.matcher(line);
                    if (matcher.matches()) {
                        String key = matcher.group(1).trim();
                        String value = formatValue(matcher.group(2).trim());
                        if (!value.isEmpty()) {
                            JsonObject jsonObject = new JsonObject();
                            jsonObject.addProperty(key, value);
                            keyValueOutput.append(gson.toJson(jsonObject)).append("\n");
                            handled = true;
                        }
                    }
                }

                // If no processing was successful, keep the line as original
                if (!handled) {
                    untouchedOutput.append("{").append(line).append("}").append("\n");
                }
            }

            // Write outputs
            if (jsonOutput.length() > 0) writer.write("// JSON\n" + jsonOutput.toString());
            if (keyValueOutput.length() > 0) writer.write("// Key-Value Pairs\n" + keyValueOutput.toString());
            if (untouchedOutput.length() > 0) writer.write("// Untouched\n" + untouchedOutput.toString());
        }
    }

    // Ensure values are properly formatted as JSON values
    private static String formatValue(String value) {
        if (!value.startsWith("\"") && !value.matches("-?\\d+(\\.\\d+)?")) { // check if not a number
            value = "\"" + value + "\""; // quote the value if not a numeric
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
