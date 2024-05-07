import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

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
        JsonArray batches = new JsonArray();
        JsonObject batch = null;
        JsonArray entries = null;

        try (BufferedReader reader = Files.newBufferedReader(INPUT_FILE_PATH);
             BufferedWriter writer = Files.newBufferedWriter(OUTPUT_FILE_PATH, StandardOpenOption.CREATE)) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.trim().startsWith("//") || line.trim().startsWith("#")) {
                    continue; // Skip empty or commented lines
                }

                boolean handled = false;

                // Handle NACHA lines
                if (line.length() > 0 && Character.isDigit(line.charAt(0))) {
                    char recordType = line.charAt(0);
                    switch (recordType) {
                        case '1': // File Header
                            JsonObject fileHeader = parseFileHeader(line);
                            jsonOutput.append(gson.toJson(fileHeader)).append("\n");
                            handled = true;
                            break;
                        case '5': // Batch Header
                            batch = parseBatchHeader(line);
                            entries = new JsonArray();
                            handled = true;
                            break;
                        case '6': // Entry Detail
                            JsonObject entry = parseEntryDetail(line);
                            if (entries != null) {
                                entries.add(entry);
                            }
                            handled = true;
                            break;
                        case '8': // Batch Control
                            JsonObject batchControl = parseBatchControl(line);
                            if (batch != null) {
                                batch.add("batchControl", batchControl);
                                batch.add("entries", entries);
                                batches.add(batch);
                                batch = null;
                            }
                            handled = true;
                            break;
                        case '9': // File Control
                            JsonObject fileControl = parseFileControl(line);
                            jsonOutput.append(gson.toJson(fileControl)).append("\n");
                            jsonOutput.append(gson.toJson(batches)).append("\n");
                            handled = true;
                            break;
                    }
                }

                // Try to parse as JSON
                if (!handled) {
                    try {
                        JsonElement jsonElement = JsonParser.parseString(line);
                        if (jsonElement.isJsonObject() || jsonElement.isJsonArray()) {
                            jsonOutput.append(gson.toJson(jsonElement)).append("\n");
                            handled = true;
                        }
                    } catch (JsonSyntaxException ignored) {
                        // Not JSON or malformed JSON
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

    // NACHA parsing methods: you will need to flesh these out based on the actual file structure and requirements.
    private static JsonObject parseFileHeader(String line) {
        JsonObject json = new JsonObject();
        json.addProperty("recordTypeCode", line.substring(0, 1));
        // Add more fields as per NACHA file header record specifications
        return json;
    }

    private static JsonObject parseBatchHeader(String line) {
        JsonObject json = new JsonObject();
        json.addProperty("recordTypeCode", line.substring(0, 1));
        // Add more fields as per NACHA batch header record specifications
        return json;
    }

    private static JsonObject parseEntryDetail(String line) {
        JsonObject json = new JsonObject();
        json.addProperty("recordTypeCode", line.substring(0, 1));
        // Add more fields as per NACHA entry detail record specifications
        return json;
    }

    private static JsonObject parseBatchControl(String line) {
        JsonObject json = new JsonObject();
        json.addProperty("recordTypeCode", line.substring(0, 1));
        // Add more fields as per NACHA batch control record specifications
        return json;
    }

    private static JsonObject parseFileControl(String line) {
        JsonObject json = new JsonObject();
        json.addProperty("recordTypeCode", line.substring(0, 1));
        // Add more fields as per NACHA file control record specifications
        return json;
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
