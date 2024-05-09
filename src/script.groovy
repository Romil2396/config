import groovy.json.JsonSlurper
import groovy.json.JsonOutput

// Define the path to the input and output files
String inputFile = "input.config"
String outputFile = "output.json"

// Read the entire file as a list of strings
List<String> lines = new File(inputFile).readLines()

// Initialize structures to hold different parts
List<String> annotations = []
List<String> comments = []
StringBuilder jsonOrArrayBlock = new StringBuilder()
List<Object> jsonOrArrayObjects = []

boolean inJsonOrArray = false

lines.each { line ->
    line = line.trim()
    if (line.startsWith("//") || line.contains("/*")) {
        // Handle single line and block comments
        comments.add(line.replaceAll("//|/\\*|\\*/", "").trim())
    } else if (line.matches("@\\w+")) {
        // Handle annotations
        annotations.add(line)
    } else if (line.startsWith("[") || line.startsWith("{")) {
        // Start of JSON or array block
        inJsonOrArray = true
        jsonOrArrayBlock.append(line)
    } else if (inJsonOrArray) {
        // Continuation or end of JSON or array block
        jsonOrArrayBlock.append(line)
        if (line.endsWith("]") || line.endsWith("}")) {
            // End of block, process it
            inJsonOrArray = false
            def jsonParser = new JsonSlurper()
            jsonOrArrayObjects.add(jsonParser.parseText(jsonOrArrayBlock.toString()))
            jsonOrArrayBlock = new StringBuilder() // Reset for the next block
        }
    }
}

// Combine everything into a single JSON tree
def result = [
        annotations: annotations,
        jsonOrArray: jsonOrArrayObjects,
        comments: comments.collect { "{ ${it} }" } // Format comments as requested
]

// Write the result to the output file
new File(outputFile).write(JsonOutput.prettyPrint(JsonOutput.toJson(result)))

println "Processing complete. Output written to: ${outputFile}"
