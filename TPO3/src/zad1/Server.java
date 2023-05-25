/**
 *
 *  @author Woźnicki Piotr SO0139
 *
 */

package zad1;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Server {
    private final String serverHost;
    private final int serverPort;
    private ServerSocketChannel srvSocketChannel;
    private Selector srvSelector;
    private Thread srvThread;
    private Map<String, StringBuilder> clientLogsMap;
    private StringBuilder srvLog;
    private boolean serverActive;
    private String cId;

    public Server(String host, int port) {
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
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        srvThread.start();
    }

    public void stopServer() {
        serverActive = false;
        try {
            srvThread.interrupt();
            srvSocketChannel.close();
            srvSelector.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private void processClientRequest(SelectionKey key) throws IOException {
        SocketChannel client = (SocketChannel) key.channel();
        ByteBuffer buf = ByteBuffer.allocate(1000000);
        client.read(buf);
        String req = new String(buf.array()).trim();
        buf.clear();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

        if (!req.isEmpty()) {
            String[] reqParts = req.split(" ");
            String resp;

            switch (reqParts[0]) {
                case "login":
                    cId = reqParts[1];
                    clientLogsMap.put(cId, new StringBuilder());
                    clientLogsMap.get(cId)
                            .append("=== ")
                            .append(cId)
                            .append(" log start ===")
                            .append("\n")
                            .append("logged in")
                            .append("\n");
                    resp = "logged in";
                    srvLog
                            .append(cId)
                            .append(" logged in at ")
                            .append(now.format(formatter))
                            .append("\n");
                    break;
                case "bye":
                    clientLogsMap.get(cId)
                            .append("logged out")
                            .append("\n")
                            .append("=== ")
                            .append(cId)
                            .append(" log end ===")
                            .append("\n");
                    srvLog.append(cId)
                            .append(" logged out at ")
                            .append(now.format(formatter))
                            .append("\n");
                    if (req.equals("bye")) {
                        resp = "logged out";
                    } else {
                        resp = clientLogsMap.get(cId).toString();
                    }
                    break;
                default:
                    String dateFrom = reqParts[0];
                    String dateTo = reqParts[1];
                    resp = Time.passed(dateFrom, dateTo);
                    clientLogsMap.get(cId)
                            .append("Request: ")
                            .append(dateFrom)
                            .append(" ")
                            .append(dateTo)
                            .append("\n")
                            .append("Result:")
                            .append('\n')
                            .append(Time.passed(dateFrom, dateTo))
                            .append("\n");
                    srvLog.append(cId)
                            .append(" request at ")
                            .append(now.format(formatter))
                            .append(": \"")
                            .append(req)
                            .append("\"")
                            .append("\n");
                    break;
            }
            buf.put(resp.getBytes());
            buf.flip();
            client.write(buf);
        }
    }
}
