package edu.scu.csen275.smartgarden.ui;

import edu.scu.csen275.smartgarden.model.Plant;
import edu.scu.csen275.smartgarden.model.PlantType;
import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.effect.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Animated tile component for garden grid cells.
 * Provides smooth animations for plant growth, watering, and death.
 */
public class AnimatedTile extends StackPane {
    private final ImageView plantImageView; // Using ImageView for emoji images
    private final StackPane baseTile;
    private final StackPane shadowPane; // For soft shadow under plant
    private Plant plant;
    private String currentStyle;
    private String currentPlantType; // Cache current plant type to avoid reloading image
    private Image cachedImage; // Cache the actual Image object to prevent reloading
    private ScaleTransition growthAnimation;
    private FadeTransition fadeAnimation;
    private boolean isWatering;
    private String originalStyle;
    private int tileIndex; // For pastel color assignment
    
    // Pest management
    private PestTileOverlay pestOverlay;
    private List<PestSprite> activePests;
    private Timeline damageTickTimeline;
    private boolean isUnderAttack;
    private Pane animationContainer; // For floating text animations
    
    // Pastel color palette
    private static final String[] PASTEL_COLORS = {
        "#E4F6D4", // Mint
        "#FDFCC5", // Lemon
        "#FFE7D1", // Peach
        "#E9F2FF", // Sky
        "#F0E6FF"  // Light Lavender
    };
    
    private static final double BASE_SIZE = 60;
    private static final double PLANT_IMAGE_SIZE = 44; // Plant image size (larger than before)
    
    public AnimatedTile() {
        this(0); // Default index
    }
    
    public AnimatedTile(int index) {
        this.tileIndex = index;
        this.setMinSize(BASE_SIZE, BASE_SIZE);
        this.setMaxSize(BASE_SIZE, BASE_SIZE);
        this.setAlignment(Pos.CENTER);
        
        // Shadow pane for soft shadow under plant
        shadowPane = new StackPane();
        shadowPane.setMinSize(BASE_SIZE * 0.8, 12);
        shadowPane.setMaxSize(BASE_SIZE * 0.8, 12);
        shadowPane.setStyle("-fx-background-color: rgba(0,0,0,0.175); -fx-background-radius: 6;");
        shadowPane.setVisible(false); // Hidden until plant appears
        
        // Base tile with pastel color and soft edges
        baseTile = new StackPane();
        baseTile.setMinSize(BASE_SIZE, BASE_SIZE);
        baseTile.setMaxSize(BASE_SIZE, BASE_SIZE);
        baseTile.setStyle(getPastelEmptyStyle());
        // Defer effect application until node is in scene
        safeSetEffect(baseTile, createSoftShadow());
        
        // Plant image view (using emoji images from CDN instead of fonts)
        plantImageView = new ImageView();
        plantImageView.setFitWidth(PLANT_IMAGE_SIZE);
        plantImageView.setFitHeight(PLANT_IMAGE_SIZE);
        plantImageView.setPreserveRatio(true);
        plantImageView.setSmooth(true);
        // Defer effect application until node is in scene
        safeSetEffect(plantImageView, createPlantShadow()); // Shadow under plant
        StackPane.setAlignment(plantImageView, Pos.CENTER);
        
        // Pest overlay
        pestOverlay = new PestTileOverlay(BASE_SIZE, BASE_SIZE);
        pestOverlay.setVisible(false);
        
        // Initialize pest lists
        activePests = new ArrayList<>();
        isUnderAttack = false;
        
        // CRITICAL: Add children in correct z-order
        // baseTile (bottom) -> shadowPane -> plantImageView -> pestOverlay (top)
        this.getChildren().addAll(baseTile, shadowPane, plantImageView, pestOverlay);
        
        // Ensure pest overlay is always on top
        pestOverlay.toFront();
        
        // Position shadow at bottom
        StackPane.setAlignment(shadowPane, Pos.BOTTOM_CENTER);
        
        // Initialize animations
        setupAnimations();
    }
    
