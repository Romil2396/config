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
import java.util.Map;

public class HoconToYamlConverter {

    public static void main(String[] args) {
        // Hardcoded input and output file paths
        String inputFilePath = "input.config";
        String outputFilePath = "output.yaml";

        // Load the HOCON file
        Config config = ConfigFactory.parseFile(new File(inputFilePath));

        // Convert Config object to a Map
        Map<String, Object> configMap = config.root().unwrapped();

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
}


//<dependencies>
//    <!-- HOCON Parser -->
//    <dependency>
//        <groupId>com.typesafe</groupId>
//        <artifactId>config</artifactId>
//        <version>1.4.2</version>
//    </dependency>
//
//    <!-- SnakeYAML for YAML processing -->
//    <dependency>
//        <groupId>org.yaml</groupId>
//        <artifactId>snakeyaml</artifactId>
//        <version>1.30</version>
//    </dependency>
//</dependencies>