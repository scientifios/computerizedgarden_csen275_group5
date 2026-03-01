package edu.scu.csen275.smartgarden.model;

/**
 * Enumerates selectable plant types along with their display metadata.
 */
public enum PlantType {

    STRAWBERRY("🍓", "Strawberry", PlantCategory.FRUIT),
    GRAPEVINE("🍇", "Grapevine", PlantCategory.FRUIT),
    APPLE("🍎", "Apple Sapling", PlantCategory.FRUIT),
    
    CARROT("🥕", "Carrot", PlantCategory.VEGETABLE),
    TOMATO("🍅", "Tomato", PlantCategory.VEGETABLE),
    ONION("🧅", "Onion", PlantCategory.VEGETABLE),
    
    SUNFLOWER("🌻", "Sunflower", PlantCategory.FLOWER),
    TULIP("🌸", "Tulip", PlantCategory.FLOWER),
    ROSE("🌹", "Rose", PlantCategory.FLOWER);
    
    private final String emoji;
    private final String displayName;
    private final PlantCategory category;
    
    PlantType(String emoji, String displayName, PlantCategory category) {
        this.emoji = emoji;
        this.displayName = displayName;
        this.category = category;
    }
    
    public String getEmoji() {
        return emoji;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public PlantCategory getCategory() {
        return category;
    }
    
    /**
    * Returns a formatted UI header for the given plant category.
    */
    public static String getCategoryHeader(PlantCategory category) {
        return switch (category) {
            case FRUIT -> "🍓 Fruit Plants";
            case VEGETABLE -> "🥕 Vegetable Crops";
            case FLOWER -> "🌸 Flowers";
        };
    }
}

