package com.example.socialnetwork.Model;

import android.provider.ContactsContract;

import com.google.firebase.database.Exclude;

public class UserInformation {
private String imageUri;
    String fullName;
    String userName;
    String email;
    String dateOfBirth;
    String gender;
    String password;
    String confirmPassword;
    private String key;


    @Exclude
    public String getKey() {

        return key;
    }

    @Exclude
    public void setKey(String key) {

        this.key = key;
    }


    public UserInformation() {

    }

    public UserInformation( String fullName, String userName, String email,
                           String dateOfBirth, String gender, String password, String confirmPassword,String imageUri) {

        this.fullName = fullName;
        this.userName = userName;
        this.email = email;
        this.dateOfBirth = dateOfBirth;
        this.gender = gender;
        this.password = password;
        this.confirmPassword = confirmPassword;
        this.imageUri = imageUri;

    }


    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }
}
