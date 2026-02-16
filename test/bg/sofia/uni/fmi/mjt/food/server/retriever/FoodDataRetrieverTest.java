package bg.sofia.uni.fmi.mjt.food.server.retriever;

import bg.sofia.uni.fmi.mjt.food.exceptions.BarcodeNotFoundException;
import bg.sofia.uni.fmi.mjt.food.exceptions.FoodRetrievalException;
import bg.sofia.uni.fmi.mjt.food.exceptions.NoResultsFoundException;
import bg.sofia.uni.fmi.mjt.food.server.cache.Cache;
import bg.sofia.uni.fmi.mjt.food.server.retriever.model.FoodDetails;
import bg.sofia.uni.fmi.mjt.food.server.retriever.model.FoodReport;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class FoodDataRetrieverTest {
    private HttpClient client = mock(HttpClient.class);
    private Cache cache = mock(Cache.class);
    private Gson gson = new Gson();
    private String reportJSON = """
        {"fdcId":2494378,
        "description":"COLA",
        "ingredients":"CARBONATED WATER, HIGH FRUCTOSE CORN SYRUP, CARAMEL COLOR, SUGAR, PHOSPHORIC ACID, SODIUM CITRATE, NATURAL FLAVOR, CAFFEINE, POTASSIUM SORBATE (PRESERVES FRESHNESS), SUCRALOSE, CITRIC ACID, ACESULFAME POTASSIUM.",
        "gtinUpc":"012000338960",
        "foodNutrients":[{"nutrient":{"name":"Carbohydrate, by difference","unitName":"g"},"amount":4.22},
                        {"nutrient":{"name":"Sodium, Na","unitName":"mg"},"amount":17.0},
                        {"nutrient":{"name":"Total Sugars","unitName":"g"},"amount":4.22},
                        {"nutrient":{"name":"Protein","unitName":"g"},"amount":0.0},
                        {"nutrient":{"name":"Energy","unitName":"kcal"},"amount":17.0},
                        {"nutrient":{"name":"Total lipid (fat)","unitName":"g"},"amount":0.0}]}
        """;

    private String keywordJson = """
        {"foods":[{"fdcId":415269,"description":"RAFFAELLO, ALMOND COCONUT TREAT","gtinUpc":"009800146130"},
                  {"fdcId":123456,"description":"RAFFAELLO, CHOCOLATE TREAT","gtinUpc":"009800146131"}]}
        """;


    @Test
    void testConstructorNullClientThrows() {
        assertThrows(IllegalArgumentException.class, () -> new FoodDataRetriever("string", null, null));
    }

    @Test
    void testConstructorNullApiKeyThrows() {
        assertThrows(IllegalArgumentException.class, () -> new FoodDataRetriever(null, client, null));
    }

    @Test
    void testConstructorNullCacheThrows() {
        assertThrows(IllegalArgumentException.class, () -> new FoodDataRetriever("string", client, null));
    }

    @Test
    void testGetReportByIdNullThrows() {
        FoodDataRetriever retriever = new FoodDataRetriever("string", client, cache);
        assertThrows(IllegalArgumentException.class, () -> retriever.getFoodReport(-1), "Negative ID should throw");
    }

    @Test
    void testGetReportRetrievesFromCache() throws IOException, FoodRetrievalException, NoResultsFoundException {
        when(cache.loadReport(2494378)).thenReturn(reportJSON);
        FoodDataRetriever retriever = new FoodDataRetriever("string", client, cache);
        FoodReport report = retriever.getFoodReport(2494378);
        assertEquals(2494378, report.fdcId(), "FDC ID should match");
        assertEquals("COLA", report.description(), "Description should match");
        assertEquals("012000338960", report.gtinUpc(), "Barcode should match");
        assertTrue(report.ingredients().contains("CARBONATED WATER"),
            "Ingredients should contain expected text");
        assertEquals(4, report.foodNutrients().size(),
            "Food nutrients should be 4, because we only want energy, protein, carbs and fat");
    }

    @Test
    void testGetReportHttpClient()
        throws IOException, InterruptedException, FoodRetrievalException, NoResultsFoundException {
        when(cache.loadReport(2494378)).thenReturn(null);
        HttpResponse response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(reportJSON);
        when(client.send(any(), any())).thenReturn(response);
        FoodDataRetriever retriever = new FoodDataRetriever("string", client, cache);
        FoodReport report = retriever.getFoodReport(2494378);
        assertEquals(2494378, report.fdcId(), "FDC ID should match");
        assertEquals("COLA", report.description(), "Description should match");
        assertEquals("012000338960", report.gtinUpc(), "Barcode should match");
        assertTrue(report.ingredients().contains("CARBONATED WATER"),
            "Ingredients should contain text");
        assertEquals(4, report.foodNutrients().size(),
            "Food nutrients should be 4, because we only want energy, protein, carbs and fat");
    }

    @Test
    void testGetReportNotFoundThrows() throws IOException, InterruptedException {
        when(cache.loadReport(2494378)).thenReturn(null);
        HttpResponse response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(404);
        when(client.send(any(), any())).thenReturn(response);
        FoodDataRetriever retriever = new FoodDataRetriever("string", client, cache);
        assertThrows(NoResultsFoundException.class, () -> retriever.getFoodReport(2494378),
            "Should throw NoResultsFoundException when code is 404");
    }

    @Test
    void testGetReportOtherCodeThrows() throws IOException, InterruptedException {
        when(cache.loadReport(2494378)).thenReturn(null);
        HttpResponse response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(400);
        when(client.send(any(), any())).thenReturn(response);
        FoodDataRetriever retriever = new FoodDataRetriever("string", client, cache);
        assertThrows(FoodRetrievalException.class, () -> retriever.getFoodReport(2494378),
            "Should throw FoodRetrievalException when code is other");
    }

    @Test
    void testGetReportThrowsWhenInterruptedException() throws IOException, InterruptedException {
        when(cache.loadReport(1)).thenReturn(null);
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenThrow(new InterruptedException("interrupt"));
        FoodDataRetriever retriever = new FoodDataRetriever("key", client, cache);

        assertThrows(FoodRetrievalException.class, () -> retriever.getFoodReport(1),
            "Should throw FoodRetrievalException on interrupted exception");
    }

    @Test
    void testGetReportErrorWithGettingFromCacheThrows() throws IOException {
        when(cache.loadReport(1)).thenThrow(new IOException("Cache error"));
        FoodDataRetriever retriever = new FoodDataRetriever("key", client, cache);
        assertThrows(FoodRetrievalException.class, () -> retriever.getFoodReport(1),
            "Should throw FoodRetrievalException when cache throws IOException");
    }

    @Test
    void testGetFoodByKeywordsRetrievesFromCache() throws IOException, FoodRetrievalException, NoResultsFoundException {
        when(cache.loadByKeywords("raffaello treat")).thenReturn(keywordJson);

        FoodDataRetriever retriever = new FoodDataRetriever("testKey", client, cache);
        List<FoodDetails> foods = retriever.getFoodByKeywords(List.of("raffaello", "treat"));
        assertEquals(2, foods.size(), "Should return two foods");
        assertEquals(415269, foods.get(0).fdcId(), "First food FDC ID should match");
        assertEquals("RAFFAELLO, ALMOND COCONUT TREAT", foods.get(0).description(), "Description should match");
    }

    @Test
    void testGetFoodByKeywordsRetrievesFromAPI() throws IOException, InterruptedException, FoodRetrievalException, NoResultsFoundException {
        when(cache.loadByKeywords("rafaelo")).thenReturn(null);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(keywordJson);
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        FoodDataRetriever retriever = new FoodDataRetriever("key", client, cache);
        List<FoodDetails> foods = retriever.getFoodByKeywords(List.of("rafaelo"));
        assertNotNull(foods, "Foods list should not be null");
        assertEquals(2, foods.size(), "Should return one food");
        assertEquals(415269, foods.get(0).fdcId(), "FDC ID should match");
    }

    @Test
    void testGetFoodByKeywordsThrowsNoResultsFoundException() throws IOException, InterruptedException {
        when(cache.loadByKeywords("food")).thenReturn(null);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(404);
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        FoodDataRetriever retriever = new FoodDataRetriever("key", client, cache);

        assertThrows(NoResultsFoundException.class, () -> retriever.getFoodByKeywords(List.of("food")),
            "Should throw NoResultsFoundException for 404 code");
    }

    @Test
    void testGetFoodByKeywordsThrowsFoodRetrievalExceptionForOtherCode() throws IOException, InterruptedException {
        when(cache.loadByKeywords("test")).thenReturn(null);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(500);
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        FoodDataRetriever retriever = new FoodDataRetriever("key", client, cache);

        assertThrows(FoodRetrievalException.class, () -> retriever.getFoodByKeywords(List.of("test")),
            "Should throw FoodRetrievalException for other errors");
    }

    @Test
    void testGetFoodByKeywordsWithNullKeywordsThrowsException() {
        FoodDataRetriever retriever = new FoodDataRetriever("testKey", client, cache);

        assertThrows(IllegalArgumentException.class, () -> retriever.getFoodByKeywords(null),
            "Should throw IllegalArgumentException for null keywords");
    }

    @Test
    void testGetFoodByKeywordsSavesMultipleBarcodes() throws IOException, InterruptedException, FoodRetrievalException, NoResultsFoundException {
        when(cache.loadByKeywords("test")).thenReturn(null);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(keywordJson);
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        FoodDataRetriever retriever = new FoodDataRetriever("testKey", client, cache);
        retriever.getFoodByKeywords(List.of("test"));

        verify(cache).saveByBarcode(eq("009800146130"), anyString());
        verify(cache).saveByBarcode(eq("009800146131"), anyString());
    }

    @Test
    void testGetFoodByKeywordsWithMultipleKeywords() throws IOException, InterruptedException, FoodRetrievalException, NoResultsFoundException {
        String searchResponse = """
            {"foods":[{"fdcId":1,"description":"Beef Noodle Soup","gtinUpc":"999999"}]}
            """;
        when(cache.loadByKeywords("beef noodle soup")).thenReturn(null);
        HttpResponse<String> response = mock(HttpResponse.class);
        when(response.statusCode()).thenReturn(200);
        when(response.body()).thenReturn(searchResponse);
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class))).thenReturn(response);

        FoodDataRetriever retriever = new FoodDataRetriever("key", client, cache);
        List<FoodDetails> foods = retriever.getFoodByKeywords(List.of("beef", "noodle", "soup"));

        verify(cache).loadByKeywords("beef noodle soup");
        verify(cache).saveByKeywords(eq("beef noodle soup"), anyString());
        assertEquals(1, foods.size(), "Should return one food");
    }

    @Test
    void testGetFoodByBarcodeRetrievesFromCache() throws IOException, FoodRetrievalException, BarcodeNotFoundException {
        String barcodeJson = """
            {"fdcId":415269,"description":"RAFFAELLO","gtinUpc":"009800146130"}
            """;
        when(cache.loadBarcode("009800146130")).thenReturn(barcodeJson);

        FoodDataRetriever retriever = new FoodDataRetriever("key", client, cache);
        FoodDetails food = retriever.getFoodByBarcode("009800146130");
        assertEquals(415269, food.fdcId(), "ID should match");
        assertEquals("RAFFAELLO", food.description(), "Description should match");
        assertEquals("009800146130", food.gtinUpc(), "Barcode should match");
    }

    @Test
    void testGetFoodByBarcodeNotFoundThrows() throws IOException {
        when(cache.loadBarcode("0")).thenReturn(null);
        FoodDataRetriever retriever = new FoodDataRetriever("key", client, cache);
        assertThrows(BarcodeNotFoundException.class, () -> retriever.getFoodByBarcode("0"),
            "Should throw when barcode not in cache");
    }

    @Test
    void testGetFoodByBarcodeWithNullBarcodeThrowsException() {
        FoodDataRetriever retriever = new FoodDataRetriever("testKey", client, cache);

        assertThrows(IllegalArgumentException.class, () -> retriever.getFoodByBarcode(null),
            "Should throw IllegalArgumentException for null barcode");
    }

    @Test
    void testGetFoodByBarcodeWithBlankBarcodeThrows() {
        FoodDataRetriever retriever = new FoodDataRetriever("key", client, cache);
        assertThrows(IllegalArgumentException.class, () -> retriever.getFoodByBarcode("   "),
            "Should throw IllegalArgumentException for blank barcode");
    }

    @Test
    void testGetFoodByBarcodeErrorWithGettingFromCacheThrows() throws IOException {
        FoodDataRetriever retriever = new FoodDataRetriever("key", client, cache);
        when(cache.loadBarcode("009800146130")).thenThrow(new IOException("Cache error"));
        assertThrows(FoodRetrievalException.class, () -> retriever.getFoodByBarcode("009800146130"),
            "Should throw FoodRetrievalException when cache throws IOException");
    }

    @Test
    void testGetFoodByKeywordsThrowsOnInterruptedException() throws IOException, InterruptedException {
        when(cache.loadByKeywords("test")).thenReturn(null);
        when(client.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenThrow(new InterruptedException("Thread interrupted"));
        FoodDataRetriever retriever = new FoodDataRetriever("testKey", client, cache);

        assertThrows(FoodRetrievalException.class, () -> retriever.getFoodByKeywords(List.of("test")),
            "Should throw FoodRetrievalException when interrupted");
    }

}

