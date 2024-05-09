import groovy.json.JsonBuilder
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.ast.ASTNode
import java.io.File

def parseConfigToAST(String filePath) {
    CompilerConfiguration config = new CompilerConfiguration()
    config.setSourceEncoding("UTF-8")  // Ensure the encoding is set if needed
    CompilationUnit compUnit = new CompilationUnit(config)

    File sourceFile = new File(filePath)
    if (!sourceFile.exists()) {
        println("File does not exist: $filePath")
        return null
    }

    try {
        compUnit.addSource(sourceFile)
        compUnit.compile(Phase.CONVERSION)  // Compile to the conversion phase to get the AST
        return compUnit.AST
    } catch (MultipleCompilationErrorsException e) {
        println("Error compiling file: $filePath")
        return null
    }
}

def convertASTToJson(ASTNode ast, File jsonFile) {
    JsonBuilder jsonBuilder = new JsonBuilder()
    jsonBuilder.call(ast)
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
    def ast = parseConfigToAST(configFilePath)
    File astFile = new File(configFilePath.replace(".config", ".ast"))
    convertASTToJson(ast, astFile)

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
