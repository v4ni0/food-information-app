package bg.sofia.uni.fmi.mjt.food.server.retriever.model;

import java.util.List;
import java.util.Set;

public class FoodReport {
    private int fdcId;
    private String description;
    private String ingredients;
    private String gtinUpc;
    private List<NutrientDetails> foodNutrients;
    private static final Set<String> WANTED_NUTRIENTS = Set.of(
        "protein",
        "total lipid",
        "carbohydrate",
        "fiber",
        "energy"
    );
    public FoodReport() {
    }

    public int fdcId() {
        return fdcId;
    }

    public String description() {
        return description;
    }

    public String ingredients() {
        return ingredients;
    }

    public String gtinUpc() {
        return gtinUpc;
    }

    public List<NutrientDetails> foodNutrients() {
        return foodNutrients;
    }

    private boolean isWantedNutrient(NutrientDetails nutrientDetails) {
        if (nutrientDetails == null || nutrientDetails.nutrient().name() == null) {
            return false;
        }

        String nutrientName = nutrientDetails.nutrient().name().toLowerCase();
        return WANTED_NUTRIENTS
            .stream()
            .anyMatch(wantedNutrient -> nutrientName.contains(wantedNutrient));
    }

    public void filterNutrients() {
        this.foodNutrients = foodNutrients.stream()
            .filter(this::isWantedNutrient)
            .toList();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ").append(description == null ? "" : description).append(System.lineSeparator());
        sb.append("Ingredients: ").append(ingredients == null ? "" : ingredients).append(System.lineSeparator());
        sb.append("Nutritional value per 100g:").append(System.lineSeparator());
        for (NutrientDetails nutrient : foodNutrients) {
            sb.append(" - ").append(nutrient.toString()).append(System.lineSeparator());
        }
        return sb.toString();
    }
}
