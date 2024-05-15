import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

public class CustomAndHoconToYaml {

    public static Map<String, Object> parseCustomFormat(String input) {
        input = input.replaceAll("\\s+", ""); // Remove all whitespace
        Map<String, Object> resultMap = new HashMap<>();
        Stack<Map<String, Object>> stack = new Stack<>();
        stack.push(resultMap);

        StringBuilder key = new StringBuilder();
        StringBuilder value = new StringBuilder();
        boolean isKey = true;
        boolean isInQuotes = false;

        for (char c : input.toCharArray()) {
            switch (c) {
                case '{':
                    Map<String, Object> newMap = new HashMap<>();
                    if (isKey && key.length() > 0) {
                        stack.peek().put(key.toString(), newMap);
                        key.setLength(0);
                    }
                    stack.push(newMap);
                    isKey = true;
                    break;
                case '}':
                    if (value.length() > 0 && key.length() > 0) {
                        stack.peek().put(key.toString(), value.toString());
                        key.setLength(0);
                        value.setLength(0);
                    }
                    stack.pop();
                    isKey = true;
                    break;
                case '=':
                    isKey = false;
                    break;
                case '\'':
                    isInQuotes = !isInQuotes;
                    break;
                case '(':
                    if (!isInQuotes && key.length() > 0) {
                        value.append(c);
                    } else {
                        key.append(c);
                    }
                    break;
                case ')':
                    if (!isInQuotes && value.length() > 0) {
                        value.append(c);
                        stack.peek().put(key.toString(), value.toString());
                        key.setLength(0);
                        value.setLength(0);
                        isKey = true;
                    } else {
                        key.append(c);
                    }
                    break;
                default:
                    if (isKey) {
                        key.append(c);
                    } else {
                        value.append(c);
                    }
                    break;
            }
        }
        return resultMap;
    }

    public static Map<String, Object> parseHocon(String input) {
        Config config = ConfigFactory.parseString(input);
        return convertConfigToMap(config.root().unwrapped());
    }

    private static Map<String, Object> convertConfigToMap(Map<String, Object> map) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() instanceof Map) {
                result.put(entry.getKey(), convertConfigToMap((Map<String, Object>) entry.getValue()));
            } else {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }

    public static void writeYaml(Map<String, Object> data, String outputFile) {
        DumperOptions options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        Yaml yaml = new Yaml(options);
        try (FileWriter writer = new FileWriter(outputFile)) {
            yaml.dump(data, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String inputFile = "input.config";
        String outputFile = "output.yaml";

        StringBuilder input = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                input.append(line).append("\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        String content = input.toString();
        Map<String, Object> customDataMap = new HashMap<>();
        Map<String, Object> hoconDataMap = new HashMap<>();

        boolean isHocon = false;
        StringBuilder customBuilder = new StringBuilder();
        StringBuilder hoconBuilder = new StringBuilder();

        for (String line : content.split("\n")) {
            if (line.trim().startsWith("{") || line.trim().endsWith("}")) {
                customBuilder.append(line).append("\n");
            } else {
                isHocon = true;
                hoconBuilder.append(line).append("\n");
            }
        }

        if (customBuilder.length() > 0) {
            customDataMap = parseCustomFormat(customBuilder.toString());
        }
        if (hoconBuilder.length() > 0) {
            hoconDataMap = parseHocon(hoconBuilder.toString());
        }

        // Merging customDataMap and hoconDataMap
        Map<String, Object> mergedMap = new HashMap<>(customDataMap);
        mergedMap.putAll(hoconDataMap);

        writeYaml(mergedMap, outputFile);
        System.out.println("Data successfully written to " + outputFile);
    }
}
