package edu.virginia.sde.reviews;

public class UserSession {
    private static User currentUser;

    public static void setCurrentUser(User user){
        currentUser = user;
    }

    public static User getCurrentUser(){
        return currentUser;
    }
}
