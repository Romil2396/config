import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;

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
        CompilerConfiguration config = new CompilerConfiguration();
        config.setScriptBaseClass("groovy.transform.BaseScript");
        GroovyShell groovyShell = new GroovyShell(config);

        StringBuilder jsonOutput = new StringBuilder();
        StringBuilder groovyOutput = new StringBuilder();
        StringBuilder originalOutput = new StringBuilder(); // For lines that don't match any criteria

        try (BufferedReader reader = Files.newBufferedReader(INPUT_FILE_PATH);
             BufferedWriter writer = Files.newBufferedWriter(OUTPUT_FILE_PATH, StandardOpenOption.CREATE)) {

            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println("Processing line: " + line); // Debugging statement
                if (line.trim().startsWith("//") || line.trim().startsWith("#")) {
                    System.out.println("Skipping commented line."); // Debugging statement
                    continue; // Skip commented lines
                }

                boolean handled = false;

                // Attempt to parse as JSON first
                try {
                    JsonElement jsonElement = JsonParser.parseString(line);
                    if (jsonElement.isJsonObject() || jsonElement.isJsonArray()) {
                        String jsonStr = gson.toJson(jsonElement);
                        jsonOutput.append(jsonStr).append("\n");
                        System.out.println("Processed as JSON: " + jsonStr); // Debugging confirmation
                        handled = true;
                    }
                } catch (JsonSyntaxException ignored) {}

                if (!handled) {
                    // Check for key-value pairs
                    Matcher matcher = KEY_VALUE_PATTERN.matcher(line);
                    if (matcher.matches()) {
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty(matcher.group(1).trim(), matcher.group(2).replace("\"", ""));
                        String jsonStr = gson.toJson(jsonObject);
                        jsonOutput.append(jsonStr).append("\n");
                        System.out.println("Processed as Key-Value to JSON: " + jsonStr); // Debugging confirmation
                        handled = true;
                    }
                }

                if (!handled) {
                    // Process as Groovy code for AST generation
                    try {
                        Object result = groovyShell.evaluate("{ -> " + line + " }"); // Wrap in closure for safer evaluation
                        String resultStr = "Groovy AST: " + result.getClass().getSimpleName() + " -> " + result;
                        groovyOutput.append(resultStr).append("\n");
                        System.out.println("Processed as Groovy AST."); // Debugging statement
                        handled = true;
                    } catch (Exception e) {
                        String errorMessage = "Error processing line as Groovy code: " + line + " | Error: " + e.getMessage();
                        System.out.println(errorMessage); // Detailed error logging
                    }
                }

                if (!handled) {
                    // Keep original if no processing was successful
                    originalOutput.append(line).append("\n");
                    System.out.println("Keeping original line as is."); // Debugging statement
                }
            }

            // Write JSON outputs first, then AST outputs, and lastly the original lines
            writer.write(jsonOutput.toString());
            writer.write(groovyOutput.toString());
            writer.write(originalOutput.toString());
        }
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
