
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

// Helper function to process lines for config.add or config.get
void parseConfiguration(String line, Map configMap) {
    String configContent = line.replaceAll(/^config\.(add|get)\(/, '').replaceAll(/\)$/, '')
    configContent = configContent.replaceAll(/'/, '"') // Replace single quotes with double quotes
    configContent = configContent.replaceAll(/(\w+)\s*:\s*/, '"$1":') // Convert to JSON key-value pairs

    // Handling arrays and nested objects by replacing brackets and ensuring JSON compatibility
    configContent = handleNestedStructures(configContent)

    try {
        def jsonSlurper = new JsonSlurper()
        def content = jsonSlurper.parseText(configContent)
        configMap.put(line, content) // Store parsed content under the original line
    } catch (Exception e) {
        println("Error parsing configuration: ${line}, Error: ${e.message}")
        configMap.put(line, "Error: ${e.message}")
    }
}

String handleNestedStructures(String content) {
    content = content.replaceAll(/\[/, '{').replaceAll(/\]/, '}')
    return "{${content}}" // Ensure it's properly encapsulated as a JSON object
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
        String blockName = line.replaceAll("\\{", "").trim()
        customConfig[blockName] = [:] // Initialize the map for block content
    } else if (line == "}") {
        // Placeholder to handle closing of blocks if needed
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
