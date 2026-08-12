package com.verify_x.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "skill_domains")
public class SkillDomain {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    public SkillDomain() {
    }

    public SkillDomain(String name) {
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