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
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.Random;

public class SmoothAurora extends Application {

    private static final int INIT_WIDTH = 1280;
    private static final int INIT_HEIGHT = 720;
    private static final int INIT_RADIUS = 720;
    private static final int NUM_LAYERS = 4;
    private static final double MAX_BLUR = 100.0;
    private static final double MIN_BLUR = 80.0;

    private static final Color[] COLOR_PALETTE = {
            Color.color(0.125490203499794, 0, 0.2235294133424759),
            Color.rgb(64, 13, 103),
            Color.color(0.33725491166114807, 0, 0.6000000238418579),
            Color.color(0.565454, 0, 1),
            Color.color(1, 0, 0.9019607901573181),
            Color.rgb(204, 25, 168)
    };

    private final AuroraLayer[] layers = new AuroraLayer[NUM_LAYERS];
    private Timeline animationTimeline;
    private double globalTime = 0;

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
    }

    private Pane createRootPane() {
        Pane pane = new Pane();
        pane.setBackground(new Background(new BackgroundFill(
                Color.rgb(32, 32, 32), null, null)));
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

    private void updateLayers() {
        for (AuroraLayer layer : layers) {
            layer.update(globalTime);
        }
    }

    @Override
    public void stop() {
        if (animationTimeline != null) {
            animationTimeline.stop();
        }
    }

    private class AuroraLayer {
        private final Circle rectangle;
        private final int layerIndex;
        private final Random rand;
        private final double[] motionParams = new double[4];

        AuroraLayer(Pane parent, int index, Random random) {
            this.layerIndex = index;
            this.rand = random;
            this.rectangle = createCircle();
            parent.getChildren().add(rectangle);
            initializeMotionParams();
        }

        private Circle createCircle() {
            Circle rect = new Circle(INIT_RADIUS);
            rect.setOpacity(0.8);
            return rect;
        }

        private void initializeMotionParams() {
            for (int i = 0; i < motionParams.length; i++) {
                motionParams[i] = rand.nextDouble() * 2 * Math.PI;
            }
        }

        void update(double time) {
            // Smooth color transition through palette
            double colorProgress = (time * 0.1) % COLOR_PALETTE.length;
            int colorIndex = (int) colorProgress;
            double blend = colorProgress - colorIndex;
            Color color = interpolateColors(
                    COLOR_PALETTE[colorIndex % COLOR_PALETTE.length],
                    COLOR_PALETTE[(colorIndex + 1) % COLOR_PALETTE.length],
                    blend
            );

            // Organic motion using combined sine waves
            double xOffset = calculateOffset(time, 0) * INIT_WIDTH / 3;
            double yOffset = calculateOffset(time, 1) * INIT_HEIGHT / 3;
            double rotation = calculateRotation(time);
            double scale = calculateScale(time);
            double blur = calculateBlur();

            updateCircle(xOffset, yOffset, rotation, scale, blur, color);
        }

        private Color interpolateColors(Color c1, Color c2, double t) {
            return c1.interpolate(c2, t);
        }

        private double calculateOffset(double time, int paramIndex) {
            return Math.sin(time * 0.5 + motionParams[paramIndex]) * 0.8
                    + Math.cos(time * 0.3 + motionParams[paramIndex + 1]) * 0.4;
        }

        private double calculateRotation(double time) {
            return Math.sin(time * 0.2 + motionParams[2]) * 15;
        }

        private double calculateScale(double time) {
            return 0.8 + Math.sin(time * 0.4 + motionParams[3]) * 0.2;
        }

        private double calculateBlur() {
            return MIN_BLUR + (MAX_BLUR - MIN_BLUR) * (layerIndex / (double) NUM_LAYERS);
        }

        private void updateCircle(double x, double y, double rot,
                                     double scale, double blur, Color color) {
            LinearGradient gradient = new LinearGradient(
                    0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                    new Stop(0, color.deriveColor(0, 1, 1, 0.9)),
                    new Stop(1, color.deriveColor(30, 1, 0.9, 0.6))
            );

            rectangle.setFill(gradient);
            rectangle.setTranslateX(x);
            rectangle.setTranslateY(y);
            rectangle.setRotate(rot);
            rectangle.setScaleX(scale);
            rectangle.setScaleY(scale);
            rectangle.setEffect(new BoxBlur(blur, blur, 10));
        }
    }
}