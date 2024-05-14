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
StringBuilder complexConfig = new StringBuilder()
boolean capturing = false

def parseConfiguration(String content, Map configMap) {
    content = content.replaceAll(/'/, '"'); // Single quotes to double quotes
    content = content.replaceAll(/\s*=\s*/, ':'); // '=' to ':'
    content = content.replaceAll(/(\w+)\((.*?)\)/) { // Handle nested function calls'"${it[1]}":{${it[2].replaceAll(/(\w+):/,'"$1":')}}'
    }
    content = "{${content}}"; // Ensure proper JSON object wrapping

    try {
        def jsonSlurper = new JsonSlurper()
        def parsedContent = jsonSlurper.parseText(content)
        configMap["config"] = parsedContent
    } catch (Exception e) {
        println("Error parsing configuration: ${content}, Error: ${e.message}")
        configMap["config"] = "Error: ${e.message}"
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
        capturing = true
        complexConfig.append(line)
        if (line.endsWith(")")) {
            parseConfiguration(complexConfig.toString(), line.startsWith("config.add(") ? configAdd : configGet)
            complexConfig.setLength(0)
            capturing = false
        }
    } else if (capturing) {
        complexConfig.append(" ").append(line)
        if (line.endsWith(")")) {
            parseConfiguration(complexConfig.toString(), complexConfig.toString().startsWith("config.add(") ? configAdd : configGet)
            complexConfig.setLength(0)
            capturing = false
        }
    } else {
        code.add(line)
    }
}

def result = [
        imports: imports,
        annotations: annotations,
        configAdd: configAdd,
        configGet: configGet,
        customConfigurations: customConfig,
        comments: comments,
        code: code
]

new File(outputFile).write(JsonOutput.prettyPrint(JsonOutput.toJson(result)))
println("Processing complete. Output written to: ${outputFile}")
