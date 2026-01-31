import bg.sofia.uni.fmi.mjt.food.server.cache.Cache;
import bg.sofia.uni.fmi.mjt.food.server.retriever.model.FoodReport;
import bg.sofia.uni.fmi.mjt.food.server.retriever.FoodDataRetriever;
import bg.sofia.uni.fmi.mjt.food.server.retriever.model.FoodDetails;

import java.net.http.HttpClient;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        String apiKey = "dF6jECphtmorpegIQhALKbafRFakWQV0lhu1WdMo";

        HttpClient client = HttpClient.newHttpClient();
        Cache cache = new Cache();
        FoodDataRetriever retriever = new FoodDataRetriever(apiKey, client, cache);

        List<FoodDetails> foods = retriever.getFoodByKeywords(List.of("raffaello", "treat"));
        if (foods.isEmpty()) {
            System.out.println("No foods found for the given keywords.");
        } else {
            System.out.println("Search results:");
            for (FoodDetails fd : foods) {
                System.out.printf(fd.toString());
            }

            FoodReport report = retriever.getFoodReport(2543615);
            System.out.println();
            System.out.println("Detailed report for first result:");
            System.out.println(report.toString());
        }
    }
}
