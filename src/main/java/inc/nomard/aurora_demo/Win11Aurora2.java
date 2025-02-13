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

import java.util.Random;

public class Win11Aurora2 extends Application {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int NUM_LAYERS = 3;
    private static final double TRANSITION_TIME = 5; // seconds

    private final Random random = new Random();
    private final AuroraLayer[] auroraLayers = new AuroraLayer[NUM_LAYERS];

    // Base colors matching Windows 11 aesthetic
    private final Color[] palette = {
            Color.color(0.125490203499794, 0, 0.2235294133424759),
            Color.color(0.33725491166114807, 0, 0.6000000238418579),
            Color.color(1, 0, 0.9019607901573181)
    };

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();
        root.setBackground(new Background(new BackgroundFill(
                Color.rgb(32, 32, 32), null, null
        )));

        initializeLayers(root);
        setupAnimation();

        primaryStage.setScene(new Scene(root, WIDTH, HEIGHT));
        primaryStage.show();
    }

    private void initializeLayers(Pane root) {
        for (int i = 0; i < NUM_LAYERS; i++) {
            auroraLayers[i] = new AuroraLayer();
            root.getChildren().add(auroraLayers[i].rectangle);
            randomizeLayer(i, true);
        }
    }

    private void setupAnimation() {
        // Main animation timeline (60 FPS)
        Timeline animation = new Timeline(
                new KeyFrame(Duration.millis(16.666), e -> updateLayers())
        );
        animation.setCycleCount(Animation.INDEFINITE);

        // Parameter randomization timeline
        Timeline randomizer = new Timeline(
                new KeyFrame(Duration.seconds(TRANSITION_TIME), e -> randomizeAllLayers())
        );
        randomizer.setCycleCount(Animation.INDEFINITE);

        animation.play();
        randomizer.play();
    }

    private void updateLayers() {
        for (AuroraLayer layer : auroraLayers) {
            updateLayerMotion(layer);
            updateLayerAppearance(layer);
        }
    }

    private void updateLayerMotion(AuroraLayer layer) {
        layer.time += 0.016; // ~60 FPS
        double progress = layer.time / TRANSITION_TIME;

        // Smooth parameter interpolation
        double xParam = interpolate(
                layer.currentXParams, layer.nextXParams, progress
        );
        double yParam = interpolate(
                layer.currentYParams, layer.nextYParams, progress
        );

        // Lissajous-style motion
        layer.rectangle.setTranslateX(
                WIDTH / 2 * Math.sin(xParam * layer.time + layer.xPhase)
        );
        layer.rectangle.setTranslateY(
                HEIGHT / 2 * Math.cos(yParam * layer.time + layer.yPhase)
        );

        // Rotational movement
        layer.rectangle.setRotate(
                layer.rotationSpeed * layer.time * 360 / (2 * Math.PI)
        );
    }

    private void updateLayerAppearance(AuroraLayer layer) {
        // Animated gradient with color transitions
        LinearGradient gradient = new LinearGradient(
                0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, layer.currentColor),
                new Stop(1, layer.nextColor)
        );

        layer.rectangle.setFill(gradient);
        layer.rectangle.setOpacity(
                0.4 + 0.2 * Math.sin(layer.time * Math.PI)
        );
    }

    private void randomizeAllLayers() {
        for (int i = 0; i < NUM_LAYERS; i++) {
            randomizeLayer(i, false);
        }
    }

    private void randomizeLayer(int index, boolean initial) {
        AuroraLayer layer = auroraLayers[index];

        // Store previous values for interpolation
        if (!initial) {
            layer.currentXParams = layer.nextXParams;
            layer.currentYParams = layer.nextYParams;
            layer.currentColor = layer.nextColor;
        }

        // Random motion parameters
        layer.nextXParams = 0.2 + random.nextDouble() * 0.8;
        layer.nextYParams = 0.2 + random.nextDouble() * 0.8;
        layer.xPhase = random.nextDouble() * 2 * Math.PI;
        layer.yPhase = random.nextDouble() * 2 * Math.PI;
        layer.rotationSpeed = (random.nextDouble() - 0.5) * 0.2;

        // Random color progression
        layer.nextColor = palette[random.nextInt(palette.length)]
                .deriveColor(random.nextDouble() * 30, 1, 1, 0.6);

        // Random blur effect
        layer.rectangle.setEffect(new BoxBlur(
                random.nextInt(50) + 80,
                random.nextInt(50) + 80,
                random.nextInt(3) + 2
        ));

        if (initial) {
            layer.currentXParams = layer.nextXParams;
            layer.currentYParams = layer.nextYParams;
            layer.currentColor = layer.nextColor;
            layer.time = 0;
        }
    }

    private double interpolate(double start, double end, double progress) {
        // Smoothstep interpolation
        double t = Math.max(0, Math.min(1, progress));
        t = t * t * (3 - 2 * t);
        return start + t * (end - start);
    }

    private static class AuroraLayer {
        Circle rectangle = new Circle(200);
        double time = 0;
        double currentXParams, nextXParams;
        double currentYParams, nextYParams;
        double xPhase, yPhase;
        double rotationSpeed;
        Color currentColor, nextColor;

        AuroraLayer() {
            rectangle.setOpacity(0.6);
        }
    }
}