package bg.sofia.uni.fmi.mjt.food.server;

import bg.sofia.uni.fmi.mjt.food.exceptions.InvalidClientMessageException;
import bg.sofia.uni.fmi.mjt.food.server.command.CommandParser;
import bg.sofia.uni.fmi.mjt.food.server.command.model.Command;
import bg.sofia.uni.fmi.mjt.food.server.retriever.FoodDataRetriever;
import bg.sofia.uni.fmi.mjt.food.server.retriever.model.FoodDetails;
import bg.sofia.uni.fmi.mjt.food.server.retriever.model.FoodReport;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.List;

public class ClientRequestHandler implements Runnable {
    private static final String EXIT_COMMAND = "exit";

    private final Socket socket;
    private final FoodDataRetriever retriever;

    public ClientRequestHandler(Socket socket, FoodDataRetriever retriever) {
        this.socket = socket;
        this.retriever = retriever;
    }

    private void handleGetFoodCommand(Command command, PrintWriter out) {
        try {
            List<FoodDetails> foods = retriever.getFoodByKeywords(command.keywords());
            if (foods == null || foods.isEmpty()) {
                out.println("No foods found for the given keywords");
            } else {
                for (FoodDetails food : foods) {
                    out.println(food.toString());
                }
            }
        } catch (IOException | InterruptedException e) {
            out.println("Error retrieving food data: " + e.getMessage());
        }
    }

    private void handleGetFoodReportCommand(Command command, PrintWriter out) {
        try {
            FoodReport foodReport = retriever.getFoodReport(command.id());
            if (foodReport == null) {
                out.println("No food found for the given ID");
            } else {
                out.println(foodReport.toString());
            }
        } catch (Exception e) {
            out.println("Error retrieving food report: " + e.getMessage());
        }
    }

    private void handleGetFoodByBarcodeCommand(Command command, PrintWriter out) {
        try {
            FoodDetails foodDetails = retriever.getFoodByBarcode(command.barcode());
            if (foodDetails == null) {
                out.println("No food found for the given barcode in cache");
            } else {
                out.println(foodDetails.toString());
            }
        } catch (Exception e) {
            out.println("Product with barcode " + command.barcode() + " not found in cache");
        }
    }

    private void processRequest(String line, PrintWriter out) {
        Command command;
        try {
            command = CommandParser.parse(line);
        } catch (InvalidClientMessageException e) {
            out.println("Invalid command: " + e.getMessage());
            out.println("END");
            return;
        }

        switch (command.type()) {
            case GET_FOOD -> handleGetFoodCommand(command, out);
            case GET_FOOD_REPORT -> handleGetFoodReportCommand(command, out);
            case GET_FOOD_BY_BARCODE -> handleGetFoodByBarcodeCommand(command, out);
        }
        out.println("END");
    }

    @Override
    public void run() {
        Thread.currentThread().setName("Client Request Handler for " + socket.getRemoteSocketAddress());

        try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             socket) {

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                if (inputLine.equalsIgnoreCase(EXIT_COMMAND)) {
                    out.println("Connection closed");
                    break;
                }
                processRequest(inputLine, out);
            }
        } catch (IOException e) {
            System.err.println("Error handling client: " + e.getMessage());
        }
    }
}