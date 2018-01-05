package com.nj.Tools;



/**
 * Created by zbsz on 2017/9/14.
 */

public class User {
    private String id;
    private String name;
    private String photo;
    private String fingerprintPhoto;
    private String fingerprintId;
    private String fingerprintKey;
    private String type;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPhoto() {
        return photo;
    }

    public String getFingerprintPhoto() {
        return fingerprintPhoto;
    }

    public String getFingerprintId() {
        return fingerprintId;
    }

    public String getFingerprintKey() {
        return fingerprintKey;
    }

    public String getType() {
        return type;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public void setFingerprintPhoto(String fingerprintPhoto) {
        this.fingerprintPhoto = fingerprintPhoto;
    }

    public void setFingerprintId(String fingerprintId) {
        this.fingerprintId = fingerprintId;
    }

    public void setFingerprintKey(String fingerprintKey) {
        this.fingerprintKey = fingerprintKey;
    }

    public void setType(String type) {
        this.type = type;
    }
}
