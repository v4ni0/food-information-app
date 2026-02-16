package bg.sofia.uni.fmi.mjt.food.server;

import bg.sofia.uni.fmi.mjt.food.server.logging.Logger;
import bg.sofia.uni.fmi.mjt.food.server.cache.Cache;
import bg.sofia.uni.fmi.mjt.food.server.retriever.FoodDataRetriever;
import bg.sofia.uni.fmi.mjt.food.validation.Validator;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FoodAnalyzerServer {
    private static final int MAX_THREADS = 5;
    public static final int SERVER_PORT = 5000;
    private final ExecutorService executor;
    private final FoodDataRetriever retriever;
    private static final Logger LOGGER = Logger.getInstance();

    public FoodAnalyzerServer(FoodDataRetriever retriever) {
        Validator.validateNotNull(retriever, "FoodDataRetriever cannot be null");
        this.executor = Executors.newFixedThreadPool(MAX_THREADS);
        this.retriever = retriever;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Server is listening on port " + SERVER_PORT);
            while (true) {
                try {
                    Socket socket = serverSocket.accept();
                    System.out.println("Client connected: " + socket.getRemoteSocketAddress());
                    ClientRequestHandler handler = new ClientRequestHandler(socket, retriever);
                    executor.execute(handler);
                } catch (IOException e) {
                    String errorMessage = "Error accepting client connection";
                    System.err.println(errorMessage);
                    LOGGER.log(errorMessage, e);
                }
            }
        } catch (IOException e) {
            String errorMessage = "Server failed to start on port " + SERVER_PORT;
            System.err.println(errorMessage);
            LOGGER.log(errorMessage, e);
            throw new RuntimeException("Server opening problem occurred", e);
        } finally {
            executor.shutdown();
        }
    }

    public static String readKeyFromFile(Path filePath) throws IOException {
        return Files.readString(filePath).strip();
    }

    public static void main(String[] args) throws IOException {
        String key;
        try {
            key = readKeyFromFile(Path.of("D:/IntelliJ/java/Food Analyzer/FoodAnalzerKey.txt"));
            System.out.println("API Key read successfully");
        } catch (IOException e) {
            LOGGER.log("Couldn`t read key from file", e);
            System.err.println("Error reading API key from file");
            throw e;
        }
        FoodDataRetriever retriever = new FoodDataRetriever(key, HttpClient.newHttpClient(), Cache.getInstance());
        FoodAnalyzerServer server = new FoodAnalyzerServer(retriever);
        server.start();

    }
}
