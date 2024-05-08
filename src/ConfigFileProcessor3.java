import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
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
        File inputFile = new File(inputPath);
        BufferedReader reader = new BufferedReader(new FileReader(inputFile));
        BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath));

        JsonArray importsArray = new JsonArray();
        JsonObject outputJson = new JsonObject();

        String line;
        while ((line = reader.readLine()) != null) {
            Matcher importMatcher = importPattern.matcher(line);
            if (importMatcher.matches()) {
                JsonObject importObject = new JsonObject();
                importObject.addProperty("value", importMatcher.group(1));
                importsArray.add(importObject);
                continue;
            }

            try {
                JsonObject jsonElement = JsonParser.parseString(line).getAsJsonObject();
                writer.write(gson.toJson(jsonElement) + "\n");
            } catch (Exception e) {
                JsonObject achRecord = achParser.parseRecord(line);
                if (achRecord != null) {
                    writer.write(gson.toJson(achRecord) + "\n");
                } else if (line.contains("class") || line.contains("public static")) {
                    try {
                        CompilationUnit cu = StaticJavaParser.parse(line);
                        writer.write(gson.toJson(cu.toString()) + "\n");
                    } catch (Exception ex) {
                        writer.write("// Unhandled Line // " + line + "\n");
                    }
                } else {
                    writer.write("// Unhandled Line // " + line + "\n");
                }
            }
        }

        outputJson.add("imports", importsArray);
        writer.write("Imports JSON: " + gson.toJson(outputJson) + "\n");

        reader.close();
        writer.close();
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
