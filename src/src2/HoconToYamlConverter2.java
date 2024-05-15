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

public class HoconToYamlConverter2 {

    public static void main(String[] args) {
        // Hardcoded input and output file paths
        String inputFilePath = "input.config";
        String outputFilePath = "output.yaml";

        // Load the HOCON file
        Config config = ConfigFactory.parseFile(new File(inputFilePath));

        // Convert Config object to a Map
        Map<String, Object> configMap = config.root().unwrapped();

        // Parse custom format if any
        String customFormat = "xyz{ abc{ name ='rtyu' deloveryPoints{ 'rtyu' (abc:primary.home) }}}";
        Map<String, Object> customMap = parseCustomFormat(customFormat);

        // Merge HOCON and custom format maps
        configMap.putAll(customMap);

        // Set up SnakeYAML
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Representer representer = new Representer();
        representer.addClassTag(Map.class, Tag.MAP);
        Yaml yaml = new Yaml(representer, options);

        // Write the YAML output
        try (FileWriter writer = new FileWriter(outputFilePath)) {
            yaml.dump(configMap, writer);
            System.out.println("Successfully converted HOCON and custom format to YAML.");
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error writing YAML file.");
        }
    }

    // Simple parser for custom format
    private static Map<String, Object> parseCustomFormat(String customFormat) {
        Map<String, Object> result = new HashMap<>();
        String[] tokens = customFormat.split("\\s*\\{\\s*|\\s*\\}\\s*|\\s*'\\s*|\\s*=\\s*|\\s*\\(\\s*|\\s*\\)\\s*");
        parseTokens(result, tokens, 0, tokens.length);
        return result;
    }

    private static int parseTokens(Map<String, Object> map, String[] tokens, int start, int end) {
        String key = null;
        for (int i = start; i < end; i++) {
            if (tokens[i].isEmpty()) continue;
            if (tokens[i].contains(":")) {
                String[] pair = tokens[i].split(":");
                map.put(pair[0], pair[1]);
            } else if (tokens[i].equals("=")) {
                key = tokens[i - 1];
            } else if (tokens[i].equals("{")) {
                Map<String, Object> nestedMap = new HashMap<>();
                i = parseTokens(nestedMap, tokens, i + 1, end);
                map.put(key, nestedMap);
                key = null;
            } else if (tokens[i].equals("}")) {
                return i;
            } else if (key != null) {
                map.put(key, tokens[i]);
                key = null;
            }
        }
        return end;
    }
}
