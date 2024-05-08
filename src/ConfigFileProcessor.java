import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.*;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigFileProcessor {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Pattern KEY_VALUE_PATTERN = Pattern.compile("^\\s*(\\w+)\\s*:\\s*(.*?);\\s*$");

    public static void processConfigFile(String inputPath, String outputPath) throws IOException {
        JsonArray data = new JsonArray();

        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty() || line.trim().startsWith("//") || line.trim().startsWith("#")) {
                    continue; // Skip empty or commented lines
                }

                JsonObject jsonItem = new JsonObject();
                jsonItem.addProperty("text", line); // Use the line itself as the text

                try {
                    JsonObject jsonElement = StaticJavaParser.parse(line).toJSON();
                    jsonItem.add("state", createStateObject(true, true)); // Nodes are opened and selected by default
                    jsonItem.addProperty("text", jsonElement.toString());
                } catch (Exception e) {
                    Matcher matcher = KEY_VALUE_PATTERN.matcher(line);
                    if (matcher.matches()) {
                        JsonObject keyValueJson = new JsonObject();
                        keyValueJson.addProperty("text", matcher.group(1) + ": " + matcher.group(2));
                        jsonItem.add("children", new JsonArray());
                        jsonItem.getAsJsonArray("children").add(keyValueJson);
                    } else {
                        jsonItem.add("children", new JsonArray());
                        jsonItem.getAsJsonArray("children").add(createTextNode("Non-transformable line"));
                    }
                }

                data.add(jsonItem);
            }

            JsonObject root = new JsonObject();
            root.addProperty("id", "using_json");
            root.add("core", createCoreObject(data));

            writer.write("$('#using_json').jstree(" + gson.toJson(root) + ");");
        }
    }

    private static JsonObject createCoreObject(JsonArray data) {
        JsonObject core = new JsonObject();
        core.add("data", data);
        return core;
    }

    private static JsonObject createStateObject(boolean opened, boolean selected) {
        JsonObject state = new JsonObject();
        state.addProperty("opened", opened);
        state.addProperty("selected", selected);
        return state;
    }

    private static JsonObject createTextNode(String text) {
        JsonObject node = new JsonObject();
        node.addProperty("text", text);
        return node;
    }

    public static void main(String[] args) {
        try {
            String inputPath = "path/to/your/input/config.txt";
            String outputPath = "path/to/your/output/result.txt";
            processConfigFile(inputPath, outputPath);
            System.out.println("Processing completed. Output written to " + outputPath);
        } catch (IOException e) {
            System.err.println("Failed to process the config file: " + e.getMessage());
        }
    }
}
