import groovy.json.JsonSlurper
import groovy.json.JsonException

// Function to read and parse the JSON content from a .config file
def readAndParseJson(String path) {
    def jsonSlurper = new JsonSlurper()
    def file = new File(path)
    def validJsonLines = []
    def problematicLines = []

    file.eachLine { line ->
        println("Reading line: $line")  // Debug: print each line
        if (!line.startsWith('/') && line.trim()) {
            try {
                // Attempt to parse each line to see if it's valid JSON
                def parsedLine = jsonSlurper.parseText(line)
                validJsonLines << parsedLine
            } catch (JsonException e) {
                problematicLines << line
                println("Problem parsing line: $line")
            }
        }
    }

    // Log valid and problematic lines
    println("Valid JSON Lines: $validJsonLines.size()")
    println("Problematic Lines: $problematicLines.size()")

    if (!problematicLines.isEmpty()) {
        new File('path/to/your/problematic_lines.log').text = problematicLines.join('\n')
    }
}

// Example usage
String filePath = "path/to/your/file.config"
readAndParseJson(filePath)
