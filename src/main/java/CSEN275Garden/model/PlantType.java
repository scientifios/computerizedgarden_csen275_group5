package CSEN275Garden.model;

/**
 * Enumerates selectable plant types along with their display metadata.
 */
public enum PlantType {

    STRAWBERRY("🍓", "Strawberry", PlantCategory.FRUIT),
    CHERRY("🍇","Cherry", PlantCategory.FRUIT),
    APPLE("🍎","Apple Sapling", PlantCategory.FRUIT),
    
    CABBAGE("🥕", "Cabbage", PlantCategory.VEGETABLE),
    TOMATO("🍅","Tomato", PlantCategory.VEGETABLE),
    SCALLION("🧅", "Scallion", PlantCategory.VEGETABLE),
    
    DAISY("🌻","Daisy", PlantCategory.FLOWER),
    LILY("🌸","Lily", PlantCategory.FLOWER),
    PEONY("🌹","Peony", PlantCategory.FLOWER);
    
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
            case FRUIT -> "Fruits";
            case VEGETABLE -> "Vegetable";
            case FLOWER -> "Flowers";
        };
    }
}

