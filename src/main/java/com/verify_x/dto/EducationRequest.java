package com.verify_x.dto;

import com.verify_x.enums.BacklogStatus;
import com.verify_x.enums.BoardType;
import com.verify_x.enums.ModeOfStudy;
import com.verify_x.enums.StreamType;
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

    // =========================================================
    // 10th / SECONDARY EDUCATION
    // Upload marks card -> backend extracts these fields
    // =========================================================

    private String tenthSchoolName;

    private BoardType tenthBoard;

    private String tenthSchoolLocation;

    private String tenthRollNumber;

    private Integer tenthPassingYear;

    private Double tenthPercentage;

    private MultipartFile tenthMarksCard;


    // =========================================================
    // 12th / PUC / INTERMEDIATE
    // Upload marks card -> backend extracts these fields
    // =========================================================

    private String twelfthInstitutionName;

    private String twelfthBoardUniversity;

    private StreamType twelfthStream;

    private String twelfthRegistrationNumber;

    private Integer twelfthPassingYear;

    private Double twelfthPercentage;

    private MultipartFile twelfthMarksCard;


    // =========================================================
    // DEGREE
    // Upload degree certificate / marks document
    // -> backend extracts these fields
    // =========================================================

    private String degreeName;

    private String specialization;

    private String collegeName;

    private String universityName;

    private String usnNumber;

    private Integer degreeStartYear;

    private Integer degreeEndYear;

    private Double degreePercentage;

    private BacklogStatus backlogStatus;

    private MultipartFile degreeCertificate;


    // =========================================================
    // MASTER'S
    // OPTIONAL
    //
    // If candidate has completed Master's:
    // upload the documents and backend extracts the data.
    //
    // If candidate has NOT completed Master's:
    // all Master's fields/files can be null.
    // =========================================================

    private String mastersDegree;

    private String mastersSpecialization;

    private String mastersCollege;

    private String mastersUniversity;

    private String mastersRegistrationNumber;

    private ModeOfStudy modeOfStudy;

    private Integer mastersStartYear;

    private Integer mastersEndYear;

    private Double mastersPercentage;

    private MultipartFile mastersMarksCard;

    private MultipartFile mastersDegreeCertificate;


    // =========================================================
    // TECHNICAL SKILLS
    // =========================================================

    private List<TechnicalSkill> technicalSkills;
}