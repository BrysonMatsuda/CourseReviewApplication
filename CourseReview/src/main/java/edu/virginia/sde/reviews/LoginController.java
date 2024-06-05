package edu.virginia.sde.reviews;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import jakarta.persistence.*;
import org.hibernate.Session;

import java.io.IOException;
import java.util.List;

public class LoginController {
//    static void setupLogin(Stage primaryStage) throws IOException {
//        var fxmlLoader = new FXMLLoader(LoginController.class.getResource("login.fxml"));
//        var scene = new Scene(fxmlLoader.load());
//    }

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label incorrectLogin1;

    @FXML
    private Label incorrectLogin2;


    public void handleLabelClick(MouseEvent mouseEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader(LoginController.class.getResource("/edu/virginia/sde/reviews/create-new.fxml"));
        //AI Agent: ChatGPT
        //Prompt: How do I make it so that clicking a button in an fxml page sets the current fxml display to a new file?
        Parent createNewRoot = loader.load();
        Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(createNewRoot);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }

    @FXML
    public void handleClose(MouseEvent mouseEvent) throws IOException{
        Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
        stage.close();
//        System.out.println("Application successfully closed");
    }

    public void handleLogin(MouseEvent mouseEvent) throws IOException {
        String username = usernameField.getText();
        String password = passwordField.getText();
        User user = new User(usernameField.getText(), passwordField.getText());

        Session session = HibernateUtil.getSessionFactory().openSession();

        boolean exists = findExistingUser(session, username, password);
        if (exists){
            session.beginTransaction();
            String hql2 = "Select id From User u Where u.username = :username AND u.password = :password";
            Query query2 = session.createQuery(hql2);
            query2.setParameter("username", username);
            query2.setParameter("password", password);
            List result2 = ((org.hibernate.query.Query<?>) query2).list();

            Object obj = result2.get(0);
            int id = (int) obj;
         //   System.out.println(id);
            user.setId(id);

            //System.out.println("Login success");
            UserSession.setCurrentUser(user);
           // System.out.println(user);
            session.getTransaction().commit();
            session.close();
            FXMLLoader loader1 = new FXMLLoader(LoginController.class.getResource("/edu/virginia/sde/reviews/course-search.fxml"));
            Parent createNewRoot = loader1.load();
            Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
            Scene scene = new Scene(createNewRoot, 1280, 720);
            stage.setScene(scene);
            stage.show();
        } else {
            //System.out.println("Login failed. Please enter a valid username and password");
        }

        //Only use this when closing the app
        //HibernateUtil.shutdown();
    }

    private boolean findExistingUser(Session session, String username, String password) {
        String hql = "SELECT 1 FROM User u WHERE u.username = :username AND u.password = :password";
        Query query = session.createQuery(hql);
        query.setParameter("username", username);
        query.setParameter("password", password);

        List result = ((org.hibernate.query.Query<?>) query).list();

        if (result.isEmpty()){
            incorrectLogin1.setVisible(true);
            incorrectLogin2.setVisible(true);
            return false;
        }
        return true;
    }

//    private User findUserByUsernameAndPassword(Session session, String username, String password) {
//        String hql = "SELECT 1 FROM User u WHERE u.username = :username AND u.password = :password";
//        Query query = session.createQuery(hql);
//        query.setParameter("username", username);
//        query.setParameter("password", password);
//
//        List result = ((org.hibernate.query.Query<?>) query).list();
//
//        result.get(0);
//
//    }
}
