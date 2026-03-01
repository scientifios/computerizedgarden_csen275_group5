package edu.scu.csen275.smartgarden.ui;

import edu.scu.csen275.smartgarden.model.Plant;
import edu.scu.csen275.smartgarden.model.PlantType;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.effect.Glow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.util.Duration;

/**
 * Tile component for garden grid cells.
 * Pest and watering visuals are represented by static status icons.
 */
public class AnimatedTile extends StackPane {
    private final ImageView plantImageView;
    private final StackPane baseTile;
    private final StackPane shadowPane;
    private final HBox statusIcons;
    private final Label pestIndicator;
    private final Label waterIndicator;

    private Plant plant;
    private String currentStyle = "empty";
    private String currentPlantType;
    private Image cachedImage;
    private ScaleTransition growthAnimation;
    private FadeTransition fadeAnimation;
    private int tileIndex;
    private boolean hasPest;
    private boolean isWatering;
    private Pane animationContainer;
    private long waterHintUntilMs;
    private static final long WATER_HINT_DURATION_MS = 500;

    private static final String[] PASTEL_COLORS = {
        "#E4F6D4",
        "#FDFCC5",
        "#FFE7D1",
        "#E9F2FF",
        "#F0E6FF"
    };

    private static final double BASE_SIZE = 60;
    private static final double PLANT_IMAGE_SIZE = 44;

    public AnimatedTile() {
        this(0);
    }

    public AnimatedTile(int index) {
        this.tileIndex = index;
        this.setMinSize(BASE_SIZE, BASE_SIZE);
        this.setMaxSize(BASE_SIZE, BASE_SIZE);
        this.setAlignment(Pos.CENTER);

        shadowPane = new StackPane();
        shadowPane.setMinSize(BASE_SIZE * 0.8, 12);
        shadowPane.setMaxSize(BASE_SIZE * 0.8, 12);
        shadowPane.setStyle("-fx-background-color: rgba(0,0,0,0.175); -fx-background-radius: 6;");
        shadowPane.setVisible(false);

        baseTile = new StackPane();
        baseTile.setMinSize(BASE_SIZE, BASE_SIZE);
        baseTile.setMaxSize(BASE_SIZE, BASE_SIZE);
        baseTile.setStyle(getPastelEmptyStyle());
        safeSetEffect(baseTile, createSoftShadow());

        plantImageView = new ImageView();
        plantImageView.setFitWidth(PLANT_IMAGE_SIZE);
        plantImageView.setFitHeight(PLANT_IMAGE_SIZE);
        plantImageView.setPreserveRatio(true);
        plantImageView.setSmooth(true);
        safeSetEffect(plantImageView, createPlantShadow());
        StackPane.setAlignment(plantImageView, Pos.CENTER);

        pestIndicator = new Label("✚");
        pestIndicator.setStyle("-fx-text-fill: #E53935; -fx-font-size: 13px; -fx-font-weight: bold;");
        pestIndicator.setVisible(false);

        waterIndicator = new Label("💧");
        waterIndicator.setStyle("-fx-font-size: 12px; -fx-text-fill: #1E88E5;");
        waterIndicator.setVisible(false);

        statusIcons = new HBox(3, pestIndicator, waterIndicator);
        statusIcons.setMouseTransparent(true);
        statusIcons.setAlignment(Pos.TOP_LEFT);
        StackPane.setAlignment(statusIcons, Pos.TOP_LEFT);
        StackPane.setMargin(statusIcons, new Insets(2, 0, 0, 2));

        this.getChildren().addAll(baseTile, shadowPane, plantImageView, statusIcons);
        StackPane.setAlignment(shadowPane, Pos.BOTTOM_CENTER);

        setupAnimations();
    }

    private void safeSetEffect(javafx.scene.Node node, Effect effect) {
        if (node.getScene() != null && node.getBoundsInLocal().getWidth() > 0 && node.getBoundsInLocal().getHeight() > 0) {
            node.setEffect(effect);
        } else {
            node.sceneProperty().addListener((obs, oldScene, newScene) -> {
                if (newScene != null) {
                    javafx.application.Platform.runLater(() -> {
                        if (node.getBoundsInLocal().getWidth() > 0 && node.getBoundsInLocal().getHeight() > 0) {
                            node.setEffect(effect);
                        }
                    });
                }
            });
        }
    }

