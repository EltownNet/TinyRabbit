package net.eltown.tinyrabbit.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class Delivery {

    @Getter
    private final String key;
    @Getter
    private final String[] data;
    private final boolean error;

    public boolean hasError() {
        return error;
    }

}
