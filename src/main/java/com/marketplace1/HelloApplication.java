package com.marketplace1;

import javafx.application.Application;
import javafx.stage.Stage;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) {
        HelloController controller = new HelloController();
        controller.openLoginWindow(stage);
        stage.show();   // <-- আগে এই লাইনটা ছিল না, তাই কোনো window-ই আসত না
    }

    public static void main(String[] args) {
        launch(args);
    }
}