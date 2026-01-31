package bg.sofia.uni.fmi.mjt.food.server.command.model;

public enum Type {
    GET_FOOD("get-food"),
    GET_FOOD_REPORT("get-food-report"),
    GET_FOOD_BY_BARCODE("get-food-by-barcode");
    private final String value;

    Type(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
