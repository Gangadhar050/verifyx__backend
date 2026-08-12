package com.verify_x.entity;

import jakarta.persistence.*;

@Entity
@Table(
        name = "it_role_technologies",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"it_role_id", "technology_id"}
                )
        }
)
public class ITRoleTechnology {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "it_role_id", nullable = false)
    private ITRole role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technology_id", nullable = false)
    private Technology technology;

    public ITRoleTechnology() {
    }

    public ITRoleTechnology(ITRole role, Technology technology) {
        this.role = role;
        this.technology = technology;
    }

    public Long getId() {
        return id;
    }

    public ITRole getRole() {
        return role;
    }

    public void setRole(ITRole role) {
        this.role = role;
    }

    public Technology getTechnology() {
        return technology;
    }

    public void setTechnology(Technology technology) {
        this.technology = technology;
    }
}