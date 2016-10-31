package com.vaadin.addon.excel;

import java.io.Serializable;

/**
 * Created by basakpie on 2016-10-10.
 */
public class TestBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String email;

    private String name;

    public TestBean() {}

    public TestBean(Long id) {
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

        TestBean testBean = (TestBean) o;

        if (id != null ? !id.equals(testBean.id) : testBean.id != null) return false;
        if (email != null ? !email.equals(testBean.email) : testBean.email != null) return false;
        return name != null ? name.equals(testBean.name) : testBean.name == null;

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
        return "TestBean{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
