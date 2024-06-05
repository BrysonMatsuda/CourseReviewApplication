package edu.virginia.sde.reviews;

import jakarta.persistence.*;
import org.hibernate.Session;

import java.text.DecimalFormat;
import java.util.List;

@Entity
@Table (name = "Courses")
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Integer courseID;

    @Column(name = "Course Name", nullable = false)
    private String courseName;

    @Column(name = "Subject", nullable = false)
    private String courseSubject;

    @Column(name = "Course Number", nullable = false)
    private int courseNumber;

    @Column(name = "Average Course Rating", nullable = false)
    private double courseRating;

    @OneToMany(mappedBy = "course", fetch = FetchType.EAGER,
            cascade = CascadeType.ALL)
    private List<Review> reviews;

    public Course(String subject, String name, int courseNumber, int courseRating) {
        this.courseSubject = subject;
        this.courseName = name;
        this.courseNumber = courseNumber;
        this.courseRating = courseRating;

    }

    public Course(String subject, String name, int courseNumber) {
        this.courseSubject = subject;
        this.courseName = name;
        this.courseNumber = courseNumber;
    }

    public Course() {

    }

    public int getCourseID() {
        return courseID;
    }

    public void setCourseID(int courseId) {
        this.courseID = courseId;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCourseSubject() {
        return courseSubject;
    }

    public void setCourseSubject(String courseSubject) {
        this.courseSubject = courseSubject;
    }

    public int getCourseNumber() {
        return courseNumber;
    }

    public void setCourseNumber(int courseMnemonic) {
        this.courseNumber = courseMnemonic;
    }

    public double getCourseRating() {
        return courseRating;
    }

    public void setCourseRating(double courseRating) {
        this.courseRating = courseRating;
    }

    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    public double calculateAverageRating() {
//        System.out.println("I hit this");
        if (this.getReviews() == null || this.getReviews().isEmpty()) {
            return 0;
        }
        int numberOfReviews = 0;
        int totalRating = 0;
        for (Review review : this.getReviews()) {
            numberOfReviews += 1;
            totalRating += review.getRating();
            if (this.getReviews().size() > 1) {
                //System.out.println(totalRating);
            }
        }
        double finalRating = (double) totalRating / numberOfReviews;
        finalRating = roundToTwoDecimalPlaces(finalRating);

        return finalRating;
    }


    private static double roundToTwoDecimalPlaces(double value) {
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        String formattedValue = decimalFormat.format(value);
        return Double.parseDouble(formattedValue);
    }
}