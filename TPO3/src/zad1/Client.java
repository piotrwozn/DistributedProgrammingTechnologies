/**
 *
 *  @author Woźnicki Piotr SO0139
 *
 */

package zad1;


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class Client {
    private String host;
    private int port;
    private String id;
    private SocketChannel clientChannel;

    public Client(String host, int port, String id) {
        this.host = host;
        this.port = port;
        this.id = id;
    }

    public void connect() {
        try {
            clientChannel = SocketChannel.open();
            clientChannel.connect(new InetSocketAddress(host, port));
            clientChannel.configureBlocking(false);
        } catch (IOException ignored) {
        }
    }

    public String send(String req) {
        writeRequest(req);
        return readResponseAsync().join();
    }

    private void writeRequest(String req) {
        try {
            ByteBuffer requestBuffer = ByteBuffer.wrap(req.getBytes(StandardCharsets.UTF_8));
            clientChannel.write(requestBuffer);
        } catch (IOException e) {
            System.err.println("Error while sending request for client with ID " + id + ": " + e.getMessage());
        }
    }

    private CompletableFuture<String> readResponseAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Selector selector = Selector.open();
                clientChannel.register(selector, SelectionKey.OP_READ);
                while (true) {
                    int readyChannels = selector.select(500);
                    if (readyChannels > 0) {
                        return processSelectedKeys(selector.selectedKeys());
                    } else {
                        return null;
                    }
                }
            } catch (IOException e) {
                System.err.println("Error while reading response for client with ID " + id + ": " + e.getMessage());
                return null;
            }
        });
    }

    private String processSelectedKeys(Set<SelectionKey> selectedKeys) {
        Iterator<SelectionKey> iterator = selectedKeys.iterator();
        while (iterator.hasNext()) {
            SelectionKey key = iterator.next();
            iterator.remove();
            if (key.isReadable()) {
                return readResponse(key);
            }
        }
        return null;
    }

    private String readResponse(SelectionKey key) {
        try {
            ByteBuffer responseBuffer = ByteBuffer.allocate(1024);
            int bytesRead = clientChannel.read(responseBuffer);
            if (bytesRead > 0) {
                responseBuffer.flip();
                return StandardCharsets.UTF_8.decode(responseBuffer).toString();
            }
        } catch (IOException e) {
            System.err.println("Error while reading response for client with ID " + id + ": " + e.getMessage());
        }
        return null;
    }

    public String getId() {
        return id;
    }
}
