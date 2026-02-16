package bg.sofia.uni.fmi.mjt.food.client;

import bg.sofia.uni.fmi.mjt.food.client.logging.Logger;
import bg.sofia.uni.fmi.mjt.food.client.parsing.message.MessageParser;
import bg.sofia.uni.fmi.mjt.food.exceptions.BarcodeParsingException;
import bg.sofia.uni.fmi.mjt.food.exceptions.InvalidClientMessageException;
import bg.sofia.uni.fmi.mjt.food.validation.Validator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class FoodAnalyzerClient {
    private static final int SERVER_PORT = 5000;
    private static final String SERVER_HOST = "localhost";
    private static final String EXIT_MESSAGE = "exit";
    private static final String END_MARKER = "END";
    private static final Logger LOGGER = Logger.getInstance();

    private static void readResponse(BufferedReader reader) throws IOException {
        Validator.validateNotNull(reader, "Reader cannot be null");
        String response;
        while ((response = reader.readLine()) != null) {
            if (response.equals(END_MARKER)) {
                break;
            }
            System.out.println(response);
        }
    }

    private static void processUserInput(Scanner scanner, PrintWriter writer, BufferedReader reader) {
        while (true) {
            System.out.print("> ");
            String message = scanner.nextLine();

            if (message.equalsIgnoreCase(EXIT_MESSAGE)) {
                writer.println(EXIT_MESSAGE);
                System.out.println("Disconnecting from server...");
                break;
            }
            try {
                message = MessageParser.parse(message);
                writer.println(message);
                readResponse(reader);
            } catch (InvalidClientMessageException e) {
                System.out.println("Invalid command format, please check input and try again.");
            } catch (BarcodeParsingException e) {
                System.out.println("Error parsing barcode, check image path");
                LOGGER.log("Error parsing barcode for message: " + message, e);
            } catch (IOException e) {
                System.err.println("Connection error occurred while communicating with server.");
                LOGGER.log("Error during server communication for command: " + message, e);
            }
        }
    }

    public static void start() {
        try (Socket client = new Socket(SERVER_HOST, SERVER_PORT);
             PrintWriter writer = new PrintWriter(client.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connected to Food Analyzer Server");
            System.out.println("Enter commands (type 'exit' to quit):");
            System.out.println("""
                Examples: get-food beef noodle soup
                get-food-report 415269
                get-food-by-barcode --img=D:/IntelliJ/java/Food Analyzer/barcodeImages/pepsi.jpg""");

            processUserInput(scanner, writer, reader);
        } catch (IOException e) {
            String userMessage = "Unable to connect to the server at " + SERVER_HOST + ":" + SERVER_PORT;
            System.err.println(userMessage);
            LOGGER.log("Failed to connect to server at port" + SERVER_PORT, e);
        }
    }

    public static void main(String[] args) {
        start();
    }
}