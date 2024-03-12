package app.earningreportaction;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class MainApp extends Application {

    public void start(Stage primaryStage) {
        try {
            // Replace '/your/fxml/file.fxml' with the path to your actual FXML file
            Parent root = FXMLLoader.load(getClass().getResource("main-view.fxml"));
            Scene scene = new Scene(root);
            primaryStage.setScene(scene);
            primaryStage.setTitle("Earnings Report Action");
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}