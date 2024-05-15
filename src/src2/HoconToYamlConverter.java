package src2;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class HoconToYamlConverter {

    public static void main(String[] args) {
        // Hardcoded input and output file paths
        String inputFilePath = "input.config";
        String outputFilePath = "output.yaml";

        // Load the HOCON file
        Config config = ConfigFactory.parseFile(new File(inputFilePath));

        // Convert Config object to a Map
        Map<String, Object> configMap = config.root().unwrapped();

        // If you have custom format data, you can add it to the input.config file
        // and parse it here using the parseCustomFormat method

        // Set up SnakeYAML
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Representer representer = new Representer();
        representer.addClassTag(Map.class, Tag.MAP);
        Yaml yaml = new Yaml(representer, options);

        // Write the YAML output
        try (FileWriter writer = new FileWriter(outputFilePath)) {
            yaml.dump(configMap, writer);
            System.out.println("Successfully converted HOCON to YAML.");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error writing YAML file.");
        }
    }

    // Enhanced parser for custom format
    private static Map<String, Object> parseCustomFormat(String customFormat) {
        Map<String, Object> result = new HashMap<>();
        Stack<Map<String, Object>> stack = new Stack<>();
        stack.push(result);

        String[] tokens = customFormat.split("(?=[{}()='])|(?<=[{}()='])|\\s+");
        String key = null;
        for (String token : tokens) {
            token = token.trim();
            if (token.isEmpty()) continue;

            switch (token) {
                case "{":
                    Map<String, Object> newMap = new HashMap<>();
                    stack.peek().put(key, newMap);
                    stack.push(newMap);
                    key = null;
                    break;
                case "}":
                    stack.pop();
                    break;
                case "(":
                    break;
                case ")":
                    break;
                case "=":
                    break;
                case "'":
                    break;
                default:
                    if (key == null) {
                        key = token;
                    } else {
                        stack.peek().put(key, token);
                        key = null;
                    }
                    break;
            }
        }

        return result;
    }
}
