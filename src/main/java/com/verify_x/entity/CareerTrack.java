package com.verify_x.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "career_tracks")
public class CareerTrack {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "domain_id", nullable = false)
    private SkillDomain domain;

    public CareerTrack() {
    }

    public CareerTrack(String name, SkillDomain domain) {
        this.name = name;
        this.domain = domain;
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

    public SkillDomain getDomain() {
        return domain;
    }

    public void setDomain(SkillDomain domain) {
        this.domain = domain;
    }
}