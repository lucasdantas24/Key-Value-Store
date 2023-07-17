import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;

public class Server {
    private final String DEFAULT_IP = "127.0.0.1";
    private final int DEFAULT_PORT = 10097;
    private int port;
    private String IP;
    private Boolean isLeader;
    private ArrayList<Integer> serverPorts;
    private HashMap<String, valueInfo> keyValuePairs = new HashMap<>();
    private int leaderPort;
    private String leaderIP;

    public Server(int port, String IP, Boolean isLeader, int leaderPort, String leaderIP) {
        this.port = port;
        this.IP = IP;
        this.isLeader = isLeader;
        this.leaderPort = leaderPort;
        this.leaderIP = leaderIP;

        setLeader(port, leaderPort);
        setServerPorts(port);
    }

    public static void main(String[] args) {

    }

    private void putRequisition() {

        Timestamp ts = new Timestamp(System.currentTimeMillis());

    }

    private void getRequisition() {

    }

    private void replicationRequisition() {

    }

    static class requisitionsHandler implements Runnable {

        @Override
        public void run() {

        }
    }

    public void setServerPorts(int port) {
        int possiblePorts[] = {10097, 10098, 10099};

        for (int i: possiblePorts) {
            if (i != port) serverPorts.add(i);
        }
    }

    public void setLeader(int port, int leaderPort) {
        this.isLeader = port == leaderPort;
    }

    private class valueInfo {
        private String value;
        private Timestamp timestamp;

        public valueInfo(String value, Timestamp timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Timestamp getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(Timestamp timestamp) {
            this.timestamp = timestamp;
        }
    }
}
