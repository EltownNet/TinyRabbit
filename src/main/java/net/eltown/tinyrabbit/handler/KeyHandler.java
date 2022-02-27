package net.eltown.tinyrabbit.handler;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class KeyHandler {

    private final Map<String, Consumer<String[]>> keys;

    private KeyHandler(Map<String, Consumer<String[]>> keys) {
        this.keys = keys;
    }

    public void handle(final String key, final String[] data) {
        keys.forEach((s, consumer) -> {
            if (s.equalsIgnoreCase(key)) consumer.accept(data);
        });
    }

    public static class Builder {


        final Map<String, Consumer<String[]>> keys = new HashMap<>();

        public KeyHandler.Builder on(final String key, final Consumer<String[]> callback) {
            keys.put(key, callback);
            return this;
        }

        public KeyHandler build() {
            return new KeyHandler(keys);
        }

    }


}
