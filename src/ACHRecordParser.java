import com.google.gson.JsonObject;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ACHRecordParser {
    // Adjusted patterns based on expected input details
    private static final Pattern BLOCK_OPEN = Pattern.compile("^(\\w+)\\s*\\{\\s*$");
    private static final Pattern BLOCK_CLOSE = Pattern.compile("^\\}\\s*$");
    private static final Pattern KEY_VALUE_PAIR = Pattern.compile("^(\\w+)\\s*:\\s*([^;]+);\\s*$");

    public static JsonObject parseACHData(String[] lines) {
        Deque<JsonObject> stack = new ArrayDeque<>();
        JsonObject root = new JsonObject();
        stack.push(root);

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            System.out.println("Processing line: " + line);  // Debug output

            Matcher openMatcher = BLOCK_OPEN.matcher(line);
            Matcher closeMatcher = BLOCK_CLOSE.matcher(line);
            Matcher kvMatcher = KEY_VALUE_PAIR.matcher(line);

            if (openMatcher.matches()) {
                String blockName = openMatcher.group(1);
                JsonObject newBlock = new JsonObject();
                stack.peek().add(blockName, newBlock);
                stack.push(newBlock);
                System.out.println("Opened new block: " + blockName);  // Debugging block opening
            } else if (closeMatcher.matches()) {
                stack.pop();
                System.out.println("Closed a block");  // Debugging block closing
            } else if (kvMatcher.matches()) {
                String key = kvMatcher.group(1);
                String value = kvMatcher.group(2).trim();
                stack.peek().addProperty(key, value);
                System.out.println("Added key-value pair: " + key + " -> " + value);  // Debugging key-value addition
            } else {
                System.out.println("Line did not match any pattern");  // Debugging unmatched lines
            }
        }

        return root; // Return the root object, which contains the entire parsed structure
    }
}
