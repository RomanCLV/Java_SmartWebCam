package launcher;

import fxmlController.ApplicationController;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import utils.Utils;

import javax.imageio.ImageIO;

public class Launcher extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    /**
     * The method call by the application to run
     * @param primaryStage The current stage
     * @throws IOException if error
     */
    @Override
    public void start(Stage primaryStage) throws IOException {

        ArrayList<String> allLabels = Utils.readFile("inception5h/labels.txt");
        byte[] graphDef = Utils.readFileToBytes("inception5h/tensorflow_inception_graph.pb");

        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource("/fxml/application.fxml"));
        Parent root = loader.load();

        ApplicationController controller = loader.getController();

        primaryStage.setOnCloseRequest(we -> controller.onExit());

        controller.setOwner(primaryStage);
        controller.setAllLabels(allLabels);
        controller.setGraphDef(graphDef);

        Scene scene = new Scene(root);
        primaryStage.setTitle("Smart WebCam");
        String separator = System.getProperty("os.name").toLowerCase().contains("win") ? "/" : "//";
        String srcIcon = System.getProperty("user.dir") + "/src/main/resources/images/webcam-icon.png";
        primaryStage.getIcons().add(new Image("file:" + separator + srcIcon));
        primaryStage.setResizable(false);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
