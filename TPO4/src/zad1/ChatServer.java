/**
 *
 *  @author Woźnicki Piotr SO0139
 *
 */

package zad1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ChatServer {
    private final String serverHost;
    private final int serverPort;
    private ServerSocketChannel srvSocketChannel;
    private Selector srvSelector;
    private Thread srvThread;
    private Map<String, StringBuilder> clientLogsMap;
    private StringBuilder srvLog;
    private boolean serverActive;
    private ArrayList<String> cIds = new ArrayList<>();
    private ArrayList<Socket> clientSockets = new ArrayList<>();

    public ChatServer(String host, int port) {
        this.serverHost = host;
        this.serverPort = port;
        clientLogsMap = new HashMap<>();
        srvLog = new StringBuilder();
    }

    public void startServer() {
        serverActive = true;
        srvThread = new Thread(() -> {
            try {
                srvSocketChannel = ServerSocketChannel.open();
                srvSocketChannel.socket().bind(new InetSocketAddress(serverHost, serverPort));
                srvSocketChannel.configureBlocking(false);
                srvSelector = Selector.open();
                srvSocketChannel.register(srvSelector, SelectionKey.OP_ACCEPT);
                manageConnections();
            } catch (IOException ignored) {
            }
        });
        srvThread.start();
        System.out.println("Server started");
    }

    public void stopServer() {
        serverActive = false;
        try {
            srvThread.interrupt();
            srvSocketChannel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Server stopped");
    }

    public String getServerLog() {
        return srvLog.toString();
    }

    private void manageConnections() {
        while (serverActive) {
            try {
                try {
                    srvSelector.select();
                    Set<SelectionKey> selKeys = srvSelector.selectedKeys();
                    Iterator<SelectionKey> iter = selKeys.iterator();
                    while (serverActive && iter.hasNext()) {
                        SelectionKey key = iter.next();
                        if (key.isAcceptable()) {
                            addClient();
                        }
                        if (key.isReadable()) {
                            processClientRequest(key);
                        }
                        iter.remove();
                    }
                } catch (ClosedSelectorException ignored) {
                }
            } catch (IOException e) {
                if (serverActive) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void addClient() throws IOException {
        SocketChannel client = srvSocketChannel.accept();
        client.configureBlocking(false);
        client.register(srvSelector, SelectionKey.OP_READ);
    }

    private synchronized void processClientRequest(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buf = ByteBuffer.allocate(1024);
        StringBuilder messageBuilder = new StringBuilder();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

        int bytesRead;
        while ((bytesRead = client.read(buf)) > 0) {
            buf.flip();
            byte[] bytes = new byte[bytesRead];
            buf.get(bytes);
            messageBuilder.append(new String(bytes));
            buf.clear();
        }

        if (bytesRead == -1) {
            client.close();
            key.cancel();
            return;
        }

        String[] messages = messageBuilder.toString().split("\n");

        for (String req : messages) {

            if (!req.isEmpty()) {
                String[] reqParts = req.split(" ");
                String resp;

                switch (reqParts[0]) {
                    case "login":
                        cIds.add(reqParts[1]);
                        clientSockets.add(client.socket());
                        clientLogsMap.put(cIds.get(cIds.size() - 1), new StringBuilder());
                        clientLogsMap.get(cIds.get(cIds.size() - 1))
                                .append("=== ")
                                .append(cIds.get(cIds.size() - 1))
                                .append(" chat view")
                                .append("\n")
                                .append(now.format(formatter))
                                .append(" ")
                                .append(cIds.get(cIds.size() - 1))
                                .append(" logged in")
                                .append("\n");
                        srvLog
                                .append(now.format(formatter))
                                .append(" ")
                                .append(cIds.get(cIds.size() - 1))
                                .append(" logged in")
                                .append("\n");
                        for (int i = 0; i < cIds.size(); i++) {
                            if (!Objects.equals(cIds.get(i), reqParts[1])) {
                                clientLogsMap.get(cIds.get(i))
                                        .append(now.format(formatter))
                                        .append(" ")
                                        .append(cIds.get(cIds.size() - 1))
                                        .append(" logged in")
                                        .append("\n");
                            }
                        }
                        resp = "";
                        break;
                    case "_bye_bye_":
                        int tmp = 0;
                        for (int i = 0; i < clientSockets.size(); i++) {
                            if (clientSockets.get(i) == client.socket()) {
                                tmp = i;
                            }
                        }
                        clientLogsMap.get(cIds.get(tmp))
                                .append(now.format(formatter))
                                .append(" ")
                                .append(cIds.get(tmp))
                                .append(" logged out")
                                .append("\n");
                        srvLog.append(now.format(formatter))
                                .append(" ")
                                .append(cIds.get(tmp))
                                .append(" logged out")
                                .append("\n");
                        for (String cId : cIds) {
                            if (!Objects.equals(cId, cIds.get(tmp))) {
                                clientLogsMap.get(cId)
                                        .append(now.format(formatter))
                                        .append(" ")
                                        .append(cIds.get(tmp))
                                        .append(" logged out")
                                        .append("\n");
                            }
                        }
                        resp = clientLogsMap.get(cIds.get(tmp)).toString();
                        break;
                    default:
                        int tmp2 = 0;
                        for (int i = 0; i < clientSockets.size(); i++) {
                            if (clientSockets.get(i) == client.socket()) {
                                tmp2 = i;
                            }
                        }
                        srvLog.append(now.format(formatter))
                                .append(" ")
                                .append(cIds.get(tmp2))
                                .append(": ")
                                .append(req)
                                .append("\n");
                        for (int i = 0; i < cIds.size(); i++) {
                            clientLogsMap.get(cIds.get(i))
                                    .append(now.format(formatter))
                                    .append(" ")
                                    .append(cIds.get(tmp2))
                                    .append(": ")
                                    .append(req)
                                    .append("\n");
                        }
                        resp = "";
                        break;
                }
                ByteBuffer responseBuffer = ByteBuffer.allocate(resp.getBytes().length);
                responseBuffer.put(resp.getBytes());
                responseBuffer.flip();
                client.write(responseBuffer);
            }
        }
    }
}
