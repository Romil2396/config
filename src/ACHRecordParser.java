import com.google.gson.JsonObject;

public class ACHRecordParser {

    public static JsonObject parseACHRecord(String line) {
        JsonObject json = new JsonObject();
        if (line.length() < 1) {
            return null; // Not enough data to determine type
        }

        char type = line.charAt(0); // Assuming the first character determines the record type
        switch (type) {
            case '1': // Header Record
                json.addProperty("RecordType", "Header");
                json.addProperty("Company Name", line.substring(1, 20).trim());
                json.addProperty("File ID", line.substring(20, 40).trim());
                break;
            case '5': // Detail Record
                json.addProperty("RecordType", "Detail");
                json.addProperty("Account Number", line.substring(1, 16).trim());
                json.addProperty("Amount", line.substring(16, 26).trim());
                json.addProperty("Transaction Code", line.substring(26, 28).trim());
                break;
            case '9': // Control Record
                json.addProperty("RecordType", "Control");
                json.addProperty("Batch Count", line.substring(1, 7).trim());
                json.addProperty("Block Count", line.substring(7, 13).trim());
                json.addProperty("Entry Addenda Count", line.substring(13, 21).trim());
                break;
            default:
                return null; // Unknown record type
        }

        return json;
    }
}
