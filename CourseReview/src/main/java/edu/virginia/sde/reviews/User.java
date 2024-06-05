package edu.virginia.sde.reviews;

import jakarta.persistence.*;

import java.util.List;


@Entity
@Table(name = "ACCOUNTS")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column (name = "USER_ID")
    private Integer id;

    @Column(name="USERNAME", nullable = false)
    private String username;

    @Column(name="PASSWORD", nullable = false)
    private String password;

    @OneToMany(mappedBy = "user" ,
            cascade = CascadeType.ALL)
    private List<Review> reviews;

    public User() {}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }
    public User(int id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
    public List<Review> getReviews() {
        return reviews;
    }

    public void setReviews(List<Review> reviews) {
        this.reviews = reviews;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
