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
StringBuilder jsonOrArrayBlock = new StringBuilder()
List<Object> jsonOrArrayObjects = []
boolean inJsonOrArray = false

lines.each { line ->
    line = line.trim()
    if (line.startsWith("//") || line.contains("/*")) {
        // Handle single line and block comments
        comments.add("{ ${line.replaceAll("//|/\\*|\\*/", "").trim()} }")
    } else if (line.matches("@\\w+.*")) {
        // Handle annotations
        annotations.add(line)
    } else if (line.matches("import .*")) {
        // Handle import statements
        imports.add(line)
    } else if (line.startsWith("[") || line.startsWith("{") || inJsonOrArray) {
        // Detect JSON or array blocks
        jsonOrArrayBlock.append(line)
        if (line.endsWith("]") || line.endsWith("}")) {
            // End of JSON or array block
            try {
                def jsonParser = new JsonSlurper()
                jsonOrArrayObjects.add(jsonParser.parseText(jsonOrArrayBlock.toString()))
            } catch (Exception e) {
                println("Error parsing JSON or Array: ${e.message}")
            }
            jsonOrArrayBlock.setLength(0) // Clear the StringBuilder for the next block
            inJsonOrArray = false
        } else {
            inJsonOrArray = true
        }
    }
}

// Combine everything into a single JSON tree
def result = [
        imports: imports,
        annotations: annotations,
        jsonOrArrays: jsonOrArrayObjects,
        comments: comments
]

// Write the result to the output file
new File(outputFile).write(JsonOutput.prettyPrint(JsonOutput.toJson(result)))

println "Processing complete. Output written to: ${outputFile}"
