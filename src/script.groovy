import groovy.json.JsonBuilder
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.MultipleCompilationErrorsException

def parseConfigToAST(String filePath) {
    CompilerConfiguration config = new CompilerConfiguration()
    SourceUnit sourceUnit = SourceUnit.create("script", new File(filePath), config)
    try {
        sourceUnit.parse()
        sourceUnit.completePhase()
        sourceUnit.convert()
        return sourceUnit.AST
    } catch (MultipleCompilationErrorsException e) {
        println("Error parsing file: $filePath")
        return null
    }
}

def convertASTToJson(ASTNode ast, File jsonFile) {
    JsonBuilder jsonBuilder = new JsonBuilder()
    jsonBuilder(ast)
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
