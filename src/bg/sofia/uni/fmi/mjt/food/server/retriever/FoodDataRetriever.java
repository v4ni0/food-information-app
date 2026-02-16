package bg.sofia.uni.fmi.mjt.food.server.retriever;

import bg.sofia.uni.fmi.mjt.food.exceptions.FoodRetrievalException;
import bg.sofia.uni.fmi.mjt.food.exceptions.BarcodeNotFoundException;
import bg.sofia.uni.fmi.mjt.food.exceptions.NoResultsFoundException;
import bg.sofia.uni.fmi.mjt.food.server.cache.Cache;
import bg.sofia.uni.fmi.mjt.food.server.retriever.model.FoodDetails;
import bg.sofia.uni.fmi.mjt.food.server.retriever.model.FoodReport;
import bg.sofia.uni.fmi.mjt.food.server.retriever.model.SearchResponse;
import bg.sofia.uni.fmi.mjt.food.validation.Validator;
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
    private static final int NO_RESULTS_FOUND_CODE = 404;

    public FoodDataRetriever(String apiKey, HttpClient client, Cache cache) {
        Validator.validateString(apiKey, "API key cannot be null or blank");
        Validator.validateNotNull(client, "Http client cannot be null");
        Validator.validateNotNull(cache, "Cache cannot be null");
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

    private FoodReport getCachedReport(int id) throws FoodRetrievalException {
        String json;
        try {
            json = cache.loadReport(id);
        } catch (IOException e) {
            throw new FoodRetrievalException("Error while loading report from cache for id: " + id, e);
        }
        if (json != null) {
            FoodReport report = gson.fromJson(json, FoodReport.class);
            report.filterNutrients();
            return report;
        }
        return null;
    }

    public FoodReport getFoodReport(int id)
        throws FoodRetrievalException, NoResultsFoundException {
        Validator.validateNumberNonNegative(id, "Food ID cannot be negative");
        FoodReport cachedReport = getCachedReport(id);
        if (cachedReport != null) {
            return cachedReport;
        }
        URI uri = createUriForFoodId(id);
        HttpRequest request = HttpRequest.newBuilder().uri(uri).build();
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new FoodRetrievalException("Error while retrieving food with id: " + id, e);
        }
        switch (response.statusCode()) {
            case GOOD_STATUS_CODE -> {
                FoodReport report = gson.fromJson(response.body(), FoodReport.class);
                try {
                    cache.saveReport(id, gson.toJson(report));
                } catch (IOException e) {
                    throw new FoodRetrievalException("Error while saving report to cache for id: " + id, e);
                }
                report.filterNutrients();
                return report;
            }
            case NO_RESULTS_FOUND_CODE -> throw new NoResultsFoundException("No food found with id: " + id);
            default -> throw new FoodRetrievalException(
                "Couldnt retrieve food with id: " + id + " status code: " + response.statusCode());
        }
    }

    private void handleStatusCode(int statusCode, List<String> keywords)
        throws FoodRetrievalException, NoResultsFoundException {
        if (statusCode == NO_RESULTS_FOUND_CODE) {
            throw new NoResultsFoundException("No food found with keywords: " + String.join(" ", keywords));
        }
        if (statusCode != GOOD_STATUS_CODE) {
            throw new FoodRetrievalException(
                "Couldnt retrieve food with keywords: " + String.join(" ", keywords) + " status code: " +
                    statusCode);
        }
    }

    private List<FoodDetails> getCachedKeywordSearch(String keywords) throws FoodRetrievalException {
        try {
            String cachedJson = cache.loadByKeywords(keywords);
            if (cachedJson != null) {
                SearchResponse cached = gson.fromJson(cachedJson, SearchResponse.class);
                return cached.foods();
            }
        } catch (IOException e) {
            throw new FoodRetrievalException("Error loading keywords from cache: " + keywords, e);
        }
        return null;
    }

    private void saveFoodsToCache(String keywords, SearchResponse response)
        throws FoodRetrievalException {
        try {
            String responseJSON = gson.toJson(response);
            cache.saveByKeywords(keywords, responseJSON);
            for (FoodDetails food : response.foods()) {
                if (food.gtinUpc() != null) {
                    cache.saveByBarcode(food.gtinUpc(), gson.toJson(food));
                }
            }
        } catch (IOException e) {
            throw new FoodRetrievalException("Error saving keywords to cache: " + keywords, e);
        }
    }

    public List<FoodDetails> getFoodByKeywords(List<String> keywords)
        throws FoodRetrievalException, NoResultsFoundException {
        Validator.validateNotNull(keywords, "Keywords cannot be null");
        String keywordKey = String.join(" ", keywords);

        List<FoodDetails> cached = getCachedKeywordSearch(keywordKey);
        if (cached != null) {
            return cached;
        }

        URI uri = createUriForKeywords(keywords);
        HttpRequest request = HttpRequest.newBuilder().uri(uri).build();
        HttpResponse<String> response;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            throw new FoodRetrievalException("Error retrieving food with keywords: " + keywordKey, e);
        }
        handleStatusCode(response.statusCode(), keywords);

        SearchResponse searchResponse = gson.fromJson(response.body(), SearchResponse.class);
        saveFoodsToCache(keywordKey, searchResponse);
        return searchResponse.foods();
    }

    public FoodDetails getFoodByBarcode(String barcode) throws BarcodeNotFoundException, FoodRetrievalException {
        Validator.validateString(barcode, "Barcode cannot be null or blank");
        try {
            String json;
            json = cache.loadBarcode(barcode);
            if (json == null) {
                throw new BarcodeNotFoundException("Barcode not found in cache" + barcode);
            }
            return gson.fromJson(json, FoodDetails.class);
        } catch (IOException e) {
            throw new FoodRetrievalException("Error loading barcode from cache: " + barcode, e);
        }
    }
}

