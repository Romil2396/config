import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.google.gson.*;

import java.io.*;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigFileProcessor {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile("^\\s*([\\w\\s]+)\\s*:\\s*(.*?)\\s*;\\s*$");

    public void processConfigFile(String inputPath, String outputPath) throws IOException {
        JsonArray data = new JsonArray();

        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.trim().startsWith("//") || line.trim().startsWith("#")) {
                    continue; // Skip empty or commented lines
                }

                JsonObject jsonItem = parseLineToJson(line);
                data.add(jsonItem);
            }

            JsonObject root = new JsonObject();
            root.add("data", data);
            writer.write(gson.toJson(root));
        }
    }

    private JsonObject parseLineToJson(String line) {
        JsonObject jsonItem = new JsonObject();
        Matcher matcher = KEY_VALUE_PATTERN.matcher(line);
        if (matcher.find()) {
            String key = matcher.group(1).trim().replaceAll("\\s+", "_"); // Replace spaces with underscores in key names
            String value = matcher.group(2).trim();
            jsonItem.addProperty(key, value);
        } else {
            // Attempt to parse as JSON directly
            try {
                JsonElement jsonElement = JsonParser.parseString(line);
                jsonItem.add("parsed_json", jsonElement);
            } catch (Exception e) {
                // If it fails, treat as plain text
                jsonItem.addProperty("plain_text", line);
            }
        }
        return jsonItem;
    }

    public static void main(String[] args) {
        try {
            ConfigFileProcessor processor = new ConfigFileProcessor();
            String inputPath = "path/to/your/input/config.txt";
            String outputPath = "path/to/your/output/result.txt";
            processor.processConfigFile(inputPath, outputPath);
            System.out.println("Processing completed. Output written to " + outputPath);
        } catch (IOException e) {
            System.err.println("Failed to process the config file: " + e.getMessage());
        }
    }
}
