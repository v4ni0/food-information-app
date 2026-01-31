package bg.sofia.uni.fmi.mjt.food.server.retriever.model;

import java.util.List;

public record SearchResponse(List<FoodDetails> foods) {
}