    private Effect createSoftShadow() {
        DropShadow dropShadow = new DropShadow();
        dropShadow.setOffsetX(2);
        dropShadow.setOffsetY(2);
        dropShadow.setRadius(5);
        dropShadow.setColor(javafx.scene.paint.Color.rgb(0, 0, 0, 0.15));
        dropShadow.setBlurType(BlurType.GAUSSIAN);
        return dropShadow;
    }

    private Effect createPlantShadow() {
        DropShadow shadow = new DropShadow();
        shadow.setOffsetX(0);
        shadow.setOffsetY(4);
        shadow.setRadius(8);
        shadow.setColor(javafx.scene.paint.Color.rgb(0, 0, 0, 0.35));
        shadow.setBlurType(BlurType.GAUSSIAN);
        return shadow;
    }

    private void setupAnimations() {
        growthAnimation = new ScaleTransition(Duration.millis(300), plantImageView);
        growthAnimation.setFromX(0.5);
        growthAnimation.setFromY(0.5);
        growthAnimation.setToX(1.0);
        growthAnimation.setToY(1.0);
        growthAnimation.setInterpolator(Interpolator.EASE_OUT);

        fadeAnimation = new FadeTransition(Duration.millis(500), this);
        fadeAnimation.setFromValue(1.0);
        fadeAnimation.setToValue(0.7);
    }

    public void update(Plant plant) {
        this.plant = plant;

        if (plant == null) {
            setEmpty();
        } else if (plant.isDead()) {
            setDead();
        } else {
            setPlant(plant);
        }

        updateStatusIcons();
    }

    private void setEmpty() {
        plantImageView.setImage(null);
        cachedImage = null;
        currentPlantType = null;
        shadowPane.setVisible(false);
        baseTile.setStyle(getPastelEmptyStyle());
        baseTile.setOpacity(1.0);
        safeSetEffect(baseTile, createSoftShadow());
        currentStyle = "empty";
        hasPest = false;
        isWatering = false;
        waterHintUntilMs = 0;
    }

    private void setPlant(Plant plant) {
        String plantType = plant.getPlantType();
        boolean isNewPlant = (currentPlantType == null || !plantType.equals(currentPlantType));

        if (isNewPlant) {
            currentPlantType = plantType;
            try {
                String imagePath = getPlantImagePath(plant);
                if (imagePath != null) {
                    cachedImage = new Image(getClass().getResourceAsStream(imagePath), PLANT_IMAGE_SIZE, PLANT_IMAGE_SIZE, true, true);
                    plantImageView.setImage(cachedImage);
                } else {
                    String emoji = getPlantEmoji(plant);
                    cachedImage = new Image(getEmojiImageUrl(emoji), PLANT_IMAGE_SIZE, PLANT_IMAGE_SIZE, true, true);
                    plantImageView.setImage(cachedImage);
                }
            } catch (Exception e) {
                try {
                    String emoji = getPlantEmoji(plant);
                    cachedImage = new Image(getEmojiImageUrl(emoji), PLANT_IMAGE_SIZE, PLANT_IMAGE_SIZE, true, true);
                    plantImageView.setImage(cachedImage);
                } catch (Exception ignored) {
                    cachedImage = null;
                    plantImageView.setImage(null);
                }
            }
        }

        plantImageView.setVisible(true);
        plantImageView.setOpacity(1.0);
        plantImageView.setScaleX(1.0);
        plantImageView.setScaleY(1.0);
        shadowPane.setVisible(true);

        String healthColor = plant.getHealthColor();
        baseTile.setStyle(getPastelPlantStyle(healthColor));
        baseTile.setOpacity(1.0);
        safeSetEffect(baseTile, createSoftShadow());
        currentStyle = healthColor.toLowerCase();
    }

    private void setDead() {
        try {
            Image emojiImage = new Image(getEmojiImageUrl("🪃"), PLANT_IMAGE_SIZE, PLANT_IMAGE_SIZE, true, true);
            plantImageView.setImage(emojiImage);
        } catch (Exception e) {
            plantImageView.setImage(null);
        }
        baseTile.setStyle(getDeadStyle());
        fadeAnimation.play();
        currentStyle = "dead";
        hasPest = false;
    }

    private void updateStatusIcons() {
        boolean hasLivingPlant = plant != null && !plant.isDead();
        boolean lowWaterNow = hasLivingPlant && plant.getWaterLevel() < plant.getWaterRequirement();

        if (lowWaterNow) {
            waterHintUntilMs = System.currentTimeMillis() + WATER_HINT_DURATION_MS;
        }
        boolean showWaterHint = lowWaterNow || (hasLivingPlant && System.currentTimeMillis() < waterHintUntilMs);

        pestIndicator.setVisible(hasLivingPlant && hasPest);
        waterIndicator.setVisible(showWaterHint);
    }

