package com.nj.Tools;



/**
 * Created by zbsz on 2017/9/14.
 */

public class User {
    private String courIds;
    private String cardId;
    private String name;
    private String photo;
    private String fingerprintPhoto;
    private String fingerprintId;
    private String fingerprintKey;
    private String courType;

    public String getCourIds() {
        return courIds;
    }

    public String getCardId() {
        return cardId;
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

    public String getCourType() {
        return courType;
    }

    public void setCourIds(String courIds) {
        this.courIds = courIds;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
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

    public void setCourType(String courType) {
        this.courType = courType;
    }
}
