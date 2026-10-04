package com.vocnjak.domain;

import java.util.LinkedHashMap;
import java.util.Map;

public final class FruitSpeciesCatalog {

    public static final String OTHER = "__other__";

    private static final Map<String, String> IMAGE_FILES = new LinkedHashMap<>();

    static {
        IMAGE_FILES.put("Apple", "apple.jpg");
        IMAGE_FILES.put("Apricot", "apricot.jpg");
        IMAGE_FILES.put("Cherry", "cherry.jpg");
        IMAGE_FILES.put("Dogwood berry", "dogwood_berry.jpg");
        IMAGE_FILES.put("Hazelnut", "hazelnut.jpg");
        IMAGE_FILES.put("Pear", "pear.jpg");
        IMAGE_FILES.put("Plum", "plum.jpg");
        IMAGE_FILES.put("Quince", "quince.jpg");
        IMAGE_FILES.put("Sorb fruit", "sorb_fruit.jpg");
        IMAGE_FILES.put("Sour cherry", "sour_cherry.jpg");
        IMAGE_FILES.put("Vineyard peach", "vineyard_peach.jpg");
        IMAGE_FILES.put("Walnut", "walnut.jpg");
        IMAGE_FILES.put("Wild pear (Takisa)", "wild_pear_takisa.jpg");
    }

    private FruitSpeciesCatalog() {
    }

    public static String imageFileNameFor(String species) {
        return IMAGE_FILES.get(species);
    }

    public static boolean isCanonical(String species) {
        return IMAGE_FILES.containsKey(species);
    }
}