    public void animateGrowth() {
        growthAnimation.playFromStart();
    }

    public void animateWatering() {
        // Disabled by requirement.
    }

    public void startWateringAnimation() {
        // Disabled by requirement.
        isWatering = false;
    }

    public boolean isWatering() {
        return isWatering;
    }

    public void animatePesticide() {
        // Disabled by requirement.
    }

    private String getPlantImagePath(Plant plant) {
        String plantTypeName = plant.getPlantType();
        try {
            for (PlantType type : PlantType.values()) {
                if (type.getDisplayName().equalsIgnoreCase(plantTypeName) || plantTypeName.contains(type.getDisplayName())) {
                    return getImagePathForPlantType(type);
                }
            }
        } catch (Exception ignored) {
        }

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
        return null;
    }

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

    private String getEmojiImageUrl(String emoji) {
        StringBuilder codePoints = new StringBuilder();
        emoji.codePoints().forEach(cp -> {
            if (codePoints.length() > 0) codePoints.append("-");
            codePoints.append(String.format("%04X", cp).toLowerCase());
        });
        return "https://cdn.jsdelivr.net/gh/twitter/twemoji@14.0.2/assets/72x72/" + codePoints + ".png";
    }

    private String getPlantEmoji(Plant plant) {
        String plantTypeName = plant.getPlantType();
        try {
            for (PlantType type : PlantType.values()) {
                if (type.getDisplayName().equalsIgnoreCase(plantTypeName) || plantTypeName.contains(type.getDisplayName())) {
                    return type.getEmoji();
                }
            }
        } catch (Exception ignored) {
        }

        String lowerName = plantTypeName.toLowerCase();
        if (lowerName.contains("strawberry")) return "🍜";
        if (lowerName.contains("grapevine")) return "🍌";
        if (lowerName.contains("apple")) return "🍕";
        if (lowerName.contains("carrot")) return "🥚";
        if (lowerName.contains("tomato")) return "🍊";
        if (lowerName.contains("onion")) return "🥊";
        if (lowerName.contains("sunflower")) return "🌰";
        if (lowerName.contains("tulip")) return "🌭";
        if (lowerName.contains("rose")) return "🌮";
        return "🌡";
    }

    private String getPastelColor() {
        return PASTEL_COLORS[tileIndex % PASTEL_COLORS.length];
    }

    private String getPastelEmptyStyle() {
        String pastelColor = getPastelColor();
        return "-fx-background-color: " + pastelColor + "; " +
               "-fx-background-radius: 6; " +
               "-fx-border-color: rgba(129, 199, 132, 0.5); " +
               "-fx-border-width: 1; " +
               "-fx-border-radius: 6;";
    }

    private String getPastelPlantStyle(String healthColor) {
        String pastelBase = getPastelColor();
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

    private String getDeadStyle() {
        return "-fx-background-color: #424242; " +
               "-fx-background-radius: 4; " +
               "-fx-border-color: #212121; " +
               "-fx-border-width: 1; " +
               "-fx-border-radius: 4;";
    }

    public void applyHoverEffect() {
        if (!"dead".equals(currentStyle)) {
            safeSetEffect(baseTile, new Glow(0.3));
        }
    }

    public void removeHoverEffect() {
        safeSetEffect(baseTile, createSoftShadow());
    }

    public void setTileIndex(int index) {
        this.tileIndex = index;
        if ("empty".equals(currentStyle)) {
            baseTile.setStyle(getPastelEmptyStyle());
        }
    }

    public Plant getPlant() {
        return plant;
    }

    public void spawnPest(String pestType) {
        hasPest = true;
        updateStatusIcons();
    }

    public void showDamageVisual(int damage) {
        // Disabled by requirement.
    }

    public void applyPesticide() {
        hasPest = false;
        updateStatusIcons();
    }

    public void setAnimationContainer(Pane container) {
        this.animationContainer = container;
    }

    public boolean hasPests() {
        return hasPest;
    }

    public boolean isUnderAttack() {
        return hasPest;
    }

    public void updatePestAnimations() {
        updateStatusIcons();
    }
    
    public int getActivePestCount() {
        return hasPest ? 1 : 0;
    }

    /**
     * Forces the water indicator to be shown briefly for UI feedback.
     */
    public void showTemporaryWaterHint() {
        waterHintUntilMs = System.currentTimeMillis() + WATER_HINT_DURATION_MS;
        updateStatusIcons();
    }
}
