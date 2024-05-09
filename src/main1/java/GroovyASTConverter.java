package main1.java;

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class GroovyASTConverter {
    public static void main(String[] args) {
        String inputFilePath = "path/to/your/configfile.config"; // Ensure this path is correct
        String outputFilePath = "path/to/your/outputfile.ast";  // Ensure this path is correct

        convertGroovyCodeToAST(inputFilePath, outputFilePath);
    }

    private static void convertGroovyCodeToAST(String inputFilePath, String outputFilePath) {
        try {
            // Read the content of the Groovy file
            String groovyCode = new String(Files.readAllBytes(Paths.get(inputFilePath)));

            // Configure the compiler
            CompilerConfiguration config = new CompilerConfiguration();
            config.setSourceEncoding("UTF-8");

            // Create a CompilationUnit
            CompilationUnit compilationUnit = new CompilationUnit(config);
            compilationUnit.addSource("GroovyConfig", groovyCode);

            // Compile the Groovy code
            compilationUnit.compile();

            // Get the classes as ClassNode objects
            @SuppressWarnings("unchecked")  // Suppress the unchecked warning for this cast
            List<ClassNode> classes = (List<ClassNode>) (List<?>) compilationUnit.getClasses();

            // Write the ClassNodes to a file
            try (FileWriter writer = new FileWriter(outputFilePath)) {
                for (ClassNode classNode : classes) {
                    writer.write(classNode.toString());
                    writer.write("\n");
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling file: " + e.getMessage());
        } catch (MultipleCompilationErrorsException e) {
            System.err.println("Compilation errors: " + e.getMessage());
        } catch (ClassCastException e) {
            System.err.println("Error casting class list: " + e.getMessage());
        }
    }
}
