import groovy.json.JsonSlurper
import groovy.json.JsonBuilder
import groovy.json.JsonException

// Function to read and parse the entire JSON content from a .config file with UTF-8 encoding
def parseAndSaveJson(String inputFile, String outputFile) {
    def jsonSlurper = new JsonSlurper()
    def fileContent = ''
    def parsedJson = null

    try {
        // Read the entire file content with UTF-8 encoding
        fileContent = new File(inputFile).getText('UTF-8').trim()
        // Parse the whole JSON content
        parsedJson = jsonSlurper.parseText(fileContent)
        // Save parsed JSON to output file
        new File(outputFile).withWriter('UTF-8') { writer ->
            def jsonBuilder = new JsonBuilder(parsedJson)
            writer.write(jsonBuilder.toPrettyString())  // Write formatted JSON
        }
        println("JSON has been successfully parsed and saved to: $outputFile")
    } catch (JsonException e) {
        println("Failed to parse JSON: ${e.message}")
        // Optionally save problematic JSON to a log file for further analysis
        new File('path/to/your/problematic_json.log').text = fileContent
    }
}

// Example usage
String inputFilePath = "path/to/your/file.config"
String outputFilePath = "path/to/your/output.json"
parseAndSaveJson(inputFilePath, outputFilePath)
