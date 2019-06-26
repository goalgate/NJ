package com.nj.Bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Generated;

@Entity
public class ReUploadBean {

    @Id(autoincrement = true)
    private Long id;

    private String method;

    private String content;

    @Generated(hash = 1169161392)
    public ReUploadBean(Long id, String method, String content) {
        this.id = id;
        this.method = method;
        this.content = content;
    }

    @Generated(hash = 1965321528)
    public ReUploadBean() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMethod() {
        return this.method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getContent() {
        return this.content;
    }

    public void setContent(String content) {
        this.content = content;
    }

}
