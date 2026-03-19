package CSEN275Garden.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.StackPane;

import java.util.Random;

/**
 * UI tile used as the default background for an empty garden cell.
 * Optionally displays a small flower marker.
 */
public class GrassTile extends StackPane {
    private final StackPane baseTile;
    private final Label flowerLabel;
    private final Random random;
    private boolean hasFlower;

    private static final double BASE_SIZE = 60;

    public GrassTile() {
        this.random = new Random();
        this.hasFlower = false;

        this.setMinSize(BASE_SIZE, BASE_SIZE);
        this.setMaxSize(BASE_SIZE, BASE_SIZE);
        this.setAlignment(Pos.CENTER);

        baseTile = new StackPane();
        baseTile.setMinSize(BASE_SIZE, BASE_SIZE);
        baseTile.setMaxSize(BASE_SIZE, BASE_SIZE);
        baseTile.setStyle(getGrassTileStyle());
        safeSetEffect(baseTile, createSoftShadow());

        flowerLabel = new Label("");
        flowerLabel.setFont(javafx.scene.text.Font.font(20));
        flowerLabel.setAlignment(Pos.CENTER);
        flowerLabel.setVisible(false);

        this.getChildren().addAll(baseTile, flowerLabel);
    }

    /**
     * Returns the base CSS style for the grass tile.
     */
    private String getGrassTileStyle() {
        return "-fx-background-color: #8D6E63; " +
               "-fx-background-radius: 8; " +
               "-fx-border-color: rgba(109, 76, 65, 0.35); " +
               "-fx-border-width: 1; " +
               "-fx-border-radius: 8;";
    }

    /**
     * Applies an effect once the node is attached to a scene and has valid bounds.
     */
    private void safeSetEffect(javafx.scene.Node node, javafx.scene.effect.Effect effect) {
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

    private DropShadow createSoftShadow() {
        DropShadow shadow = new DropShadow();
        shadow.setOffsetX(2);
        shadow.setOffsetY(2);
        shadow.setRadius(6);
        shadow.setColor(javafx.scene.paint.Color.rgb(0, 0, 0, 0.2));
        shadow.setBlurType(javafx.scene.effect.BlurType.GAUSSIAN);
        return shadow;
    }

    /**
     * Displays a flower marker on the tile (if not already shown).
     */
    public void bloomFlower() {
        if (!hasFlower) {
            hasFlower = true;
            String[] flowerEmojis = {"🌸", "🌺", "🌻", "🌷", "🌼"};
            flowerLabel.setText(flowerEmojis[random.nextInt(flowerEmojis.length)]);
            flowerLabel.setVisible(true);
        }
    }

    public void removeFlower() {
        if (hasFlower) {
            hasFlower = false;
            flowerLabel.setVisible(false);
            flowerLabel.setText("");
        }
    }

    /**
     * Optional visual effect hook (unused in the current UI mode).
     */
    public void floatPetals() {
    }

    public boolean hasFlower() {
        return hasFlower;
    }

    /**
     * Stops any tile-specific effects (no-op for this implementation).
     */
    public void stop() {
    }
}
