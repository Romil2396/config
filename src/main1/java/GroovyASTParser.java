package main1.java;

import groovy.lang.GroovyClassLoader;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.tools.GroovyClass;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class GroovyASTConverter {
    public static void main(String[] args) {
        String inputFilePath = "path/to/your/configfile.config";
        String outputFilePath = "path/to/your/outputfile.ast";

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
            compilationUnit.compile(); // Compile through all phases

            // Retrieve all classes in the CompilationUnit
            List<GroovyClass> classes = compilationUnit.getClasses();
            try (FileWriter writer = new FileWriter(outputFilePath)) {
                for (GroovyClass classNode : classes) {
                    writer.write(classNode.toString());
                    writer.write("\n");
                }
            }

        } catch (IOException e) {
            System.err.println("Error handling file: " + e.getMessage());
        }
    }
}
