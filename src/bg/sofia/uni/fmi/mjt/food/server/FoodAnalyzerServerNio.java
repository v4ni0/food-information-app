package bg.sofia.uni.fmi.mjt.food.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

public class FoodAnalyzerServerNio {
    public static final int SERVER_PORT = 5000;
    private static final int BUFFER_SIZE = 1024;
    private static final String EXIT_MESSAGE = "exit";
    private static final String REQUEST_BY_KEYWORD = "get-food";
    private static final String REQUEST_BY_ID = "get-food-details";
    private static final String REQUEST_BY_BARCODE = "get-food-by-barcode";
    private ByteBuffer buffer;
    private Selector selector;

    public FoodAnalyzerServerNio() {
        this.buffer = ByteBuffer.allocate(BUFFER_SIZE);
    }

    private void configureServer(ServerSocketChannel serverSocketChannel) throws IOException {
        serverSocketChannel.bind(new InetSocketAddress(SERVER_PORT));
        serverSocketChannel.configureBlocking(false);

        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("server is listening");
    }

    private void registerClient(ServerSocketChannel serverChannel) throws IOException {
        SocketChannel acceptableChannel = serverChannel.accept();
        acceptableChannel.configureBlocking(false);
        acceptableChannel.register(selector, SelectionKey.OP_READ);
    }

    private String readFromClient(SocketChannel clientChannel) throws IOException {
        buffer.clear();
        int readBytes = clientChannel.read(buffer);
        if (readBytes < 0) {
            clientChannel.close();
            return null;
        }
        buffer.flip();
        byte[] clientInputBytes = new byte[buffer.remaining()];
        buffer.get(clientInputBytes);
        return new String(clientInputBytes, StandardCharsets.UTF_8);
    }

    private void sendToClient(SocketChannel clientChannel, String message) throws IOException {
        byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
        ByteBuffer writeBuffer = ByteBuffer.wrap(messageBytes);
        clientChannel.write(writeBuffer);
    }

    public void start() {
        try (ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            configureServer(serverSocketChannel);
            while (true) {
                int readyChannels = selector.select();
                if (readyChannels == 0) {
                    continue;
                }
                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    if (key.isReadable()) {
                        SocketChannel clientChannel = (SocketChannel) key.channel();
                        String message = readFromClient(clientChannel);
                        sendToClient(clientChannel, message);

                    } else if (key.isAcceptable()) {
                        registerClient((ServerSocketChannel) key.channel());
                    }
                    keyIterator.remove();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("There is a problem with the server socket", e);
        }
    }

    public static void main(String[] args) {
        FoodAnalyzerServerNio server = new FoodAnalyzerServerNio();
        server.start();
    }

}

