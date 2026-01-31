package bg.sofia.uni.fmi.mjt.food.server;

import bg.sofia.uni.fmi.mjt.food.server.cache.Cache;
import bg.sofia.uni.fmi.mjt.food.server.retriever.FoodDataRetriever;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.http.HttpClient;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FoodAnalyzerServer {
    private static final int MAX_THREADS = 5;
    public static final int SERVER_PORT = 5000;
    private final ExecutorService executor;
    private final FoodDataRetriever retriever;

    public FoodAnalyzerServer(FoodDataRetriever retriever) {
        this.executor = Executors.newFixedThreadPool(MAX_THREADS);
        this.retriever = retriever;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT)) {
            System.out.println("Server is listening");
            while (true) {
                Socket socket = serverSocket.accept();
                ClientRequestHandler handler = new ClientRequestHandler(socket, retriever);
                executor.execute(handler);
            }
        } catch (IOException e) {
            throw new RuntimeException("Server opening problem occured", e);
        } finally {
            executor.shutdown();
        }
    }

    public static void main() {
        FoodDataRetriever retriever = new FoodDataRetriever("dF6jECphtmorpegIQhALKbafRFakWQV0lhu1WdMo",
                HttpClient.newHttpClient(),
                new Cache());
        FoodAnalyzerServer server = new FoodAnalyzerServer(retriever);
        server.start();

    }
}
