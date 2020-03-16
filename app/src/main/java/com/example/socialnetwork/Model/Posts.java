package com.example.socialnetwork.Model;

public class Posts {

    public String uid,userName,description,currentTime,currentDate,imageUri,postImage;


    public Posts(){

    }

    public Posts(String uid,String userName, String description, String currentTime, String currentDate, String imageUri, String postImage) {
        this.uid = uid;
        this.userName = userName;
        this.description = description;
        this.currentTime = currentTime;
        this.currentDate = currentDate;
        this.imageUri = imageUri;
        this.postImage = postImage;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }



    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCurrentTime() {
        return currentTime;
    }

    public void setCurrentTime(String currentTime) {
        this.currentTime = currentTime;
    }

    public String getCurrentDate() {
        return currentDate;
    }

    public void setCurrentDate(String currentDate) {
        this.currentDate = currentDate;
    }

    public String getImageUri() {
        return imageUri;
    }

    public void setImageUri(String imageUri) {
        this.imageUri = imageUri;
    }

    public String getpostImage() {
        return postImage;
    }

    public void setpostImage(String postImage) {
        this.postImage = postImage;
    }
}

