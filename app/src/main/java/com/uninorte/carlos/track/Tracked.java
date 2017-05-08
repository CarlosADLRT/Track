package com.uninorte.carlos.track;

import java.util.ArrayList;

/**
 * Created by carlos on 7/05/17.
 */

public class Tracked {
    public static ArrayList<String> track;

    public Tracked(ArrayList<String> track) {
        Tracked.track = track;
    }

    public static ArrayList<String> getTrack() {
        return track;
    }

    public static void setTrack(ArrayList<String> track) {
        Tracked.track = track;
    }
}
