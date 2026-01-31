package bg.sofia.uni.fmi.mjt.food.client;

import bg.sofia.uni.fmi.mjt.food.client.message.MessageParser;
import bg.sofia.uni.fmi.mjt.food.exceptions.BarcodeParsingException;
import bg.sofia.uni.fmi.mjt.food.exceptions.InvalidClientMessageException;
import bg.sofia.uni.fmi.mjt.food.exceptions.UnableToConnectToServerException;

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

    public static void readResponse(BufferedReader reader) throws IOException {
        String response;
        while ((response = reader.readLine()) != null) {
            if (response.equals(END_MARKER)) {
                break;
            }
            System.out.println(response);
        }
    }

    public static void start() throws UnableToConnectToServerException {
        try (Socket client = new Socket(SERVER_HOST, SERVER_PORT);
             PrintWriter writer = new PrintWriter(client.getOutputStream(), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
             Scanner scanner = new Scanner(System.in)) {

            while (true) {
                String message = scanner.nextLine();
                if (message.equalsIgnoreCase(EXIT_MESSAGE)) {
                    break;
                }
                try {
                    message = MessageParser.parseMessage(message);
                } catch (BarcodeParsingException e) {
                    System.out.println("Invalid barcode format, enter message again");
                    continue;
                } catch (InvalidClientMessageException e) {
                    System.out.println("Invalid message format, enter message again");
                    continue;
                }
                System.out.println("Sending to server: " + message);
                writer.println(message);
                readResponse(reader);
            }
        } catch (IOException e) {
            throw new UnableToConnectToServerException("Couldn`t connect to server");
        }
    }

    public static void main() throws UnableToConnectToServerException {
        start();
    }
}