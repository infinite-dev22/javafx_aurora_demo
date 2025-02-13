package inc.nomard.aurora_demo;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.*;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Random;

public class AuroraCircles extends Application {

    private static final int NUM_LAYERS = 6;
    private static final double MAX_BLUR = 200;
    private static final double MIN_BLUR = 80;
    private static final double BASE_OPACITY = 0.7;

    private final Color[] COLOR_PALETTE = {
            Color.color(0.125, 0, 0.224),    // Deep purple
            Color.rgb(64, 13, 103),          // Royal purple
            Color.color(0.337, 0, 0.6),      // Electric purple
            Color.color(0.565, 0, 1),        // Vivid violet
            Color.color(1, 0, 0.902),        // Hot pink
            Color.rgb(204, 25, 168)          // Neon pink
    };

    private final AuroraLayer[] layers = new AuroraLayer[NUM_LAYERS];
    private Timeline animationTimeline;
    private double globalTime = 0;

    @Override
    public void start(Stage stage) {
        Pane root = createRootPane();
        initializeLayers(root);
        setupAnimation();

        Scene scene = new Scene(root, 1280, 720);
        stage.setScene(scene);
        stage.show();

        // Initial update after stage is visible
        updateLayerSizes(scene.getWidth(), scene.getHeight());
        setupResizeListeners(scene);
    }

    private Pane createRootPane() {
        Pane pane = new Pane();
        pane.setBackground(new Background(new BackgroundFill(
                Color.rgb(15, 15, 15),  // Dark background
                null,
                null
        )));
        return pane;
    }

    private void initializeLayers(Pane parent) {
        Random rand = new Random();
        for (int i = 0; i < NUM_LAYERS; i++) {
            layers[i] = new AuroraLayer(parent, i, rand);
        }
    }

    private void setupAnimation() {
        animationTimeline = new Timeline(
                new KeyFrame(Duration.millis(16), e -> {
                    globalTime += 0.016;
                    updateLayers();
                })
        );
        animationTimeline.setCycleCount(Animation.INDEFINITE);
        animationTimeline.play();
    }

    private void setupResizeListeners(Scene scene) {
        scene.widthProperty().addListener((obs, ov, nv) ->
                updateLayerSizes(nv.doubleValue(), scene.getHeight())
        );

        scene.heightProperty().addListener((obs, ov, nv) ->
                updateLayerSizes(scene.getWidth(), nv.doubleValue())
        );
    }

    private void updateLayerSizes(double width, double height) {
        for (AuroraLayer layer : layers) {
            layer.updateSize(width, height);
        }
    }

    private void updateLayers() {
        for (AuroraLayer layer : layers) {
            layer.update(globalTime);
        }
    }

    private class AuroraLayer {
        private final Circle circle;
        private final int layerIndex;
        private final Random rand;
        private final double[] motionParams;
        private double baseX, baseY;

        AuroraLayer(Pane parent, int index, Random random) {
            this.layerIndex = index;
            this.rand = random;
            this.motionParams = new double[4];
            this.circle = createCircle(parent);
            initializeMotionParams();
        }

        private Circle createCircle(Pane parent) {
            Circle c = new Circle();
            c.setOpacity(BASE_OPACITY);
            c.setBlendMode(BlendMode.SCREEN);  // Soft blending
            parent.getChildren().add(c);
            return c;
        }

        private void initializeMotionParams() {
            for (int i = 0; i < motionParams.length; i++) {
                motionParams[i] = rand.nextDouble() * 2 * Math.PI;
            }
        }

        void update(double time) {
            // Smooth color transition
            Color color = calculateColor(time);

            // Organic motion calculations
            double xOffset = calculateXOffset(time);
            double yOffset = calculateYOffset(time);
            double scale = calculateScale(time);
            double rotation = calculateRotation(time);
            double blur = calculateBlur();

            updateVisuals(xOffset, yOffset, scale, rotation, blur, color);
        }

        private Color calculateColor(double time) {
            double colorProgress = (time * 0.15 + layerIndex * 0.2) % COLOR_PALETTE.length;
            int index1 = (int) colorProgress;
            int index2 = (index1 + 1) % COLOR_PALETTE.length;
            double blend = colorProgress - index1;

            return interpolateColor(
                    COLOR_PALETTE[index1],
                    COLOR_PALETTE[index2],
                    smoothStep(blend)
            );
        }

        private Color interpolateColor(Color c1, Color c2, double t) {
            return c1.interpolate(c2, t);
        }

        private double smoothStep(double t) {
            return t * t * (3 - 2 * t);
        }

        private double calculateXOffset(double time) {
            return Math.sin(time * 0.35 + motionParams[0]) * 250
                    + Math.cos(time * 0.25 + motionParams[1]) * 150;
        }

        private double calculateYOffset(double time) {
            return Math.sin(time * 0.3 + motionParams[2]) * 200
                    + Math.cos(time * 0.4 + motionParams[3]) * 100;
        }

        private double calculateScale(double time) {
            return 0.8 + Math.sin(time * 0.4) * 0.3;
        }

        private double calculateRotation(double time) {
            return Math.sin(time * 0.2) * 15;
        }

        private double calculateBlur() {
            return MIN_BLUR + (MAX_BLUR - MIN_BLUR) *
                    (1 - (layerIndex / (double) NUM_LAYERS));
        }

        private void updateVisuals(double xOffset, double yOffset,
                                   double scale, double rotation,
                                   double blur, Color color) {
            // Update gradient with dynamic center
            RadialGradient gradient = new RadialGradient(
                    0, 0,
                    circle.getCenterX() + xOffset * 0.5,
                    circle.getCenterY() + yOffset * 0.5,
                    circle.getRadius(),
                    false,
                    CycleMethod.NO_CYCLE,
                    new Stop(0, color.deriveColor(0, 1, 1.2, 0.9)),
                    new Stop(0.7, color.deriveColor(0, 1, 0.8, 0.4)),
                    new Stop(1, color.deriveColor(0, 1, 0.5, 0.1))
            );

            circle.setFill(gradient);
            circle.setEffect(new BoxBlur(blur, blur, 3));
            circle.setScaleX(scale);
            circle.setScaleY(scale);
            circle.setRotate(rotation);

            // Apply smooth movement
            circle.setCenterX(baseX + xOffset);
            circle.setCenterY(baseY + yOffset);
        }

        void updateSize(double width, double height) {
            baseX = width / 2;
            baseY = height / 2;
            double radius = Math.hypot(width, height) * 0.8;
            circle.setRadius(radius);
            circle.setCenterX(baseX);
            circle.setCenterY(baseY);
        }
    }

    @Override
    public void stop() {
        if (animationTimeline != null) {
            animationTimeline.stop();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}