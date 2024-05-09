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
List<String> unparsedData = []
boolean inJsonOrArray = false

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
    } else if (line.startsWith("[") || line.startsWith("{") || inJsonOrArray) {
        // Handle JSON or array blocks
        jsonOrArrayBlock.append(line)
        if (line.endsWith("]") || line.endsWith("}")) {
            // Attempt to parse when block ends
            try {
                def jsonParser = new JsonSlurper()
                jsonOrArrayObjects.add(jsonParser.parseText(jsonOrArrayBlock.toString()))
                jsonOrArrayBlock.setLength(0) // Clear the StringBuilder for the next block
                inJsonOrArray = false
            } catch (Exception e) {
                unparsedData.add(jsonOrArrayBlock.toString())
                jsonOrArrayBlock.setLength(0)
                inJsonOrArray = false
            }
        } else {
            inJsonOrArray = true
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
        jsonOrArrays: jsonOrArrayObjects,
        comments: comments,
        unparsed: unparsedData
]

// Write the result to the output file
new File(outputFile).write(JsonOutput.prettyPrint(JsonOutput.toJson(result)))

println "Processing complete. Output written to: ${outputFile}"
