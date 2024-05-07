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

        try (BufferedReader reader = Files.newBufferedReader(INPUT_FILE_PATH);
             BufferedWriter writer = Files.newBufferedWriter(OUTPUT_FILE_PATH, StandardOpenOption.CREATE)) {

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().startsWith("//") || line.trim().startsWith("#")) {
                    continue; // Skip commented lines
                }

                // Attempt to parse as JSON first
                try {
                    JsonElement jsonElement = JsonParser.parseString(line);
                    if (jsonElement.isJsonObject() || jsonElement.isJsonArray()) {
                        jsonOutput.append(gson.toJson(jsonElement)).append("\n");
                        continue;
                    }
                } catch (JsonSyntaxException ignored) {
                    // Not a JSON line, check for key-value pairs
                    Matcher matcher = KEY_VALUE_PATTERN.matcher(line);
                    if (matcher.matches()) {
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty(matcher.group(1).trim(), matcher.group(2).replace("\"", ""));
                        jsonOutput.append(gson.toJson(jsonObject)).append("\n");
                        continue;
                    }
                }

                // Process as Groovy code for AST generation
                try {
                    Object result = groovyShell.evaluate("{ -> " + line + " }"); // Wrap in closure for safer evaluation
                    groovyOutput.append("Groovy AST: ").append(result.getClass().getSimpleName()).append(" -> ").append(result).append("\n");
                } catch (Exception e) {
                    groovyOutput.append("Error processing line as Groovy code: ").append(line).append(" | Error: ").append(e.getMessage()).append("\n");
                }
            }

            // Write JSON outputs first, then AST outputs
            writer.write(jsonOutput.toString());
            writer.write(groovyOutput.toString());
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
