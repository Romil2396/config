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
import java.util.stream.Collectors;

public class GroovyConfigParser {

    public static void main(String[] args) {
        String filePath = "path/to/your/config.groovy";
        String outputJsonPath = "path/to/output.json";
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode jsonRoot = mapper.createObjectNode();

        try {
            List<String> lines = Files.readAllLines(Paths.get(filePath));
            GroovyShell shell = new GroovyShell(new CompilerConfiguration());
            ObjectNode parseableAST = mapper.createObjectNode();
            ObjectNode unparsableLines = mapper.createObjectNode();

            for (String line : lines) {
                try {
                    Script script = shell.parse(line);
                    parseableAST.put(line, script.getClass().getName());
                } catch (MultipleCompilationErrorsException e) {
                    unparsableLines.put("unparsable", line);
                }
            }

            jsonRoot.set("parseableAST", parseableAST);
            jsonRoot.set("unparsableLines", unparsableLines);

            mapper.writeValue(new File(outputJsonPath), jsonRoot);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
