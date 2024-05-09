import groovy.json.JsonBuilder
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import java.io.File

def parseConfigToAST(String filePath) {
    CompilerConfiguration config = new CompilerConfiguration()
    config.setSourceEncoding("UTF-8")
    CompilationUnit compUnit = new CompilationUnit(config)

    File sourceFile = new File(filePath)
    if (!sourceFile.exists()) {
        println("File does not exist: $filePath")
        return null
    }

    try {
        compUnit.addSource(sourceFile)
        compUnit.compile()  // Compile the unit
        // Retrieve the class nodes only if compilation succeeded
        def classNodes = compUnit.classes
        return classNodes.isEmpty() ? null : classNodes
    } catch (MultipleCompilationErrorsException e) {
        println("Error compiling file: $filePath")
        return null
    }
}

def convertASTToJson(List classNodes, File jsonFile) {
    if (classNodes == null || classNodes.isEmpty()) {
        println("No class nodes available to convert to JSON.")
        return
    }
    JsonBuilder jsonBuilder = new JsonBuilder()
    jsonBuilder.call(classNodes)
    jsonFile.text = jsonBuilder.toPrettyString()
}

def createJsonFromConfigFile(File file, File jsonFile) {
    JsonBuilder jsonBuilder = new JsonBuilder()
    List<String> unparseableLines = []
    file.eachLine { line, lineNumber ->
        try {
            jsonBuilder(lineNumber: line)
        } catch (Exception e) {
            unparseableLines << "Line $lineNumber: $line"
        }
    }
    jsonFile.text = jsonBuilder.toPrettyString()
    return unparseableLines
}

def runConfiguration(String[] args) {
    String configFilePath = args[0]

    // Generate AST from .config file
    List classNodes = parseConfigToAST(configFilePath)
    File astFile = new File(configFilePath.replace(".config", ".ast"))
    convertASTToJson(classNodes, astFile)

    // Parse .config file to JSON
    File configFile = new File(configFilePath)
    File configJsonFile = new File(configFilePath.replace(".config", ".json"))
    List<String> errorLines = createJsonFromConfigFile(configFile, configJsonFile)

    // Handle error lines
    if (!errorLines.isEmpty()) {
        File errorFile = new File(configFilePath.replace(".config", "_errors.txt"))
        errorFile.text = errorLines.join("\n")
    }
}

runConfiguration(args)
