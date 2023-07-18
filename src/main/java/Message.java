import java.io.Serializable;
import java.sql.Timestamp;

public class Message implements Serializable {
    private final String key;
    private String value;
    private String requisitionType;
    private Timestamp timestamp;
    private String requesterIP;
    private String requesterPort;

    public Message(String key, String value, String requisitionType, String requesterIP, String requesterPort) {
        this.key = key;
        this.value = value;
        this.requisitionType = requisitionType;
        this.timestamp = new Timestamp(Long.MIN_VALUE);
        this.requesterIP = requesterIP;
        this.requesterPort = requesterPort;
    }

    public Message(String key, String value, String requisitionType, Timestamp timestamp) {
        this.key = key;
        this.value = value;
        this.requisitionType = requisitionType;
        this.timestamp = timestamp;
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

    public String getRequesterIP() {
        return requesterIP;
    }

    public String getRequesterPort() {
        return requesterPort;
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

    public void setRequesterIP(String requesterIP) {
        this.requesterIP = requesterIP;
    }

    public void setRequesterPort(String requesterPort) {
        this.requesterPort = requesterPort;
    }
}
