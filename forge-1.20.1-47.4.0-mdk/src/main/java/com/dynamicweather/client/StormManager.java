package com.dynamicweather.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class StormManager {
    private static final List<StormCell> storms = new ArrayList<>();

    public static void tickStorms() {
        Iterator<StormCell> it = storms.iterator();
        while (it.hasNext()) {
            StormCell storm = it.next();
            storm.tick();
            if (storm.isExpired()) {
                it.remove();
            }
        }
    }

    public static void spawnStorm(StormCell storm) {
        storms.add(storm);
    }

    public static void clear() {
        storms.clear();
    }
}
