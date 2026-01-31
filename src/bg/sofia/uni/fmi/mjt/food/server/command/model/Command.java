package bg.sofia.uni.fmi.mjt.food.server.command.model;

import java.util.List;

public class Command {
    private final Type type;
    private final List<String> keywords;
    private final int id;
    private final String barcode;

    public Command(CommandBuilder builder) {
        this.type = builder.commandType;
        this.keywords = builder.keywords;
        this.id = builder.id;
        this.barcode = builder.barcode;
    }

    public static CommandBuilder builder(Type type) {
        return new CommandBuilder(type);
    }

    public Type type() {
        return type;
    }

    public List<String> keywords() {
        return keywords;
    }

    public int id() {
        return id;
    }

    public String barcode() {
        return barcode;
    }

    public static class CommandBuilder {
        private final Type commandType;
        private List<String> keywords;
        private int id;
        private String barcode;

        public CommandBuilder(Type commandType) {
            this.commandType = commandType;
        }

        public CommandBuilder setKeywords(List<String> keywords) {
            this.keywords = keywords;
            return this;
        }

        public CommandBuilder setId(int id) {
            this.id = id;
            return this;
        }

        public CommandBuilder setBarcode(String barcode) {
            this.barcode = barcode;
            return this;
        }

        public Command build() {
            return new Command(this);
        }
    }
}


