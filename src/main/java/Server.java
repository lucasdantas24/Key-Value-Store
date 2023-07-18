import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Server {
    private static final String DEFAULT_IP = "127.0.0.1";
    private static final int DEFAULT_PORT = 10097;
    private int port;
    private String IP;
    private Boolean isLeader;
    private ArrayList<Integer> serverPorts;
    private HashMap<String, ValueInfo> keyValuePairs = new HashMap<>();
    private int leaderPort;
    private String leaderIP;

    public Server(int port, String IP, int leaderPort, String leaderIP) {
        this.port = port;
        this.IP = IP;
        this.isLeader = isLeader;
        this.leaderPort = leaderPort;
        this.leaderIP = leaderIP;

        setLeader(port, leaderPort);
        setServerPorts(port);
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        Server server = serverInicialization(sc);

        server.start();
    }

    private static Server serverInicialization(Scanner sc) {
        String ip, leaderIP;
        int port, leaderPort;

        System.out.print("Favor informar IP do servidor:");
        ip = sc.nextLine().trim();

        if (ip.isEmpty()) ip = DEFAULT_IP;

        System.out.print("Favor informar porta do servidor:");
        port = Integer.parseInt(sc.nextLine().trim());

        System.out.print("Favor informar IP do servidor lider:");
        leaderIP = sc.nextLine().trim();

        System.out.print("Favor informar porta do servidor lider:");
        leaderPort = Integer.parseInt(sc.nextLine().trim());

        return new Server(port, ip, leaderPort, leaderIP);
    }

    private void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(this.getPort());

            do {
                Socket socket = serverSocket.accept();

                RequisitionHandler rh = new RequisitionHandler(socket);

                // Inicia thread para enviar arquivo paralelamente
                Thread rhThread = new Thread(rh);
                rhThread.start();
            } while (true);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void putRequisition(Socket socket, Message ms) throws IOException, ClassNotFoundException {
        if (!this.isLeader()) {


        } else {
            OutputStream os = socket.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);

            System.out.println("Cliente "
                    + ms.getRequesterIP()
                    + ":"
                    + ms.getRequesterPort()
                    + " PUT key:"
                    + ms.getKey()
                    + " value:"
                    + ms.getValue()
                    + ".");

            String key = ms.getKey();
            String value = ms.getValue();

            Timestamp ts = new Timestamp(System.currentTimeMillis());

            ValueInfo vi = new ValueInfo(value, ts);

            keyValuePairs.put(key, vi);

            ms.setTimestamp(ts);

            if (replicate(ms)) {
                ms.setRequisitionType("PUT_OK");
                oos.writeObject(ms);
            } else {
                ms.setRequisitionType("PUT_FAILED");
                oos.writeObject(ms);
            }
        }
    }

    private boolean replicate(Message ms) throws IOException, ClassNotFoundException {
        for (int port : serverPorts) {
            Socket socket = new Socket(IP, port);

            OutputStream os = socket.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);

            ms.setRequisitionType("REPLICATION");

            oos.writeObject(ms);

            InputStream is = socket.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(is);

            ms = (Message) ois.readObject();

            if (ms.getRequisitionType().equals("REPLICATION_OK")) return false;
        }
        return true;
    }

    private void getRequisition() {

    }

    private void replicationRequisition() {

    }

    public void setServerPorts(int port) {
        int[] possiblePorts = {10097, 10098, 10099};

        for (int i: possiblePorts) {
            if (i != port) serverPorts.add(i);
        }
    }

    public void setLeader(int port, int leaderPort) {
        this.isLeader = port == leaderPort;
    }

    public int getPort() {
        return port;
    }

    public String getIP() {
        return IP;
    }

    public Boolean isLeader() {
        return isLeader;
    }

    public ArrayList<Integer> getServerPorts() {
        return serverPorts;
    }

    public int getLeaderPort() {
        return leaderPort;
    }

    public String getLeaderIP() {
        return leaderIP;
    }

    class RequisitionHandler implements Runnable {
        private Socket socket;

        public RequisitionHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                InputStream is = socket.getInputStream();
                ObjectInputStream ois = new ObjectInputStream(is);

                Message ms = (Message) ois.readObject();

                String requisitionType = ms.getRequisitionType();

                switch (requisitionType) {
                    case "PUT":
                        putRequisition(socket, ms);
                        break;
                    case "GET":
                        getRequisition();
                        break;
                    case "REPLICATION":
                        replicationRequisition();
                        break;
                }

            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }

    private class ValueInfo {
        private String value;
        private Timestamp timestamp;

        public ValueInfo(String value, Timestamp timestamp) {
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
