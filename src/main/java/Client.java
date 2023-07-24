import java.io.*;
import java.net.Socket;
import java.sql.Timestamp;
import java.util.*;


public class Client {
    private static final String DEFAULT_SERVER_IP = "127.0.0.1";
    private final ArrayList<ServerInfo> servers;
    private final HashMap<String, Timestamp> knownTimestamps = new HashMap<>();

    public Client(ArrayList<ServerInfo> servers) {
        this.servers = servers;
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        ArrayList<ServerInfo> servers = new ArrayList<>();
        String ip;
        String p;
        int port;

        System.out.print("Informe o IP do primeiro servidor:(DEFAULT - 127.0.0.1)");
        ip = sc.nextLine().trim();
        if (ip.isEmpty()) ip = DEFAULT_SERVER_IP;

        System.out.print("Informe a porta do primeiro servidor:(DEFAULT - 10097)");
        p = sc.nextLine().trim();
        if (p.isEmpty()) {
            port = 10097;
        } else {
            port = Integer.parseInt(p);
        }

        servers.add(new ServerInfo(port, ip));

        System.out.print("Informe o IP do segundo servidor:(DEFAULT - 127.0.0.1)");
        ip = sc.nextLine().trim();
        if (ip.isEmpty()) ip = DEFAULT_SERVER_IP;

        System.out.print("Informe a porta do segundo servidor:(DEFAULT - 10098)");
        p = sc.nextLine().trim();
        if (p.isEmpty()) {
            port = 10098;
        } else {
            port = Integer.parseInt(p);
        }

        servers.add(new ServerInfo(port, ip));

        System.out.print("Informe o IP do terceiro servidor:(DEFAULT - 127.0.0.1)");
        ip = sc.nextLine().trim();
        if (ip.isEmpty()) ip = DEFAULT_SERVER_IP;

        System.out.print("Informe a porta do primeiro servidor:(DEFAULT - 10099)");
        p = sc.nextLine().trim();
        if (p.isEmpty()) {
            port = 10099;
        } else {
            port = Integer.parseInt(p);
        }

        servers.add(new ServerInfo(port, ip));

        Client client = new Client(servers);

        client.menu();

    }

    public void menu() {
        while (true) {
            Scanner sc = new Scanner(System.in);
            System.out.println("---------Menu Cliente---------");
            System.out.println("Escolha qual funcao utilizar entre as abaixo");
            System.out.println("1 - PUT");
            System.out.println("2 - GET");
            System.out.println("3 - QUIT");
            System.out.print("Escolha a funcao:");
            int funcao = sc.nextInt();

            switch (funcao) {
                case 1:
                    putRequisition();
                    break;
                case 2:
                    getRequisition();
                    break;
                case 3:
                    System.exit(1);
                    break;
            }
        }
    }

    private void putRequisition() {
        Scanner sc = new Scanner(System.in);

        Random random = new Random();

        int servidorEscolhido = random.nextInt(servers.size());

        System.out.print("Favor informar a chave:");
        String key = sc.nextLine().trim();

        System.out.print("Favor informar o valor:");
        String value = sc.nextLine().trim();

        String requisitionType = "PUT";

        ServerInfo si = servers.get(servidorEscolhido);

        String ip = si.getIP();

        int port = si.getPort();

        try (Socket socket = new Socket(ip, port)) {
            String requesterIP = String.valueOf(socket.getLocalAddress()).substring(1);
            String requesterPort = String.valueOf(socket.getLocalPort());

            OutputStream os = socket.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);

            Message ms = new Message(key, value, requisitionType, requesterIP, requesterPort);

            oos.writeObject(ms);

            InputStream is = socket.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(is);

            Message returnMs = (Message) ois.readObject();

            if (returnMs.getRequisitionType().equals("PUT_OK")) {
                System.out.println("PUT_OK key:"
                        + key
                        + " value:"
                        + value
                        + " timestamp:"
                        + returnMs.getTimestamp()
                        + " realizada no servidor "
                        + returnMs.getRequesterIP()
                        + ":"
                        + returnMs.getRequesterPort());
                knownTimestamps.put(returnMs.getKey(), returnMs.getTimestamp());
            } else {
                System.out.println("Requisicao de PUT falhou!");
            }

            ois.close();
            oos.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    private void getRequisition() {
        Scanner sc = new Scanner(System.in);

        Random random = new Random();

        int servidorEscolhido = random.nextInt(servers.size());

        System.out.print("Favor informar a chave:");
        String key = sc.nextLine().trim();

        String requisitionType = "GET";

        ServerInfo si = servers.get(servidorEscolhido);

        Timestamp ts = knownTimestamps.get(key);

        try (Socket socket = new Socket(si.getIP(), si.getPort())) {
            String requesterIP = String.valueOf(socket.getLocalAddress());
            String requesterPort = String.valueOf(socket.getLocalPort());

            OutputStream os = socket.getOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(os);

            Message ms;

            if (ts != null) {
                ms = new Message(key, ts, requisitionType, requesterIP, requesterPort);
            } else {
                ms = new Message(key, requisitionType, requesterIP, requesterPort);
                ts = new Timestamp(0);
            }

            oos.writeObject(ms);

            InputStream is = socket.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(is);

            Message returnMs = (Message) ois.readObject();

            if (returnMs.getValue() != null) {
                System.out.println("GET key:"
                        + returnMs.getKey()
                        + " value:"
                        + returnMs.getValue()
                        + " obtido do servidor "
                        + si.getIP()
                        + ":"
                        + si.getPort()
                        + ", meu timestamp "
                        + ts
                        + " e do servidor "
                        + returnMs.getTimestamp());
                if (!returnMs.getValue().equals( "TRY_OTHER_SERVER_OR_LATER")) knownTimestamps.put(returnMs.getKey(), returnMs.getTimestamp());
            } else {
                System.out.println("Chave nao encontrada");
            }

            oos.close();
            ois.close();

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    private static class ServerInfo {
        private final int port;
        private final String IP;

        public ServerInfo(int port, String IP) {
            this.port = port;
            this.IP = IP;
        }

        public int getPort() {
            return port;
        }

        public String getIP() {
            return IP;
        }

    }
}
