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

    public static void processConfigFile() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        GroovyShell groovyShell = new GroovyShell(new CompilerConfiguration());
        Pattern keyValuePattern = Pattern.compile("^\\s*(\\w+)\\s*:\\s*(.*)$");

        try (BufferedReader reader = Files.newBufferedReader(INPUT_FILE_PATH);
             BufferedWriter writer = Files.newBufferedWriter(OUTPUT_FILE_PATH, StandardOpenOption.CREATE)) {

            String line;
            while ((line = reader.readLine()) != null) {
                // Attempt to parse as JSON first
                try {
                    JsonElement jsonElement = JsonParser.parseString(line);
                    if (jsonElement.isJsonObject() || jsonElement.isJsonArray()) {
                        writer.write(gson.toJson(jsonElement));
                        writer.newLine();
                        continue;
                    }
                } catch (JsonSyntaxException ignored) {
                    // Not a JSON line, check for key-value pairs
                    Matcher matcher = keyValuePattern.matcher(line);
                    if (matcher.matches()) {
                        JsonObject jsonObject = new JsonObject();
                        jsonObject.addProperty(matcher.group(1), matcher.group(2));
                        writer.write(gson.toJson(jsonObject));
                        writer.newLine();
                        continue;
                    }
                }

                // Process as Groovy code for AST generation
                try {
                    Object result = groovyShell.evaluate("{ -> " + line + " }"); // Wrap in closure for safer evaluation
                    writer.write("Groovy AST: " + result.getClass().getSimpleName() + " -> " + result);
                    writer.newLine();
                } catch (Exception e) {
                    writer.write("Error processing line as Groovy code: " + e.getMessage());
                    writer.newLine();
                }
            }
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
