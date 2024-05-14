
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
StringBuilder configBuffer = new StringBuilder()
boolean isCapturing = false

def parseConfiguration(String configContent, Map configMap) {
    configContent = configContent.replaceAll(/^config\.(add|get)\(/, '{').replaceAll(/\)$/, '}');
    configContent = configContent.replaceAll(/(\w+)\s*=\s*/, '"$1":');
    configContent = configContent.replaceAll(/'/, '"');

    try {
        def parsedContent = new JsonSlurper().parseText(configContent);
        configMap[configContent] = parsedContent;
    } catch (Exception e) {
        println("Error parsing configuration: ${configContent}, Error: ${e.message}");
        configMap[configContent] = "Error: ${e.message}";
    }
}

lines.each { line ->
    line = line.trim()
    if (line.startsWith("//") || line.contains("/*")) {
        comments.add(line);
    } else if (line.matches("@\\w+.*")) {
        annotations.add(line);
    } else if (line.matches("import .*")) {
        imports.add(line);
    } else if ((line.startsWith("config.add(") || line.startsWith("config.get(")) && !isCapturing) {
        isCapturing = true;
        configBuffer.append(line);
        if (line.endsWith(")")) {
            parseConfiguration(configBuffer.toString(), line.startsWith("config.add(") ? configAdd : configGet);
            configBuffer.setLength(0); // Clear the buffer
            isCapturing = false;
        }
    } else if (isCapturing) {
        configBuffer.append(" ").append(line);
        if (line.endsWith(")") || line.endsWith("]")) {
            parseConfiguration(configBuffer.toString(), configBuffer.toString().startsWith("config.add(") ? configAdd : configGet);
            configBuffer.setLength(0); // Clear the buffer
            isCapturing = false;
        }
    } else if (line.matches("\\w+ \\{")) {
        currentBlockName = line.replaceAll("\\{", "").trim();
        customConfig[currentBlockName] = [:];
    } else if (currentBlockName != null && line == "}") {
        currentBlockName = null;
    } else if (currentBlockName != null && line.contains("=")) {
        def (key, value) = line.split("=") as List;
        customConfig[currentBlockName][key.trim()] = value.trim().replaceAll(/^'(.*)'$/, '$1');
    } else {
        code.add(line);
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
