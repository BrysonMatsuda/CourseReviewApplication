package edu.virginia.sde.reviews;

import com.sun.javafx.scene.control.IntegerField;
import jakarta.persistence.Query;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.boot.model.source.internal.hbm.AttributesHelper;

import java.io.IOException;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseReviewsController {

    // Connect all of these to whatever you label in the fxml

    @FXML
    private Label alreadyReviewed;
    @FXML
    private Label noItems;
    @FXML
    private Label invalidReview;
    @FXML
    private IntegerField averageRating;
    @FXML
    private Label courseName;
    @FXML
    private RadioButton button1;
    @FXML
    private RadioButton button2;
    @FXML
    private RadioButton button3;
    @FXML
    private RadioButton button4;
    @FXML
    private RadioButton button5;
    @FXML
    private Button submitButton;
    @FXML
    private Button resubmitButton;
    @FXML
    private TextArea commentBox;
    @FXML
    private TableView<Review> reviewTableView;
    @FXML
    private TableColumn<Review, String> ratingColumn;
    @FXML
    private TableColumn<Review, String> commentColumn;
    @FXML
    private TableColumn<Review, Long> dateColumn;
    @FXML
    private Label haveNotReviewed;
    @FXML
    private Label averageRatingLabel;
    @FXML
    private BarChart<String, Number> ratingChart;
    @FXML
    private CategoryAxis xAxis;

    private SelectedCourse selectedCourse = new SelectedCourse();


    ToggleGroup toggleGroup = new ToggleGroup();

    @FXML
    private void initialize(){
        Course course = SelectedCourse.getCurrentCourseReview();

        button1.setToggleGroup(toggleGroup);
        button2.setToggleGroup(toggleGroup);
        button3.setToggleGroup(toggleGroup);
        button4.setToggleGroup(toggleGroup);
        button5.setToggleGroup(toggleGroup);

        Session session = HibernateUtil.getSessionFactory().openSession();

        //courseName.setText(SelectedCourse.getCurrentCourseReview().getCourseName());
        //System.out.println("Course name: " + selectedCourse.getCurrentCourseReview());
        getReviewsForCourse(session, course);
        populateBarGraph(session, course);

    }

    public void populateBarGraph(Session session, Course course){
        //URL: https://www.tutorialspoint.com/javafx/bar_chart.htm
        //Description: Used this to help me figure out how to put data into the bar graph
        String hql = "FROM Review r WHERE r.course = :course";
        Query query = session.createQuery(hql, Review.class);
        query.setParameter("course", course);
        List<Review> result = query.getResultList();
        XYChart.Series<String, Number> series1 = new XYChart.Series<>();
        Map<Integer, String> map = getPercentages(result);

        //Ai Agent: ChatGPT
        //Prompt: How do I make it so that all possible value show up on an axis even if there aren't values there in a javafx bargraph?

        for (int i = 1; i <= 5; i++) {
            series1.getData().add(new XYChart.Data<>(String.valueOf(i), 0));
        }
        for (Map.Entry<Integer, String> entry : map.entrySet()) {
            for (XYChart.Data<String, Number> data : series1.getData()) {
                if (data.getXValue().equals(String.valueOf(entry.getKey()))) {
                    data.setYValue(Double.parseDouble(entry.getValue()));
                    break;
                }
            }
        }
        ratingChart.getData().clear();
        ratingChart.getData().add(series1);
    }

    public Map<Integer, String> getPercentages(List<Review> list){
        Map<Integer, Integer> countMap = new HashMap<>();
        for (Review review : list){
            countMap.put(review.getRating(), countMap.getOrDefault(review.getRating(), 0) + 1);
        }

        Map<Integer, String> percentageMap = new HashMap<>();
        int length = list.size();
        if (length > 0) {
            for (Map.Entry<Integer, Integer> entry : countMap.entrySet()) {
                double percentage = (double) entry.getValue() / length * 100;
                percentageMap.put(entry.getKey(), String.valueOf(percentage));
            }
        }
        return percentageMap;
    }

    public void setCourseTitle(Course course){
        selectedCourse.setCurrentCourseReview(course);
        //System.out.println("course: " + selectedCourse.getCurrentCourseReview().getCourseName());
        String tempName = selectedCourse.getCurrentCourseReview().getCourseName();
        String tempMnem = selectedCourse.getCurrentCourseReview().getCourseSubject();
        String tempNumber = String.valueOf(selectedCourse.getCurrentCourseReview().getCourseNumber());
        String title = tempMnem + " " + tempNumber + ": " + tempName;

        courseName.setText(title);
    }
    public void setAverageRating(Course course){
        averageRatingLabel.setText("");

        double tempRating = course.calculateAverageRating();
        course.setCourseRating(tempRating);

        //System.out.println("reviews: " + course.getReviews().size());

        Platform.runLater( () ->
        {
            averageRatingLabel.setText(String.valueOf(tempRating));
        });
    }

    public void handleTransition(MouseEvent mouseEvent) throws IOException {
        FXMLLoader loader = new FXMLLoader(LoginController.class.getResource("/edu/virginia/sde/reviews/course-search.fxml"));
        Parent createNewRoot = loader.load();
        Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(createNewRoot);
        stage.setScene(scene);
        stage.show();
    }

    public void handleEditReview(MouseEvent mouseEvent) throws IOException{
        try {
            alreadyReviewed.setVisible(false);
            User user = UserSession.getCurrentUser();
//        int userID = currentUser.getId();
            Course course = selectedCourse.getCurrentCourseReview();

            Session session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            boolean alreadyReviewed = alreadyMadeReview(user, course);
            invalidReview.setVisible(false);

            if (!alreadyReviewed) {
                haveNotReviewed.setVisible(true);
                session.close();
                throw new IOException("You cannot edit a review if you haven't submitted one");
            } else {
                submitButton.setVisible(false);
                resubmitButton.setVisible(true);
                String hql = "SELECT comment FROM Review r WHERE r.user = :user AND r.course = :course";
                Query query = session.createQuery(hql);
                query.setParameter("user", user);
                query.setParameter("course", course);
                List result = ((org.hibernate.query.Query<?>) query).list();
                commentBox.setText((String) result.get(0));

                reselectRadioButtonForEdit(session, user, course);

            }

            session.getTransaction().commit();
            session.close();

            Session session3 = HibernateUtil.getSessionFactory().openSession();
            getReviewsForCourse(session3, course);
            populateBarGraph(session, course);
            setAverageRating(course);
            session3.close();
        }
        catch (Exception e){
            //System.out.println("");;
        }
    }

    public void handleResubmitReview(MouseEvent mouseEvent) throws IOException {
        try {
            Course course = selectedCourse.getCurrentCourseReview();
            User user = UserSession.getCurrentUser();
            //   int userID = currentUser.getId();

            Session session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            //AI Agent: ChatGpt
            // Prompt: Can you give me an example of a hibernate query that deletes an entry
            String deleteHql = "DELETE FROM Review r WHERE r.user = :user AND r.course = :course";
            Query deleteQuery = session.createQuery(deleteHql);
            deleteQuery.setParameter("user", user);
            deleteQuery.setParameter("course", course);
            deleteQuery.executeUpdate();

            //System.out.println("DELETE THE ENTRY FROM THE DATABASE");


            int rating = findRatingValue(toggleGroup);
            String comment = commentBox.getText();
            session.close();

            if (rating > 0) {
                addReviewToDatabase(mouseEvent, rating, user, comment, course);
                course.setCourseRating(course.calculateAverageRating());
                invalidReview.setVisible(false);
            }
            resubmitButton.setVisible(false);
            submitButton.setVisible(true);
            haveNotReviewed.setVisible(false);

            Session session3 = HibernateUtil.getSessionFactory().openSession();
            getReviewsForCourse(session3, course);
            session3.close();
            setAverageRating(course);
        }
        catch (Exception e){
            //System.out.println("");
        }
    }

    public void handleDeleteReview(MouseEvent mouseEvent) throws IOException{
        try {
            Course course = selectedCourse.getCurrentCourseReview();
            User user = UserSession.getCurrentUser();
            // int userID = currentUser.getId();

            Session session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();

            boolean alreadyReviewed = alreadyMadeReview(user, course);
            if (!alreadyReviewed) {
                haveNotReviewed.setVisible(true);
            } else {
                //AI Agent: ChatGpt
                // Prompt: Can you give me an example of a hibernate query that deletes an entry
                String deleteHql = "DELETE FROM Review r WHERE r.user = :user AND r.course = :course";
                Query deleteQuery = session.createQuery(deleteHql);
                deleteQuery.setParameter("user", user);
                deleteQuery.setParameter("course", course);
                deleteQuery.executeUpdate();
                course.setCourseRating(course.calculateAverageRating());
                populateBarGraph(session, course);
                setAverageRating(course);
            }
            session.getTransaction().commit();
            session.close();
            commentBox.clear();
            resubmitButton.setVisible(false);
            submitButton.setVisible(true);
            invalidReview.setVisible(false);

            Session session3 = HibernateUtil.getSessionFactory().openSession();
            getReviewsForCourse(session3, course);
            session3.close();
        }
        catch (Exception e){
            //System.out.println("");
        }
    }

    public void handleAddReview(MouseEvent mouseEvent) throws IOException {
        try {
            int rating = findRatingValue(toggleGroup);

            //System.out.println("GOT INTO HANDLEADDREVIEW");

            User user = UserSession.getCurrentUser();
            int userID = user.getId();
            String comment = commentBox.getText();
            Course course = selectedCourse.getCurrentCourseReview();

            //System.out.println(course.getCourseName());

            //   Session session = HibernateUtil.getSessionFactory().openSession();

            boolean alreadyReviewedBool = alreadyMadeReview(user, course);

            if (alreadyReviewedBool) {
                //System.out.println("You cannot create more than one review on this course");
                haveNotReviewed.setVisible(false);
                invalidReview.setVisible(false);
                alreadyReviewed.setVisible(true);
            }
            if (!alreadyReviewedBool) {
                //    session.beginTransaction();

                //System.out.println("HAVENT ALREADY REVIEWED");
                if (rating > 0 ) {
                    invalidReview.setVisible(false);
                    addReviewToDatabase(mouseEvent, rating, user, comment, course);
                    setAverageRating(course);
                }
            }
            commentBox.clear();
            haveNotReviewed.setVisible(false);

            Session session3 = HibernateUtil.getSessionFactory().openSession();
            getReviewsForCourse(session3, course);
            setAverageRating(course);
            session3.close();
        }
        catch (Exception e){
            //System.out.println("");;;
        }
    }

    @FXML
    public void handleClose(MouseEvent mouseEvent) throws IOException{
        Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
        stage.close();
        //System.out.println("Application successfully closed");
    }

    public boolean doesCourseExist(int courseId, Session session) {
        String hql = "SELECT count(c) FROM Course c WHERE c.courseID = :courseId";
        Query query = session.createQuery(hql);
        query.setParameter("courseId", courseId);
        long count = (long) ((org.hibernate.query.Query<?>) query).uniqueResult();
        return count > 0;
    }

    private void addReviewToDatabase(MouseEvent mouseEvent, int rating, User user, String comment, Course course) throws IOException {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();

            long timestamp = System.currentTimeMillis();
            Review review = new Review(rating, comment, course, user, timestamp);

            String sql = "INSERT INTO Reviews (Comment, CourseID, Date, Rating, UserID) VALUES (?, ?, ?, ?, ?)";
            Query query = session.createNativeQuery(sql);
            query.setParameter(1, review.getComment());
            query.setParameter(2, review.getCourse().getCourseID());
            query.setParameter(3, review.getDate());
            query.setParameter(4, review.getRating());
            query.setParameter(5, review.getUser().getId());

            query.executeUpdate();

            transaction.commit();
            populateBarGraph(session, course);
            List<Review> reviews = course.getReviews();
        } catch (HibernateException e) {
            if (transaction != null) {
                transaction.rollback();
            }
            e.printStackTrace(); // Handle or log the exception
        } finally {
            session.close();
        }
    }

    public Integer findRatingValue(ToggleGroup toggleGroup){
        // AI Agent: ChatGpt
        // Prompt: In JavaFX, I have 5 different radio buttons. How do I make it so the user
        // can only select one button at a time

        if (toggleGroup.getSelectedToggle() != null) {
            RadioButton selectedRadioButton = (RadioButton) toggleGroup.getSelectedToggle();
            //System.out.println("Selected RadioButton: " + selectedRadioButton.getText());
            return Integer.parseInt(selectedRadioButton.getText());
        } else {
            // Handle the case when no button is selected, return a default value or throw an exception
            invalidReview.setVisible(true);
            return 0;
        }
    }

    public void getReviewsForCourse(Session session, Course course) {
        try {
            //System.out.println(course.getReviews());
            String hql = "FROM Review r WHERE r.course = :course";
            Query query = session.createQuery(hql, Review.class);
            query.setParameter("course", course);
            List<Review> result = query.getResultList();
            //AI Agent: ChatGPT
            //Prompt: How do I make sure that UI changes are made in javafx?
            Platform.runLater(() -> {
                if (result.isEmpty()) {
                    noItems.setVisible(true);
                    populateColumns(result);
                    course.setReviews(result);
                    setAverageRating(course);
                    reviewTableView.refresh();
                } else {
                    noItems.setVisible(false);
                    ObservableList<Review> reviewObservableList = FXCollections.observableArrayList(result);
                    reviewTableView.setItems(reviewObservableList);
                    populateColumns(result);
                    course.setReviews(result);
                    setAverageRating(course);
                    reviewTableView.refresh();

                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void populateColumns(List<Review> reviews) {
        // AI Agent: ChatGpt
        // Prompt: I am using JavaFX and Hibernate, how do I populate a table in the GUI using results from a query
        reviewTableView.getItems().clear();

        // AI Agent: ChatGPT
        // Prompt: How can I add the date from currentMilliseconds to DateTime formatin java when
        // I have to pull from a database

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        ratingColumn.setCellValueFactory(new PropertyValueFactory<>("rating"));
        commentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));

        //AI Agent: Chat Gpt
        //Prompt: How do you convert a timestamp to a date using a DateTimeFormatter?
        dateColumn.setCellValueFactory(cellData -> {
            long timestampFromDatabase = cellData.getValue().getDate();
            String formattedDate = convertTimestampToFormattedDate(timestampFromDatabase);
            return new SimpleObjectProperty(formattedDate);
        });

        reviewTableView.getItems().addAll(reviews);
    }

    private String convertTimestampToFormattedDate(long timestampFromDatabase) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new Timestamp(timestampFromDatabase));
    }


    public boolean alreadyMadeReview(User user, Course course) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        //System.out.println(user);

        String hql = "SELECT 1 FROM Review r WHERE r.user = :user AND r.course = :course";
        Query query = session.createQuery(hql);
        query.setParameter("user", user);
        query.setParameter("course", course);
        List result = ((org.hibernate.query.Query<?>) query).list();

        session.close();

        if (!result.isEmpty()) {
            return true;
        }
        else {
            return false;
        }
    }

    private void reselectRadioButtonForEdit(Session session, User user, Course course){
        //AI Agent: ChatGpt
        // Prompt: How can you manually select a radioButton in JavaFX

        String hql = "SELECT rating FROM Review r WHERE r.user = :user AND r.course = :course";
        Query query = session.createQuery(hql);
        query.setParameter("user", user);
        query.setParameter("course", course);
        List result = ((org.hibernate.query.Query<?>) query).list();

        if (!result.isEmpty()) {
            int rating = (int) result.get(0);

            switch (rating) {
                case 1:
                    button1.setSelected(true);
                    break;
                case 2:
                    button2.setSelected(true);
                    break;
                case 3:
                    button3.setSelected(true);
                    break;
                case 4:
                    button4.setSelected(true);
                    break;
                case 5:
                    button5.setSelected(true);
                    break;
            }
        }
    }

}