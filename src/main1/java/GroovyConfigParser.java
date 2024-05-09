package main1.java;

import groovy.lang.GroovyShell;
import groovy.lang.Script;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class GroovyConfigParser {

    public static void main(String[] args) {
        String filePath = "path/to/your/config.groovy";
        String outputJsonPath = "path/to/output.json";
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonRoot = mapper.createObjectNode();

        try {
            String scriptContent = new String(Files.readAllBytes(Paths.get(filePath)));
            GroovyShell shell = new GroovyShell(new CompilerConfiguration());
            ObjectNode parseableAST = mapper.createObjectNode();
            ObjectNode unparsableSections = mapper.createObjectNode();

            try {
                // Attempt to parse the entire file as a single script
                Script script = shell.parse(scriptContent);
                parseableAST.put("wholeScript", script.getClass().getName());
            } catch (MultipleCompilationErrorsException e) {
                // Handle parsing errors by possibly trying to parse smaller sections or reporting error sections
                unparsableSections.put("unparsableSection", e.getMessage());
            }

            jsonRoot.set("parseableAST", parseableAST);
            jsonRoot.set("unparsableSections", unparsableSections);

            mapper.writeValue(new File(outputJsonPath), jsonRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
