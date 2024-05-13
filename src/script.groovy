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
List<String> code = []
Map<String, Object> configAdd = [:]
Map<String, Object> configGet = [:]
Map<String, String> keyValuePairs = [:]

// Helper function to process lines for config.add or config.get
void processConfigLine(Map configMap, String line) {
    String trimmedLine = line.replaceAll(/^config\.(add|get)\(/, '').replaceAll(/\)$/, '')
    trimmedLine = trimmedLine.replaceAll(/\(/, '{').replaceAll(/\)/, '}').replaceAll(/(\w+)\s*=/, '"$1":')
    trimmedLine = "{${trimmedLine}}" // Ensure it's a proper JSON object

    try {
        def jsonSlurper = new JsonSlurper()
        def content = jsonSlurper.parseText(trimmedLine)
        configMap.put(line, content)  // Store parsed content under the original line
    } catch (Exception e) {
        println("Error parsing JSON from content: ${trimmedLine}, Error: ${e.message}")
        configMap.put(line, "Error parsing: ${e.message}")
    }
}

// Process each line from the file
lines.each { line ->
    line = line.trim()
    if (line.startsWith("//") || line.contains("/*")) {
        comments.add(line)
    } else if (line.matches("@\\w+.*")) {
        annotations.add(line)
    } else if (line.matches("import .*")) {
        imports.add(line)
    } else if (line.startsWith("config.add(")) {
        processConfigLine(configAdd, line)
    } else if (line.startsWith("config.get(")) {
        processConfigLine(configGet, line)
    } else if (line.matches("\\w+\\s*=\\s*'.*'")) {
        // Process simple key-value pairs
        String key = line.split("=")[0].trim()
        String value = line.split("=")[1].trim().replaceAll(/^'(.*)'$/, '$1')
        keyValuePairs.put(key, value)
    } else {
        code.add(line)
    }
}

// Construct the final JSON structure to output
def result = [
        imports: imports,
        annotations: annotations,
        configAdd: configAdd,
        configGet: configGet,
        keyValuePairs: keyValuePairs,
        comments: comments,
        code: code
]

// Write the JSON result to the output file
new File(outputFile).write(JsonOutput.prettyPrint(JsonOutput.toJson(result)))
println("Processing complete. Output written to: ${outputFile}")
