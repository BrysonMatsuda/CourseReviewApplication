package edu.virginia.sde.reviews;

import jakarta.persistence.Query;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.hibernate.Session;

import java.io.IOException;
import java.util.List;



public class CreateNewController {
    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label incorrectCreate;

    @FXML
    private Label badPassword;

    @FXML
    private Label backToLogin;

    public void handleTransition(MouseEvent mouseEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader(LoginController.class.getResource("/edu/virginia/sde/reviews/login.fxml"));
        Parent createNewRoot = loader.load();
        Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(createNewRoot);
        stage.setScene(scene);
        stage.show();
    }

    public void handleCreateNew(MouseEvent mouseEvent) throws IOException {
        String username = usernameField.getText();
        String password = passwordField.getText();

        Session session = HibernateUtil.getSessionFactory().openSession();

        boolean exists = usernameExists(session, username);

        if(!exists) {
            session.beginTransaction();

            if(sufficientPassword(password)) {
                User newUser = new User(username, password);
                session.persist(newUser);

                session.getTransaction().commit();
                session.close();
                //Only use this when closing the app
                //HibernateUtil .shutdown();
                handleTransition(mouseEvent);
            } else {
                badPassword.setText("Your password must be at least 8 characters long. Consider making it longer.");
                badPassword.setVisible(true);
                incorrectCreate.setVisible(false);
            }

        }
    }

    @FXML
    public void handleClose(MouseEvent mouseEvent) throws IOException{
        Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
        stage.close();
        //System.out.println("Application successfully closed");
    }

    private boolean usernameExists(Session session, String username) {
        if (username == ""){
            badPassword.setText("Please enter a username in the username textbox.");
            incorrectCreate.setVisible(false);
            badPassword.setVisible(true);
            return true;
        }
        String hql = "SELECT 1 FROM User u WHERE u.username = :username";
        Query query = session.createQuery(hql);
        query.setParameter("username", username);
        List result = ((org.hibernate.query.Query<?>) query).list();

        if(!result.isEmpty()){
            //System.out.println("Username already exists");
            badPassword.setVisible(false);
            incorrectCreate.setVisible(true);
            return true;
        }
        return false;
    }

    private boolean sufficientPassword(String password){
        if (password.length() < 8){
            return false;
        }
        return true;
    }
}
