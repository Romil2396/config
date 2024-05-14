
import groovy.json.JsonSlurper
import groovy.json.JsonOutput

// Define the path to the input and output files
String inputFile = "input.config"
String outputFile = "output.json"

// Read the entire file as a list of strings
List<String> lines = new File(inputFile).readLines()

// Initialize structures to hold different parts
List<String> annotations = []
List<String> imports = []
List<String> comments = []
Map<String, Object> customConfig = [:]
Map<String, Object> configAdd = [:]
Map<String, Object> configGet = [:]
List<String> code = []
String currentBlockName = null  // Ensured it's defined outside the closures

def parseConfiguration(String line, Map configMap) {
    String configContent = line.replaceAll(/^config\.(add|get)\(/, '{').replaceAll(/\)$/, '}')
    configContent = configContent.replaceAll(/(\w+)\s*=\s*/, '"$1":')  // Convert to JSON key-value pairs
    configContent = configContent.replaceAll(/'/, '"')  // Replace single quotes with double quotes for JSON

    try {
        def parsedContent = new JsonSlurper().parseText(configContent)
        configMap[line] = parsedContent
    } catch (Exception e) {
        println("Error parsing configuration: ${line}, Error: ${e.message}")
        configMap[line] = "Error: ${e.message}"
    }
}

def parseComplexArrayStructure(String line) {
    String key = line.takeWhile { it != ' ' }
    String arrayContent = line.dropWhile { it != '[' }.replaceAll(/\[|\]/, '{' : '}')
    arrayContent = arrayContent.replaceAll(/(\w+)\s*:\s*'([^']*)'/, '"$1":"$2"')

    try {
        def jsonSlurper = new JsonSlurper()
        def content = jsonSlurper.parseText("{ \"$key\": $arrayContent }")
        customConfig[key] = content
    } catch (Exception e) {
        println("Error parsing complex array structure: ${line}, Error: ${e.message}")
        customConfig[key] = "Error: ${e.message}"
    }
}

lines.each { line ->
    line = line.trim()
    if (line.startsWith("//") || line.contains("/*")) {
        comments.add(line)
    } else if (line.matches("@\\w+.*")) {
        annotations.add(line)
    } else if (line.matches("import .*")) {
        imports.add(line)
    } else if (line.startsWith("config.add(")) {
        parseConfiguration(line, configAdd)
    } else if (line.startsWith("config.get(")) {
        parseConfiguration(line, configGet)
    } else if (line.matches("\\w+ \\[")) {
        parseComplexArrayStructure(line)
    } else if (line.matches("\\w+ \\{")) {
        currentBlockName = line.replaceAll("\\{", "").trim()
        customConfig[currentBlockName] = [:]
    } else if (currentBlockName != null && line == "}") {
        currentBlockName = null
    } else if (currentBlockName != null && line.contains("=")) {
        def parts = line.split("=")
        customConfig[currentBlockName][parts[0].trim()] = parts[1].trim().replaceAll(/^'(.*)'$/, '$1')
    } else {
        code.add(line)
    }
}

def result = [
        imports: imports,
        annotations: annotations,
        customConfigurations: customConfig,
        configAdd: configAdd,
        configGet: configGet,
        comments: comments,
        code: code
]

new File(outputFile).write(JsonOutput.prettyPrint(JsonOutput.toJson(result)))
println("Processing complete. Output written to: ${outputFile}")
