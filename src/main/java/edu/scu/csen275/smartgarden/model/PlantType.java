package edu.scu.csen275.smartgarden.model;

/**
 * Enum representing all available plant types with their emojis, display names, and categories.
 */
public enum PlantType {
    // Fruit Plants
    STRAWBERRY("🍓", "Strawberry", PlantCategory.FRUIT),
    GRAPEVINE("🍇", "Grapevine", PlantCategory.FRUIT),
    APPLE("🍎", "Apple Sapling", PlantCategory.FRUIT),
    
    // Vegetable Crops
    CARROT("🥕", "Carrot", PlantCategory.VEGETABLE),
    TOMATO("🍅", "Tomato", PlantCategory.VEGETABLE),
    ONION("🧅", "Onion", PlantCategory.VEGETABLE),
    
    // Flowers
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
     * Gets the category header text for display in dropdown.
     */
    public static String getCategoryHeader(PlantCategory category) {
        return switch (category) {
            case FRUIT -> "🍓 Fruit Plants";
            case VEGETABLE -> "🥕 Vegetable Crops";
            case FLOWER -> "🌸 Flowers";
        };
    }
}

