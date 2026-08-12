package com.verify_x.serviceImpl;

import com.verify_x.dto.EducationRequest;
import com.verify_x.dto.EducationResponse;
import com.verify_x.entity.Candidate;
import com.verify_x.entity.Education;
import com.verify_x.enums.BacklogStatus;
import com.verify_x.enums.BoardType;
import com.verify_x.enums.EducationDocumentType;
import com.verify_x.enums.ModeOfStudy;
import com.verify_x.enums.StreamType;
import com.verify_x.enums.TechnicalSkill;
import com.verify_x.exception.BadRequestException;
import com.verify_x.exception.ResourceNotFoundException;
import com.verify_x.jwt.UserPrincipal;
import com.verify_x.repository.CandidateRepository;
import com.verify_x.repository.EducationRepository;
import com.verify_x.services.EducationService;

import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.Block;
import software.amazon.awssdk.services.textract.model.BlockType;
import software.amazon.awssdk.services.textract.model.DetectDocumentTextRequest;
import software.amazon.awssdk.services.textract.model.DetectDocumentTextResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class EducationServiceImpl implements EducationService {

    private final EducationRepository educationRepository;

    private final CandidateRepository candidateRepository;

    private final TextractClient textractClient;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/jpeg",
            "image/png",
            "image/jpg"
    );

    // ============================================================
    // LOGGED-IN CANDIDATE
    // ============================================================

    private Candidate getLoggedInCandidate() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                authentication.getPrincipal() == null) {

            throw new RuntimeException(
                    "User is not authenticated."
            );
        }

        if (!(authentication.getPrincipal()
                instanceof UserPrincipal)) {

            throw new RuntimeException(
                    "Invalid authenticated user."
            );
        }

        UserPrincipal principal =
                (UserPrincipal) authentication.getPrincipal();

        return candidateRepository
                .findById(principal.getUserId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Candidate not found."
                        )
                );
    }

    // ============================================================
    // MAP ENTITY -> RESPONSE
    // ============================================================

    private EducationResponse mapToResponse(
            Education education) {

        return EducationResponse.builder()

                .id(education.getId())

                // 10TH
                .tenthSchoolName(
                        education.getTenthSchoolName()
                )

                .tenthBoard(
                        education.getTenthBoard()
                )

                .tenthSchoolLocation(
                        education.getTenthSchoolLocation()
                )

                .tenthRollNumber(
                        education.getTenthRollNumber()
                )

                .tenthPassingYear(
                        education.getTenthPassingYear()
                )

                .tenthPercentage(
                        education.getTenthPercentage()
                )

                .tenthMarksCardName(
                        education.getTenthMarksCardName()
                )

                // 12TH
                .twelfthInstitutionName(
                        education.getTwelfthInstitutionName()
                )

                .twelfthBoardUniversity(
                        education.getTwelfthBoardUniversity()
                )

                .twelfthStream(
                        education.getTwelfthStream()
                )

                .twelfthRegistrationNumber(
                        education.getTwelfthRegistrationNumber()
                )

                .twelfthPassingYear(
                        education.getTwelfthPassingYear()
                )

                .twelfthPercentage(
                        education.getTwelfthPercentage()
                )

                .twelfthMarksCardName(
                        education.getTwelfthMarksCardName()
                )

                // DEGREE
                .degreeName(
                        education.getDegreeName()
                )

                .specialization(
                        education.getSpecialization()
                )

                .collegeName(
                        education.getCollegeName()
                )

                .universityName(
                        education.getUniversityName()
                )

                .usnNumber(
                        education.getUsnNumber()
                )

                .degreeStartYear(
                        education.getDegreeStartYear()
                )

                .degreeEndYear(
                        education.getDegreeEndYear()
                )

                .degreePercentage(
                        education.getDegreePercentage()
                )

                .backlogStatus(
                        education.getBacklogStatus()
                )

                .degreeCertificateName(
                        education.getDegreeCertificateName()
                )

                // MASTER'S
                .mastersDegree(
                        education.getMastersDegree()
                )

                .mastersSpecialization(
                        education.getMastersSpecialization()
                )

                .mastersCollege(
                        education.getMastersCollege()
                )

                .mastersUniversity(
                        education.getMastersUniversity()
                )

                .mastersRegistrationNumber(
                        education.getMastersRegistrationNumber()
                )

                .modeOfStudy(
                        education.getModeOfStudy()
                )

                .mastersStartYear(
                        education.getMastersStartYear()
                )

                .mastersEndYear(
                        education.getMastersEndYear()
                )

                .mastersPercentage(
                        education.getMastersPercentage()
                )

                .mastersMarksCardName(
                        education.getMastersMarksCardName()
                )

                .mastersDegreeCertificateName(
                        education.getMastersDegreeCertificateName()
                )

                // TECHNICAL SKILLS
                .technicalSkills(
                        education.getCandidate() != null
                                ? education.getCandidate()
                                .getTechnicalSkills()
                                : null
                )

                .build();
    }

    // ============================================================
    // FILE VALIDATION
    // ============================================================

    private void validateFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return;
        }

        if (file.getSize() > MAX_FILE_SIZE) {

            throw new BadRequestException(
                    "Maximum allowed file size is 5 MB."
            );
        }

        String contentType =
                file.getContentType();

        if (contentType == null ||
                !ALLOWED_CONTENT_TYPES.contains(
                        contentType.toLowerCase(Locale.ROOT)
                )) {

            throw new BadRequestException(
                    "Only PDF, JPG, JPEG and PNG files are allowed."
            );
        }
    }

    // ============================================================
    // SAVE FILE
    // ============================================================

    private void saveFile(
            MultipartFile file,
            Consumer<byte[]> dataSetter,
            Consumer<String> fileNameSetter,
            Consumer<String> contentTypeSetter)
            throws IOException {

        if (file == null || file.isEmpty()) {
            return;
        }

        validateFile(file);

        dataSetter.accept(file.getBytes());

        fileNameSetter.accept(
                file.getOriginalFilename()
        );

        contentTypeSetter.accept(
                file.getContentType()
        );
    }

    // ============================================================
    // SET IF NOT NULL
    // ============================================================

    private <T> void setIfNotNull(
            T value,
            Consumer<T> setter) {

        if (value != null) {
            setter.accept(value);
        }
    }

    // ============================================================
    // OCR TEXT EXTRACTION
    // ============================================================

    private String extractTextFromFile(
            MultipartFile file) {

        if (file == null || file.isEmpty()) {

            throw new BadRequestException(
                    "Education document is required."
            );
        }

        try {

            validateFile(file);

            byte[] fileBytes =
                    file.getBytes();

            DetectDocumentTextRequest request =
                    DetectDocumentTextRequest.builder()

                            .document(
                                    software.amazon.awssdk
                                            .services
                                            .textract
                                            .model
                                            .Document
                                            .builder()
                                            .bytes(
                                                    SdkBytes.fromByteArray(
                                                            fileBytes
                                                    )
                                            )
                                            .build()
                            )

                            .build();

            DetectDocumentTextResponse response =
                    textractClient.detectDocumentText(
                            request
                    );

            StringBuilder text =
                    new StringBuilder();

            for (Block block : response.blocks()) {

                if (block.blockType() ==
                        BlockType.LINE) {

                    if (block.text() != null &&
                            !block.text().isBlank()) {

                        text.append(block.text())
                                .append("\n");
                    }
                }
            }

            String result =
                    text.toString().trim();

            if (result.isBlank()) {

                throw new BadRequestException(
                        "No text could be extracted from the education document."
                );
            }

            log.info(
                    "OCR completed successfully for file: {}",
                    file.getOriginalFilename()
            );

            return result;

        } catch (BadRequestException ex) {

            throw ex;

        } catch (IOException ex) {

            log.error(
                    "Unable to read education document.",
                    ex
            );

            throw new RuntimeException(
                    "Unable to read education document.",
                    ex
            );

        } catch (Exception ex) {

            log.error(
                    "AWS Textract failed.",
                    ex
            );

            throw new RuntimeException(
                    "Unable to extract text from education document.",
                    ex
            );
        }
    }

    // ============================================================
    // NORMALIZE TEXT
    // ============================================================

    private String normalizeText(String text) {

        if (text == null) {
            return "";
        }

        return text
                .replace("\r", "\n")
                .replaceAll("[ \\t]+", " ")
                .replaceAll("\n{2,}", "\n")
                .trim();
    }

    // ============================================================
    // FIND VALUE AFTER LABEL
    // ============================================================

    private String findValue(
            String text,
            String... labels) {

        if (text == null || text.isBlank()) {
            return null;
        }

        String[] lines =
                text.split("\\n");

        for (String originalLine : lines) {

            String line =
                    originalLine.trim();

            if (line.isBlank()) {
                continue;
            }

            String lower =
                    line.toLowerCase(Locale.ROOT);

            for (String label : labels) {

                String lowerLabel =
                        label.toLowerCase(Locale.ROOT);

                int index =
                        lower.indexOf(lowerLabel);

                if (index < 0) {
                    continue;
                }

                String value =
                        line.substring(
                                index + label.length()
                        )
                        .replaceFirst(
                                "^[\\s:=-]+",
                                ""
                        )
                        .trim();

                if (!value.isBlank()) {

                    return cleanExtractedValue(
                            value
                    );
                }
            }
        }

        return null;
    }

    // ============================================================
    // FIND REGEX VALUE
    // ============================================================

    private String findByPattern(
            String text,
            String regex) {

        if (text == null ||
                text.isBlank()) {

            return null;
        }

        Pattern pattern =
                Pattern.compile(
                        regex,
                        Pattern.CASE_INSENSITIVE
                );

        Matcher matcher =
                pattern.matcher(text);

        if (matcher.find()) {

            return cleanExtractedValue(
                    matcher.group(1)
            );
        }

        return null;
    }

    // ============================================================
    // CLEAN OCR VALUE
    // ============================================================

    private String cleanExtractedValue(
            String value) {

        if (value == null) {
            return null;
        }

        return value
                .replaceAll("[|]+", " ")
                .replaceAll("\\s{2,}", " ")
                .trim();
    }

    // ============================================================
    // EXTRACT YEAR
    // ============================================================

    private Integer extractYear(
            String text,
            String... labels) {

        String value =
                findValue(
                        text,
                        labels
                );

        if (value == null) {
            return null;
        }

        Matcher matcher =
                Pattern.compile(
                        "\\b(19|20)\\d{2}\\b"
                )
                .matcher(value);

        if (matcher.find()) {

            return Integer.valueOf(
                    matcher.group()
            );
        }

        return null;
    }

    // ============================================================
    // EXTRACT NUMBER
    // ============================================================

    private String extractNumber(
            String text,
            String... labels) {

        String value =
                findValue(
                        text,
                        labels
                );

        if (value == null) {
            return null;
        }

        Matcher matcher =
                Pattern.compile(
                        "[A-Z0-9][A-Z0-9\\-/]{3,}"
                )
                .matcher(
                        value.toUpperCase(Locale.ROOT)
                );

        if (matcher.find()) {

            return matcher.group().trim();
        }

        return value;
    }

    // ============================================================
    // EXTRACT PERCENTAGE
    // ============================================================

    private Double extractPercentage(
            String text) {

        String value =
                findValue(
                        text,
                        "Percentage",
                        "Percentage %",
                        "Percentage of Marks",
                        "Aggregate Percentage",
                        "Overall Percentage"
                );

        if (value != null) {

            Matcher matcher =
                    Pattern.compile(
                            "(\\d+(?:\\.\\d+)?)\\s*%"
                    )
                    .matcher(value);

            if (matcher.find()) {

                return Double.valueOf(
                        matcher.group(1)
                );
            }
        }

        Matcher globalMatcher =
                Pattern.compile(
                        "(?:percentage|aggregate|overall)"
                                + "[^\\d]{0,20}"
                                + "(\\d+(?:\\.\\d+)?)\\s*%",
                        Pattern.CASE_INSENSITIVE
                )
                .matcher(text);

        if (globalMatcher.find()) {

            return Double.valueOf(
                    globalMatcher.group(1)
            );
        }

        return null;
    }

    // ============================================================
    // PERCENTAGE FROM MARKS
    // ============================================================

    private Double extractPercentageFromMarks(
            String text) {

        Double percentage =
                extractPercentage(text);

        if (percentage != null) {
            return percentage;
        }

        Matcher matcher =
                Pattern.compile(
                        "(\\d+(?:\\.\\d+)?)\\s*%"
                )
                .matcher(text);

        if (matcher.find()) {

            double value =
                    Double.parseDouble(
                            matcher.group(1)
                    );

            if (value >= 0 &&
                    value <= 100) {

                return value;
            }
        }

        return null;
    }

    // ============================================================
    // DETECT BOARD
    // ============================================================

    private BoardType detectBoard(
            String text) {

        String lower =
                text.toLowerCase(Locale.ROOT);

        if (lower.contains("central board") ||
                lower.contains("cbse")) {

            return BoardType.CBSE;
        }

        if (lower.contains("icse") ||
                lower.contains(
                        "council for the indian school"
                )) {

            return BoardType.ICSE;
        }

        if (lower.contains(
                "government of karnataka"
        ) ||
                lower.contains(
                        "karnataka school examination"
                ) ||
                lower.contains(
                        "karnataka secondary"
                ) ||
                lower.contains(
                        "state board"
                )) {

            return BoardType.STATE_BOARD;
        }

        return BoardType.OTHER;
    }

    // ============================================================
    // DETECT STREAM
    // ============================================================

    private StreamType detectStream(
            String text) {

        String lower =
                text.toLowerCase(Locale.ROOT);

        if (lower.contains("science")) {

            return StreamType.SCIENCE;
        }

        if (lower.contains("commerce")) {

            return StreamType.COMMERCE;
        }

        if (lower.contains("arts") ||
                lower.contains("humanities")) {

            return StreamType.ARTS;
        }

        if (lower.contains("diploma")) {

            return StreamType.DIPLOMA;
        }

        if (lower.contains("vocational")) {

            /*
             * IMPORTANT:
             * StreamType does not contain VOCATIONAL.
             * Therefore use OTHER.
             */
            return StreamType.OTHER;
        }

        return null;
    }

    // ============================================================
    // DETECT MODE OF STUDY
    // ============================================================

    private ModeOfStudy detectModeOfStudy(
            String text) {

        String lower =
                text.toLowerCase(Locale.ROOT);

        if (lower.contains("full time") ||
                lower.contains("full-time")) {

            return ModeOfStudy.FULL_TIME;
        }

        if (lower.contains("part time") ||
                lower.contains("part-time")) {

            return ModeOfStudy.PART_TIME;
        }

        if (lower.contains("distance")) {

            return ModeOfStudy.DISTANCE;
        }

        if (lower.contains("online")) {

            return ModeOfStudy.ONLINE;
        }

        return null;
    }

    // ============================================================
    // DETECT BACKLOG
    // ============================================================

    private BacklogStatus detectBacklog(
            String text) {

        String lower =
                text.toLowerCase(Locale.ROOT);

        if (lower.contains("no backlog") ||
                lower.contains("backlog: no") ||
                lower.contains("backlogs: no") ||
                lower.contains("nil backlog")) {

            return BacklogStatus.NO;
        }

        if (lower.contains("backlog: yes") ||
                lower.contains("backlogs: yes")) {

            return BacklogStatus.YES;
        }

        return null;
    }

    // ============================================================
    // EXTRACT 10TH DATA
    // ============================================================

    private void extractTenthData(
            Education education,
            String rawText) {

        String text =
                normalizeText(rawText);

        education.setTenthBoard(
                detectBoard(text)
        );

        String rollNumber =
                extractNumber(
                        text,
                        "Register No",
                        "Register Number",
                        "Roll No",
                        "Roll Number",
                        "Registration No",
                        "Registration Number"
                );

        setIfNotNull(
                rollNumber,
                education::setTenthRollNumber
        );

        Integer passingYear =
                extractYear(
                        text,
                        "Passing Year",
                        "Year of Passing",
                        "Examination Year",
                        "Year"
                );

        setIfNotNull(
                passingYear,
                education::setTenthPassingYear
        );

        Double percentage =
                extractPercentageFromMarks(text);

        setIfNotNull(
                percentage,
                education::setTenthPercentage
        );

        String schoolName =
                findValue(
                        text,
                        "School Name",
                        "School Name and Address",
                        "Name of School",
                        "Institution Name"
                );

        if (schoolName == null) {

            schoolName =
                    findByPattern(
                            text,
                            "(?:school\\s+name(?:\\s+and\\s+address)?)[\\s:=-]*([^\\n]+)"
                    );
        }

        setIfNotNull(
                schoolName,
                education::setTenthSchoolName
        );

        String schoolLocation =
                findValue(
                        text,
                        "School Location",
                        "School Address",
                        "Address",
                        "Location"
                );

        setIfNotNull(
                schoolLocation,
                education::setTenthSchoolLocation
        );
    }

    // ============================================================
    // EXTRACT 12TH DATA
    // ============================================================

    private void extractTwelfthData(
            Education education,
            String rawText) {

        String text =
                normalizeText(rawText);

        BoardType board =
                detectBoard(text);

        String boardName =
                findValue(
                        text,
                        "Board",
                        "Board Name",
                        "Board of Education",
                        "University"
                );

        if (boardName == null &&
                board != null) {

            boardName =
                    board.name();
        }

        setIfNotNull(
                boardName,
                education::setTwelfthBoardUniversity
        );

        String institution =
                findValue(
                        text,
                        "College Name",
                        "Institution Name",
                        "School Name",
                        "Name of Institution",
                        "Name of College"
                );

        setIfNotNull(
                institution,
                education::setTwelfthInstitutionName
        );

        String registrationNumber =
                extractNumber(
                        text,
                        "Register No",
                        "Register Number",
                        "Registration No",
                        "Registration Number",
                        "Roll No",
                        "Roll Number"
                );

        setIfNotNull(
                registrationNumber,
                education::setTwelfthRegistrationNumber
        );

        Integer passingYear =
                extractYear(
                        text,
                        "Passing Year",
                        "Year of Passing",
                        "Examination Year",
                        "Year"
                );

        setIfNotNull(
                passingYear,
                education::setTwelfthPassingYear
        );

        Double percentage =
                extractPercentageFromMarks(text);

        setIfNotNull(
                percentage,
                education::setTwelfthPercentage
        );

        StreamType stream =
                detectStream(text);

        setIfNotNull(
                stream,
                education::setTwelfthStream
        );
    }

    // ============================================================
    // EXTRACT DEGREE DATA
    // ============================================================

    private void extractDegreeData(
            Education education,
            String rawText) {

        String text =
                normalizeText(rawText);

        String degreeName =
                findValue(
                        text,
                        "Degree",
                        "Degree Name",
                        "Qualification",
                        "Course",
                        "Programme",
                        "Program"
                );

        setIfNotNull(
                degreeName,
                education::setDegreeName
        );

        String specialization =
                findValue(
                        text,
                        "Specialization",
                        "Specialisation",
                        "Branch",
                        "Major",
                        "Stream"
                );

        setIfNotNull(
                specialization,
                education::setSpecialization
        );

        String college =
                findValue(
                        text,
                        "College Name",
                        "Name of College",
                        "College",
                        "Institution Name",
                        "Institution"
                );

        setIfNotNull(
                college,
                education::setCollegeName
        );

        String university =
                findValue(
                        text,
                        "University Name",
                        "Name of University",
                        "University"
                );

        setIfNotNull(
                university,
                education::setUniversityName
        );

        String usn =
                extractNumber(
                        text,
                        "USN",
                        "USN Number",
                        "University Seat Number",
                        "Register Number",
                        "Registration Number"
                );

        setIfNotNull(
                usn,
                education::setUsnNumber
        );

        Integer startYear =
                extractYear(
                        text,
                        "Start Year",
                        "Starting Year",
                        "Admission Year",
                        "From"
                );

        setIfNotNull(
                startYear,
                education::setDegreeStartYear
        );

        Integer endYear =
                extractYear(
                        text,
                        "End Year",
                        "Completion Year",
                        "Graduation Year",
                        "Passing Year",
                        "Year of Passing"
                );

        setIfNotNull(
                endYear,
                education::setDegreeEndYear
        );

        Double percentage =
                extractPercentageFromMarks(text);

        setIfNotNull(
                percentage,
                education::setDegreePercentage
        );

        BacklogStatus backlog =
                detectBacklog(text);

        setIfNotNull(
                backlog,
                education::setBacklogStatus
        );
    }

    // ============================================================
    // EXTRACT MASTER'S DATA
    // ============================================================

    private void extractMastersData(
            Education education,
            String rawText) {

        String text =
                normalizeText(rawText);

        String mastersDegree =
                findValue(
                        text,
                        "Degree",
                        "Degree Name",
                        "Qualification",
                        "Course",
                        "Programme",
                        "Program"
                );

        setIfNotNull(
                mastersDegree,
                education::setMastersDegree
        );

        String specialization =
                findValue(
                        text,
                        "Specialization",
                        "Specialisation",
                        "Branch",
                        "Major"
                );

        setIfNotNull(
                specialization,
                education::setMastersSpecialization
        );

        String college =
                findValue(
                        text,
                        "College Name",
                        "Name of College",
                        "College",
                        "Institution Name",
                        "Institution"
                );

        setIfNotNull(
                college,
                education::setMastersCollege
        );

        String university =
                findValue(
                        text,
                        "University Name",
                        "Name of University",
                        "University"
                );

        setIfNotNull(
                university,
                education::setMastersUniversity
        );

        String registrationNumber =
                extractNumber(
                        text,
                        "Registration No",
                        "Registration Number",
                        "Register No",
                        "Register Number",
                        "Roll No",
                        "Roll Number"
                );

        setIfNotNull(
                registrationNumber,
                education::setMastersRegistrationNumber
        );

        Integer startYear =
                extractYear(
                        text,
                        "Start Year",
                        "Starting Year",
                        "Admission Year",
                        "From"
                );

        setIfNotNull(
                startYear,
                education::setMastersStartYear
        );

        Integer endYear =
                extractYear(
                        text,
                        "End Year",
                        "Completion Year",
                        "Graduation Year",
                        "Passing Year",
                        "Year of Passing"
                );

        setIfNotNull(
                endYear,
                education::setMastersEndYear
        );

        Double percentage =
                extractPercentageFromMarks(text);

        setIfNotNull(
                percentage,
                education::setMastersPercentage
        );

        ModeOfStudy mode =
                detectModeOfStudy(text);

        setIfNotNull(
                mode,
                education::setModeOfStudy
        );
    }

    // ============================================================
    // MAP REQUEST -> ENTITY
    // ============================================================

    private void mapRequestToEntity(
            Education education,
            EducationRequest request)
            throws IOException {

        if (request == null) {

            throw new BadRequestException(
                    "Education request is required."
            );
        }

        // ========================================================
        // 10TH
        // ========================================================

        setIfNotNull(
                request.getTenthSchoolName(),
                education::setTenthSchoolName
        );

        setIfNotNull(
                request.getTenthBoard(),
                education::setTenthBoard
        );

        setIfNotNull(
                request.getTenthSchoolLocation(),
                education::setTenthSchoolLocation
        );

        setIfNotNull(
                request.getTenthRollNumber(),
                education::setTenthRollNumber
        );

        setIfNotNull(
                request.getTenthPassingYear(),
                education::setTenthPassingYear
        );

        setIfNotNull(
                request.getTenthPercentage(),
                education::setTenthPercentage
        );

        // ========================================================
        // 12TH
        // ========================================================

        setIfNotNull(
                request.getTwelfthInstitutionName(),
                education::setTwelfthInstitutionName
        );

        setIfNotNull(
                request.getTwelfthBoardUniversity(),
                education::setTwelfthBoardUniversity
        );

        setIfNotNull(
                request.getTwelfthStream(),
                education::setTwelfthStream
        );

        setIfNotNull(
                request.getTwelfthRegistrationNumber(),
                education::setTwelfthRegistrationNumber
        );

        setIfNotNull(
                request.getTwelfthPassingYear(),
                education::setTwelfthPassingYear
        );

        setIfNotNull(
                request.getTwelfthPercentage(),
                education::setTwelfthPercentage
        );

        // ========================================================
        // DEGREE
        // ========================================================

        setIfNotNull(
                request.getDegreeName(),
                education::setDegreeName
        );

        setIfNotNull(
                request.getSpecialization(),
                education::setSpecialization
        );

        setIfNotNull(
                request.getCollegeName(),
                education::setCollegeName
        );

        setIfNotNull(
                request.getUniversityName(),
                education::setUniversityName
        );

        setIfNotNull(
                request.getUsnNumber(),
                education::setUsnNumber
        );

        setIfNotNull(
                request.getDegreeStartYear(),
                education::setDegreeStartYear
        );

        setIfNotNull(
                request.getDegreeEndYear(),
                education::setDegreeEndYear
        );

        setIfNotNull(
                request.getDegreePercentage(),
                education::setDegreePercentage
        );

        setIfNotNull(
                request.getBacklogStatus(),
                education::setBacklogStatus
        );

        // ========================================================
        // MASTER'S
        // ========================================================

        setIfNotNull(
                request.getMastersDegree(),
                education::setMastersDegree
        );

        setIfNotNull(
                request.getMastersSpecialization(),
                education::setMastersSpecialization
        );

        setIfNotNull(
                request.getMastersCollege(),
                education::setMastersCollege
        );

        setIfNotNull(
                request.getMastersUniversity(),
                education::setMastersUniversity
        );

        setIfNotNull(
                request.getMastersRegistrationNumber(),
                education::setMastersRegistrationNumber
        );

        setIfNotNull(
                request.getModeOfStudy(),
                education::setModeOfStudy
        );

        setIfNotNull(
                request.getMastersStartYear(),
                education::setMastersStartYear
        );

        setIfNotNull(
                request.getMastersEndYear(),
                education::setMastersEndYear
        );

        setIfNotNull(
                request.getMastersPercentage(),
                education::setMastersPercentage
        );

        // ========================================================
        // 10TH DOCUMENT
        // ========================================================

        if (request.getTenthMarksCard() != null &&
                !request.getTenthMarksCard().isEmpty()) {

            saveFile(
                    request.getTenthMarksCard(),
                    education::setTenthMarksCard,
                    education::setTenthMarksCardName,
                    education::setTenthMarksCardContentType
            );

            String text =
                    extractTextFromFile(
                            request.getTenthMarksCard()
                    );

            extractTenthData(
                    education,
                    text
            );
        }

        // ========================================================
        // 12TH DOCUMENT
        // ========================================================

        if (request.getTwelfthMarksCard() != null &&
                !request.getTwelfthMarksCard().isEmpty()) {

            saveFile(
                    request.getTwelfthMarksCard(),
                    education::setTwelfthMarksCard,
                    education::setTwelfthMarksCardName,
                    education::setTwelfthMarksCardContentType
            );

            String text =
                    extractTextFromFile(
                            request.getTwelfthMarksCard()
                    );

            extractTwelfthData(
                    education,
                    text
            );
        }

        // ========================================================
        // DEGREE CERTIFICATE
        // ========================================================

        if (request.getDegreeCertificate() != null &&
                !request.getDegreeCertificate().isEmpty()) {

            saveFile(
                    request.getDegreeCertificate(),
                    education::setDegreeCertificate,
                    education::setDegreeCertificateName,
                    education::setDegreeCertificateContentType
            );

            String text =
                    extractTextFromFile(
                            request.getDegreeCertificate()
                    );

            extractDegreeData(
                    education,
                    text
            );
        }

        // ========================================================
        // MASTER'S MARKS CARD
        // ========================================================

        if (request.getMastersMarksCard() != null &&
                !request.getMastersMarksCard().isEmpty()) {

            saveFile(
                    request.getMastersMarksCard(),
                    education::setMastersMarksCard,
                    education::setMastersMarksCardName,
                    education::setMastersMarksCardContentType
            );

            String text =
                    extractTextFromFile(
                            request.getMastersMarksCard()
                    );

            extractMastersData(
                    education,
                    text
            );
        }

        // ========================================================
        // MASTER'S DEGREE CERTIFICATE
        // ========================================================

        if (request.getMastersDegreeCertificate() != null &&
                !request.getMastersDegreeCertificate().isEmpty()) {

            saveFile(
                    request.getMastersDegreeCertificate(),
                    education::setMastersDegreeCertificate,
                    education::setMastersDegreeCertificateName,
                    education::setMastersDegreeCertificateContentType
            );

            String text =
                    extractTextFromFile(
                            request.getMastersDegreeCertificate()
                    );

            extractMastersData(
                    education,
                    text
            );
        }

        // ========================================================
        // TECHNICAL SKILLS
        // ========================================================

        Candidate candidate =
                education.getCandidate();

        if (candidate != null &&
                request.getTechnicalSkills() != null) {

            candidate.setTechnicalSkills(
                    request.getTechnicalSkills()
            );

            candidateRepository.save(candidate);
        }
    }

    // ============================================================
    // SAVE EDUCATION
    // ============================================================

    @Override
    public EducationResponse saveEducation(
            EducationRequest request) {

        try {

            Candidate candidate =
                    getLoggedInCandidate();

            if (educationRepository
                    .existsByCandidate(candidate)) {

                throw new BadRequestException(
                        "Education details already exist."
                );
            }

            Education education =
                    new Education();

            education.setCandidate(candidate);

            mapRequestToEntity(
                    education,
                    request
            );

            Education savedEducation =
                    educationRepository.save(
                            education
                    );

            return mapToResponse(
                    savedEducation
            );

        } catch (BadRequestException ex) {

            throw ex;

        } catch (IOException ex) {

            log.error(
                    "Failed to save education documents.",
                    ex
            );

            throw new RuntimeException(
                    "Unable to save education documents.",
                    ex
            );
        }
    }

    // ============================================================
    // UPDATE EDUCATION
    // ============================================================

    @Override
    public EducationResponse updateEducation(
            EducationRequest request) {

        try {

            Candidate candidate =
                    getLoggedInCandidate();

            Education education =
                    educationRepository
                            .findByCandidate(candidate)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException(
                                            "Education details not found."
                                    )
                            );

            mapRequestToEntity(
                    education,
                    request
            );

            Education updatedEducation =
                    educationRepository.save(
                            education
                    );

            return mapToResponse(
                    updatedEducation
            );

        } catch (BadRequestException ex) {

            throw ex;

        } catch (IOException ex) {

            log.error(
                    "Failed to update education documents.",
                    ex
            );

            throw new RuntimeException(
                    "Unable to update education documents.",
                    ex
            );
        }
    }

    // ============================================================
    // GET MY EDUCATION
    // ============================================================

    @Override
    public EducationResponse getMyEducation() {

        Candidate candidate =
                getLoggedInCandidate();

        Education education =
                educationRepository
                        .findByCandidate(candidate)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Education details not found."
                                )
                        );

        return mapToResponse(
                education
        );
    }

    // ============================================================
    // GET EDUCATION BY CANDIDATE ID
    // ============================================================

    @Override
    public EducationResponse getEducationByCandidateId(
            Long candidateId) {

        if (candidateId == null) {

            throw new BadRequestException(
                    "Candidate ID is required."
            );
        }

        Education education =
                educationRepository
                        .findByCandidateId(candidateId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Education details not found."
                                )
                        );

        return mapToResponse(
                education
        );
    }

    // ============================================================
    // VIEW DOCUMENT
    // ============================================================

    @Override
    public Resource viewDocument(
            Long educationId,
            EducationDocumentType documentType) {

        if (educationId == null) {

            throw new BadRequestException(
                    "Education ID is required."
            );
        }

        if (documentType == null) {

            throw new BadRequestException(
                    "Document type is required."
            );
        }

        Education education =
                educationRepository
                        .findById(educationId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Education details not found."
                                )
                        );

        byte[] fileData;

        switch (documentType) {

            case TENTH_MARKS_CARD:

                fileData =
                        education.getTenthMarksCard();

                break;

            case TWELFTH_MARKS_CARD:

                fileData =
                        education.getTwelfthMarksCard();

                break;

            case DEGREE_CERTIFICATE:

                fileData =
                        education.getDegreeCertificate();

                break;

            case MASTERS_MARKS_CARD:

                fileData =
                        education.getMastersMarksCard();

                break;

            case MASTERS_DEGREE_CERTIFICATE:

                fileData =
                        education.getMastersDegreeCertificate();

                break;

            default:

                throw new BadRequestException(
                        "Invalid education document type."
                );
        }

        if (fileData == null ||
                fileData.length == 0) {

            throw new ResourceNotFoundException(
                    "Document not found."
            );
        }

        return new ByteArrayResource(
                fileData
        );
    }

    // ============================================================
    // DELETE EDUCATION
    // ============================================================

    @Override
    public void deleteEducation() {

        Candidate candidate =
                getLoggedInCandidate();

        Education education =
                educationRepository
                        .findByCandidate(candidate)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Education details not found."
                                )
                        );

        educationRepository.delete(
                education
        );
    }

    // ============================================================
    // GET TECHNICAL SKILLS
    // ============================================================

    @Override
    public List<TechnicalSkill> getMyTechnicalSkills() {

        Candidate candidate =
                getLoggedInCandidate();

        if (candidate.getTechnicalSkills() == null) {

            return List.of();
        }

        return new ArrayList<>(
                candidate.getTechnicalSkills()
        );
    }

    // ============================================================
    // EXTRACT EDUCATION TEXT
    // ============================================================

    @Override
    public String extractEducationText(
            MultipartFile file) {

        return extractTextFromFile(file);
    }

    // ============================================================
    // EXTRACT AND POPULATE EDUCATION
    //
    // THIS IS THE METHOD FROM YOUR INTERFACE
    // ============================================================

    @Override
    public EducationResponse extractAndPopulateEducation(
            EducationRequest request) {

        if (request == null) {

            throw new BadRequestException(
                    "Education request is required."
            );
        }

        try {

            /*
             * We directly use mapRequestToEntity().
             *
             * It:
             * 1. Accepts manually supplied fields.
             * 2. Saves uploaded documents.
             * 3. Sends documents to Textract.
             * 4. Extracts OCR information.
             * 5. Populates the Education entity.
             */

            Candidate candidate =
                    getLoggedInCandidate();

            Education education =
                    educationRepository
                            .findByCandidate(candidate)
                            .orElse(null);

            if (education == null) {

                education =
                        new Education();

                education.setCandidate(
                        candidate
                );
            }

            mapRequestToEntity(
                    education,
                    request
            );

            Education savedEducation =
                    educationRepository.save(
                            education
                    );

            log.info(
                    "Education extracted and populated successfully for candidate {}",
                    candidate.getId()
            );

            return mapToResponse(
                    savedEducation
            );

        } catch (BadRequestException ex) {

            throw ex;

        } catch (IOException ex) {

            log.error(
                    "Failed to extract and populate education.",
                    ex
            );

            throw new RuntimeException(
                    "Unable to process education documents.",
                    ex
            );
        }
    }
}