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
String buffer = ""
boolean capturing = false

def parseComplexArrayStructure(String content) {
    if (content.isEmpty()) return

    // Normalize and prepare the content for JSON conversion
    content = content.replaceAll(/\s*\n\s*/, " ")  // Remove newlines and extra spaces
    content = content.replaceAll(/(\w+)\s*:\s*'([^']*)'/, '"$1":"$2"')  // Convert to JSON key-value pairs
    content = "{${content}}"  // Enclose in braces to form a valid JSON object

    try {
        def jsonSlurper = new JsonSlurper()
        def contentObject = jsonSlurper.parseText(content)
        customConfig[content.takeWhile { it != ' ' }] = contentObject
    } catch (Exception e) {
        println("Error parsing complex array structure: Error: ${e.message}")
        customConfig[content.takeWhile { it != ' ' }] = "Error: ${e.message}"
    }
}

lines.each { line ->
    line = line.trim()
    if (capturing) {
        if (line.endsWith("]")) {
            buffer += " " + line[0..-2]  // Capture line excluding the closing bracket
            parseComplexArrayStructure(buffer)
            buffer = ""
            capturing = false
        } else {
            buffer += " " + line  // Continue capturing lines
        }
    } else if (line.startsWith("config.add(")) {
        parseConfiguration(line, configAdd)
    } else if (line.startsWith("config.get(")) {
        parseConfiguration(line, configGet)
    } else if (line.matches("\\w+ \\[.*")) {  // Start capturing multiline configuration
        capturing = true
        buffer = line.replaceAll("\\[", "")  // Start new buffer excluding the opening bracket
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
        code: code
]

new File(outputFile).write(JsonOutput.prettyPrint(JsonOutput.toJson(result)))
println("Processing complete. Output written to: ${outputFile}")
