package com.nj.Bean;


import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by zbsz on 2017/9/14.
 */

@Entity
public class UserBean {
    @Id(autoincrement = true)
    private Long id;
    private String courIds;
    private String cardId;
    private String name;
    private String photo;
    private String fingerprintPhoto;
    private String fingerprintId;
    private String fingerprintKey;
    private String courType;
    @Generated(hash = 695655039)
    public UserBean(Long id, String courIds, String cardId, String name,
            String photo, String fingerprintPhoto, String fingerprintId,
            String fingerprintKey, String courType) {
        this.id = id;
        this.courIds = courIds;
        this.cardId = cardId;
        this.name = name;
        this.photo = photo;
        this.fingerprintPhoto = fingerprintPhoto;
        this.fingerprintId = fingerprintId;
        this.fingerprintKey = fingerprintKey;
        this.courType = courType;
    }
    @Generated(hash = 1203313951)
    public UserBean() {
    }
    public Long getId() {
        return this.id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getCourIds() {
        return this.courIds;
    }
    public void setCourIds(String courIds) {
        this.courIds = courIds;
    }
    public String getCardId() {
        return this.cardId;
    }
    public void setCardId(String cardId) {
        this.cardId = cardId;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPhoto() {
        return this.photo;
    }
    public void setPhoto(String photo) {
        this.photo = photo;
    }
    public String getFingerprintPhoto() {
        return this.fingerprintPhoto;
    }
    public void setFingerprintPhoto(String fingerprintPhoto) {
        this.fingerprintPhoto = fingerprintPhoto;
    }
    public String getFingerprintId() {
        return this.fingerprintId;
    }
    public void setFingerprintId(String fingerprintId) {
        this.fingerprintId = fingerprintId;
    }
    public String getFingerprintKey() {
        return this.fingerprintKey;
    }
    public void setFingerprintKey(String fingerprintKey) {
        this.fingerprintKey = fingerprintKey;
    }
    public String getCourType() {
        return this.courType;
    }
    public void setCourType(String courType) {
        this.courType = courType;
    }

   
}
