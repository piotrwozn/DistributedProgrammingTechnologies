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
import java.util.concurrent.atomic.AtomicBoolean;

public class ChatClient {
    private final String host;
    private final int port;
    private final String id;
    private SocketChannel clientChannel;
    private String chatView;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public ChatClient(String host, int port, String id) {
        this.host = host;
        this.port = port;
        this.id = id;
    }

    public void login() {
        try {
            clientChannel = SocketChannel.open();
            clientChannel.connect(new InetSocketAddress(host, port));
            clientChannel.configureBlocking(false);
        } catch (IOException ignored) {
        }
        send("login " + id + "\n");
        Thread receiveThread = new Thread(this::receiveLoop);
        receiveThread.start();
    }

    private void receiveLoop() {
        while (running.get()) {
            String response = readResponseAsync().join();
            if (response != null) {
                setChatView(response);
            }
        }
    }

    public void logout() {
        send("_bye_bye_" + "\n");
        running.set(false);
    }

    public synchronized void send(String req) {
        try {
            ByteBuffer requestBuffer = ByteBuffer.wrap(req.getBytes(StandardCharsets.UTF_8));
            clientChannel.write(requestBuffer);
        } catch (IOException e) {

        }
    }


    CompletableFuture<String> readResponseAsync() {
        return CompletableFuture.supplyAsync(() -> {
            try {
                Selector selector = Selector.open();
                clientChannel.register(selector, SelectionKey.OP_READ);
                while (true) {
                    selector.select(); // Usunięto timeout
                    Set<SelectionKey> selectedKeys = selector.selectedKeys();
                    if (!selectedKeys.isEmpty()) {
                        String response = processSelectedKeys(selectedKeys);
                        if (response != null) {
                            return response;
                        }
                    }
                }
            } catch (IOException e) {
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
        }
        return null;
    }

    public String getId() {
        return id;
    }

    public String getChatView() {
        return chatView;
    }

    public synchronized void setChatView(String chatView) {
        this.chatView = chatView;
    }
}
