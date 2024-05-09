import groovy.json.JsonBuilder
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.MultipleCompilationErrorsException

def parseFileToAST(String filePath) {
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

def parseASTToJson(ASTNode ast, File jsonFile) {
    JsonBuilder jsonBuilder = new JsonBuilder()
    jsonBuilder(ast)
    jsonFile.text = jsonBuilder.toPrettyString()
}

def parseConfigFileToJson(File file, File jsonFile) {
    JsonBuilder jsonBuilder = new JsonBuilder()
    List<String> errorLines = []
    file.eachLine { line, lineNumber ->
        try {
            jsonBuilder(lineNumber: line)
        } catch (Exception e) {
            errorLines << "Line $lineNumber: $line"
        }
    }
    jsonFile.text = jsonBuilder.toPrettyString()
    return errorLines
}

def main(String[] args) {
    String configFilePath = args[0]

    // Generate AST file
    def ast = parseFileToAST(configFilePath)
    File astFile = new File(configFilePath.replace(".config", ".ast"))
    parseASTToJson(ast, astFile)

    // Parse .config to JSON
    File configFile = new File(configFilePath)
    File configJsonFile = new File(configFilePath.replace(".config", ".json"))
    List<String> errorLines = parseConfigFileToJson(configFile, configJsonFile)

    // Error handling
    if (!errorLines.isEmpty()) {
        File errorFile = new File(configFilePath.replace(".config", "_errors.txt"))
        errorFile.text = errorLines.join("\n")
    }
}

main(args)

