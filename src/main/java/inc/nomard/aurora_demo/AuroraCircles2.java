package inc.nomard.aurora_demo;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.CacheHint;
import javafx.scene.Scene;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;

public class AuroraCircles2 extends Application {

    private static final int NUM_LAYERS = 6;
    private static final double BASE_OPACITY = 0.7;
    private static final int MAX_GRADIENT_CACHE = 50;

    private final Color[] COLOR_PALETTE = {
            Color.color(0.125, 0, 0.224),    // Deep purple
            Color.rgb(64, 13, 103),          // Royal purple
            Color.color(0.337, 0, 0.6),      // Electric purple
            Color.color(0.565, 0, 1),        // Vivid violet
            Color.color(1, 0, 0.902),        // Hot pink
            Color.rgb(204, 25, 168)          // Neon pink
    };

    private final AuroraLayer[] layers = new AuroraLayer[NUM_LAYERS];
    private final BoxBlur sharedBlur = new BoxBlur();
    private double globalTime = 0;

    @Override
    public void start(Stage stage) {
        Pane root = createRootPane();
        initializeLayers(root);
        setupAnimation();

        Scene scene = new Scene(root, 1280, 720);
        stage.setScene(scene);
        stage.show();

        setupResizeListeners(scene);
        updateLayerSizes(scene.getWidth(), scene.getHeight());
    }

    private Pane createRootPane() {
        Pane pane = new Pane();
        pane.setBackground(new Background(new BackgroundFill(
                Color.rgb(15, 15, 15), null, null
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
        AnimationTimer timer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 16_000_000) { // 60 FPS
                    globalTime += 0.016;
                    updateLayers();
                    lastUpdate = now;
                }
            }
        };
        timer.start();
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
        private final LinkedHashMap<ColorPair, RadialGradient> gradientCache =
                new LinkedHashMap<ColorPair, RadialGradient>(MAX_GRADIENT_CACHE, 0.75f, true) {
                    @Override
                    protected boolean removeEldestEntry(Map.Entry eldest) {
                        return size() > MAX_GRADIENT_CACHE;
                    }
                };

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
            c.setCache(true);
            c.setCacheHint(CacheHint.SPEED);
            parent.getChildren().add(c);
            return c;
        }

        private void initializeMotionParams() {
            for (int i = 0; i < motionParams.length; i++) {
                motionParams[i] = rand.nextDouble() * 2 * Math.PI;
            }
        }

        void update(double time) {
            Color color = calculateColor(time);
            double xOffset = calculateXOffset(time);
            double yOffset = calculateYOffset(time);
            double scale = 0.8 + Math.sin(time * 0.4) * 0.3;
            double blur = 80 + (120 * (1 - (layerIndex / (double) NUM_LAYERS)));

            updateVisuals(xOffset, yOffset, scale, blur, color);
        }

        private Color calculateColor(double time) {
            double colorProgress = (time * 0.15 + layerIndex * 0.2) % COLOR_PALETTE.length;
            int index1 = (int) colorProgress;
            int index2 = (index1 + 1) % COLOR_PALETTE.length;
            double blend = smoothStep(colorProgress - index1);

            return COLOR_PALETTE[index1].interpolate(
                    COLOR_PALETTE[index2],
                    blend
            );
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

        private void updateVisuals(double xOffset, double yOffset,
                                   double scale, double blur, Color color) {
            RadialGradient gradient = getCachedGradient(
                    color,
                    baseX + xOffset * 0.5,
                    baseY + yOffset * 0.5,
                    circle.getRadius()
            );

            circle.setFill(gradient);
            circle.setScaleX(scale);
            circle.setScaleY(scale);
            circle.setCenterX(baseX + xOffset);
            circle.setCenterY(baseY + yOffset);

            sharedBlur.setWidth(blur);
            sharedBlur.setHeight(blur);
            circle.setEffect(sharedBlur);
        }

        private RadialGradient getCachedGradient(Color color, double x, double y, double radius) {
            ColorPair key = new ColorPair(color, x, y, radius);
            return gradientCache.computeIfAbsent(key, k ->
                    new RadialGradient(
                            0, 0,
                            x, y,
                            radius,
                            false,
                            CycleMethod.NO_CYCLE,
                            new Stop(0, color.deriveColor(0, 1, 1.2, 0.9)),
                            new Stop(0.7, color.deriveColor(0, 1, 0.8, 0.4)),
                            new Stop(1, color.deriveColor(0, 1, 0.5, 0.1))
                    )
            );
        }

        void updateSize(double width, double height) {
            baseX = width / 2;
            baseY = height / 2;
            double radius = Math.hypot(width, height) * 0.8;
            circle.setRadius(radius);
            circle.setCenterX(baseX);
            circle.setCenterY(baseY);
        }

        private record ColorPair(Color color, double x, double y, double radius) {}
    }

    public static void main(String[] args) {
        launch(args);
    }
}