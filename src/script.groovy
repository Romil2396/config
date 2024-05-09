import groovy.json.JsonSlurper
import groovy.json.JsonException
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.CompilationUnit
import groovy.transform.Field

@Field Map configData = [:]

// Read the .config file, skip lines starting with '/', and parse JSON, handling errors
def readAndParseJson(String path) {
    def jsonSlurper = new JsonSlurper()
    def file = new File(path)
    def validJsonLines = []
    def problematicLines = []

    file.eachLine { line ->
        if (!line.startsWith('/')) {
            try {
                // Validate line by attempting to parse it
                jsonSlurper.parseText(line)
                validJsonLines << line  // Add to valid lines if no exception is thrown
            } catch (JsonException e) {
                problematicLines << line  // Add to problematic lines if there is a parsing error
            }
        }
    }

    if (validJsonLines) {
        // Attempt to parse the entire valid JSON lines as a JSON array
        String jsonArrayString = "[${validJsonLines.join(",")}]"
        configData = jsonSlurper.parseText(jsonArrayString)
    }

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
