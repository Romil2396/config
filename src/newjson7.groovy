
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
boolean capturingComplexConfig = false

def parseConfiguration(String content, Map configMap) {
    // Normalizing and converting the content into JSON structure
    content = content.replaceAll(/'/, '"'); // Replace single quotes with double quotes
    content = content.replaceAll(/\[(.+?)\]/) { match -> // Convert array notations to JSON objects
        "{${match[1].replaceAll(/(\w+)\s*=\s*/,'"$1":')}}"
    }
    content = content.replaceAll(/\(\s*/, '{').replaceAll(/\s*\)/, '}'); // Convert parenthesis to JSON objects

    try {
        def jsonSlurper = new JsonSlurper()
        def parsedContent = jsonSlurper.parseText(content)
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
        if (line.endsWith(")")) {
            parseConfiguration(line, line.startsWith("config.add(") ? configAdd : configGet)
        } else {
            capturingComplexConfig = true
            complexConfigBuffer.append(line)
        }
    } else if (capturingComplexConfig) {
        complexConfigBuffer.append(" ").append(line)
        if (line.endsWith(")")) {
            parseConfiguration(complexConfigBuffer.toString(), complexConfigBuffer.toString().startsWith("config.add(") ? configAdd : configGet)
            complexConfigBuffer.setLength(0) // Clear the buffer
            capturingComplexConfig = false
        }
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
        comments: comments,
        code: code
]

new File(outputFile).write(JsonOutput.prettyPrint(JsonOutput.toJson(result)))
println("Processing complete. Output written to: ${outputFile}")
