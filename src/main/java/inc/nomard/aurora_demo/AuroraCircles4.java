package inc.nomard.aurora_demo;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.CacheHint;
import javafx.scene.Scene;
import javafx.scene.effect.BlendMode;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;

public class AuroraCircles4 extends Application {

    private static final int NUM_LAYERS = 6;
    private static final double BASE_OPACITY = 0.7;
    private static final int FRAME_TIMEOUT_MS = 8;

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
    private final ExecutorService threadPool = Executors.newVirtualThreadPerTaskExecutor();
    private final AtomicReference<Double> globalTime = new AtomicReference<>(0.0);
    private AnimationTimer animationTimer;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        Pane root = createRootPane();
        initializeLayers(root);
        setupAnimation();

        Scene scene = new Scene(root, 1280, 720);
        scene.setFill(null);

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
        pane.setCache(true);
        pane.setCacheHint(CacheHint.SPEED);
        return pane;
    }

    private void initializeLayers(Pane parent) {
        Random rand = new Random();
        for (int i = 0; i < NUM_LAYERS; i++) {
            layers[i] = new AuroraLayer(parent, i, rand);
        }
    }

    private void setupAnimation() {
        animationTimer = new AnimationTimer() {
            private final List<Future<LayerState>> futures = new ArrayList<>(NUM_LAYERS);
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (now - lastUpdate >= 16_666_667) { // ~60 FPS
                    try {
                        // Submit parallel layer calculations
                        futures.clear();
                        for (AuroraLayer layer : layers) {
                            futures.add(threadPool.submit(layer.new ComputeTask(globalTime.get())));
                        }

                        // Collect results with timeout
                        List<LayerState> states = new ArrayList<>(NUM_LAYERS);
                        for (Future<LayerState> future : futures) {
                            states.add(future.get(FRAME_TIMEOUT_MS, TimeUnit.MILLISECONDS));
                        }

                        // Batch UI updates
                        Platform.runLater(() -> {
                            for (int i = 0; i < layers.length; i++) {
                                layers[i].applyState(states.get(i));
                            }
                        });

                        globalTime.set(globalTime.get() + 0.016);
                        lastUpdate = now;
                    } catch (TimeoutException e) {
                        System.err.println("Frame computation timeout - skipping render");
                    } catch (Exception e) {
                        e.printStackTrace();
                        stop();
                    }
                }
            }
        };
        animationTimer.start();
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

    @Override
    public void stop() {
        if (animationTimer != null) {
            animationTimer.stop();
        }
        threadPool.shutdownNow();
    }

    private static class LayerState {
        Color color;
        double xOffset;
        double yOffset;
        double scale;
        double blur;
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
            c.setCache(true);
            c.setCacheHint(CacheHint.SPEED);
            c.setMouseTransparent(true);
            parent.getChildren().add(c);
            return c;
        }

        private void initializeMotionParams() {
            for (int i = 0; i < motionParams.length; i++) {
                motionParams[i] = rand.nextDouble() * 2 * Math.PI;
            }
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

        private double calculateScale(double time) {
            return 0.8 + Math.sin(time * 0.4) * 0.3;
        }

        private double calculateBlur() {
            return 80 + (120 * (1 - (layerIndex / (double) NUM_LAYERS)));
        }

        void applyState(LayerState state) {
            RadialGradient gradient = new RadialGradient(
                    0, 0,
                    circle.getCenterX() + state.xOffset * 0.5,
                    circle.getCenterY() + state.yOffset * 0.5,
                    circle.getRadius(),
                    false,
                    CycleMethod.NO_CYCLE,
                    new Stop(0, state.color.deriveColor(0, 1, 1.2, 0.9)),
                    new Stop(0.7, state.color.deriveColor(0, 1, 0.8, 0.4)),
                    new Stop(1, state.color.deriveColor(0, 1, 0.5, 0.1))
            );

            circle.setFill(gradient);
            circle.setScaleX(state.scale);
            circle.setScaleY(state.scale);
            circle.setCenterX(baseX + state.xOffset);
            circle.setCenterY(baseY + state.yOffset);

            sharedBlur.setWidth(state.blur);
            sharedBlur.setHeight(state.blur);
            circle.setEffect(sharedBlur);
        }

        void updateSize(double width, double height) {
            baseX = width / 2;
            baseY = height / 2;
            double radius = Math.hypot(width, height) * 0.8;
            circle.setRadius(radius);
            circle.setCenterX(baseX);
            circle.setCenterY(baseY);
        }

        class ComputeTask implements Callable<LayerState> {
            private final double currentTime;

            ComputeTask(double currentTime) {
                this.currentTime = currentTime;
            }

            @Override
            public LayerState call() {
                LayerState state = new LayerState();
                state.color = calculateColor(currentTime);
                state.xOffset = calculateXOffset(currentTime);
                state.yOffset = calculateYOffset(currentTime);
                state.scale = calculateScale(currentTime);
                state.blur = calculateBlur();
                return state;
            }
        }
    }
}