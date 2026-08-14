package com.verify_x.dto;

import com.verify_x.enums.TechnicalSkill;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EducationRequest {

    // 10TH / SSLC
    private String tenthSchoolName;
    private String tenthBoard;
    private String tenthSchoolLocation;
    private String tenthRollNumber;
    private Integer tenthPassingYear;
    private Double tenthPercentage;
    private MultipartFile tenthMarksCard;

    // 12TH / PUC
    private String twelfthInstitutionName;
    private String twelfthLocation;
    private String twelfthBoardUniversity;
    private String twelfthRegistrationNumber;
    private Integer twelfthPassingYear;
    private Double twelfthPercentage;
    private MultipartFile twelfthMarksCard;

    // DEGREE / BACHELOR'S
    private String degreeName;
    private String specialization;
    private String collegeName;
    private String universityName;
    private String degreeLocation;
    private String usnNumber;
    private Integer degreeStartYear;
    private Integer degreeEndYear;
    private Double degreePercentage;
    private MultipartFile degreeCertificate;

    // MASTER'S
    private String mastersDegree;
    private String mastersSpecialization;
    private String mastersCollege;
    private String mastersUniversity;
    private String mastersLocation;
    private String mastersRegistrationNumber;
    private Integer mastersStartYear;
    private Integer mastersEndYear;
    private Double mastersPercentage;
    private MultipartFile mastersDegreeCertificate;

    // TECHNICAL SKILLS
    // These belong to Candidate, not Education
    private List<TechnicalSkill> technicalSkills;
}