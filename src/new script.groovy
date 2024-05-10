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
List<String> code = []  // Renamed from 'unparsedData'
String currentBlockName = null
Map<String, String> currentBlockContent = [:]

def parseComplexStructure(String line) {
    // Prepare line by trimming and removing function call syntax
    line = line.trim().replaceAll(/^config\.add\(/, '').replaceAll(/\)$/, '')

    // Attempt to transform configuration syntax to JSON-like syntax
    line = line.replaceAll(/([a-zA-Z_]\w*):/, '"$1":') // Quote keys
    line = line.replaceAll(/: ?'([^']*)'/, ': "$1"') // Quote values

    // Ensure proper wrapping in brackets to form a valid JSON structure
    if (!line.startsWith("[") && !line.startsWith("{")) {
        line = "[${line}]"
    }

    // Try parsing the modified line as JSON
    try {
        return new JsonSlurper().parseText(line)
    } catch (Exception e) {
        println("Error parsing: $e")
        return null // Return null to indicate failure
    }
}

lines.each { line ->
    line = line.trim()
    if (line.startsWith("//") || line.contains("/*")) {
        // Handle comments, ensuring they remain as-is in JSON output
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
        // Within a custom block, process content
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
        // Process additional complex configuration lines
        Object parsed = parseComplexStructure(line)
        if (parsed != null) {
            additionalConfigs.add(parsed)
        }
    } else {
        // Collect lines that do not fit any known pattern
        code.add(line)
    }
}

// Combine all parts into a single JSON tree
def result = [
        imports: imports,
        annotations: annotations,
        customConfigurations: customConfig,
        additionalConfigurations: additionalConfigs,
        comments: comments,
        code: code  // Updated terminology here
]

// Output the result to a JSON file
new File(outputFile).write(JsonOutput.prettyPrint(JsonOutput.toJson(result)))

println "Processing complete. Output written to: ${outputFile}"
