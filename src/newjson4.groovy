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
StringBuilder buffer = new StringBuilder()
String currentBlockName = null

def parseConfiguration(String content, Map configMap) {
    content = content.replaceAll(/'/, '"')  // Replace single quotes with double quotes
    content = content.replaceAll(/\[(\w+)\s*:\s*/, '{"$1":').replaceAll(/,(\w+)\s*:\s*/, ',"$1":').replaceAll(/\]/, '}')  // Convert array-like to JSON-like

    try {
        def jsonSlurper = new JsonSlurper()
        def parsedContent = jsonSlurper.parseText(content)
        configMap.put(content, parsedContent)
    } catch (Exception e) {
        println("Error parsing configuration: ${content}, Error: ${e.message}")
        configMap.put(content, "Error: ${e.message}")
    }
}

lines.each { line ->
    line = line.trim()
    if (line.startsWith("//") || line.contains("/*")) {
        comments.add(line)
    } else if (line.matches("@\\w+.*")) {
        annotations.add(line)
    } else if (line.matches("import .*")) {
        imports.add(line)
    } else if (line.startsWith("config.add(") || line.startsWith("config.get(")) {
        buffer.append(line)
        if (line.endsWith(")")) {
            parseConfiguration(buffer.toString(), line.startsWith("config.add(") ? configAdd : configGet)
            buffer.setLength(0)  // Clear the buffer
        }
    } else if (buffer.length() > 0 && buffer.toString().startsWith("config.")) {
        buffer.append(" ").append(line)
        if (line.endsWith(")")) {
            parseConfiguration(buffer.toString(), buffer.toString().startsWith("config.add(") ? configAdd : configGet)
            buffer.setLength(0)  // Clear the buffer
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
