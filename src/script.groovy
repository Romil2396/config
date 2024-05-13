import groovy.json.JsonSlurper
import groovy.json.JsonOutput

String inputFile = "input.config"
String outputFile = "output.json"

List<String> lines = new File(inputFile).readLines()

List<String> annotations = []
List<String> imports = []
List<String> comments = []
Map<String, Object> customConfig = [:]
List<String> code = []
Map<String, Object> configAdd = [:]
Map<String, Object> configGet = [:]

lines.each { line ->
    line = line.trim()
    if (line.startsWith("//") || line.contains("/*")) {
        comments.add(line)
    } else if (line.matches("@\\w+.*")) {
        annotations.add(line)
    } else if (line.matches("import .*")) {
        imports.add(line)
    } else if (line.startsWith("config.add(")) {
        processConfigLine(configAdd, line)
    } else if (line.startsWith("config.get(")) {
        processConfigLine(configGet, line)
    } else {
        code.add(line)
    }
}

Map result = [
        imports: imports,
        annotations: annotations,
        customConfigurations: customConfig,
        comments: comments,
        configAdd: configAdd,
        configGet: configGet,
        code: code
]

new File(outputFile).write(JsonOutput.prettyPrint(JsonOutput.toJson(result)))
println("Processing complete. Output written to: ${outputFile}")
