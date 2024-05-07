import com.google.gson.*;

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
        StringBuilder jsonOutput = new StringBuilder();
        StringBuilder otherOutput = new StringBuilder();

        try (BufferedReader reader = Files.newBufferedReader(INPUT_FILE_PATH);
             BufferedWriter writer = Files.newBufferedWriter(OUTPUT_FILE_PATH, StandardOpenOption.CREATE)) {

            String line;
            while ((line = reader.readLine()) != null) {
                boolean handled = false;

                // Try to parse as JSON first
                if (tryParseJson(line, jsonOutput, gson)) {
                    handled = true;
                }

                // Check for key-value pairs
                if (!handled && tryParseKeyValue(line, jsonOutput, gson)) {
                    handled = true;
                }

                // If no processing was successful, check if it's a NACHA record
                if (!handled && tryParseNACHA(line, otherOutput)) {
                    handled = true;
                }

                // If still not handled, it's truly untouched
                if (!handled) {
                    otherOutput.append(line).append("\n");
                }
            }

            // Write JSON outputs first, then other outputs
            writer.write("// JSON Output //\n");
            writer.write(jsonOutput.toString());
            writer.write("// Other Output //\n");
            writer.write(otherOutput.toString());
        }
    }

    private static boolean tryParseJson(String line, StringBuilder output, Gson gson) {
        try {
            JsonElement jsonElement = JsonParser.parseString(line);
            if (jsonElement.isJsonObject() || jsonElement.isJsonArray()) {
                output.append(gson.toJson(jsonElement)).append("\n");
                return true;
            }
        } catch (JsonSyntaxException ignored) {}
        return false;
    }

    private static boolean tryParseKeyValue(String line, StringBuilder output, Gson gson) {
        Matcher matcher = KEY_VALUE_PATTERN.matcher(line);
        if (matcher.matches()) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty(matcher.group(1).trim(), matcher.group(2).replace("\"", ""));
            output.append(gson.toJson(jsonObject)).append("\n");
            return true;
        }
        return false;
    }

    private static boolean tryParseNACHA(String line, StringBuilder output) {
        // Here you can add logic specific to parsing NACHA records
        // For now, we simulate a check for NACHA record types based on expected starting characters
        if (line.length() > 1 && Character.isDigit(line.charAt(0))) {
            output.append("NACHA Record: ").append(line).append("\n");
            return true;
        }
        return false;
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
