import com.google.gson.JsonObject;

public class DynamicACHParser {

    public JsonObject parseRecord(String line) {
        // Basic dynamic parsing based on delimiters like spaces
        String[] parts = line.split("\\s+");
        JsonObject record = new JsonObject();
        for (int i = 0; i < parts.length; i++) {
            record.addProperty("Field" + (i + 1), parts[i]);
        }
        return record;
    }
}
