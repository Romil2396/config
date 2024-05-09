import groovy.json.JsonSlurper
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.CompilationUnit
import groovy.transform.Field

@Field Map configData = [:]

// Read the .config file, attempt to parse each line, skip unparsable lines, and log them
def readAndParseJson(String path) {
    def jsonSlurper = new JsonSlurper()
    def file = new File(path)
    def problematicLines = []
    def stringBuilder = new StringBuilder()

    file.eachLine { line ->
        if (!line.startsWith('/')) {
            try {
                // Try parsing each line to see if it's valid JSON part
                jsonSlurper.parseText("[${line}]")  // Wrapping line in array brackets to ensure JSON format
                stringBuilder.append(line).append('\n')
            } catch (Exception e) {
                problematicLines << line  // Add to problematic lines if there is a parsing error
            }
        }
    }

    configData = jsonSlurper.parseText("{${stringBuilder.toString()}}")  // Assuming the content forms a JSON object
    new File('path/to/your/problematic_lines.log').text = problematicLines.join('\n')  // Saving problematic lines to a separate file
}

// Optionally, convert parsed JSON to a Groovy script and then to AST
def jsonToGroovyAST(Map jsonData) {
    String groovyScript = jsonData.collect { key, value ->
        "def $key = '${value.toString().replace("'", "\\'")}'"
    }.join("\n")

    CompilerConfiguration config = new CompilerConfiguration()
    CompilationUnit cu = new CompilationUnit(config)
    SourceUnit source = SourceUnit.create("jsonAsGroovy", groovyScript, config)
    cu.addSource(source)
    cu.compile(Phases.CONVERSION)
    return source.getAST()
}

// Example usage
String filePath = "path/to/your/file.config"
readAndParseJson(filePath)
def ast = jsonToGroovyAST(configData)
println ast.dump()
