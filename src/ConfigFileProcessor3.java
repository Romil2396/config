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
    private static final Pattern keyValuePattern = Pattern.compile("^\\s*(\\w+)\\s*:\\s*(.*?);\\s*$");

    public static void processConfigFile(String inputPath, String outputPath) throws IOException {
        JsonArray dataSections = new JsonArray();
        JsonArray untouchedLines = new JsonArray();

        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {

            String line;
            while ((line = reader.readLine()) != null) {
                JsonObject section = new JsonObject();
                JsonElement parsedElement;

                if (importPattern.matcher(line).matches()) {
                    continue; // Skip import lines as they should be handled separately
                }

                try {
                    parsedElement = JsonParser.parseString(line);
                } catch (Exception e) {
                    parsedElement = null;
                }

                if (parsedElement != null && parsedElement.isJsonObject()) {
                    section.addProperty("JSON Tree", gson.toJson(parsedElement));
                    dataSections.add(section);
                } else {
                    Matcher kvMatcher = keyValuePattern.matcher(line);
                    if (kvMatcher.matches()) {
                        JsonObject kvObject = new JsonObject();
                        kvObject.addProperty(kvMatcher.group(1), kvMatcher.group(2));
                        section.addProperty("Key-Value Pair", gson.toJson(kvObject));
                        dataSections.add(section);
                    } else if (line.contains("class") || line.contains("public static")) {
                        try {
                            CompilationUnit cu = StaticJavaParser.parse(line);
                            section.addProperty("Java AST", cu.toString());
                            dataSections.add(section);
                        } catch (Exception ex) {
                            JsonObject achRecord = achParser.parseRecord(line);
                            if (achRecord != null) {
                                section.addProperty("ACH/NACHA", gson.toJson(achRecord));
                                dataSections.add(section);
                            } else {
                                untouchedLines.add(line);
                            }
                        }
                    } else {
                        untouchedLines.add(line);
                    }
                }
            }

            JsonObject outputJson = new JsonObject();
            outputJson.add("Processed Sections", dataSections);
            outputJson.add("Untouched Lines", untouchedLines);

            writer.write(gson.toJson(outputJson));
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
