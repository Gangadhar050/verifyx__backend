package com.verify_x.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "education_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Education {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // =========================================================
    // CANDIDATE
    // =========================================================

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "candidate_id",
            nullable = false,
            unique = true
    )
    private Candidate candidate;


    // =========================================================
    // 10TH / SSLC
    // =========================================================

    private String tenthSchoolName;

    private String tenthBoard;

    private String tenthSchoolLocation;

    private String tenthRollNumber;

    private Integer tenthPassingYear;

    private Double tenthPercentage;

    private String tenthMarksCardName;

    private String tenthMarksCardContentType;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] tenthMarksCard;


    // =========================================================
    // 12TH / PUC
    // =========================================================

    private String twelfthInstitutionName;

    private String twelfthLocation;

    private String twelfthBoardUniversity;

    private String twelfthRegistrationNumber;

    private Integer twelfthPassingYear;

    private Double twelfthPercentage;

    private String twelfthMarksCardName;

    private String twelfthMarksCardContentType;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] twelfthMarksCard;


    // =========================================================
    // DEGREE / BACHELOR'S
    // =========================================================

    private String degreeName;

    private String specialization;

    private String collegeName;

    private String universityName;

    private String degreeLocation;

    private String usnNumber;

    private Integer degreeStartYear;

    private Integer degreeEndYear;

    private Double degreePercentage;

    private String degreeCertificateName;

    private String degreeCertificateContentType;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] degreeCertificate;


    // =========================================================
    // MASTER'S
    // OPTIONAL
    // ONLY DEGREE CERTIFICATE
    // =========================================================

    @Column(nullable = true)
    private String mastersDegree;

    @Column(nullable = true)
    private String mastersSpecialization;

    @Column(nullable = true)
    private String mastersCollege;

    @Column(nullable = true)
    private String mastersUniversity;

    @Column(nullable = true)
    private String mastersLocation;

    @Column(nullable = true)
    private String mastersRegistrationNumber;

    @Column(nullable = true)
    private Integer mastersStartYear;

    @Column(nullable = true)
    private Integer mastersEndYear;

    @Column(nullable = true)
    private Double mastersPercentage;

    @Column(nullable = true)
    private String mastersDegreeCertificateName;

    @Column(nullable = true)
    private String mastersDegreeCertificateContentType;

    @Lob
    @Column(
            columnDefinition = "LONGBLOB",
            nullable = true
    )
    private byte[] mastersDegreeCertificate;


    // =========================================================
    // TIMESTAMPS
    // =========================================================

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}