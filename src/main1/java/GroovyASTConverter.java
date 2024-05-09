package main1.java;

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.tools.GroovyClass;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class GroovyASTConverter {
    public static void main(String[] args) {
        String inputFilePath = "path/to/your/configfile.config";
        String outputFilePath = "path/to/your/outputfile.ast";

        convertGroovyCodeToAST(inputFilePath, outputFilePath);
    }

    private static void convertGroovyCodeToAST(String inputFilePath, String outputFilePath) {
        try {
            String content = new String(Files.readAllBytes(Paths.get(inputFilePath)));
            List<String> snippets = Arrays.asList(content.split("---")); // Assuming '---' is the delimiter
            try (FileWriter writer = new FileWriter(outputFilePath)) {
                for (String snippet : snippets) {
                    CompilerConfiguration config = new CompilerConfiguration();
                    config.setSourceEncoding("UTF-8");
                    CompilationUnit compilationUnit = new CompilationUnit(config);
                    compilationUnit.addSource("Snippet", snippet);
                    try {
                        compilationUnit.compile();
                        List<GroovyClass> classes = compilationUnit.getClasses();
                        writer.write("Converted AST:\n");
                        for (GroovyClass classNode : classes) {
                            writer.write(classNode.toString() + "\n");
                        }
                        writer.write("---\n"); // Delimiter in output file
                    } catch (MultipleCompilationErrorsException e) {
                        writer.write("Unconverted Code:\n");
                        writer.write(snippet + "\n");
                        writer.write("---\n"); // Delimiter in output file
                        System.err.println("Skipping problematic code snippet due to errors: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error handling file: " + e.getMessage());
        }
    }
}
