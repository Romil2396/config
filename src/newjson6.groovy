
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
String currentBlockName = null

def parseConfiguration(String line, Map configMap) {
    line = line.replaceAll(/'/, '"');  // Replace single quotes with double quotes for JSON

    // Handling nested structures and method calls with complex parameters
    line = line.replaceAll(/\(([\w\.]+)\)/, '("$1")');  // Handle method calls without complex parameters
    line = line.replaceAll(/(\w+)\s*\(/, '"$1": {').replaceAll(/\)/, '}');  // Convert methods calls to JSON objects
    line = line.replaceAll(/(\w+)\s*=\s*/, '"$1":');  // Handle key=value pairs
    line = line.replaceAll(/\[/, '{').replaceAll(/\]/, '}');  // Convert arrays to JSON objects

    try {
        def parsedContent = new JsonSlurper().parseText("{${line}}");  // Ensure it is a valid JSON object
        configMap.put(line, parsedContent);
    } catch (Exception e) {
        println("Error parsing configuration: ${line}, Error: ${e.message}");
        configMap.put(line, "Error: ${e.message}");
    }
}

lines.each { line ->
    line = line.trim();
    if (line.startsWith("//") || line.contains("/*")) {
        comments.add(line);
    } else if (line.matches("@\\w+.*")) {
        annotations.add(line);
    } else if (line.matches("import .*")) {
        imports.add(line);
    } else if (line.startsWith("config.add(") || line.startsWith("config.get(")) {
        parseConfiguration(line, line.startsWith("config.add(") ? configAdd : configGet);
    } else if (line.matches("\\w+ \\{")) {
        currentBlockName = line.replaceAll("\\{", "").trim();
        customConfig[currentBlockName] = [:];
    } else if (currentBlockName != null && line == "}") {
        currentBlockName = null;
    } else if (currentBlockName != null && line.contains("=")) {
        def parts = line.split("=");
        customConfig[currentBlockName][parts[0].trim()] = parts[1].trim().replaceAll(/^"(.*)"$/, '$1');
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
];

new File(outputFile).write(JsonOutput.prettyPrint(JsonOutput.toJson(result)));
println("Processing complete. Output written to: ${outputFile}");
