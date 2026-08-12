package com.verify_x.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "it_roles",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "name")
        }
)
public class ITRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    public ITRole() {
    }

    public ITRole(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}