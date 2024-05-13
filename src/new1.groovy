import groovy.json.JsonBuilder
import groovy.json.JsonSlurper

// Define the path to the input and output files
String inputFile = "input.config"
String outputFile = "output.json"

// Read the entire file as a list of strings
List<String> lines = new File(inputFile).readLines()

// Initialize structures to hold different parts
List<Map<String, String>> annotations = []
List<String> imports = []
List<Map<String, String>> configurations = []
List<String> code = []

lines.each { line ->
    line = line.trim()
    if (line.startsWith("//") || line.contains("/*")) {
        // Skip comments
    } else if (line.matches("@\\w+.*")) {
        // Annotations - assume format is "@Annotation(value)"
        Map<String, String> annotation = [:]
        String key = line.find(/\w+/)
        String value = line.find(/\((.*?)\)/)[1] ?: ""
        annotation[key] = value
        annotations.add(annotation)
    } else if (line.matches("import .*")) {
        // Import statements
        imports.add(line)
    } else if (line.contains("config.add") && line.endsWith("]")) {
        // Configuration additions - parsing simpler nested structures
        String content = line.replaceAll(/config\.add\(|\)$/g, "")
        Map parsedContent = parseAsJson(content)
        configurations.add(parsedContent)
    } else {
        // Other code lines
        code.add(line)
    }
}

Map result = [
        imports: imports,
        annotations: annotations,
        configurations: configurations,
        code: code
]

// Write the result to the output file
new File(outputFile).newWriter().with { writer ->
    writer << new JsonBuilder(result).toPrettyString()
    writer.close()
}

println("Processing complete. Output written to: ${outputFile}")

Map parseAsJson(String content) {
    // Simplified JSON parsing for structured line content
    try {
        def jsonSlurper = new JsonSlurper()
        content = content.replaceAll(/(\w+)=/g, '"$1":') // Prepare string as JSON
        content = "{${content}}" // Encapsulate in braces for JSON object
        return jsonSlurper.parseText(content)
    } catch (Exception e) {
        println("Error parsing JSON from content: ${content}, Error: ${e.message}")
        return [:] // Return empty map on error
    }
}
