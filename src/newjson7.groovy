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
StringBuilder complexConfigBuffer = new StringBuilder()
boolean isCapturing = false

def parseConfiguration(String content, Map configMap) {
    content = content.replaceAll(/'/, '"'); // Replace single quotes with double quotes
    content = content.replaceAll(/\(/, '{').replaceAll(/\)/, '}'); // Replace brackets to form JSON-like structure
    content = content.replaceAll(/\[(.+?)\]/) { match -> // Handle arrays
        "{${match[1].replaceAll(/(\w+)\s*=\s*/, '"$1":')}}"
    }
    content = content.replaceAll(/(\w+)\s*=\s*/, '"$1":'); // Transform key=value pairs

    try {
        def jsonSlurper = new JsonSlurper()
        def parsedContent = jsonSlurper.parseText("{${content}}") // Ensure JSON object encapsulation
        configMap[content] = parsedContent
    } catch (Exception e) {
        println("Error parsing configuration: ${content}, Error: ${e.message}")
        configMap[content] = "Error: ${e.message}"
    }
}

lines.each { line ->
    line = line.trim()
    if (line.startsWith("//") || line.startsWith("/*") || line.startsWith("*")) {
        comments.add(line)
    } else if (line.matches("@\\w+.*")) {
        annotations.add(line)
    } else if (line.matches("import .*")) {
        imports.add(line)
    } else if (line.startsWith("config.add(") || line.startsWith("config.get(")) {
        if (!isCapturing) {
            complexConfigBuffer.append(line)
            if (line.endsWith(")")) {
                parseConfiguration(complexConfigBuffer.toString(), line.startsWith("config.add(") ? configAdd : configGet)
                complexConfigBuffer.setLength(0) // Clear the buffer
            } else {
                isCapturing = true // Start capturing
            }
        } else {
            complexConfigBuffer.append(" ").append(line)
            if (line.endsWith(")")) {
                parseConfiguration(complexConfigBuffer.toString(), complexConfigBuffer.toString().startsWith("config.add(") ? configAdd : configGet)
                complexConfigBuffer.setLength(0) // Clear the buffer
                isCapturing = false
            }
        }
    } else {
        if (!isCapturing) {
            code.add(line)
        } else {
            complexConfigBuffer.append(" ").append(line)
        }
    }
}

def result = [
        imports: imports,
        annotations: annotations,
        customConfigurations: customConfig,
        configAdd: configAdd,
        configGet: configGet,
        comments: comments,
        code: code
]

new File(outputFile).write(JsonOutput.prettyPrint(JsonOutput.toJson(result)))
println("Processing complete. Output written to: ${outputFile}")
