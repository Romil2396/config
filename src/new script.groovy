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
List<String> comments = []
Map<String, Object> customConfig = [:]
List<Object> additionalConfigs = []
List<Object> getStatements = []
List<String> code = []

def parseComplexStructure(String line) {
    line = line.trim().replaceAll(/^config\.add\(/, '').replaceAll(/\)$/, '')
    line = line.replaceAll(/([a-zA-Z_]\w*):/, '"$1":')
    line = line.replaceAll(/: ?'([^']*)'/, ': "$1"')
    if (!line.startsWith("[") && !line.startsWith("{")) {
        line = "[${line}]"
    }
    try {
        return new JsonSlurper().parseText(line)
    } catch (Exception e) {
        println("Error parsing: $e")
        return null
    }
}

def parseConfigGetStructure(String line) {
    line = line.trim()
    line = line.replaceAll(/^config\.get\(/, '').replaceAll(/\),\?.*$/, '')
    def params = line.split(',')
    params = params.collect { it.trim().replaceAll(/^'(.*)'$/, '"$1"') }
    return params.size() == 1 ? params[0] : params
}

lines.each { line ->
    line = line.trim()
    if (line.startsWith("//") || line.contains("/*")) {
        comments.add("{ ${line.replaceAll("//|/\\*|\\*/", "").trim()} }")
    } else if (line.matches("@\\w+.*")) {
        annotations.add(line)
    } else if (line.matches("import .*")) {
        imports.add(line)
    } else if (line.matches("\\w+ \\{")) {
        currentBlockName = line.replaceAll("[\\{\\s]", "")
        currentBlockContent = [:]
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
    } else if (line.startsWith("config.get")) {
        getStatements.add(parseConfigGetStructure(line))
    } else {
        code.add(line)
    }
}

def result = [
        imports: imports,
        annotations: annotations,
        customConfigurations: customConfig,
        additionalConfigurations: additionalConfigs,
        getStatements: getStatements,
        comments: comments,
        code: code
]

new File(outputFile).write(JsonOutput.prettyPrint(JsonOutput.toJson(result)))

println "Processing complete. Output written to: ${outputFile}"
