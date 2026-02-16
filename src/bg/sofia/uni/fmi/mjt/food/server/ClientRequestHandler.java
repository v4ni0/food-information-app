package bg.sofia.uni.fmi.mjt.food.server;

import bg.sofia.uni.fmi.mjt.food.exceptions.BarcodeNotFoundException;
import bg.sofia.uni.fmi.mjt.food.exceptions.FoodRetrievalException;
import bg.sofia.uni.fmi.mjt.food.exceptions.InvalidClientMessageException;
import bg.sofia.uni.fmi.mjt.food.exceptions.NoResultsFoundException;
import bg.sofia.uni.fmi.mjt.food.server.logging.Logger;
import bg.sofia.uni.fmi.mjt.food.server.command.CommandParser;
import bg.sofia.uni.fmi.mjt.food.server.command.model.Command;
import bg.sofia.uni.fmi.mjt.food.server.retriever.FoodDataRetriever;
import bg.sofia.uni.fmi.mjt.food.server.retriever.model.FoodDetails;
import bg.sofia.uni.fmi.mjt.food.server.retriever.model.FoodReport;
import bg.sofia.uni.fmi.mjt.food.validation.Validator;

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
    private final Logger logger = Logger.getInstance();

    public ClientRequestHandler(Socket socket, FoodDataRetriever retriever) {
        Validator.validateNotNull(socket, "Socket cannot be null");
        Validator.validateNotNull(retriever, "FoodDataRetriever cannot be null");
        this.socket = socket;
        this.retriever = retriever;
    }

    private String getClientInfo() {
        return "Client: " + socket.getRemoteSocketAddress();
    }

    private void handleGetFoodCommand(Command command, PrintWriter out) {
        try {
            List<FoodDetails> foods = retriever.getFoodByKeywords(command.keywords());
            if (foods == null || foods.isEmpty()) {
                out.println("No foods found for the given keywords");
            } else {
                for (FoodDetails food : foods) {
                    out.println(food);
                }
            }
        } catch (NoResultsFoundException e) {
            out.println("No foods found for keywords: " + String.join(" ", command.keywords()));
        } catch (FoodRetrievalException e) {
            String additionalInfo = getClientInfo() + ", Keywords: " + String.join(" ", command.keywords());
            System.err.println("Error while retrieving food data: " + e.getMessage());
            logger.log("Unable to retrieve food data", e, additionalInfo);
            out.println("Error while retrieving food. Try again later or contact administrator");
        }
    }

    private void handleGetFoodReportCommand(Command command, PrintWriter out) {
        try {
            FoodReport foodReport = retriever.getFoodReport(command.id());
            if (foodReport == null) {
                out.println("No food found for the given ID");
            } else {
                out.println(foodReport);
            }
        } catch (NoResultsFoundException e) {
            out.println("No food found with ID " + command.id());
        } catch (FoodRetrievalException e) {
            String additionalInfo = getClientInfo() + ", Food ID: " + command.id();
            logger.log("Unable to retrieve food report", e, additionalInfo);
            out.println("Error while retrieving food report. Try again later or contact administrator");
        }
    }

    private void handleGetFoodByBarcodeCommand(Command command, PrintWriter out) {
        try {
            FoodDetails foodDetails = retriever.getFoodByBarcode(command.barcode());
            if (foodDetails == null) {
                out.println("No food found for the given barcode in cache");
            } else {
                out.println(foodDetails);
            }
        } catch (BarcodeNotFoundException e) {
            out.println("Product with barcode " + command.barcode() + " not found in cache");
        } catch (FoodRetrievalException e) {
            String additionalInfo = getClientInfo() + ", Barcode: " + command.barcode();
            logger.log("Unable to retrieve food by barcode", e, additionalInfo);
            out.println("Error while retrieving food by barcode. Try again later or contact administrator");
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
        Thread.currentThread().setName("Client Handler: " + socket.getRemoteSocketAddress());

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
            String errorMessage = "Error handling client connection";
            String additionalInfo = getClientInfo();
            System.err.println(errorMessage + ": " + e.getMessage());
            logger.log(errorMessage, e, additionalInfo);
        }
    }
}