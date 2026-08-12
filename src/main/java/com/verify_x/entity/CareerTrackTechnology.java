package com.verify_x.entity;

import jakarta.persistence.*;

@Entity
@Table(
    name = "career_track_technologies",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"career_track_id", "technology_id"}
        )
    }
)
public class CareerTrackTechnology {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "career_track_id", nullable = false)
    private CareerTrack careerTrack;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "technology_id", nullable = false)
    private Technology technology;

    public CareerTrackTechnology() {
    }

    public CareerTrackTechnology(
            CareerTrack careerTrack,
            Technology technology) {
        this.careerTrack = careerTrack;
        this.technology = technology;
    }

    public Long getId() {
        return id;
    }

    public CareerTrack getCareerTrack() {
        return careerTrack;
    }

    public void setCareerTrack(CareerTrack careerTrack) {
        this.careerTrack = careerTrack;
    }

    public Technology getTechnology() {
        return technology;
    }

    public void setTechnology(Technology technology) {
        this.technology = technology;
    }
}