package com.verify_x.entity;

import com.verify_x.enums.*;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id", nullable = false, unique = true)
    private Candidate candidate;

    private String tenthSchoolName;

    @Enumerated(EnumType.STRING)
    private BoardType tenthBoard;

    private String tenthSchoolLocation;

    private String tenthRollNumber;

    private Integer tenthPassingYear;

    private Double tenthPercentage;


    private String tenthMarksCardName;

    private String tenthMarksCardContentType;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] tenthMarksCard;


    private String twelfthInstitutionName;

    private String twelfthBoardUniversity;

    @Enumerated(EnumType.STRING)
    private StreamType twelfthStream;

    private String twelfthRegistrationNumber;

    private Integer twelfthPassingYear;

    private Double twelfthPercentage;


    private String twelfthMarksCardName;

    private String twelfthMarksCardContentType;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] twelfthMarksCard;

    private String degreeName;

    private String specialization;

    private String collegeName;

    private String universityName;

    private String usnNumber;

    private Integer degreeStartYear;

    private Integer degreeEndYear;

    private Double degreePercentage;

    @Enumerated(EnumType.STRING)
    private BacklogStatus backlogStatus;


    private String degreeCertificateName;

    private String degreeCertificateContentType;

    @Lob
    @Column(columnDefinition = "LONGBLOB")
    private byte[] degreeCertificate;

    @Column(nullable = true)
    private String mastersDegree;

    @Column(nullable = true)
    private String mastersSpecialization;

    @Column(nullable = true)
    private String mastersCollege;

    @Column(nullable = true)
    private String mastersUniversity;

    @Column(nullable = true)
    private String mastersRegistrationNumber;

    @Enumerated(EnumType.STRING)
    private ModeOfStudy modeOfStudy;

    @Column(nullable = true)
    private Integer mastersStartYear;

    @Column(nullable = true)
    private Integer mastersEndYear;

    @Column(nullable = true)
    private Double mastersPercentage;

    @Column(nullable = true)
    private String mastersMarksCardName;

    @Column(nullable = true)
    private String mastersMarksCardContentType;

    @Lob
    @Column(columnDefinition = "LONGBLOB",nullable = true)
    private byte[] mastersMarksCard;

    @Column(nullable = true)
    private String mastersDegreeCertificateName;

    @Column(nullable = true)
    private String mastersDegreeCertificateContentType;

    @Lob
    @Column(columnDefinition = "LONGBLOB",nullable = true)
    private byte[] mastersDegreeCertificate;


//    @ElementCollection(fetch = FetchType.LAZY)
//    @CollectionTable(
//            name = "education_technical_skills",
//            joinColumns = @JoinColumn(name = "education_id")
//    )
//    @Enumerated(EnumType.STRING)
//    @Column(name = "technical_skill")
//    @Builder.Default
//    private Set<TechnicalSkill> technicalSkills = new HashSet<>();
//
    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}