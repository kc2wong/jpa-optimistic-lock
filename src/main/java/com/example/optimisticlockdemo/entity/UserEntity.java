package com.example.optimisticlockdemo.entity;

import javax.persistence.*;

@Entity
@Table(name = "USER")
public class UserEntity {

    @Id
    @Column(name = "ID")
    Long id;

    @Version
    @Column(name = "VERSION")
    Long version;

    @Column(name = "NAME")
    String name;

    @Column(name = "EMAIL")
    String email;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
