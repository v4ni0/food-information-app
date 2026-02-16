package bg.sofia.uni.fmi.mjt.food.server;

import bg.sofia.uni.fmi.mjt.food.exceptions.BarcodeNotFoundException;
import bg.sofia.uni.fmi.mjt.food.exceptions.FoodRetrievalException;
import bg.sofia.uni.fmi.mjt.food.exceptions.NoResultsFoundException;
import bg.sofia.uni.fmi.mjt.food.server.retriever.FoodDataRetriever;
import bg.sofia.uni.fmi.mjt.food.server.retriever.model.FoodDetails;
import bg.sofia.uni.fmi.mjt.food.server.retriever.model.FoodReport;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ClientRequestHandlerTest {
    private Socket socket = mock(Socket.class);
    private FoodDataRetriever retriever = mock(FoodDataRetriever.class);
    private Gson gson = new Gson();
    private String reportJSON = """
        {"fdcId":2494378,
        "description":"COLA",
        "ingredients":"CARBONATED WATER",
        "gtinUpc":"012000338960",
        "foodNutrients":[{"nutrient":{"name":"Carbohydrate, by difference","unitName":"g"},"amount":4.22},
                        {"nutrient":{"name":"Protein","unitName":"g"},"amount":0.0},
                        {"nutrient":{"name":"Energy","unitName":"kcal"},"amount":17.0},
                        {"nutrient":{"name":"Total lipid (fat)","unitName":"g"},"amount":0.0}]}
        """;

    @Test
    void testProcessRequestSendsEndMarker() throws IOException, FoodRetrievalException, NoResultsFoundException {
        String input = "get-food apple\n";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(socket.getInputStream()).thenReturn(inputStream);
        when(socket.getOutputStream()).thenReturn(outputStream);

        when(retriever.getFoodByKeywords(any())).thenReturn(List.of());
        ClientRequestHandler handler = new ClientRequestHandler(socket, retriever);
        handler.run();

        String response = outputStream.toString();
        assertTrue(response.contains("END"), "Server must send END after processing command");
    }

    @Test
    void testProcessRequestReturnsReport() throws IOException, FoodRetrievalException, NoResultsFoundException {
        String input = "get-food-report 2494378";
        FoodReport report = gson.fromJson(reportJSON, FoodReport.class);
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        String expected = """
            Name: COLA
            Ingredients: CARBONATED WATER
            Nutritional value per 100g:
             - Carbohydrate, by difference: 4.22 g
             - Protein: 0.00 g
             - Energy: 17.00 kcal
             - Total lipid (fat): 0.00 g
            """.strip();


        when(socket.getInputStream()).thenReturn(inputStream);
        when(socket.getOutputStream()).thenReturn(outputStream);

        when(retriever.getFoodReport(2494378)).thenReturn(report);
        ClientRequestHandler handler = new ClientRequestHandler(socket, retriever);
        handler.run();

        String response = outputStream.toString().strip();
        System.out.println(response);
        System.out.println(expected);
        assertTrue(response.contains("Name: COLA"), "Server response contain the expected report format");
        assertTrue(response.contains("Ingredients: CARBONATED WATER"), "Server response should contain ingredients");
        assertTrue(response.contains("Carbohydrate, by difference: 4.22 g"), "Server response should contain carbohydrate");
        assertTrue(response.contains("Protein: 0.00"), "Server response should contain protein");
        assertTrue(response.contains("Energy: 17.00"), "Server response should contain energy");
        assertTrue(response.contains("Total lipid (fat): 0.00"), "Server response should contain total lipid");
        assertTrue(response.endsWith("END"), "Server response should end with END marker");
    }

    @Test
    void testRunReportCommandNoResults() throws IOException, FoodRetrievalException, NoResultsFoundException {
        String input = "get-food-report 1";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(socket.getInputStream()).thenReturn(inputStream);
        when(socket.getOutputStream()).thenReturn(outputStream);

        when(retriever.getFoodReport(1)).thenThrow(new NoResultsFoundException("No report found"));
        ClientRequestHandler handler = new ClientRequestHandler(socket, retriever);
        handler.run();

        String response = outputStream.toString();
        assertTrue(response.contains("No food found with ID"), "Client should recieve message");

    }

    @Test
    void testRunBarcodeCommandValid()
        throws IOException, FoodRetrievalException, BarcodeNotFoundException {
        String input = "get-food-by-barcode --code=barcode";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes());
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        when(socket.getInputStream()).thenReturn(inputStream);
        when(socket.getOutputStream()).thenReturn(outputStream);

        FoodDetails details = new FoodDetails(1, "1", "1");
        when(retriever.getFoodByBarcode("barcode")).thenReturn(details);
        ClientRequestHandler handler = new ClientRequestHandler(socket, retriever);
        handler.run();

        String response = outputStream.toString().strip();
        assertTrue(response.contains("FoodDetails : fdcId=1,description=1"), "Server response should contain food details");

    }
}
