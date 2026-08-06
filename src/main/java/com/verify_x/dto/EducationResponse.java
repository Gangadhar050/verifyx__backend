package com.verify_x.dto;

import com.verify_x.enums.*;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EducationResponse {

    private Long id;

    // ---------- 10th ----------

    private String tenthSchoolName;
    private BoardType tenthBoard;
    private String tenthSchoolLocation;
    private String tenthRollNumber;
    private Integer tenthPassingYear;
    private Double tenthPercentage;

    private String tenthMarksCardName;

    // ---------- 12th ----------

    private String twelfthInstitutionName;
    private String twelfthBoardUniversity;
    private StreamType twelfthStream;
    private String twelfthRegistrationNumber;
    private Integer twelfthPassingYear;
    private Double twelfthPercentage;

    private String twelfthMarksCardName;

    // ---------- Degree ----------

    private String degreeName;
    private String specialization;
    private String collegeName;
    private String universityName;
    private String usnNumber;
    private Integer degreeStartYear;
    private Integer degreeEndYear;
    private Double degreePercentage;

    private BacklogStatus backlogStatus;

    private String degreeCertificateName;

    // ---------- Master's ----------

    private String mastersDegree;
    private String mastersSpecialization;
    private String mastersCollege;
    private String mastersUniversity;
    private String mastersRegistrationNumber;

    private ModeOfStudy modeOfStudy;

    private Integer mastersStartYear;
    private Integer mastersEndYear;
    private Double mastersPercentage;

    private String mastersMarksCardName;

    private String mastersDegreeCertificateName;

    //Skills

    private List<TechnicalSkill> technicalSkills;
}