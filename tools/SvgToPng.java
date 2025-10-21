package tools;

import javafx.application.Application;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.Scene;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.io.File;

/**
 * Simple JavaFX app to render an SVG file in a WebView and save a PNG snapshot.
 * Run with: mvn -q -DskipTests exec:java -Dexec.mainClass="tools.SvgToPng" -Dexec.args="path/to/input.svg path/to/output.png"
 */
public class SvgToPng extends Application {
    private static String[] argsStatic;

    public static void main(String[] args) {
        argsStatic = args;
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        if (argsStatic == null || argsStatic.length < 2) {
            System.err.println("Usage: SvgToPng <input.svg> <output.png>");
            System.exit(2);
        }

        String input = argsStatic[0];
        String output = argsStatic[1];

        WebView webView = new WebView();
        webView.getEngine().load(new File(input).toURI().toString());

        webView.setPrefWidth(1200);
        webView.setPrefHeight(700);

        Scene scene = new Scene(webView);
        primaryStage.setScene(scene);
        primaryStage.show();

        // Allow the engine time to render; snapshot after short delay
        webView.getEngine().getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
            try {
                Thread.sleep(250);
                WritableImage image = webView.snapshot(new SnapshotParameters(), null);
                ImageIO.write(SwingFXUtils.fromFXImage(image, null), "png", new File(output));
                System.out.println("Saved PNG to: " + output);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                System.exit(0);
            }
        });
    }
}
