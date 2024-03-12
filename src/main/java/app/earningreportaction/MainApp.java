package app.earningreportaction;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

import java.util.Objects;

public class MainApp extends Application {

    public void start(Stage primaryStage) {
        try {
            // Replace '/your/fxml/file.fxml' with the path to your actual FXML file
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/app/earningreportaction/main-view.fxml")));
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Earnings Report Action");
            primaryStage.show();
        } catch (Exception e) {
            System.out.println("Failed to Load FXML File" + e.fillInStackTrace());
            System.exit(-1);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}