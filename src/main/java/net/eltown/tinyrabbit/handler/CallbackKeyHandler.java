package net.eltown.tinyrabbit.handler;


import net.eltown.tinyrabbit.data.Request;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CallbackKeyHandler {

    private final Map<String, BiConsumer<String[], Request>> keys;
    private final Runnable errorHandler;

    private CallbackKeyHandler(Map<String, BiConsumer<String[], Request>> keys, Runnable errorHandler) {
        this.keys = keys;
        this.errorHandler = errorHandler;
    }

    public void handle(final String key, final String[] data, final Request request) {

        if (key.equalsIgnoreCase("ERROR")) {
            if (errorHandler != null) {
                errorHandler.run();
            }
            return;
        }

        keys.forEach((s, consumer) -> {
            if (s.equalsIgnoreCase(key)) consumer.accept(data, request);
        });
    }

    public static class Builder {

        final Map<String, BiConsumer<String[], Request>> keys = new HashMap<>();
        Runnable errorHandler = null;

        public CallbackKeyHandler.Builder on(final String key, final BiConsumer<String[], Request> callback) {
            keys.put(key, callback);
            return this;
        }

        public CallbackKeyHandler.Builder onError(final Runnable runnable) {
            this.errorHandler = runnable;
            return this;
        }

        public CallbackKeyHandler build() {
            return new CallbackKeyHandler(keys, errorHandler);
        }

    }

}
