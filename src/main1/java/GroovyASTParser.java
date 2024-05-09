package main1.java;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class GroovyASTParser {

    public static void main(String[] args) {
        try {
            String groovyFilePath = "/path/to/your/file.groovy"; // Path to Groovy file
            String jsonFilePath = "/path/to/your/file.json"; // Path to output JSON file

            // Read Groovy file
            String scriptContent = new String(Files.readAllBytes(Paths.get(groovyFilePath)));

            // Parse Groovy script to AST
            ModuleNode moduleNode = parseGroovyScriptToAST(scriptContent);

            // Configure ObjectMapper and register custom serializer
            ObjectMapper mapper = new ObjectMapper();
            SimpleModule module = new SimpleModule();
            module.addSerializer(ModuleNode.class, new ModuleNodeSerializer());
            mapper.registerModule(module);

            // Serialize AST to JSON
            String json = mapper.writeValueAsString(moduleNode);

            // Save JSON to file
            Files.write(Paths.get(jsonFilePath), json.getBytes());

            System.out.println("AST JSON has been saved successfully!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static ModuleNode parseGroovyScriptToAST(String scriptContent) {
        CompilerConfiguration config = new CompilerConfiguration();
        config.setScriptBaseClass(Script.class.getName());

        GroovyShell shell = new GroovyShell(config);
        SourceUnit sourceUnit = SourceUnit.create("GroovyScript", scriptContent);
        sourceUnit.parse();
        sourceUnit.completePhase();
        sourceUnit.convert();
        return sourceUnit.getAST();
    }

    static class ModuleNodeSerializer extends JsonSerializer<ModuleNode> {
        @Override
        public void serialize(ModuleNode value, JsonGenerator gen, SerializerProvider serializers) throws IOException, IOException {
            gen.writeStartObject();
            gen.writeStringField("name", value.getPackageName());
            // Add more fields as necessary, handling each carefully to avoid recursion
            gen.writeEndObject();
        }
    }
}
