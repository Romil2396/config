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
List<String> unparsedData = []
String currentBlockName = null
Map<String, String> currentBlockContent = [:]

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
        currentBlockContent = [:]  // Initialize a new map for this block
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
            if (parts.length > 1) {  // Ensure there are two parts to prevent null access
                def key = parts[0].trim()
                def value = parts[1].trim()
                if (value.startsWith("'") && value.endsWith("'")) {
                    value = value[1..-2]
                }
                currentBlockContent[key] = value
            }
        }
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
        comments: comments,
        unparsed: unparsedData
]

// Write the result to the output file
new File(outputFile).write(JsonOutput.prettyPrint(JsonOutput.toJson(result)))

println "Processing complete. Output written to: ${outputFile}"
