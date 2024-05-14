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
boolean bufferActive = false

def parseConfiguration(String buffer, Map configMap) {
    // Normalize and prepare the buffer for JSON conversion
    String configContent = buffer.replaceAll(/^config\.(add|get)\(/, '{').replaceAll(/\)$/, '}');
    configContent = configContent.replaceAll(/(\w+)\s*=\s*/, '"$1":'); // Convert key=value to "key":"value"
    configContent = configContent.replaceAll(/'/, '"'); // Replace single quotes with double quotes for JSON

    try {
        def parsedContent = new JsonSlurper().parseText(configContent);
        configMap[buffer] = parsedContent;
    } catch (Exception e) {
        println("Error parsing configuration: ${buffer}, Error: ${e.message}");
        configMap[buffer] = "Error: ${e.message}";
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
        if (!bufferActive && line.endsWith(")")) {
            parseConfiguration(line, (line.startsWith("config.add(") ? configAdd : configGet))
        } else {
            bufferActive = true
            buffer += line + " "
        }
    } else if (bufferActive) {
        buffer += line + " "
        if (line.endsWith(")")) {
            parseConfiguration(buffer, (buffer.startsWith("config.add(") ? configAdd : configGet))
            bufferActive = false
            buffer = ""
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
