package inc.nomard.aurora_demo;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;
import javafx.util.Duration;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.Pane;
import javafx.scene.paint.*;
import javafx.stage.Stage;
import javafx.util.Duration;

public class AuroraBackground extends Application {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;
    private static final int NUM_LAYERS = 5;
    private static final double BLUR_AMOUNT = 100;

    private final double[] hueAngles = new double[NUM_LAYERS];
    private final double[] opacityAngles = new double[NUM_LAYERS];
    private final double[] movementAngles = new double[NUM_LAYERS];

    @Override
    public void start(Stage primaryStage) {
        Pane root = new Pane();
        Canvas canvas = new Canvas(WIDTH, HEIGHT);
        root.getChildren().add(canvas);

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setGlobalBlendMode(BlendMode.SOFT_LIGHT);
        gc.setEffect(new BoxBlur(BLUR_AMOUNT, BLUR_AMOUNT, 3));

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(16), e -> updateAurora(gc))
        );
        timeline.setCycleCount(Animation.INDEFINITE);
        timeline.play();

        primaryStage.setScene(new Scene(root, WIDTH, HEIGHT));
        primaryStage.show();
    }

    private void updateAurora(GraphicsContext gc) {
        gc.clearRect(0, 0, WIDTH, HEIGHT);

        for (int i = 0; i < NUM_LAYERS; i++) {
            // Animate parameters at different speeds
            movementAngles[i] += 0.3 + (i * 0.05);
            hueAngles[i] += 0.5 + (i * 0.1);
            opacityAngles[i] += 0.2 + (i * 0.05);

            // Calculate dynamic properties
            double xOffset = Math.cos(Math.toRadians(movementAngles[i])) * WIDTH / 3;
            double yOffset = Math.sin(Math.toRadians(movementAngles[i])) * HEIGHT / 4;
            double hue = (Math.sin(Math.toRadians(hueAngles[i])) * 60) + 200;
            double opacity = (Math.sin(Math.toRadians(opacityAngles[i])) * 0.2 + 0.3);

            // Create gradient with current values
            RadialGradient gradient = new RadialGradient(
                    0, 0,
                    WIDTH / 2 + xOffset,
                    HEIGHT / 2 + yOffset,
                    WIDTH * 0.8,
                    false,
                    CycleMethod.NO_CYCLE,
                    new Stop(0, Color.hsb(hue, 0.6, 0.8, opacity)),
                    new Stop(1, Color.TRANSPARENT)
            );

            gc.setFill(gradient);
            gc.fillRect(0, 0, WIDTH, HEIGHT);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}