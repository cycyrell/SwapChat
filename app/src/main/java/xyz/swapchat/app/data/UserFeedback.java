package xyz.teamcatalyst.breedr.data;

import java.io.Serializable;

/**
 * @author A-Ar Andrew Concepcion
 * @createdOn 16/08/2017
 */
public class UserFeedback implements Serializable {
    private String comment;
    private String userId;
    private String userName;
    private Double rating;
    private String profileImageUrl;

    public UserFeedback() {
    }

    public UserFeedback(String comment, String userId, String userName, Double rating, String profileImageUrl) {
        this.comment = comment;
        this.userId = userId;
        this.userName = userName;
        this.rating = rating;
        this.profileImageUrl = profileImageUrl;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getProfileImageUrl() {
        return profileImageUrl;
    }

    public void setProfileImageUrsl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

    public Double getRating() {
        if (rating == null) rating = 10d;
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public void setProfileImageUrl(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
}
