package com.amirali.wally.model;

import java.util.List;

public final class Categories {

    public static final String
            NATURE = "Nature",
            SPACE = "Space",
            CAR = "Car",
            ABSTRACT = "Abstract",
            OTHER = "Other";

    public static List<String> list() {
        return List.of(NATURE, SPACE, CAR, ABSTRACT, OTHER);
    }
}
