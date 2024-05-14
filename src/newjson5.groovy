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
boolean isCapturing = false

def parseConfiguration(String configContent, Map configMap) {
    configContent = configContent.replaceAll(/^config\.(add|get)\(/, '{').replaceAll(/\)$/, '}');
    configContent = configContent.replaceAll(/(\w+)\s*=\s*/, '"$1":');
    configContent = configContent.replaceAll(/'/, '"');

    try {
        def parsedContent = new JsonSlurper().parseText(configContent);
        configMap.put(configContent, parsedContent);
    } catch (Exception e) {
        println("Error parsing configuration: ${configContent}, Error: ${e.message}");
        configMap.put(configContent, "Error: ${e.message}");
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
        if (!isCapturing) {
            isCapturing = true;
            buffer.setLength(0);
            buffer.append(line);
            if (line.endsWith(")")) {
                parseConfiguration(buffer.toString(), line.startsWith("config.add(") ? configAdd : configGet);
                isCapturing = false;
            }
        } else {
            buffer.append(" ").append(line);
            if (line.endsWith(")")) {
                parseConfiguration(buffer.toString(), buffer.toString().startsWith("config.add(") ? configAdd : configGet);
                isCapturing = false;
            }
        }
    } else if (line.matches("\\w+ \\{")) {
        currentBlockName = line.replaceAll("\\{", "").trim();
        customConfig[currentBlockName] = [:];
    } else if (currentBlockName != null && line == "}") {
        currentBlockName = null; // End of custom block
    } else if (currentBlockName != null && line.contains("=")) {
        def parts = line.split("=") as List;
        customConfig[currentBlockName][parts[0].trim()] = parts[1].trim().replaceAll(/^'(.*)'$/, '$1');
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
