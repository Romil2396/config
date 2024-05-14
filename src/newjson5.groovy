import groovy.json.JsonSlurper
import groovy.json.JsonOutput

String inputFile = "input.config"
String outputFile = "output.json"

List<String> lines = new File(inputFile).readLines()

List<String> annotations = []
List<String> imports = []
List<String> comments = []
Map<String, Object> customConfig = [:]
Map<String, Object> configAdd = [:]
Map<String, Object> configGet = [:]
List<String> code = []
String currentBlockName = null // Correctly scoped at the script level

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
    } else if (line.matches("\\w+ \\{")) {
        currentBlockName = line.replaceAll("\\{", "").trim()
        customConfig[currentBlockName] = [:] // Initialize the map for block content
    } else if (currentBlockName != null && line == "}") {
        currentBlockName = null // Properly close the current block
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
