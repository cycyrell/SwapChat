package xyz.teamcatalyst.breedr.data;

import java.io.Serializable;
import java.util.List;

/**
 * @author A-Ar Andrew Concepcion
 * @createdOn 16/08/2017
 */
public class UserProfile implements Serializable {
    private String userId;
    private String displayName;
    private String profileUrl;
    private String contactNumber;
    private String email;
    private List<UserFeedback> feedbacks;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<UserFeedback> getFeedbacks() {
        return feedbacks;
    }

    public void setFeedbacks(List<UserFeedback> feedbacks) {
        this.feedbacks = feedbacks;
    }
}