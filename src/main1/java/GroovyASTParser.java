package main1.java;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.SourceUnit;
import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.ObjectMapper;

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

            // Wrap the ModuleNode with ASTNodeWrapper
            ASTNodeWrapper wrapper = new ASTNodeWrapper(moduleNode);

            // Serialize AST to JSON
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(wrapper);

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

    @JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "@id")
    public static class ASTNodeWrapper {
        private ModuleNode moduleNode;

        public ASTNodeWrapper(ModuleNode moduleNode) {
            this.moduleNode = moduleNode;
        }

        public ModuleNode getModuleNode() {
            return moduleNode;
        }
    }
}
