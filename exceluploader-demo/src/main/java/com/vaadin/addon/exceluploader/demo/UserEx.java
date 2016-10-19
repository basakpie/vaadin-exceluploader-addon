package com.vaadin.addon.exceluploader.demo;

import com.vaadin.addon.excel.ExcelColumn;

import java.io.Serializable;

/**
 * Created by basakpie on 2016-10-10.
 */
public class UserEx implements Serializable {

    private static final long serialVersionUID = 1L;

    @ExcelColumn("아이디")
    private Long id;

    @ExcelColumn("이메일")
    private String email;

    @ExcelColumn("이름")
    private String name;

    public UserEx() {}

    public UserEx(Long id) {
        this.setId(id);
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserEx user = (UserEx) o;

        if (id != null ? !id.equals(user.id) : user.id != null) return false;
        if (email != null ? !email.equals(user.email) : user.email != null) return false;
        return name != null ? name.equals(user.name) : user.name == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (email != null ? email.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
