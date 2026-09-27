package net.zincstudios.scgextra.data;

import java.util.ArrayList;
import java.util.List;

public record CraterData(
    @Deprecated List<Crater> craters
) {

    public static ArrayList<Crater> ALL_CRATERS = new ArrayList<>();

    public CraterData {
        ALL_CRATERS.clear();
        for (Crater crater : craters) {
            ALL_CRATERS.add(crater);
        }
    }

    public record Crater(String name, int gravity) {
    }
}
