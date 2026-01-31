package bg.sofia.uni.fmi.mjt.food.server.retriever.model;

public record NutrientDetails(Nutrient nutrient, double amount) {
    @Override
    public String toString() {
        return String.format("%s: %.2f %s",
            nutrient.name(),
            amount,
            nutrient.unitName());
    }
}
