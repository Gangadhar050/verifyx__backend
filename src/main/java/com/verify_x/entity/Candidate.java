package com.verify_x.entity;

import com.verify_x.enums.ApplicationStatus;
import com.verify_x.enums.CandidateType;
import com.verify_x.enums.Role;
import com.verify_x.enums.TechnicalSkill;
import com.verify_x.enums.ToolPlatform;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    @Column(nullable = false, length = 100)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false, unique = true, length = 10)
    private String phoneNumber;

    @Column(nullable = false)
    private String password;

    // =========================================================
    // APPLIED ROLE
    // =========================================================

    public enum AppliedRole {

        FRONTEND_DEVELOPER,
        REACT_DEVELOPER,
        ANGULAR_DEVELOPER,
        VUE_JS_DEVELOPER,
        UI_DEVELOPER,

        BACKEND_DEVELOPER,
        JAVA_DEVELOPER,
        SPRING_BOOT_DEVELOPER,

        DOT_NET_DEVELOPER,
        C_SHARP_DEVELOPER,

        PYTHON_DEVELOPER,
        NODE_JS_DEVELOPER,
        PHP_DEVELOPER,
        RUBY_ON_RAILS_DEVELOPER,
        GO_DEVELOPER
    }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppliedRole appliedRole;

    // =========================================================
    // CANDIDATE TYPE
    // =========================================================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CandidateType candidateType;

    // =========================================================
    // SYSTEM ROLE
    // =========================================================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // =========================================================
    // PERSONAL DETAILS
    // =========================================================

    @Column(length = 500)
    private String address;

    @Column(unique = true, length = 10)
    private String panNumber;

    @Column(unique = true, length = 12)
    private String aadhaarNumber;

    // =========================================================
    // TIMESTAMPS
    // =========================================================

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    // =========================================================
    // TECHNICAL SKILLS
    // =========================================================

    @ElementCollection(
            fetch = FetchType.EAGER,
            targetClass = TechnicalSkill.class
    )
    @CollectionTable(
            name = "candidate_technical_skills",
            joinColumns = @JoinColumn(name = "candidate_id")
    )
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private List<TechnicalSkill> technicalSkills = new ArrayList<>();

    
    
    // =========================================================
    // TOOLS & PLATFORMS
    // =========================================================

    @ElementCollection(
            fetch = FetchType.EAGER,
            targetClass = ToolPlatform.class
    )
    @CollectionTable(
            name = "candidate_tool_platforms",
            joinColumns = @JoinColumn(name = "candidate_id")
    )
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private List<ToolPlatform> toolPlatforms = new ArrayList<>();

    // =========================================================
    // APPLICATION STATUS
    // =========================================================

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ApplicationStatus applicationStatus =
            ApplicationStatus.PENDING_VERIFICATION;

    @Column(length = 1000)
    private String remarks;

    // =========================================================
    // EMPLOYMENT DETAILS
    // =========================================================

    @OneToOne(
            mappedBy = "candidate",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    private Employment employment;

    // =========================================================
    // DOCUMENTS
    // =========================================================

    @OneToMany(
            mappedBy = "candidate",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    private List<CandidateDocument> documents;

    // =========================================================
    // EDUCATION
    // =========================================================

    @OneToOne(
            mappedBy = "candidate",
            cascade = CascadeType.ALL,
            fetch = FetchType.LAZY,
            orphanRemoval = true
    )
    private Education education;

    // =========================================================
    // OTP / VERIFICATION
    // =========================================================

    @Column(nullable = false)
    @Builder.Default
    private boolean emailVerified = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean mobileVerified = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean enabled = false;

    private String emailOtpHash;

    private String mobileOtpHash;

    private LocalDateTime otpExpiresAt;
}