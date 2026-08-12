package com.verify_x.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "candidate_technologies",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"candidate_id", "technology_id"}
        )
    }
)
public class CandidateTechnology {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false)
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technology_id", nullable = false)
    private Technology technology;

    public CandidateTechnology() {
    }

    public CandidateTechnology(
            Candidate candidate,
            Technology technology) {
        this.candidate = candidate;
        this.technology = technology;
    }

    public Long getId() {
        return id;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    public Technology getTechnology() {
        return technology;
    }

    public void setTechnology(Technology technology) {
        this.technology = technology;
    }
}