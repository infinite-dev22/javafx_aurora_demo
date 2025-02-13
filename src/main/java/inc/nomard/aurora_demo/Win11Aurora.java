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
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public class Win11Aurora extends Application {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int NUM_LAYERS = 2;
    private static final double BLUR_AMOUNT = 150;

    private final double[] offsets = new double[NUM_LAYERS];
    private final Rectangle[] layers = new Rectangle[NUM_LAYERS];
    private final Color[] colors = {
            Color.color(0.33725491166114807, 0, 0.6000000238418579),
            Color.color(1, 0, 0.9019607901573181)
    };

    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();
        root.setBackground(new Background(new BackgroundFill(Color.color(0.125490203499794, 0, 0.2235294133424759), null, null)));
//        root.setEffect(new BoxBlur(BLUR_AMOUNT, BLUR_AMOUNT, 3));

        // Create gradient layers
        for (int i = 0; i < NUM_LAYERS; i++) {
            layers[i] = new Rectangle(WIDTH, HEIGHT);
            layers[i].setOpacity(0.6);
            root.getChildren().add(layers[i]);
        }

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(20), e -> updateBackground())
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        primaryStage.setScene(new Scene(root, WIDTH, HEIGHT));
        primaryStage.show();
    }

    private void updateBackground() {
        for (int i = 0; i < NUM_LAYERS; i++) {
            offsets[i] += (i + 1) * 0.1;

            // Create animated gradient
            LinearGradient gradient = new LinearGradient(
                    0, 0, 1, 1,
                    true, CycleMethod.NO_CYCLE,
                    new Stop(0, colors[i].deriveColor(0, 1, 1, 0.8)),
                    new Stop(1, colors[(i + 1) % colors.length].deriveColor(0, 1, 1, 0.4))
            );

            // In updateBackground()
            double progress = (Math.sin(offsets[i] * 0.01) * 0.5 + 0.5);
            Color interpolated = colors[i].interpolate(colors[(i + 1) % colors.length], progress);

            // Add transitions between color stops
            Stop[] stops = {
                    new Stop(0, interpolated.deriveColor(0, 1, 1, 0.8)),
                    new Stop(0.5, colors[(i + 2) % colors.length].deriveColor(0, 1, 1, 0.6)),
                    new Stop(1, interpolated.deriveColor(0, 1, 1, 0.4))
            };

            // Different blur amounts per layer
            var blurs = List.of(new BoxBlur(120, 120, 3),
                    new BoxBlur(80, 80, 2),
                    new BoxBlur(50, 50, 2));

            // Apply animated transformation
            layers[i].setFill(gradient);
            layers[i].setTranslateX(Math.cos(offsets[i] * 0.005) * 100);
            layers[i].setTranslateY(Math.sin(offsets[i] * 0.003) * 50);
            layers[i].setRotate(offsets[i] * 0.1);
            layers[i].setEffect(blurs.get(1));
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}