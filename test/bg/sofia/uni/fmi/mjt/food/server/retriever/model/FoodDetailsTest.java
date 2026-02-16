package bg.sofia.uni.fmi.mjt.food.server.retriever.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FoodDetailsTest {

    @Test
    void testToStringWithAllFields() {
        FoodDetails food = new FoodDetails(
            415269,
            "rafaelo",
            "0"
        );
        String result = food.toString();
        String expected = """
            FoodDetails : fdcId=415269,description=rafaelo
            """;
        System.out.println(result);
        assertEquals(expected, result, "Strings are not equal");

    }


}