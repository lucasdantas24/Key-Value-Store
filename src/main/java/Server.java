import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class Server {
    private static final String DEFAULT_IP = "127.0.0.1";
    private final int port;
    private final String IP;
    private Boolean isLeader;
    private final ArrayList<Integer> serverPorts = new ArrayList<>();
    private final HashMap<String, ValueInfo> keyValuePairs = new HashMap<>();
    private final int leaderPort;
    private final String leaderIP;

    public Server(int port, String IP, int leaderPort, String leaderIP) {
        this.port = port;
        this.IP = IP;
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
        try (ServerSocket serverSocket = new ServerSocket(port)) {

            while (true) {
                Socket socket = serverSocket.accept();

                RequisitionHandler rh = new RequisitionHandler(socket, this);

                // Inicia thread para enviar arquivo paralelamente
                Thread rhThread = new Thread(rh);
                rhThread.start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void putRequisition(Socket socket, Message ms) throws IOException, ClassNotFoundException, InterruptedException {

        if (!this.isLeader()) {

            try {

                Socket leaderSocket = new Socket(leaderIP, leaderPort);

                OutputStream leaderOs = leaderSocket.getOutputStream();
                ObjectOutputStream leaderOos = new ObjectOutputStream(leaderOs);

                System.out.println("Encaminhando PUT key:"
                        + ms.getKey()
                        + " value:"
                        + ms.getValue());

                leaderOos.writeObject(ms);

                InputStream is = leaderSocket.getInputStream();
                ObjectInputStream ois = new ObjectInputStream(is);

                Message updatedMs = (Message) ois.readObject();


                OutputStream os = socket.getOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(os);

                oos.writeObject(updatedMs);

                oos.close();
                ois.close();
                leaderOos.close();

            } catch (IOException e) {
                e.printStackTrace();
            }


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
                    + ms.getValue());

            String key = ms.getKey();
            String value = ms.getValue();

            Timestamp ts = new Timestamp(System.currentTimeMillis());

            ValueInfo vi = new ValueInfo(value, ts);

            keyValuePairs.put(key, vi);

            ms.setTimestamp(ts);

            if (replicate(ms)) {
                System.out.println("Enviando PUT_OK ao cliente "
                        + ms.getRequesterIP()
                        + ":"
                        + ms.getRequesterPort()
                        + " da key:"
                        + ms.getKey()
                        + " ts:"
                        + ms.getTimestamp());

                ms.setRequesterIP(IP);
                ms.setRequesterPort(String.valueOf(port));
                ms.setRequisitionType("PUT_OK");
                oos.writeObject(ms);
            } else {
                System.out.println("Requisicao falhou, enviando PUT_FAILED ao cliente "
                        + ms.getRequesterIP()
                        + ":"
                        + ms.getRequesterPort()
                        + "da key:"
                        + ms.getKey());
                ms.setRequisitionType("PUT_FAILED");
                oos.writeObject(ms);
            }

            oos.close();
        }
    }

    private boolean replicate(Message ms) throws IOException, ClassNotFoundException, InterruptedException {
        for (int port : serverPorts) {

            Socket socket = new Socket(IP, port);

            Thread.sleep(10000);

            OutputStream os = socket.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);

            ms.setRequisitionType("REPLICATION");

            oos.writeObject(ms);

            InputStream is = socket.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(is);

            ms = (Message) ois.readObject();

            oos.close();
            ois.close();

            if (!ms.getRequisitionType().equals("REPLICATION_OK")) return false;
        }
        return true;
    }

    private void getRequisition(Socket socket, Message ms) throws IOException {
        ValueInfo vi = keyValuePairs.get(ms.getKey());

        OutputStream os = socket.getOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(os);

        Timestamp ts = ms.getTimestamp();

        if (vi == null) {
            ms.setValue(null);
            ms.setTimestamp(null);
        } else if (vi.getTimestamp().before(ms.getTimestamp())){
            ms.setValue("TRY_OTHER_SERVER_OR_LATER");
            ms.setTimestamp(vi.getTimestamp());
        } else {
            ms.setValue(vi.getValue());
            ms.setTimestamp(vi.getTimestamp());
        }

        if (vi != null) {
            System.out.println("Cliente "
                    + ms.getRequesterIP()
                    + ":"
                    + ms.getRequesterPort()
                    + " GET key:"
                    + ms.getKey()
                    + " ts:"
                    + ts
                    + ". Meu ts e "
                    + vi.getTimestamp()
                    + ", portanto devolvendo "
                    + ms.getValue());
        } else {
            System.out.println("Cliente "
                    + ms.getRequesterIP()
                    + ":"
                    + ms.getRequesterPort()
                    + " GET key:"
                    + ms.getKey()
                    + " ts:"
                    + ts
                    + ". Chave nao encontrada, retornando null");
        }

        oos.writeObject(ms);

        oos.close();
    }

    private void replicationRequisition(Socket socket, Message ms) throws IOException {
        OutputStream os = socket.getOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(os);

        ValueInfo vi = new ValueInfo(ms.getValue(), ms.getTimestamp());

        keyValuePairs.put(ms.getKey(), vi);

        System.out.println("REPLICATION key:"
                + ms.getKey()
                + " value:"
                + ms.getValue()
                + " ts:"
                + ms.getTimestamp());

        ms.setRequisitionType("REPLICATION_OK");

        oos.writeObject(ms);

        oos.close();
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

    public Boolean isLeader() {
        return isLeader;
    }

    class RequisitionHandler implements Runnable {
        private final Socket socket;
        private final Server server;

        public RequisitionHandler(Socket socket, Server server) {
            this.socket = socket;
            this.server = server;
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
                        server.putRequisition(socket, ms);
                        break;
                    case "GET":
                        server.getRequisition(socket, ms);
                        break;
                    case "REPLICATION":
                        server.replicationRequisition(socket, ms);
                        break;
                }

                ois.close();

            } catch (IOException | ClassNotFoundException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class ValueInfo {
        private final String value;
        private final Timestamp timestamp;

        public ValueInfo(String value, Timestamp timestamp) {
            this.value = value;
            this.timestamp = timestamp;
        }

        public String getValue() {
            return value;
        }

        public Timestamp getTimestamp() {
            return timestamp;
        }

    }
}
