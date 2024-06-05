package edu.virginia.sde.reviews;

public class SelectedCourse {
    private static Course currentCourseReview;

    public static Course getCurrentCourseReview() {
        return currentCourseReview;
    }

    public static void setCurrentCourseReview(Course currentCourseReview) {
        SelectedCourse.currentCourseReview = currentCourseReview;
    }
}