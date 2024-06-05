package edu.virginia.sde.reviews;
import com.sun.javafx.scene.control.IntegerField;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.List;


public class MyReviewsController {
    @FXML
    private TableView<Review> tableView;
    @FXML
    private TableColumn<Review, String> Name;

    @FXML
    private TableColumn<Review, Long> Date;
    @FXML
    private TableColumn<Review, String> Rating;
    @FXML
    private TableColumn<Review, String> Mnemonic;
    @FXML
    private TableColumn<Review, String> Number;
    @FXML
    private TableColumn<Review, String> Comment;
    private User user;
    public void initialize(){
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        user = UserSession.getCurrentUser();

        String hql = "FROM Review r WHERE r.user = :user";
        Query query = session.createQuery(hql, Review.class);
        query.setParameter("user", user);

        List<Review> reviews = query.getResultList();

        session.close();

//        System.out.println(reviews);
//        System.out.println(user.getUsername());
        DisplayMyReviews(reviews);

//        session.getTransaction().commit();
//        session.close();
    }
    public void HandleBackClick(MouseEvent mouseEvent) throws IOException{
        FXMLLoader loader = new FXMLLoader(LoginController.class.getResource("/edu/virginia/sde/reviews/course-search.fxml"));
        Parent createNewRoot = loader.load();
        Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(createNewRoot);
        stage.setScene(scene);
        stage.show();
    }

    public void handleTransitionReview(MouseEvent mouseEvent) throws IOException {
        SelectedCourse.setCurrentCourseReview(tableView.getSelectionModel().getSelectedItem().getCourse());
        FXMLLoader loader = new FXMLLoader(LoginController.class.getResource("/edu/virginia/sde/reviews/course-reviews.fxml"));
        Parent createNewRoot = loader.load();
        Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();

        CourseReviewsController courseReviewsController = loader.getController();
        courseReviewsController.setCourseTitle(tableView.getSelectionModel().getSelectedItem().getCourse());

        Scene scene = new Scene(createNewRoot);
        stage.setScene(scene);
        stage.show();
    }

    public void DisplayMyReviews(List<Review> reviews){
        tableView.getItems().clear();

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        Name.setCellValueFactory(new PropertyValueFactory<>("Name"));
        Number.setCellValueFactory(new PropertyValueFactory<>("Number"));
        Mnemonic.setCellValueFactory(new PropertyValueFactory<>("Mnemonic"));
        Rating.setCellValueFactory(new PropertyValueFactory<>("Rating"));
        Comment.setCellValueFactory(new PropertyValueFactory<>("Comment"));


        //AI Agent: Chat Gpt
        //Prompt: How do you convert a timestamp to a date using a DateTimeFormatter?
        Date.setCellValueFactory(cellData -> {
            long timestampFromDatabase = cellData.getValue().getDate();
            String formattedDate = convertTimestampToFormattedDate(timestampFromDatabase);
            return new SimpleObjectProperty(formattedDate);
        });

        tableView.getColumns().setAll(Date, Mnemonic, Number, Name, Rating,  Comment);

        tableView.getItems().addAll(reviews);
    }

    private String convertTimestampToFormattedDate(long timestampFromDatabase) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new Timestamp(timestampFromDatabase));
    }

    public void handleCellClick(MouseEvent mouseEvent) throws IOException, InterruptedException {
        if (!tableView.getSelectionModel().isEmpty()){
            handleTransitionReview(mouseEvent);
        }
    }

    @FXML
    public void handleClose(MouseEvent mouseEvent) throws IOException{
        Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
        stage.close();
//        System.out.println("Application successfully closed");
    }
}
