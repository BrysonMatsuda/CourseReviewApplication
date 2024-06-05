package edu.virginia.sde.reviews;

import jakarta.persistence.*;

import java.sql.Timestamp;

@Entity
@Table (name = "Reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column (name = "ID")
    private Integer id;

    @Column (name = "Rating", nullable = false)
    private int rating;

    @Column (name = "Comment")
    private String comment;

    @ManyToOne
    @JoinColumn (name = "CourseID", nullable = false, referencedColumnName = "ID")
    private Course course;

    @ManyToOne
    @JoinColumn (name = "UserID", nullable = false, referencedColumnName = "USER_ID")
    private User user;

    @Column (name = "Date")
    private long date;

    public Review(int rating, String comment, Course course, User user, long date){
        setComment(comment);
        setCourse(course);
        setDate(date);
        setUser(user);
        setRating(rating);
    }

    public Review(){

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String review) {
        this.comment = review;
    }

    public Course getCourse() {
        return course;
    }

    public void setCourse(Course course) {
        this.course = course;
    }
    public String getName() {
        return course.getCourseName();
    }

    public void setName(Course course) {
        this.course = course;
    }

    public String getMnemonic() {
        return course.getCourseSubject();
    }

    public void setMnemonic(Course course) {
        this.course = course;
    }

    public int getNumber() {
        return course.getCourseNumber();
    }

    public void setNumber(Course course) {
        this.course = course;
    }


    public User getUser() {
        return user;
    }

    public void setUser(User userID) {
        this.user = userID;
    }

    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }
}