package edu.virginia.sde.reviews;

import jakarta.persistence.Query;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import javafx.util.Callback;
import org.hibernate.Session;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CourseSearchController {
    @FXML
    private TextField courseName;

    @FXML
    private TextField courseMnemonic;

    @FXML
    private TextField courseNumber;

    @FXML
    private TextField courseNameAdd;

    @FXML
    private TextField courseMnemonicAdd;

    @FXML
    private TextField courseNumberAdd;

    @FXML
    private Label invalidCourse;

    @FXML
    private Label courseAlreadyExists;

    @FXML
    private Label courseAddSuccess;

    @FXML
    private TableView<Course> tableView;

    @FXML
    private TableColumn<Course, String> mnemColumn;
    @FXML
    private TableColumn<Course, Integer> catalogNumColumn;
    @FXML
    private TableColumn<Course, String> courseNameColumn;
    @FXML
    private TableColumn<Course, Double> averageRatingColumn;

    @FXML
    private Label mnemStringError;
    @FXML
    private Label nameLengthError;
    @FXML
    private Label numberError;


    public void initialize(){
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        //Chat GPT:
        //Prompt: I would like to display - instead of 0 for a column in fxml
        averageRatingColumn.setCellValueFactory(new PropertyValueFactory<>("courseRating"));
        averageRatingColumn.setCellFactory(column -> new TextFieldTableCell<>() {
            @Override
            public void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item == 0.0) {
                    setText("-");
                } else {
                    setText(String.valueOf(item));
                }
            }
        });

        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Course> criteriaQuery = builder.createQuery(Course.class);
        Root<Course> root = criteriaQuery.from(Course.class);
        criteriaQuery.select(root);
        List<Course> courses = session.createQuery(criteriaQuery).getResultList();
        for(Course course: courses){
            course.setCourseRating(course.calculateAverageRating());
        }
        DisplayCourses(courses);

        session.getTransaction().commit();
        session.close();
    }

    public void handleCellClick(MouseEvent mouseEvent) throws IOException, InterruptedException {
        if (!tableView.getSelectionModel().isEmpty()){
            handleTransitionReview(mouseEvent);
        }

    }
    public void handleTransitionReview(MouseEvent mouseEvent) throws IOException {
        SelectedCourse.setCurrentCourseReview(tableView.getSelectionModel().getSelectedItem());
        FXMLLoader loader = new FXMLLoader(LoginController.class.getResource("/edu/virginia/sde/reviews/course-reviews.fxml"));
        Parent createNewRoot = loader.load();
        Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();

        CourseReviewsController courseReviewsController = loader.getController();
        courseReviewsController.setCourseTitle(tableView.getSelectionModel().getSelectedItem());
        courseReviewsController.setAverageRating(tableView.getSelectionModel().getSelectedItem());

        Scene scene = new Scene(createNewRoot);
        stage.setScene(scene);
        stage.show();
    }

    public void handleLabelClickLogout(MouseEvent mouseEvent) throws IOException{
        FXMLLoader loader = new FXMLLoader(LoginController.class.getResource("/edu/virginia/sde/reviews/login.fxml"));
        Parent createNewRoot = loader.load();
        Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(createNewRoot);
        stage.setScene(scene);
        stage.show();
        UserSession.setCurrentUser(null);
    }

    public void handleMyReviews(MouseEvent mouseEvent) throws IOException{
        FXMLLoader loader = new FXMLLoader(LoginController.class.getResource("/edu/virginia/sde/reviews/my-reviews.fxml"));
        Parent createNewRoot = loader.load();
        Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
        Scene scene = new Scene(createNewRoot);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

    }
    public void handleCourseSearch(MouseEvent mouseEvent) throws IOException{
        String name = courseName.getText();
        String mnemonic = courseMnemonic.getText().toUpperCase();
        String number = courseNumber.getText();

        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();

        //Chat GPT: I need a way to filter course search using hibernate
        CriteriaBuilder builder = session.getCriteriaBuilder();
        CriteriaQuery<Course> criteriaQuery = builder.createQuery(Course.class);
        Root<Course> root = criteriaQuery.from(Course.class);
        List<Predicate> predicates = new ArrayList<>();

        //Not sure if this works
        if (name != null && !name.isEmpty()) {
            predicates.add(builder.like(root.get("courseName"), "%" + name + "%"));
        }
        if (mnemonic != null && !mnemonic.isEmpty()) {
            predicates.add(builder.equal(root.get("courseSubject"), mnemonic));
        }
        if (number != null && !number.isEmpty()) {
            predicates.add(builder.equal(root.get("courseNumber"), number));
        }

        if (!predicates.isEmpty()) {
            criteriaQuery.select(root).where(predicates.toArray(new Predicate[0]));
        } else {
            // If no specific criteria are provided, get all courses
            criteriaQuery.select(root);
        }

        List<Course> courses = session.createQuery(criteriaQuery).getResultList();

        session.getTransaction().commit();
        session.close();

        if(courses.isEmpty()){
//            System.out.println("Search Empty");
        }
//        System.out.println("Search Successful!");
//        System.out.println(Arrays.toString(courses.toArray()));
        DisplayCourses(courses);
    }

    public void DisplayCourses(List<Course> courses){
        //AI Agent: ChatGPT
        //Prompt: How do I bind data to existing fxml columns in javafx?
        tableView.getItems().clear();

        mnemColumn.setCellValueFactory(new PropertyValueFactory<>("courseSubject"));
        catalogNumColumn.setCellValueFactory(new PropertyValueFactory<>("courseNumber"));
        courseNameColumn.setCellValueFactory(new PropertyValueFactory<>("courseName"));
        averageRatingColumn.setCellFactory(new CourseRatingCellFactory());

        tableView.getItems().addAll(courses);
    }

    public boolean handleAddCourse() throws IOException{
        String name = courseNameAdd.getText();
        String mnemonic = courseMnemonicAdd.getText();

        courseAddSuccess.setVisible(false);

        if(name.isEmpty() || mnemonic.isEmpty() || courseNumberAdd.getText().isEmpty()){
            System.out.println("A new course needs a name, mnemonic, and a number");
            courseAlreadyExists.setVisible(false);
            courseAddSuccess.setVisible(false);
            //   invalidCourse.setVisible(true);
            invalidCourse.setVisible(false);
            mnemStringError.setVisible(false);
            numberError.setVisible(false);
            nameLengthError.setVisible(true);
            return false;
        }
        int number = -1;
        if(isInteger(courseNumberAdd.getText())) {
            number = Integer.parseInt(courseNumberAdd.getText());
        }
        else{
            System.out.println("The course number must be an integer between 1000 and 9999 inclusive");
            courseAlreadyExists.setVisible(false);
            invalidCourse.setVisible(false);
            mnemStringError.setVisible(false);
            nameLengthError.setVisible(false);
            numberError.setVisible(true);
            return false;
        }
        if((mnemonic.length() < 2 || mnemonic.length() > 4) || name.length() > 50 || (number < 1000 || number > 9999) || !isOnlyLetters(mnemonic)){
            if ((mnemonic.length() < 2 || mnemonic.length() > 4) || !isOnlyLetters(mnemonic)) {
             //   System.out.println("The course mnemonic must a string of letters between 2 and 4 characters long inclusive");
                invalidCourse.setVisible(false);
                courseAlreadyExists.setVisible(false);
                numberError.setVisible(false);
                nameLengthError.setVisible(false);
                mnemStringError.setVisible(true);
            }
            if (name.length() > 50 || name.length() < 1) {
                System.out.println("The course name length must be 1 <= courseNameLength <= 50");
                invalidCourse.setVisible(false);
                courseAlreadyExists.setVisible(false);
                mnemStringError.setVisible(false);
                numberError.setVisible(false);
                nameLengthError.setVisible(true);

            }
            if (number < 1000 || number > 9999) {
                System.out.println("The course number must be between 1000 and 9999 inclusive");
                invalidCourse.setVisible(false);
                courseAlreadyExists.setVisible(false);
                mnemStringError.setVisible(false);
                nameLengthError.setVisible(false);
                numberError.setVisible(true);
            }
//            mnemStringError.setVisible(false);
//            nameLengthError.setVisible(false);
//            numberError.setVisible(false);
            //           invalidCourse.setVisible(true);
            return false;
        }

        Course course = new Course(mnemonic, name, number);
        Session session = HibernateUtil.getSessionFactory().openSession();

        boolean exists = CourseExists(session, course);

        if(!exists){
            session.beginTransaction();

            session.persist(course);

            CriteriaBuilder builder = session.getCriteriaBuilder();
            CriteriaQuery<Course> criteriaQuery = builder.createQuery(Course.class);
            Root<Course> root = criteriaQuery.from(Course.class);
            criteriaQuery.select(root);
            List<Course> courses = session.createQuery(criteriaQuery).getResultList();
//            for(Course course1: courses){
//                course1.setCourseRating(course.calculateAverageRating());
//            }
            DisplayCourses(courses);

            session.getTransaction().commit();
            session.close();
            invalidCourse.setVisible(false);
            mnemStringError.setVisible(false);
            numberError.setVisible(false);
            nameLengthError.setVisible(false);
            courseAlreadyExists.setVisible(false);
            invalidCourse.setVisible(false);
            courseAddSuccess.setVisible(true);
            return true;
        }
        return false;
    }


    public boolean CourseExists(Session session, Course course){
        String hql = "SELECT 1 FROM Course c WHERE Lower(c.courseName) = :courseName AND c.courseNumber = :courseNumber AND c.courseSubject = :courseSubject"; //complete proper check to see if the course already exists in the database
        Query query = session.createQuery(hql);
        query.setParameter("courseName", course.getCourseName().toLowerCase());
        query.setParameter("courseNumber", course.getCourseNumber());
        query.setParameter("courseSubject", course.getCourseSubject());

        List result = ((org.hibernate.query.Query<?>) query).list();

        if(!result.isEmpty()){
            //System.out.println("Course Already Exists");
            invalidCourse.setVisible(false);
            courseAddSuccess.setVisible(false);
            courseAlreadyExists.setVisible(true);
            return true;
        }
        courseAlreadyExists.setVisible(false);
        return false;
    }

    @FXML
    public void handleClose(MouseEvent mouseEvent) throws IOException{
        Stage stage = (Stage) ((Node) mouseEvent.getSource()).getScene().getWindow();
        stage.close();
        //System.out.println("Application successfully closed");
    }

    public boolean isOnlyLetters(String name) {
        return name.matches("[a-zA-Z]+");
    }

    public boolean isInteger(String stringToCheck) {
        try{
            Integer.parseInt(stringToCheck);
            return true;
        }
        catch (Exception e){
            return false;
        }
    }

    public class CourseRatingCellFactory implements Callback<TableColumn<Course, Double>, TableCell<Course, Double>> {
        @Override
        public TableCell<Course, Double> call(TableColumn<Course, Double> param) {
            return new TableCell<>() {
                @Override
                protected void updateItem(Double item, boolean empty) {
                    super.updateItem(item, empty);
                    if (item == null || empty) {
                        setText(null);
                    } else {
                        setText(item == 0.0 ? "-" : String.valueOf(item));
                    }
                }
            };
        }
    }
}