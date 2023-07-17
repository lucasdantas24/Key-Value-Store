import java.io.Serializable;
import java.sql.Timestamp;

public class Message implements Serializable {
    private final String key;
    private String value;
    private String requisitionType;
    private Timestamp timestamp;
    private String requisitionResponse;

    public Message(String key, String value, String requisitionType) {
        this.key = key;
        this.value = value;
        this.requisitionType = requisitionType;
        this.timestamp = new Timestamp(System.currentTimeMillis());
        this.requisitionResponse = "";
    }

    public Message(String key, String value, String requisitionType, Timestamp timestamp) {
        this.key = key;
        this.value = value;
        this.requisitionType = requisitionType;
        this.timestamp = timestamp;
        this.requisitionResponse = "";
    }

    public String getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }

    public String getRequisitionType() {
        return requisitionType;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getRequisitionResponse() {
        return requisitionResponse;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setRequisitionType(String requisitionType) {
        this.requisitionType = requisitionType;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    public void setRequisitionResponse(String requisitionResponse) {
        this.requisitionResponse = requisitionResponse;
    }
}
