import com.google.gson.JsonObject;

public class ACHRecordParser {

    // Parses a single line of ACH data based on an assumed format
    public static JsonObject parseACHRecord(String line) {
        JsonObject json = new JsonObject();
        char type = line.charAt(0);

        switch (type) {
            case '1': // Header record
                json.addProperty("RecordType", "Header");
                json.addProperty("Bank ID", line.substring(1, 10).trim());
                json.addProperty("Batch Number", line.substring(10, 20).trim());
                break;
            case '5': // Detail record
                json.addProperty("RecordType", "Detail");
                json.addProperty("Transaction Amount", line.substring(1, 10).trim());
                json.addProperty("Account Number", line.substring(10, 30).trim());
                break;
            case '9': // Control record
                json.addProperty("RecordType", "Control");
                json.addProperty("Total Amount", line.substring(1, 20).trim());
                break;
            default:
                return null; // Return null if record type is not recognized
        }
        return json;
    }
}
