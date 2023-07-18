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
    private HashMap<String, valueInfo> keyValuePairs = new HashMap<>();
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
                OutputStream os = socket.getOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(os);

                Message ms = (Message) ois.readObject();

                String requisitionType = ms.getRequisitionType();

                switch (requisitionType) {
                    case "PUT":
                        putRequisition();
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
