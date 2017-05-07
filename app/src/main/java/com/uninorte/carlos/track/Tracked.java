package com.uninorte.carlos.track;

import java.util.ArrayList;

/**
 * Created by carlos on 7/05/17.
 */

public class Tracked {
    private static ArrayList<String> track = new ArrayList<>();

    public static void addElemento(String string) {
        track.add(string);
    }

    public static ArrayList<String> getTrack() {
        return track;
    }
}
