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
List<Object> additionalConfigs = []
List<String> unparsedData = []
String currentBlockName = null
Map<String, String> currentBlockContent = [:]

def parseComplexStructure(String line) {
    // Remove initial function call syntax
    line = line.replaceAll(/^config\.add\(/, '')
    line = line.replaceAll(/\)$/, '')  // Remove the trailing parenthesis

    // Transform into JSON-like syntax
    line = line.replaceAll(/(\w+):/,'"${1}":') // Ensure keys are quoted
    line = line.replaceAll(/: ?'([^']*)'/,': "${1}"') // Ensure values are quoted

    // Wrap non-structured data in brackets to form a valid JSON structure
    if (!line.startsWith("[") && !line.startsWith("{")) {
        line = "[${line}]"
    }

    // Parse the transformed line as JSON
    try {
        return new JsonSlurper().parseText(line)
    } catch (Exception e) {
        println("Error parsing: $e")
        return null // Handle parsing failure
    }
}

lines.each { line ->
    line = line.trim()
    if (line.startsWith("//") || line.contains("/*")) {
        // Handle comments
        comments.add("{ ${line.replaceAll("//|/\\*|\\*/", "").trim()} }")
    } else if (line.matches("@\\w+.*")) {
        // Handle annotations
        annotations.add(line)
    } else if (line.matches("import .*")) {
        // Handle import statements
        imports.add(line)
    } else if (line.matches("\\w+ \\{")) {
        // Start of a custom block
        currentBlockName = line.replaceAll("[\\{\\s]", "")
        currentBlockContent = [:]
    } else if (line == "}") {
        // End of a custom block
        if (currentBlockName != null && currentBlockContent != null) {
            customConfig[currentBlockName] = currentBlockContent
            currentBlockName = null
        }
    } else if (currentBlockName != null) {
        // Within a custom block, parse content
        if (line.contains("=")) {
            def parts = line.split("=")
            if (parts.length > 1) {
                def key = parts[0].trim()
                def value = parts[1].trim()
                if (value.startsWith("'") && value.endsWith("'")) {
                    value = value[1..-2]
                }
                currentBlockContent[key] = value
            }
        }
    } else if (line.startsWith("config.add")) {
        // Handle additional complex configuration lines
        additionalConfigs.add(parseComplexStructure(line))
    } else {
        // Collect any other data that does not match known patterns
        unparsedData.add(line)
    }
}

// Combine everything into a single JSON tree
def result = [
        imports: imports,
        annotations: annotations,
        customConfigurations: customConfig,
        additionalConfigurations: additionalConfigs,
        comments: comments,
        unparsed: unparsedData
]

// Write the result to the output file
new File(outputFile).write(JsonOutput.prettyPrint(JsonOutput.toJson(result)))

println "Processing complete. Output written to: ${outputFile}"
