import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfigFileProcessor3 {

    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final DynamicACHParser achParser = new DynamicACHParser();
    private static final Pattern importPattern = Pattern.compile("^import\\s+(.*?);");

    public static void processConfigFile(String inputPath, String outputPath) throws IOException {
        JsonArray data = new JsonArray();
        data.add("Simple root node");

        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (importPattern.matcher(line).matches()) {
                    Matcher m = importPattern.matcher(line);
                    if (m.find()) {
                        JsonObject importObj = new JsonObject();
                        importObj.addProperty("text", m.group(1));
                        data.add(importObj);
                    }
                    continue;
                }

                JsonElement parsedElement;
                try {
                    parsedElement = JsonParser.parseString(line);
                } catch (Exception e) {
                    parsedElement = null;
                }

                if (parsedElement != null && parsedElement.isJsonObject()) {
                    data.add(parsedElement.getAsJsonObject());
                } else {
                    Matcher kvMatcher = Pattern.compile("^\\s*(\\w+)\\s*:\\s*(.*?);\\s*$").matcher(line);
                    if (kvMatcher.matches()) {
                        JsonObject kvObject = new JsonObject();
                        kvObject.addProperty(kvMatcher.group(1), kvMatcher.group(2));
                        data.add(kvObject);
                    } else if (line.contains("class") || line.contains("public static")) {
                        try {
                            CompilationUnit cu = StaticJavaParser.parse(line);
                            JsonObject astJson = new JsonObject();
                            astJson.addProperty("text", cu.toString());
                            data.add(astJson);
                        } catch (Exception ex) {
                            JsonObject achRecord = achParser.parseRecord(line);
                            if (achRecord != null) {
                                data.add(achRecord);
                            } else {
                                JsonObject unhandled = new JsonObject();
                                unhandled.addProperty("text", "Unhandled line: " + line);
                                data.add(unhandled);
                            }
                        }
                    }
                }
            }

            JsonObject root = new JsonObject();
            root.add("data", data);
            writer.write(gson.toJson(root));
        }
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
