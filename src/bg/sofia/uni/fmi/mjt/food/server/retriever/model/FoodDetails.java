package bg.sofia.uni.fmi.mjt.food.server.retriever.model;

public record FoodDetails(int fdcId, String description, String gtinUpc) {

    @Override
    public String toString() {
        return String.format("""
            FoodDetails : fdcId=%d,description='%s',
            """, fdcId, description);
    }
}
