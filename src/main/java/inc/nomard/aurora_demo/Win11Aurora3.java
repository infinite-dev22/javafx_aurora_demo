package inc.nomard.aurora_demo;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.WeakHashMap;

public class Win11Aurora3 extends Application {

    private static final int INIT_WIDTH = 1280;
    private static final int INIT_HEIGHT = 720;
    private static final int INIT_RADIUS = 300;
    private static final int NUM_LAYERS = 6;
    private static final double TRANSITION_TIME = 5.0;
    private static final double MAX_BLUR = 100.0;
    private static final double MIN_BLUR = 90.0;

    private static final Color[] COLOR_PALETTE = {
            Color.color(0.125490203499794, 0, 0.2235294133424759),
            Color.rgb(64, 13, 103),
            Color.color(0.33725491166114807, 0, 0.6000000238418579),
            Color.color(0.565454, 0, 1),
            Color.color(1, 0, 0.9019607901573181),
            Color.rgb(204, 25, 168)
    };

    private final AuroraLayer[] layers = new AuroraLayer[NUM_LAYERS];
    private final Random random = new Random();
    private Timeline animationTimeline;

    private static Color safeInterpolateColor(Color start, Color end, double t) {
        if (start == null || end == null) return COLOR_PALETTE[0];
        return start.interpolate(end, t);
    }

    private static double lerp(double start, double end, double t) {
        return start + t * (end - start);
    }

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        Pane root = createRootPane();
        initializeLayers(root);
        setupAnimation();

