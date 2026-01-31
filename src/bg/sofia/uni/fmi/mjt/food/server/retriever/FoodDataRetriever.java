package bg.sofia.uni.fmi.mjt.food.server.retriever;

import bg.sofia.uni.fmi.mjt.food.exceptions.MissingBarcodeException;
import bg.sofia.uni.fmi.mjt.food.server.cache.Cache;
import bg.sofia.uni.fmi.mjt.food.server.retriever.model.FoodDetails;
import bg.sofia.uni.fmi.mjt.food.server.retriever.model.FoodReport;
import bg.sofia.uni.fmi.mjt.food.server.retriever.model.SearchResponse;
import com.google.gson.Gson;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class FoodDataRetriever {
    private final Gson gson;
    private final String apiKey;
    private final HttpClient client;
    private final Cache cache;
    private static final String SEARCH_ENDPOINT = "https://api.nal.usda.gov/fdc/v1/foods/search?";
    private static final String REPORT_ENDPOINT = "https://api.nal.usda.gov/fdc/v1/food/";
    private static final String KEYWORD_SEPARATOR = "%20";
    private static final String API_KEY_STR = "api_key=";
    private static final int GOOD_STATUS_CODE = 200;

    public FoodDataRetriever(String apiKey, HttpClient client, Cache cache) {
        this.gson = new Gson();
        this.apiKey = apiKey;
        this.client = client;
        this.cache = cache;
    }

    private URI createUriForFoodId(int id) {
        String uri = REPORT_ENDPOINT + id + "?" + API_KEY_STR + apiKey;
        return URI.create(uri);
    }

    private URI createUriForKeywords(List<String> keywords) {
        String uri = SEARCH_ENDPOINT + API_KEY_STR + apiKey + "&query=" + String.join(KEYWORD_SEPARATOR, keywords);
        return URI.create(uri);
    }

    private FoodReport getCachedReport(int id) {
        String json = cache.loadReport(id);
        if (json != null) {
            FoodReport report = gson.fromJson(json, FoodReport.class);
            report.filterNutrients();
            return report;
        }
        return null;
    }

    public FoodReport getFoodReport(int id) throws Exception {
        FoodReport cachedReport = getCachedReport(id);
        if (cachedReport != null) {
            return cachedReport;
        }
        URI uri = createUriForFoodId(id);
        HttpRequest request = HttpRequest.newBuilder().uri(uri).build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != GOOD_STATUS_CODE) {
            throw new RuntimeException("Error from the API: " + response.statusCode());
        }
        cache.saveReport(id, response.body());
        FoodReport report = gson.fromJson(response.body(), FoodReport.class);
        report.filterNutrients();
        return report;
    }

    public List<FoodDetails> getFoodByKeywords(List<String> keywords) throws IOException, InterruptedException {
        URI uri = createUriForKeywords(keywords);

        HttpRequest request = HttpRequest.newBuilder().uri(uri).build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != GOOD_STATUS_CODE) {
            throw new RuntimeException("Error from the API: " + response.statusCode());
        }

        SearchResponse searchResponse = gson.fromJson(response.body(), SearchResponse.class);
        List<FoodDetails> foods = searchResponse.foods();
        if (foods != null) {
            for (FoodDetails fd : foods) {
                if (fd != null && fd.gtinUpc() != null && !fd.gtinUpc().isBlank()) {
                    cache.saveBarcode(fd.gtinUpc(), gson.toJson(fd));
                }
            }
        }
        return searchResponse.foods();
    }

    public FoodDetails getFoodByBarcode(String barcode) throws Exception {
        String json = cache.loadBarcode(barcode);
        if (json == null) {
            throw new MissingBarcodeException("Barcode not found in cache");
        }
        FoodDetails foodDetails = gson.fromJson(json, FoodDetails.class);
        return foodDetails;
    }
}

