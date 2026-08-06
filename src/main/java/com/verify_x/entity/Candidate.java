package com.verify_x.entity;

import com.verify_x.enums.ApplicationStatus;
import com.verify_x.enums.CandidateType;
import com.verify_x.enums.Role;
import com.verify_x.enums.TechnicalSkill;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "candidates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Candidate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false,length = 100)
    private String username;

    @Column(nullable = false,unique = true)
    private String email;

    @Column(nullable = false,unique = true,length = 10)
    private String phoneNumber;

    @Column(nullable = false)
    private String password;


    @Column(nullable = false)
    private String appliedRole;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CandidateType candidateType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(length = 500)
    private String address;

    @Column(unique = true, length = 10)
    private String panNumber;

    @Column(unique = true, length = 12)
    private String aadhaarNumber;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;


// Technical Skills

    @ElementCollection(fetch = FetchType.EAGER, targetClass = TechnicalSkill.class)
    @CollectionTable(
            name = "candidate_technical_skills",
            joinColumns = @JoinColumn(name = "candidate_id")
    )
    @Enumerated(EnumType.STRING)
    private List<TechnicalSkill> technicalSkills;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ApplicationStatus applicationStatus =
            ApplicationStatus.PENDING_VERIFICATION;

    @Column(length = 1000)
    private String remarks;
//Emplyment details
    @OneToOne(
            mappedBy = "candidate",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    private Employment employment;

    @OneToMany(
            mappedBy = "candidate",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    private List<CandidateDocument> documents;
    @OneToOne(
            mappedBy = "candidate",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    private Education education;
}

