package edu.virginia.sde.reviews;

import com.sun.javafx.logging.PlatformLogger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.stage.Stage;


import org.hibernate.Session;

public class CourseReviewsApplication extends Application {

    static TextField username;
    static TextField password;
    public static void main(String[] args) {
        launch(args);
    }
    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(LoginController.class.getResource("/edu/virginia/sde/reviews/login.fxml"));
        Parent root = loader.load();
        Scene scene = new Scene(root);
        primaryStage.setScene(scene);
        primaryStage.show();
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
    }

}
