package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.sql.SQLException;

public class FridgeFX extends Application {

    public static void main(String[] args) throws SQLException, ClassNotFoundException {
        launch(args);
    }


    public void start(Stage stage) throws Exception {
        FridgeDSC.connect();
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        stage.setTitle("What's in My Fridge v1.0");
        stage.setScene(new Scene(root, 550, 500));
        stage.setResizable(false);
        stage.initStyle(StageStyle.UNIFIED);
        stage.centerOnScreen();
        stage.show();

    }

    public void stop() throws Exception {
        FridgeDSC.disconnect();
    }

    public enum FILTER_COLUMNS {
        ITEM,
        SECTION,
        BOUGHT_DAYS_AGO
    }
}