    /**
     * Safely applies an effect to a node, deferring if not in scene.
     */
    private void safeSetEffect(javafx.scene.Node node, Effect effect) {
        if (node.getScene() != null && node.getBoundsInLocal().getWidth() > 0 && node.getBoundsInLocal().getHeight() > 0) {
            // Node is in scene and has valid bounds, apply immediately
            node.setEffect(effect);
        } else {
            // Defer until node is in scene
            node.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    // Use Platform.runLater to ensure layout is complete
                    javafx.application.Platform.runLater(() -> {
                        if (node.getBoundsInLocal().getWidth() > 0 && node.getBoundsInLocal().getHeight() > 0) {
                            node.setEffect(effect);
                        }
                    });
                }
            });
        }
    }
    
    /**
     * Creates a soft shadow effect for the tile.
     */
    private Effect createSoftShadow() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setOffsetX(2);
        dropShadow.setOffsetY(2);
        dropShadow.setRadius(5);
        dropShadow.setColor(javafx.scene.paint.Color.rgb(0, 0, 0, 0.15));
        dropShadow.setBlurType(BlurType.GAUSSIAN);
        return dropShadow;
    }
    
    /**
     * Creates a soft elliptical shadow under the plant.
     */
    private Effect createPlantShadow() {
        DropShadow shadow = new DropShadow();
        shadow.setOffsetX(0);
        shadow.setOffsetY(4);
        shadow.setRadius(8);
        shadow.setColor(javafx.scene.paint.Color.rgb(0, 0, 0, 0.35));
        shadow.setBlurType(BlurType.GAUSSIAN);
        return shadow;
    }
    
    /**
     * Sets up animations for tile interactions.
     */
    private void setupAnimations() {
        // Growth animation (scale up)
        growthAnimation = new ScaleTransition(Duration.millis(300), plantImageView);
        growthAnimation.setFromX(0.5);
        growthAnimation.setFromY(0.5);
        growthAnimation.setToX(1.0);
        growthAnimation.setToY(1.0);
        growthAnimation.setInterpolator(Interpolator.EASE_OUT);
        
        // Fade animation for death
        fadeAnimation = new FadeTransition(Duration.millis(500), this);
        fadeAnimation.setFromValue(1.0);
        fadeAnimation.setToValue(0.7);
    }
    
    /**
     * Updates the tile based on plant state.
     * PRESERVES pest overlay state - doesn't clear it.
     */
    public void update(Plant plant) {
        this.plant = plant;
        
        if (plant == null) {
            setEmpty();
        } else if (plant.isDead()) {
            setDead();
        } else {
            setPlant(plant);
        }
        
        // PRESERVE pest overlay visibility - don't clear it when updating
        if (pestOverlay != null && (isUnderAttack || 
            (activePests != null && !activePests.isEmpty()))) {
            pestOverlay.setVisible(true);
            pestOverlay.toFront();
            
            // Re-apply attack border if under attack
            if (isUnderAttack) {
                updateTileBorderForAttack();
            }
        }
    }
    
    /**
     * Sets tile to empty state.
     */
    private void setEmpty() {
        plantImageView.setImage(null);
        cachedImage = null; // Clear cached image
        currentPlantType = null; // Reset cached plant type
        shadowPane.setVisible(false); // Hide shadow when empty
        baseTile.setStyle(getPastelEmptyStyle());
        baseTile.setOpacity(1.0);
        currentStyle = "empty";
    }
    
    /**
     * Sets tile to show a living plant.
     * PRESERVES pest overlay - doesn't clear it.
     */
    private void setPlant(Plant plant) {
        String plantType = plant.getPlantType();
        boolean isNewPlant = (currentPlantType == null || !plantType.equals(currentPlantType));
        
        // Only reload image if plant type has changed (prevents blinking)
        if (isNewPlant) {
            currentPlantType = plantType;
            
            // Load plant image from resources/images folder
            try {
                String imagePath = getPlantImagePath(plant);
                if (imagePath != null) {
                    // Always create new image for new plant type
                    cachedImage = new Image(getClass().getResourceAsStream(imagePath), PLANT_IMAGE_SIZE, PLANT_IMAGE_SIZE, true, true);
                    plantImageView.setImage(cachedImage);
                } else {
                    // Fallback to emoji if image not found
                    String emoji = getPlantEmoji(plant);
                    String imageUrl = getEmojiImageUrl(emoji);
                    cachedImage = new Image(imageUrl, PLANT_IMAGE_SIZE, PLANT_IMAGE_SIZE, true, true);
                    plantImageView.setImage(cachedImage);
                }
            } catch (Exception e) {
                // Try emoji fallback
                try {
                    String emoji = getPlantEmoji(plant);
                    String imageUrl = getEmojiImageUrl(emoji);
                    cachedImage = new Image(imageUrl, PLANT_IMAGE_SIZE, PLANT_IMAGE_SIZE, true, true);
                    plantImageView.setImage(cachedImage);
                } catch (Exception e2) {
                    cachedImage = null;
                    plantImageView.setImage(null);
                }
            }
        }
        
        // For all plants, ensure image view properties are stable (prevents blinking)
        // Ensure image view is always at stable state and full size
        plantImageView.setVisible(true);
        plantImageView.setOpacity(1.0);
        plantImageView.setScaleX(1.0);
        plantImageView.setScaleY(1.0);
        
        // Show shadow when plant appears
        shadowPane.setVisible(true);
        
        // Determine health-based style with pastel base
        String healthColor = plant.getHealthColor();
        String style;
        
        // When pests attack, show light brown unhealthy background instead of normal colors
        if (isUnderAttack) {
            style = getUnhealthyPestStyle(); // Light brown background for pest attacks
        } else {
            style = getPastelPlantStyle(healthColor);
        }
        
        // PRESERVE attack border if under attack
        if (isUnderAttack && !style.contains("border-color: #FF4444")) {
            style = style.replace("-fx-border-width: 2;", "-fx-border-width: 3px;");
            style = style.replace("-fx-border-color: #81C784;", "-fx-border-color: #FF4444;");
            style = style.replace("-fx-border-color: #FFD54F;", "-fx-border-color: #FF4444;");
            style = style.replace("-fx-border-color: #FFB74D;", "-fx-border-color: #FF4444;");
            style = style.replace("-fx-border-color: #E57373;", "-fx-border-color: #FF4444;");
            style = style.replace("-fx-border-color: #D7CCC8;", "-fx-border-color: #FF4444;");
        }
        
        baseTile.setStyle(style);
        
        // PRESERVE attack glow effect if under attack
        if (!isUnderAttack) {
            baseTile.setOpacity(1.0);
            safeSetEffect(baseTile, createSoftShadow());
        }
        // If under attack, updateTileBorderForAttack() will set the effect
        
        currentStyle = healthColor.toLowerCase();
        
        // Plants appear at full size immediately (no growth animation)
        // Ensure image view is at full scale from the start
        plantImageView.setScaleX(1.0);
        plantImageView.setScaleY(1.0);
    }
    
    /**
     * Sets tile to show dead plant.
     */
    private void setDead() {
        try {
            String imageUrl = getEmojiImageUrl("💀");
            Image emojiImage = new Image(imageUrl, PLANT_IMAGE_SIZE, PLANT_IMAGE_SIZE, true, true);
            plantImageView.setImage(emojiImage);
        } catch (Exception e) {
            plantImageView.setImage(null);
        }
        baseTile.setStyle(getDeadStyle());
        fadeAnimation.play();
        currentStyle = "dead";
    }
    
    /**
     * Animates plant growth (scale up).
     */
    public void animateGrowth() {
        growthAnimation.playFromStart();
    }
    
    /**
     * Animates watering effect (ripple) - legacy method for single tile.
     */
    public void animateWatering() {
        FadeTransition waterRipple = new FadeTransition(Duration.millis(400), baseTile);
        waterRipple.setFromValue(1.0);
        waterRipple.setToValue(0.6);
        waterRipple.setAutoReverse(true);
        waterRipple.setCycleCount(2);
        waterRipple.play();
    }
    
    /**
     * Starts full watering animation with soil darkening.
     * Called when "Water All Zones" is triggered.
     */
    public void startWateringAnimation() {
        if (isWatering) {
            return; // Already watering
        }
        
        isWatering = true;
        originalStyle = baseTile.getStyle();
        
        // Store original style
        originalStyle = baseTile.getStyle();
        
        // Create soil darkening effect using color adjustment
        ColorAdjust colorAdjust = new ColorAdjust();
        colorAdjust.setBrightness(-0.2); // Darken
        colorAdjust.setSaturation(0.1);  // Slightly more saturated (wet look)
        
        // Combine with original effect
        Blend darkeningBlend = new Blend();
        darkeningBlend.setMode(BlendMode.MULTIPLY);
        darkeningBlend.setBottomInput(colorAdjust);
        darkeningBlend.setTopInput(createSoftShadow());
        safeSetEffect(baseTile, darkeningBlend);
        
        // Temporarily darken the background style
        String darkenedStyle = darkenStyle(originalStyle);
        baseTile.setStyle(darkenedStyle);
        
        // Fade darkening back to normal over 2 seconds
        FadeTransition darkenFade = new FadeTransition(Duration.millis(2000), baseTile);
        darkenFade.setFromValue(0.75); // Slightly darkened
        darkenFade.setToValue(1.0);    // Back to normal
        darkenFade.setOnFinished(e -> {
            isWatering = false;
            safeSetEffect(baseTile, createSoftShadow()); // Restore original effect
            // Restore original style
            if (originalStyle != null) {
                baseTile.setStyle(originalStyle);
            }
        });
        
        darkenFade.play();
    }
    
    /**
     * Checks if tile is currently being watered.
     */
    public boolean isWatering() {
        return isWatering;
    }
    
    /**
     * Darkens a CSS style string to simulate wet soil.
     */
    private String darkenStyle(String style) {
        // Add a darker overlay color to simulate wet soil
        // Replace background colors with darker versions
        String darkened = style;
        
        // Darken common soil colors
        darkened = darkened.replace("#D7CCC8", "#B89E95"); // Empty tile
        darkened = darkened.replace("#BCAAA4", "#9E857C"); // Empty tile gradient
        darkened = darkened.replace("#A5D6A7", "#8BC58A"); // Healthy green
        darkened = darkened.replace("#81C784", "#6BAF6E"); // Healthy green gradient
        darkened = darkened.replace("#FFF59D", "#E6DC89"); // Stressed yellow
        darkened = darkened.replace("#FFD54F", "#E6BF46"); // Stressed yellow gradient
        darkened = darkened.replace("#FFB74D", "#E6A544"); // Poor orange
        darkened = darkened.replace("#FF9800", "#E68900"); // Poor orange gradient
        darkened = darkened.replace("#EF9A9A", "#D68B8B"); // Critical red
        darkened = darkened.replace("#E57373", "#CF6666"); // Critical red gradient
        
        return darkened;
    }
    
    /**
     * Animates pesticide application (fade out particles).
     */
    public void animatePesticide() {
        ScaleTransition dust = new ScaleTransition(Duration.millis(300), baseTile);
        dust.setFromX(1.0);
        dust.setFromY(1.0);
        dust.setToX(0.95);
        dust.setToY(0.95);
        dust.setAutoReverse(true);
        dust.setCycleCount(2);
        dust.play();
    }
    
    /**
     * Gets the image path for a plant from resources/images folder.
     * Returns null if image file doesn't exist.
     */
    private String getPlantImagePath(Plant plant) {
        String plantTypeName = plant.getPlantType();
        
        // Try to match PlantType enum by display name
        try {
            for (PlantType type : PlantType.values()) {
                if (type.getDisplayName().equalsIgnoreCase(plantTypeName) ||
                    plantTypeName.contains(type.getDisplayName())) {
                    return getImagePathForPlantType(type);
                }
            }
        } catch (Exception e) {
            // Fall through to fallback mapping
        }
        
        // Fallback mapping based on plant type name
        String lowerName = plantTypeName.toLowerCase();
        if (lowerName.contains("strawberry")) return "/images/strawberry.png";
        if (lowerName.contains("grapevine") || lowerName.contains("grape")) return "/images/grape.png";
        if (lowerName.contains("apple")) return "/images/apple.png";
        if (lowerName.contains("carrot")) return "/images/carrot.png";
        if (lowerName.contains("tomato")) return "/images/tomato.png";
        if (lowerName.contains("onion")) return "/images/onion.png";
        if (lowerName.contains("sunflower")) return "/images/sunflower.png";
        if (lowerName.contains("tulip")) return "/images/tulip.png";
        if (lowerName.contains("rose")) return "/images/rose.png";
        
        return null; // No image found
    }
    
    /**
     * Gets the image path for a specific PlantType.
     */
    private String getImagePathForPlantType(PlantType type) {
        return switch (type) {
            case STRAWBERRY -> "/images/strawberry.png";
            case GRAPEVINE -> "/images/grape.png";
            case APPLE -> "/images/apple.png";
            case CARROT -> "/images/carrot.png";
            case TOMATO -> "/images/tomato.png";
            case ONION -> "/images/onion.png";
            case SUNFLOWER -> "/images/sunflower.png";
            case TULIP -> "/images/tulip.png";
            case ROSE -> "/images/rose.png";
        };
    }
    
    /**
     * Gets emoji image URL from CDN (using Twemoji) - used as fallback.
     * Converts emoji Unicode to image URL.
     */
    private String getEmojiImageUrl(String emoji) {
        // Convert emoji to code points and create Twemoji URL
        // Twemoji format: https://cdn.jsdelivr.net/gh/twitter/twemoji@14.0.2/assets/72x72/[code].png
        StringBuilder codePoints = new StringBuilder();
        emoji.codePoints().forEach(cp -> {
            if (codePoints.length() > 0) codePoints.append("-");
            codePoints.append(String.format("%04X", cp).toLowerCase());
        });
        
        // Twemoji CDN - reliable and colorful (fallback when local image not available)
        return "https://cdn.jsdelivr.net/gh/twitter/twemoji@14.0.2/assets/72x72/" + codePoints.toString() + ".png";
    }
    
    /**
     * Gets plant emoji based on plant type name.
     * Maps plant type names to PlantType enum emojis.
     */
    private String getPlantEmoji(Plant plant) {
        String plantTypeName = plant.getPlantType();
        
        // Try to match PlantType enum by display name
        try {
            for (PlantType type : PlantType.values()) {
                if (type.getDisplayName().equalsIgnoreCase(plantTypeName) ||
                    plantTypeName.contains(type.getDisplayName())) {
                    return type.getEmoji();
                }
            }
        } catch (Exception e) {
            // Fall through to default mapping
        }
        
        // Fallback mapping based on plant type name
        String lowerName = plantTypeName.toLowerCase();
        if (lowerName.contains("strawberry")) return "🍓";
        if (lowerName.contains("grapevine")) return "🍇";
        if (lowerName.contains("apple")) return "🍎";
        if (lowerName.contains("carrot")) return "🥕";
        if (lowerName.contains("tomato")) return "🍅";
        if (lowerName.contains("onion")) return "🧅";
        if (lowerName.contains("sunflower")) return "🌻";
        if (lowerName.contains("tulip")) return "🌸";
        if (lowerName.contains("rose")) return "🌹";
        if (lowerName.contains("flower")) return "🌸";
        if (lowerName.contains("vegetable")) return "🍅";
        if (lowerName.contains("tree")) return "🌳";
        if (lowerName.contains("grass")) return "🌿";
        if (lowerName.contains("herb")) return "🌱";
        
        return "🌱"; // Default
    }
    
    /**
     * Gets style based on plant health.
     */
    private String getPlantStyle(String healthColor) {
        return switch (healthColor) {
            case "GREEN" -> getHealthyStyle();
            case "YELLOW" -> getStressedStyle();
            case "ORANGE" -> getPoorStyle();
            case "RED" -> getCriticalStyle();
            default -> getEmptyStyle();
        };
    }
    
    /**
     * Gets pastel color based on tile index.
     */
    private String getPastelColor() {
        return PASTEL_COLORS[tileIndex % PASTEL_COLORS.length];
    }
    
    /**
     * Gets slightly darker pastel for watered tiles.
     */
    private String getWateredPastelColor() {
        String baseColor = getPastelColor();
        // Darken by 15% for watered effect
        return baseColor; // Can enhance later with actual color manipulation
    }
    
    private String getPastelEmptyStyle() {
        String pastelColor = getPastelColor();
        return "-fx-background-color: " + pastelColor + "; " +
               "-fx-background-radius: 6; " +
               "-fx-border-color: rgba(129, 199, 132, 0.5); " +
               "-fx-border-width: 1; " +
               "-fx-border-radius: 6;";
    }
    
    private String getEmptyStyle() {
        return getPastelEmptyStyle(); // Use pastel instead of brown
    }
    
    private String getPastelPlantStyle(String healthColor) {
        String pastelBase = getPastelColor();
        // Combine pastel base with health indication
        return switch (healthColor) {
            case "GREEN" -> "-fx-background-color: linear-gradient(to bottom, " + pastelBase + " 0%, #C8E6C9 100%); " +
                           "-fx-background-radius: 6; " +
                           "-fx-border-color: #81C784; " +
                           "-fx-border-width: 2; " +
                           "-fx-border-radius: 6;";
            case "YELLOW" -> "-fx-background-color: linear-gradient(to bottom, " + pastelBase + " 0%, #FFF9C4 100%); " +
                            "-fx-background-radius: 6; " +
                            "-fx-border-color: #FFD54F; " +
                            "-fx-border-width: 2; " +
                            "-fx-border-radius: 6;";
            case "ORANGE" -> "-fx-background-color: linear-gradient(to bottom, " + pastelBase + " 0%, #FFE0B2 100%); " +
                            "-fx-background-radius: 6; " +
                            "-fx-border-color: #FFB74D; " +
                            "-fx-border-width: 2; " +
                            "-fx-border-radius: 6;";
            case "RED" -> "-fx-background-color: linear-gradient(to bottom, " + pastelBase + " 0%, #FFCDD2 100%); " +
                         "-fx-background-radius: 6; " +
                         "-fx-border-color: #E57373; " +
                         "-fx-border-width: 2; " +
                         "-fx-border-radius: 6;";
            default -> getPastelEmptyStyle();
        };
    }
    
    private String getHealthyStyle() {
        return getPastelPlantStyle("GREEN");
    }
    
    private String getStressedStyle() {
        return getPastelPlantStyle("YELLOW");
    }
    
    private String getPoorStyle() {
        return getPastelPlantStyle("ORANGE");
    }
    
    private String getCriticalStyle() {
        return getPastelPlantStyle("RED");
    }
    
    private String getDeadStyle() {
        return "-fx-background-color: #424242; " +
               "-fx-background-radius: 4; " +
               "-fx-border-color: #212121; " +
               "-fx-border-width: 1; " +
               "-fx-border-radius: 4;";
    }
    
    /**
     * Gets light brown unhealthy style for tiles under pest attack.
     */
    private String getUnhealthyPestStyle() {
        // Light brown colors for unhealthy appearance during pest attacks
        return "-fx-background-color: linear-gradient(to bottom, #D7CCC8 0%, #BCAAA4 100%); " +
               "-fx-background-radius: 6; " +
               "-fx-border-color: #A1887F; " +
               "-fx-border-width: 3px; " +
               "-fx-border-radius: 6;";
    }
    
    /**
     * Applies hover effect.
     */
    public void applyHoverEffect() {
        if (!currentStyle.equals("dead")) {
            safeSetEffect(baseTile, new Glow(0.3));
        }
    }
    
    /**
     * Removes hover effect.
     */
    public void removeHoverEffect() {
        safeSetEffect(baseTile, createSoftShadow());
    }
    
    /**
     * Sets the tile index for pastel color assignment.
     */
    public void setTileIndex(int index) {
        this.tileIndex = index;
        // Update style if empty
        if ("empty".equals(currentStyle)) {
            baseTile.setStyle(getPastelEmptyStyle());
        }
    }
    
    public Plant getPlant() {
        return plant;
    }
    
    /**
     * Spawns a harmful pest on this tile.
     */
    public void spawnPest(String pestType) {
        // Initialize pest overlay if needed
        if (pestOverlay == null) {
            pestOverlay = new PestTileOverlay(BASE_SIZE, BASE_SIZE);
            pestOverlay.setVisible(false);
            activePests = new ArrayList<>();
            this.getChildren().add(pestOverlay);
        }
        
        // Check if this pest type already exists (avoid duplicates)
        boolean alreadyExists = false;
        if (activePests != null) {
            for (PestSprite existing : activePests) {
                if (existing.getPestType().equalsIgnoreCase(pestType)) {
                    alreadyExists = true;
                    break;
                }
            }
        }
        if (alreadyExists) {
            return; // Don't add duplicate pests
        }
        
        // Make sure overlay is visible and on top
        pestOverlay.setVisible(true);
        pestOverlay.toFront();
        isUnderAttack = true;
        
        // Create and add pest sprite
        PestSprite pestSprite = new PestSprite(pestType);
        activePests.add(pestSprite);
        
        pestOverlay.addPest(pestSprite);
        
        // Make sure pest is visible and on top
        pestSprite.toFront();
        pestOverlay.toFront();
        
        // Force a layout update
        pestOverlay.requestLayout();
        this.requestLayout();
        
        
        // Update tile border to red (under attack)
        updateTileBorderForAttack();
    }
    
    /**
     * Shows damage visual when pest attacks.
     */
    public void showDamageVisual(int damage) {
        if (pestOverlay != null && pestOverlay.isUnderAttack()) {
            pestOverlay.applyDamageVisual(damage);
            pestOverlay.triggerLeafShake();
            
            // Show floating damage text
            if (animationContainer != null) {
                javafx.geometry.Bounds bounds = this.localToScene(this.getBoundsInLocal());
                javafx.geometry.Bounds containerBounds = animationContainer.sceneToLocal(bounds);
                DamageTextAnimation.createDamageText(animationContainer,
                    containerBounds.getMinX() + containerBounds.getWidth() / 2,
                    containerBounds.getMinY() + containerBounds.getHeight() / 2,
                    damage);
            }
            
            // Animate pest attack
            if (activePests != null) {
                for (PestSprite pest : activePests) {
                    pest.animateAttack();
                }
            }
        }
    }
    
    /**
     * Applies pesticide treatment to this tile.
     * FIXED: Always show animation even if hasPests() check fails.
     */
    public void applyPesticide() {
        // FIXED: Check pest overlay directly, not just activePests list
        boolean hasPests = (activePests != null && !activePests.isEmpty()) ||
                           (pestOverlay != null && pestOverlay.isUnderAttack());
        
        if (pestOverlay == null || !hasPests) {
            return;
        }
        
        // Prevent multiple simultaneous pesticide applications
        if (pestOverlay.getUserData() != null && pestOverlay.getUserData().equals("spraying")) {
            return; // Already spraying
        }
        pestOverlay.setUserData("spraying");
        
        // Get tile center for animations (relative to tile, not scene)
        double centerX = BASE_SIZE / 2;
        double centerY = BASE_SIZE / 2;
        
        // Get copies of pest lists before clearing (for animation)
        List<PestSprite> pestsToAnimate = activePests != null ? new ArrayList<>(activePests) : new ArrayList<>();
        
        // Animate spray effect (this will handle pest death animations)
        PesticideSprayEngine.animateSpray(this, 
            pestsToAnimate,
            animationContainer,
            centerX,
            centerY);
        
        // Clear pests AFTER animation starts (death animation will remove them from overlay)
        // Delay clearing to let animation play - MUCH LONGER (2.4s mist + 0.8s pest death + buffer)
        javafx.animation.PauseTransition delay = new javafx.animation.PauseTransition(javafx.util.Duration.millis(3500)); // 3.5 seconds
        delay.setOnFinished(e -> {
            if (activePests != null) {
                activePests.clear();
            }
            if (pestOverlay != null) {
                pestOverlay.clearAllPests();
                pestOverlay.setUserData(null); // Clear spraying flag
            }
        });
        delay.play();
        
        // Clear damage visuals
        if (pestOverlay != null) {
            pestOverlay.clearDamageVisuals();
            isUnderAttack = false;
            pestOverlay.setVisible(false);
        }
        
        // Restore healthy border
        if (plant != null && !plant.isDead()) {
            update(plant); // Refresh tile style
        }
    }
    
    /**
     * Updates tile border to show attack state.
     * FIXED: Removed rainbow hue shift, use proper red glow.
     */
    private void updateTileBorderForAttack() {
        if (isUnderAttack) {
            // Red glowing border
            String currentStyle = baseTile.getStyle();
            if (!currentStyle.contains("border-color: #FF4444")) {
                baseTile.setStyle(currentStyle + 
                    " -fx-border-color: #FF4444; " +
                    " -fx-border-width: 3px;");
            }
            
            // FIXED: Use red drop shadow instead of ColorAdjust to avoid rainbow effect
            DropShadow redShadow = new DropShadow();
            redShadow.setColor(javafx.scene.paint.Color.rgb(255, 68, 68, 0.8));
            redShadow.setRadius(8);
            redShadow.setOffsetX(0);
            redShadow.setOffsetY(0);
            safeSetEffect(baseTile, redShadow);
        }
    }
    
    /**
     * Sets the animation container for floating text.
     */
    public void setAnimationContainer(Pane container) {
        this.animationContainer = container;
    }
    
    /**
     * Checks if tile has pests.
     */
    public boolean hasPests() {
        return activePests != null && activePests.size() > 0;
    }
    
    /**
     * Checks if tile is under attack.
     */
    public boolean isUnderAttack() {
        return isUnderAttack;
    }
    
    /**
     * Updates pest animations (called periodically).
     */
    public void updatePestAnimations() {
        if (activePests != null) {
            // Remove dead pests
            activePests.removeIf(pest -> !pest.isAlive());
        }
        
        if (pestOverlay != null) {
            // Update overlay visibility
            pestOverlay.setVisible(isUnderAttack);
        }
        
        // Update attack state
        if (activePests != null && activePests.isEmpty() && isUnderAttack) {
            isUnderAttack = false;
            if (pestOverlay != null) {
                pestOverlay.clearDamageVisuals();
            }
            // Restore normal border
            if (plant != null && !plant.isDead()) {
                update(plant);
            }
        }
    }
    
    /**
     * Gets active pest count.
     */
    public int getActivePestCount() {
        return activePests != null ? activePests.size() : 0;
    }
    
}

