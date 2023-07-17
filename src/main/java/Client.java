import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;


public class Client {
    private final String DEFAULT_SERVER_IP = "127.0.0.1";
    private final int[] DEFAULT_SERVER_PORTS = {10097, 10098, 10099};
    private int port;
    private String IP;
    private ArrayList<ServerInfo> servers;
    private HashMap<String, Timestamp> knownTimestamps = new HashMap<>();

    public static void main(String[] args) {

    }

    public void menu() {

    }

    private void getRequisition() {

    }

    private void putRequisition() {

    }

    private class ServerInfo {
        private int port;
        private String IP;

        public ServerInfo(int port, String IP) {
            this.port = port;
            this.IP = IP;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getIP() {
            return IP;
        }

        public void setIP(String IP) {
            this.IP = IP;
        }
    }
}
