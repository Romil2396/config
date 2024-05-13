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
List<String> code = []
Map<String, Object> configAdd = [:]
Map<String, Object> configGet = [:]
String currentBlockName = null
Map<String, String> currentBlockContent = [:]

lines.each { line ->
    line = line.trim()
    if (line.startsWith("//") || line.contains("/*")) {
        comments.add("{ ${line.replaceAll("//|/\\*|\\*/", "").trim()} }")
    } else if (line.matches("@\\w+.*")) {
        annotations.add(line)
    } else if (line.matches("import .*")) {
        imports.add(line)
    } else if (line.startsWith("config.add(")) {
        processConfigLine(configAdd, line)
    } else if (line.startsWith("config.get(")) {
        processConfigLine(configGet, line)
    } else if (line.matches("\\w+ \\{")) {
        currentBlockName = line.replaceAll("\\{", "").trim()
        currentBlockContent = [:]
    } else if (line == "}") {
        if (currentBlockName != null && currentBlockContent != null) {
            customConfig[currentBlockName] = currentBlockContent
            currentBlockName = null
        }
    } else if (currentBlockName != null) {
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
    } else {
        code.add(line)
    }
}

def result = [
        imports: imports,
        annotations: annotations,
        customConfigurations: customConfig,
        comments: comments,
        configAdd: configAdd,
        configGet: configGet,
        code: code  // Renamed from unparsedData
]

new File(outputFile).write(JsonOutput.prettyPrint(JsonOutput.toJson(result)))
println "Processing complete. Output written to: ${outputFile}"

void processConfigLine(Map configMap, String line) {
    String trimmedLine = line.replaceAll(/^config\.(add|get)\(/, '').replaceAll(/\)$/, '')
    def jsonSlurper = new JsonSlurper()
    def content = jsonSlurper.parseText("{${trimmedLine.replaceAll(/(\w+)\s*=/, '"$1":')}}")
    configMap[line] = content
}
