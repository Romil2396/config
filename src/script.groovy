import groovy.json.JsonSlurper
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.CompilationUnit
import groovy.transform.Field

@Field Map configData

// Read the .config file and parse JSON
def readAndParseJson(String path) {
    def jsonSlurper = new JsonSlurper()
    def file = new File(path)
    configData = jsonSlurper.parse(file)
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
