import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import groovy.lang.GroovyShell;
import org.codehaus.groovy.control.CompilerConfiguration;

import java.io.*;
import java.nio.file.*;

public class ConfigFileProcessor {

    private static final Path INPUT_FILE_PATH = Paths.get("path/to/your/input/config.txt");
    private static final Path OUTPUT_FILE_PATH = Paths.get("path/to/your/output/result.txt");

    public static void processConfigFile() throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        GroovyShell groovyShell = new GroovyShell(new CompilerConfiguration());

        try (BufferedReader reader = Files.newBufferedReader(INPUT_FILE_PATH);
             BufferedWriter writer = Files.newBufferedWriter(OUTPUT_FILE_PATH, StandardOpenOption.CREATE)) {

            String line;
            while ((line = reader.readLine()) != null) {
                try {
                    JsonElement jsonElement = JsonParser.parseString(line);
                    if (jsonElement.isJsonObject() || jsonElement.isJsonArray()) {
                        writer.write(gson.toJson(jsonElement));
                        writer.newLine();
                        continue;
                    }
                } catch (JsonSyntaxException ignored) {
                    // Not a JSON line
                }

                // Process as Groovy code
                try {
                    Object result = groovyShell.evaluate(line);
                    writer.write("Groovy AST: " + result);
                    writer.newLine();
                } catch (Exception e) {
                    writer.write("Error processing Groovy line: " + e.getMessage());
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
