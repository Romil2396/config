

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
Map<String, Object> customConfig = [:]
List<Object> additionalConfigs = []
List<String> code = []
String currentBlockName = null
Map<String, String> currentBlockContent = [:]

def parseComplexStructure(String line) {
    line = line.trim().replaceAll(/^config\.add\(/, '').replaceAll(/\)$/, '')
    line = line.replaceAll(/(\w+)\s*=\s*/g, '"$1":')
    int openBrackets = 0
    StringBuilder jsonBuilder = new StringBuilder()
    jsonBuilder.append('[')

    line.each { char ->
        switch (char) {
            case '[':
                openBrackets++
                jsonBuilder.append('{')
                break
            case ']':
                openBrackets--
                jsonBuilder.append('}')
                if (openBrackets > 0) {
                    jsonBuilder.append(',')
                }
                break
            case ',':
                if (openBrackets > 0) {
                    jsonBuilder.append(',')
                }
                break
            default:
                jsonBuilder.append(char)
        }
    }
    jsonBuilder.append(']')
    try {
        return new JsonSlurper().parseText(jsonBuilder.toString())
    } catch (Exception e) {
        println("Error parsing complex structure: $e")
        return null
    }
}

lines.each { line ->
    line = line.trim()
    if (line.startsWith("//") || line.contains("/*")) {
        // Skip comments
    } else if (line.matches("@\\w+.*")) {
        annotations.add(line)
    } else if (line.matches("import .*")) {
        imports.add(line)
    } else if (line.matches("\\w+ \\{")) {
        currentBlockName = line.replaceAll("\\{", "").trim()
        currentBlockContent = [:]  // Initialize a new map for this block
    } else if (line == "}") {
        if (currentBlockName != null && currentBlockContent != null) {
            customConfig[currentBlockName] = currentBlockContent
            currentBlockName = null
        }
    } else if (currentBlockName != null && line.contains("=")) {
        def parts = line.split("=")
        if (parts.length > 1) {
            currentBlockContent[parts[0].trim()] = parts[1].trim().replaceAll(/^'(.*)'$/, '"$1"')
        }
    } else if (line.startsWith("config.add")) {
        additionalConfigs.add(parseComplexStructure(line))
    } else {
        code.add(line)
    }
}

def result = [
        imports: imports,
        annotations: annotations,
        customConfigurations: customConfig,
        additionalConfigurations: additionalConfigs,
        code: code
]

new File(outputFile).write(JsonOutput.prettyPrint(JsonOutput.toJson(result)))

println "Processing complete. Output written to: ${outputFile}"
