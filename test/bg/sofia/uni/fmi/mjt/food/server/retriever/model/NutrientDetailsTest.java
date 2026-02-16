package bg.sofia.uni.fmi.mjt.food.server.retriever.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NutrientDetailsTest {

    @Test
    void testToString() {
        Nutrient nutrient = new Nutrient("protein", "g");
        NutrientDetails nutrientDetails = new NutrientDetails(nutrient, 10.5);
        System.out.println(nutrientDetails);
        String expected = "protein: 10.50 g";
        assertEquals(expected, nutrientDetails.toString());
    }

}