        Scene scene = new Scene(root, INIT_WIDTH, INIT_HEIGHT);
        stage.setScene(scene);
        stage.show();
        setupWindowListeners(scene);
    }

    private Pane createRootPane() {
        Pane pane = new Pane();
        pane.setBackground(new Background(new BackgroundFill(
                Color.rgb(32, 32, 32), null, null
        )));
        return pane;
    }

    private void initializeLayers(Pane parent) {
        for (int i = 0; i < NUM_LAYERS; i++) {
            layers[i] = new AuroraLayer(parent);
            safeRandomizeLayer(i, true);
        }
    }

    private void setupAnimation() {
        animationTimeline = new Timeline(
                new KeyFrame(Duration.millis(16), e -> updateLayers())
        );
        animationTimeline.setCycleCount(Animation.INDEFINITE);

        Timeline randomizer = new Timeline(
                new KeyFrame(Duration.seconds(TRANSITION_TIME), e -> safeRandomizeAllLayers())
        );
        randomizer.setCycleCount(Animation.INDEFINITE);

        animationTimeline.play();
        randomizer.play();
    }

    private void setupWindowListeners(Scene scene) {
        scene.widthProperty().addListener((obs, ov, nv) ->
                updateLayerSizes(INIT_RADIUS)
        );

        scene.heightProperty().addListener((obs, ov, nv) ->
                updateLayerSizes(INIT_RADIUS)
        );
    }

    private void updateLayerSizes(double radius) {
        for (AuroraLayer layer : layers) {
            if (layer != null) {
                layer.resize(radius);
            }
        }
    }

    private void updateLayers() {
        final double timeStep = 0.016;
        for (AuroraLayer layer : layers) {
            if (layer != null) {
                layer.update(timeStep, TRANSITION_TIME);
            }
        }
    }

    private void safeRandomizeAllLayers() {
        for (int i = 0; i < NUM_LAYERS; i++) {
            safeRandomizeLayer(i, false);
        }
    }

    private void safeRandomizeLayer(int index, boolean initial) {
        try {
            AuroraLayer layer = layers[index];
            layer.prepareTransition();

            Color baseColor = COLOR_PALETTE[random.nextInt(COLOR_PALETTE.length)];
            layer.targetColor = baseColor.deriveColor(
                    random.nextDouble() * 15, 1, 1, 0.8
            );

            layer.targetXSpeed = 0.3 + random.nextDouble() * 0.7;
            layer.targetYSpeed = 0.3 + random.nextDouble() * 0.7;
            layer.targetRotation = (random.nextDouble() - 0.5) * 0.15;

            double depthFactor = (double) index / NUM_LAYERS;
            layer.targetBlur = MIN_BLUR + (MAX_BLUR - MIN_BLUR) * depthFactor;
            layer.targetScale = 0.8 + (0.4 * depthFactor);

            if (initial) layer.completeTransition();
        } catch (Exception e) {
            System.err.println("Layer randomization error: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        if (animationTimeline != null) {
            animationTimeline.stop();
        }
    }

    private static class AuroraLayer {
        private final Pane parent;
        private final RectangleWrapper rectangle;

        private double time = 0;
        private double currentXSpeed = 0.5;
        private double targetXSpeed = 0.5;
        private double currentYSpeed = 0.5;
        private double targetYSpeed = 0.5;
        private double currentRotation = 0;
        private double targetRotation = 0;
        private Color currentColor = COLOR_PALETTE[0];
        private Color targetColor = COLOR_PALETTE[0];
        private double currentBlur = MIN_BLUR;
        private double targetBlur = MIN_BLUR;
        private double currentScale = 1.0;
        private double targetScale = 1.0;

        AuroraLayer(Pane parent) {
            this.parent = parent;
            this.rectangle = new RectangleWrapper();
            parent.getChildren().add(rectangle.node);
        }

        void prepareTransition() {
            currentXSpeed = targetXSpeed;
            currentYSpeed = targetYSpeed;
            currentRotation = targetRotation;
            currentColor = targetColor != null ? targetColor : COLOR_PALETTE[0];
            currentBlur = targetBlur;
            currentScale = targetScale;
            time = 0;
        }

        void completeTransition() {
            targetXSpeed = currentXSpeed;
            targetYSpeed = currentYSpeed;
            targetRotation = currentRotation;
            targetColor = currentColor;
            targetBlur = currentBlur;
            targetScale = currentScale;
        }

        void update(double deltaTime, double transitionTime) {
            time += deltaTime;
            double progress = Math.min(time / transitionTime, 1.0);

            double xSpeed = lerp(currentXSpeed, targetXSpeed, progress);
            double ySpeed = lerp(currentYSpeed, targetYSpeed, progress);
            double rotation = lerp(currentRotation, targetRotation, progress);
            Color color = safeInterpolateColor(currentColor, targetColor, progress);
            double blur = lerp(currentBlur, targetBlur, progress);
            double scale = lerp(currentScale, targetScale, progress);

            double parentWidth = parent.getWidth() > 0 ? parent.getWidth() : INIT_WIDTH;
            double parentHeight = parent.getHeight() > 0 ? parent.getHeight() : INIT_HEIGHT;
            double parentRadius = parent.getWidth() > 0 ? parent.getWidth() : INIT_RADIUS;

            double xOffset = parentWidth / 2 * Math.sin(xSpeed * time);
            double yOffset = parentHeight / 2 * Math.cos(ySpeed * time);

            rectangle.update(
                    xOffset,
                    yOffset,
                    rotation * time,
                    scale,
                    blur,
                    color,
                    parentRadius
            );
        }

        void resize(double radius) {
            rectangle.resize(radius);
        }
    }

    private static class RectangleWrapper {
        private final Circle node = new Circle();
        private final GradientManager gradientManager = new GradientManager();

        RectangleWrapper() {
            node.setOpacity(0.8);  // Increased base opacity
            node.setFill(gradientManager.updateGradient(COLOR_PALETTE[0]));
        }

        void update(double x, double y, double rotation,
                    double scale, double blur, Color color,
                    double radius) {
            // Update gradient with current color
            node.setFill(gradientManager.updateGradient(color));

            node.setTranslateX(x);
            node.setTranslateY(y);
            node.setRotate(rotation);
            node.setScaleX(scale);
            node.setScaleY(scale);
            node.setEffect(new BoxBlur(blur, blur, 3));

            // Ensure proper sizing
            node.setRadius(radius);
        }

        void resize(double radius) {
            node.setRadius(radius);
        }
    }

    private static class GradientManager {
        private final Map<ColorPair, LinearGradient> gradientCache =
                Collections.synchronizedMap(new WeakHashMap<>());

        LinearGradient updateGradient(Color color) {
            if (color == null) return createFallbackGradient();

            Color start = color.deriveColor(0, 1, 1, 0.9); // More opaque
            Color end = color.deriveColor(30, 1, 0.9, 0.6); // More visible
            return gradientCache.computeIfAbsent(
                    new ColorPair(start, end),
                    k -> new LinearGradient(
                            0, 0,
                            1, 1,
                            true,
                            CycleMethod.NO_CYCLE,
                            new Stop(0, start),
                            new Stop(1, end)
                    )
            );
        }

        private LinearGradient createFallbackGradient() {
            return new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, Color.BLUE),
                    new Stop(1, Color.PURPLE)
            );
        }

        private record ColorPair(Color start, Color end) {
        }
    }
}