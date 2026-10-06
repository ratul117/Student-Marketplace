package com.marketplace1;

import javafx.application.Application;

/**
 * IntelliJ-তে এই ক্লাসটাই Run করুন (HelloApplication না)।
 * Application ক্লাস সরাসরি main হলে classpath মোডে "JavaFX runtime components are missing" আসতে পারে।
 */
public class Launcher {
    public static void main(String[] args) {
        Application.launch(HelloApplication.class, args);
    }
}