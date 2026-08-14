package com.verify_x.serviceImpl;

import com.verify_x.dto.EducationRequest;
import com.verify_x.dto.EducationResponse;
import com.verify_x.entity.Candidate;
import com.verify_x.entity.Education;
import com.verify_x.enums.EducationDocumentType;
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
import software.amazon.awssdk.services.textract.model.Document;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
            "image/jpg",
            "image/png"
    );

    // ============================================================
    // GET LOGGED-IN CANDIDATE
    // ============================================================

    private Candidate getLoggedInCandidate() {

        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || authentication.getPrincipal() == null) {
            throw new RuntimeException("User is not authenticated.");
        }

        if (!(authentication.getPrincipal() instanceof UserPrincipal)) {
            throw new RuntimeException("Invalid authenticated user.");
        }

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        return candidateRepository
                .findById(principal.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Candidate not found."));
    }

    // ============================================================
    // ENTITY -> RESPONSE
    // ============================================================

    private EducationResponse mapToResponse(Education education) {

        Candidate candidate = education.getCandidate();

        List<TechnicalSkill> technicalSkills =
                candidate != null ? candidate.getTechnicalSkills() : null;

        return EducationResponse.builder()

                .id(education.getId())

                // 10TH
                .tenthSchoolName(education.getTenthSchoolName())
                .tenthBoard(education.getTenthBoard())
                .tenthSchoolLocation(education.getTenthSchoolLocation())
                .tenthRollNumber(education.getTenthRollNumber())
                .tenthPassingYear(education.getTenthPassingYear())
                .tenthPercentage(education.getTenthPercentage())
                .tenthMarksCardName(education.getTenthMarksCardName())

                // 12TH
                .twelfthInstitutionName(education.getTwelfthInstitutionName())
                .twelfthLocation(education.getTwelfthLocation())
                .twelfthBoardUniversity(education.getTwelfthBoardUniversity())
                .twelfthRegistrationNumber(education.getTwelfthRegistrationNumber())
                .twelfthPassingYear(education.getTwelfthPassingYear())
                .twelfthPercentage(education.getTwelfthPercentage())
                .twelfthMarksCardName(education.getTwelfthMarksCardName())

                // DEGREE
                .degreeName(education.getDegreeName())
                .specialization(education.getSpecialization())
                .collegeName(education.getCollegeName())
                .universityName(education.getUniversityName())
                .degreeLocation(education.getDegreeLocation())
                .usnNumber(education.getUsnNumber())
                .degreeStartYear(education.getDegreeStartYear())
                .degreeEndYear(education.getDegreeEndYear())
                .degreePercentage(education.getDegreePercentage())
                .degreeCertificateName(education.getDegreeCertificateName())

                // MASTER'S
                .mastersDegree(education.getMastersDegree())
                .mastersSpecialization(education.getMastersSpecialization())
                .mastersCollege(education.getMastersCollege())
                .mastersUniversity(education.getMastersUniversity())
                .mastersLocation(education.getMastersLocation())
                .mastersRegistrationNumber(education.getMastersRegistrationNumber())
                .mastersStartYear(education.getMastersStartYear())
                .mastersEndYear(education.getMastersEndYear())
                .mastersPercentage(education.getMastersPercentage())
                .mastersDegreeCertificateName(education.getMastersDegreeCertificateName())

                // TECHNICAL SKILLS - DO NOT REMOVE
                .technicalSkills(technicalSkills)

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
            throw new BadRequestException("Maximum allowed file size is 5 MB.");
        }

        String contentType = file.getContentType();

        if (contentType == null ||
                !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase(Locale.ROOT))) {
            throw new BadRequestException("Only PDF, JPG, JPEG and PNG files are allowed.");
        }
    }

    // ============================================================
    // SAVE FILE
    // ============================================================

    private void saveFile(
            MultipartFile file,
            Consumer<byte[]> dataSetter,
            Consumer<String> fileNameSetter,
            Consumer<String> contentTypeSetter) throws IOException {

        if (file == null || file.isEmpty()) {
            return;
        }

        validateFile(file);

        dataSetter.accept(file.getBytes());
        fileNameSetter.accept(file.getOriginalFilename());
        contentTypeSetter.accept(file.getContentType());
    }

    // ============================================================
    // AWS TEXTRACT OCR
    // ============================================================

    private String extractTextFromFile(MultipartFile file) {

        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Education document is required.");
        }

        try {

            validateFile(file);

            byte[] fileBytes = file.getBytes();

            DetectDocumentTextRequest request =
                    DetectDocumentTextRequest.builder()
                            .document(
                                    Document.builder()
                                            .bytes(SdkBytes.fromByteArray(fileBytes))
                                            .build()
                            )
                            .build();

            DetectDocumentTextResponse response =
                    textractClient.detectDocumentText(request);

            StringBuilder text = new StringBuilder();

            for (Block block : response.blocks()) {

                if (block.blockType() == BlockType.LINE) {

                    if (block.text() != null && !block.text().isBlank()) {
                        text.append(block.text()).append("\n");
                    }
                }
            }

            String result = text.toString().trim();

            log.info("========== TEXTRACT OCR TEXT ({}) ==========",
                    file.getOriginalFilename());
            log.info("\n{}", result);
            log.info("=============================================");

            if (result.isBlank()) {
                throw new BadRequestException(
                        "No text could be extracted from the education document."
                );
            }

            return result;

        } catch (BadRequestException ex) {
            throw ex;

        } catch (IOException ex) {
            log.error("Unable to read education document.", ex);
            throw new RuntimeException("Unable to read education document.", ex);

        } catch (Exception ex) {
            log.error("AWS Textract failed.", ex);
            throw new RuntimeException(
                    "Unable to extract text from education document.", ex
            );
        }
    }

    // ============================================================
    // OCR TEXT NORMALIZATION
    // ============================================================

    private String normalizeText(String text) {

        if (text == null) {
            return "";
        }

        return text
                .replace("\r", "\n")
                .replace('\u0000', ' ')
                .replaceAll("[\\t ]+", " ")
                .replaceAll(" *\n *", "\n")
                .replaceAll("\n{2,}", "\n")
                .trim();
    }

    private String cleanExtractedValue(String value) {

        if (value == null) {
            return null;
        }

        String cleaned = value
                .replaceAll("[|]+", " ")
                .replaceAll("\\s{2,}", " ")
                .replaceAll("^[\\s:;=\\-]+", "")
                .replaceAll("[\\s:;=\\-]+$", "")
                .trim();

        if (cleaned.isBlank() || cleaned.equalsIgnoreCase("string")) {
            return null;
        }

        return cleaned;
    }

    // ============================================================
    // KEY-VALUE BASED OCR EXTRACTION
    // ============================================================
    //
    // Printed forms are "Label : Value" pairs, often two pairs per
    // line, columns separated by a wide gap. Parse the whole document
    // into a Map<String,String> once, then every field is a direct,
    // safe lookup against that map instead of brittle line-by-line regex.

    private Map<String, String> extractKeyValuePairs(String text) {

        Map<String, String> map = new LinkedHashMap<>();

        if (text == null || text.isBlank()) {
            return map;
        }

        String[] lines = text.split("\\n");

        for (String line : lines) {

            if (line.isBlank()) {
                continue;
            }

            String[] segments = line.split("\\s{2,}");

            for (int i = 0; i < segments.length; i++) {

                String seg = segments[i].trim();

                if (seg.isEmpty()) {
                    continue;
                }

                int colonIdx = seg.indexOf(':');

                if (colonIdx <= 0) {
                    continue;
                }

                if (colonIdx < seg.length() - 1) {

                    // "Label: Value" both in the same segment
                    String label = normalizeLabel(seg.substring(0, colonIdx));
                    String value = cleanExtractedValue(seg.substring(colonIdx + 1));

                    if (!label.isEmpty() && value != null) {
                        map.putIfAbsent(label, value);
                    }

                } else {

                    // "Label:" alone; value is the next segment on this line
                    String label = normalizeLabel(seg.substring(0, colonIdx));

                    if (!label.isEmpty() && i + 1 < segments.length) {

                        String value = cleanExtractedValue(segments[i + 1]);

                        if (value != null) {
                            map.putIfAbsent(label, value);
                        }
                    }
                }
            }
        }

        return map;
    }

    // Strips non-English glyphs/punctuation noise from a label
    private String normalizeLabel(String label) {

        if (label == null) {
            return "";
        }

        return label.replaceAll("[^a-zA-Z. ]", " ")
                .replaceAll("\\s+", " ")
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    private String getValue(Map<String, String> map, String... keyFragments) {

        for (String fragment : keyFragments) {
            for (Map.Entry<String, String> entry : map.entrySet()) {
                if (entry.getKey().contains(fragment)) {
                    return entry.getValue();
                }
            }
        }

        return null;
    }

    // ------------------------------------------------------------
    // REGISTER / ROLL NUMBER FALLBACKS
    // ------------------------------------------------------------

    // Karnataka-style register number ("20" + 2-digit year + 5-9 digits)
    private String extractRegisterNumberByShape(String text) {

        Matcher matcher = Pattern.compile("\\b(20\\d{2}\\d{5,9})\\b").matcher(text);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    // DigiLocker-style "Reg. No. 20190318683" (no colon)
    private String extractRegNoNoColon(String text) {

        Matcher matcher = Pattern.compile(
                "(?i)\\bReg\\.?\\s*No\\.?\\s*[:\\-]?\\s*(\\d{6,15})\\b"
        ).matcher(text);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    // CBSE-style "Roll No 6603384" (no colon)
    private String extractRollNoNoColon(String text) {

        Matcher matcher = Pattern.compile(
                "(?i)\\bRoll\\s*No\\.?\\s*[:\\-]?\\s*(\\d{5,12})\\b"
        ).matcher(text);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    // DigiLocker-style "School Code/Name GA0206 - S.V.S. ENGLISH MEDIUM HIGH SCHOOL"
    private String[] extractSchoolCodeName(String text) {

        Matcher matcher = Pattern.compile(
                "(?i)School\\s*Code\\s*/\\s*Name\\s*[:\\-]?\\s*([A-Z0-9]{3,10})\\s*[-\u2013]\\s*(.+)"
        ).matcher(text);

        if (matcher.find()) {

            String rawName = matcher.group(2);

            rawName = rawName.split(
                    "(?i)\\s*(Date of Birth|Father'?s Name|Mother'?s Name|Gender|" +
                            "Register No|Reg\\.?\\s*No|Roll No|Candidate'?s Name)",
                    2
            )[0];

            String name = cleanExtractedValue(rawName);

            return new String[] { matcher.group(1).trim(), name };
        }

        return null;
    }

    // CBSE-style "School (Code) _KENDRIYA VIDYALAYA BERHAMPUR GANJAM OD (08314)"
    private String extractSchoolCodeLabelFormat(String text) {

        Matcher matcher = Pattern.compile(
                "(?i)School\\s*\\(Code\\)\\s*_?\\s*(.+?)\\s*\\(\\d+\\)"
        ).matcher(text);

        if (matcher.find()) {
            return cleanExtractedValue(matcher.group(1));
        }

        return null;
    }

    // CBSE-style "SENIOR SCHOOL CERTIFICATE EXAMINATION 2016" (no label)
    private Integer extractYearAfterExamination(String text) {

        Matcher matcher = Pattern.compile("(?i)EXAMINATION\\s+(\\d{4})").matcher(text);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }

        return null;
    }

    // ------------------------------------------------------------
    // BOARD - fixed phrase detection
    // ------------------------------------------------------------

    private String extractBoard(String text) {

        String lower = text.replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);

        if (lower.contains("karnataka school examination and assessment board")) {
            return "Karnataka School Examination and Assessment Board";
        }
        if (lower.contains("karnataka secondary education examination board")) {
            return "Karnataka Secondary Education Examination Board";
        }
        if (lower.contains("central board of secondary education")) {
            return "Central Board of Secondary Education";
        }
        if (lower.contains("cbse")) {
            return "CBSE";
        }
        if (lower.contains("indian certificate of secondary education")) {
            return "Indian Certificate of Secondary Education";
        }
        if (lower.contains("icse")) {
            return "ICSE";
        }
        if (lower.contains("department of pre-university education")) {
            return "Department of Pre-University Education";
        }
        if (lower.contains("pre-university education")) {
            return "Pre-University Education";
        }

        return null;
    }

    // ------------------------------------------------------------
    // UNIVERSITY - known-name detection
    // ------------------------------------------------------------

    private String extractUniversityName(String text) {

        String lower = text.toLowerCase(Locale.ROOT);

        String[] known = {
                "Visvesvaraya Technological University",
                "Bangalore University",
                "Bengaluru City University",
                "University of Mysore",
                "Mangalore University",
                "Kuvempu University",
                "Davangere University",
                "Tumkur University",
                "Gulbarga University",
                "Anna University"
        };

        for (String u : known) {
            if (lower.contains(u.toLowerCase(Locale.ROOT))) {
                return u;
            }
        }

        Matcher matcher = Pattern.compile(
                "([A-Za-z][A-Za-z .,&'-]{3,120}\\bUniversity\\b)",
                Pattern.CASE_INSENSITIVE
        ).matcher(text);

        if (matcher.find()) {
            return cleanExtractedValue(matcher.group(1));
        }

        return null;
    }

    // ------------------------------------------------------------
    // DEGREE / MASTER'S NAME - known-phrase detection
    // ------------------------------------------------------------

    private String extractDegreeName(String text) {

        String flat = text.replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);

        String[][] degrees = {
                {"master of technology", "Master of Technology"},
                {"master of engineering", "Master of Engineering"},
                {"master of science", "Master of Science"},
                {"master of commerce", "Master of Commerce"},
                {"master of arts", "Master of Arts"},
                {"master of business administration", "Master of Business Administration"},
                {"master of computer applications", "Master of Computer Applications"},
                {"bachelor of engineering", "Bachelor of Engineering"},
                {"bachelor of technology", "Bachelor of Technology"},
                {"bachelor of science", "Bachelor of Science"},
                {"bachelor of commerce", "Bachelor of Commerce"},
                {"bachelor of arts", "Bachelor of Arts"},
                {"bachelor of computer applications", "Bachelor of Computer Applications"},
                {"bachelor of business administration", "Bachelor of Business Administration"}
        };

        for (String[] degree : degrees) {
            if (flat.contains(degree[0])) {
                return degree[1];
            }
        }

        // Fuzzy fallback for stylized/cursive fonts (common on convocation certs)
        if (flat.contains("aster") && flat.contains("echnology")) return "Master of Technology";
        if (flat.contains("achelor") && flat.contains("ngineering")) return "Bachelor of Engineering";
        if (flat.contains("aster") && flat.contains("cience")) return "Master of Science";
        if (flat.contains("achelor") && flat.contains("echnology")) return "Bachelor of Technology";

        return null;
    }

    // ------------------------------------------------------------
    // PERCENTAGE - printed directly, e.g. "(91.04%)"
    // ------------------------------------------------------------

    private Double extractPercentageInParens(String text) {

        Matcher matcher = Pattern.compile(
                "\\(\\s*(\\d{1,3}(?:\\.\\d+)?)\\s*%\\s*\\)"
        ).matcher(text);

        if (matcher.find()) {
            Double value = parseDouble(matcher.group(1));
            if (value != null && value >= 0 && value <= 100) {
                return roundTwoDecimals(value);
            }
        }

        return null;
    }

    // ------------------------------------------------------------
    // PERCENTAGE - computed from a "TOTAL MARKS" row
    //
    // NOTE: grade-based sheets (subject grades like A1/A2/C1 instead
    // of numeric totals) legitimately have no percentage; returns
    // null in that case, which is correct.
    // ------------------------------------------------------------

    private Double computePercentageFromTotalsRow(String text) {

        String[] lines = text.split("\\n");

        for (int i = 0; i < lines.length; i++) {

            String lineLower = lines[i].toLowerCase(Locale.ROOT);

            if (!lineLower.contains("total marks") && !lineLower.contains("total")) {
                continue;
            }

            StringBuilder window = new StringBuilder(lines[i]);

            for (int j = i + 1; j < lines.length && j <= i + 4; j++) {
                window.append(" ").append(lines[j]);
            }

            Matcher matcher = Pattern.compile(
                    "(\\d{2,4})\\D{1,15}(\\d{2,4})"
            ).matcher(window.toString());

            while (matcher.find()) {

                Double first = parseDouble(matcher.group(1));
                Double second = parseDouble(matcher.group(2));

                if (first == null || second == null || first <= 0) {
                    continue;
                }

                double max = Math.max(first, second);
                double obtained = Math.min(first, second);

                if (max < 100) {
                    continue;
                }

                double pct = (obtained / max) * 100.0;

                if (pct >= 20 && pct <= 100) {
                    return roundTwoDecimals(pct);
                }
            }
        }

        return null;
    }

    private double roundTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    // Grabs up to N lines following a label line, joined with ", "
    private String extractBlockAfterLabel(String text, String label, int maxLines) {

        String[] lines = text.split("\\n");

        for (int i = 0; i < lines.length; i++) {

            String line = lines[i].toLowerCase(Locale.ROOT);

            if (line.contains(label)) {

                StringBuilder sb = new StringBuilder();
                int collected = 0;

                for (int j = i + 1; j < lines.length && collected < maxLines; j++) {

                    String next = cleanExtractedValue(lines[j]);

                    if (next == null) {
                        continue;
                    }

                    if (next.matches("(?i).*(school code|register|year|signature|chairperson).*")) {
                        break;
                    }

                    if (sb.length() > 0) {
                        sb.append(", ");
                    }

                    sb.append(next);
                    collected++;
                }

                String result = sb.toString();
                return result.isBlank() ? null : result;
            }
        }

        return null;
    }

    // Removes a trailing "(Autonomous College)" style annotation
    private String stripTrailingParenthetical(String value) {

        if (value == null) {
            return null;
        }

        String cleaned = value.replaceAll("\\(.*?\\)", "").trim();

        return cleaned.isBlank() ? cleanExtractedValue(value) : cleanExtractedValue(cleaned);
    }

    // Pulls a clean alphanumeric ID out of a raw value (register no, USN, etc.)
    private String extractCleanIdValue(String value) {

        if (value == null) {
            return null;
        }

        Matcher matcher = Pattern.compile("\\b[A-Za-z0-9]{4,20}\\b").matcher(value);

        if (matcher.find()) {
            return matcher.group().trim();
        }

        return null;
    }

    // Finds a plausible year (1950-2100) inside a string
    private Integer extractYearFromValue(String value) {

        if (value == null) {
            return null;
        }

        Matcher matcher = Pattern.compile("\\b(19\\d{2}|20\\d{2})\\b").matcher(value);

        while (matcher.find()) {

            int year = Integer.parseInt(matcher.group(1));

            if (year >= 1950 && year <= 2100) {
                return year;
            }
        }

        return null;
    }

    private Double parseDouble(String value) {

        try {
            return Double.parseDouble(value.replace(",", "").trim());
        } catch (Exception e) {
            return null;
        }
    }

    // Sanity guard for tiny OCR-fragment "Subject" values
    private String validateSpecialization(String value) {

        if (value == null) {
            return null;
        }

        String cleaned = cleanExtractedValue(value);

        if (cleaned == null || cleaned.length() < 4) {
            return null;
        }

        return cleaned;
    }

    // Raw-regex fallback for the "Subject" field
    private String extractSubjectFallback(String text) {

        Matcher matcher = Pattern.compile("(?i)subject\\s*[:\\-]?\\s*(.+)").matcher(text);

        if (matcher.find()) {
            return matcher.group(1);
        }

        return null;
    }

    private String resolveSubject(Map<String, String> kv, String text) {

        String subject = getValue(kv, "subject");

        if (subject == null || validateSpecialization(subject) == null) {

            String fallback = extractSubjectFallback(text);

            if (fallback != null) {
                subject = fallback;
            }
        }

        return subject;
    }

    // ============================================================
    // 10TH OCR
    // ============================================================

    private void extractTenthData(Education education, String rawText) {

        if (rawText == null || rawText.isBlank()) {
            return;
        }

        String text = normalizeText(rawText);
        Map<String, String> kv = extractKeyValuePairs(text);

        log.info("TENTH KV MAP: {}", kv);

        setIfNotNull(extractBoard(text), education::setTenthBoard);

        String regNo = getValue(kv, "register no", "reg no", "registration no");
        regNo = extractCleanIdValue(regNo);

        if (regNo == null) {
            regNo = extractRegNoNoColon(text);
        }
        if (regNo == null) {
            regNo = extractRegisterNumberByShape(text);
        }

        setIfNotNull(regNo, education::setTenthRollNumber);

        String yearVal = getValue(kv, "year of result", "year");
        Integer year = extractYearFromValue(yearVal);

        if (year == null && regNo != null && regNo.matches("20\\d{7,10}")) {
            year = Integer.parseInt(regNo.substring(0, 4));
        }

        setIfNotNull(year, education::setTenthPassingYear);

        Double percentage = extractPercentageInParens(text);
        if (percentage == null) {
            percentage = computePercentageFromTotalsRow(text);
        }

        setIfNotNull(percentage, education::setTenthPercentage);

        String[] schoolCodeName = extractSchoolCodeName(text);

        if (schoolCodeName != null) {

            setIfNotNull(schoolCodeName[1], education::setTenthSchoolName);

        } else {

            String schoolBlock = extractBlockAfterLabel(text, "school name and address", 2);

            if (schoolBlock != null) {

                String[] parts = schoolBlock.split(",", 2);

                setIfNotNull(cleanExtractedValue(parts[0]), education::setTenthSchoolName);

                if (parts.length > 1) {
                    setIfNotNull(cleanExtractedValue(parts[1]), education::setTenthSchoolLocation);
                }
            }
        }
    }

    // ============================================================
    // 12TH OCR
    // ============================================================

    private void extractTwelfthData(Education education, String rawText) {

        if (rawText == null || rawText.isBlank()) {
            return;
        }

        String text = normalizeText(rawText);
        Map<String, String> kv = extractKeyValuePairs(text);

        log.info("TWELFTH KV MAP: {}", kv);

        setIfNotNull(extractBoard(text), education::setTwelfthBoardUniversity);

        String regNo = getValue(kv, "register no", "reg no", "registration no", "regn no",
                "roll no", "admit card id", "admit card no");
        regNo = extractCleanIdValue(regNo);

        if (regNo == null) {
            regNo = extractRegNoNoColon(text);
        }
        if (regNo == null) {
            regNo = extractRollNoNoColon(text);
        }
        if (regNo == null) {
            regNo = extractRegisterNumberByShape(text);
        }

        setIfNotNull(regNo, education::setTwelfthRegistrationNumber);

        String yearVal = getValue(kv, "year of result", "year", "year of passing", "exam year");
        Integer year = extractYearFromValue(yearVal);

        if (year == null) {
            year = extractYearAfterExamination(text);
        }
        if (year == null && regNo != null && regNo.matches("20\\d{7,10}")) {
            year = Integer.parseInt(regNo.substring(0, 4));
        }
        if (year == null) {
            year = extractYearFromValue(text);
        }

        setIfNotNull(year, education::setTwelfthPassingYear);

        Double percentage = extractPercentageInParens(text);
        if (percentage == null) {
            percentage = computePercentageFromTotalsRow(text);
        }

        setIfNotNull(percentage, education::setTwelfthPercentage);

        String collegeDetails = getValue(kv, "college details", "college", "school",
                "institution", "name of institution", "name of the school");

        if (collegeDetails == null) {
            collegeDetails = extractSchoolCodeLabelFormat(text);
        }
        if (collegeDetails == null) {
            collegeDetails = extractBlockAfterLabel(text, "college details", 1);
        }
        if (collegeDetails == null) {
            collegeDetails = extractBlockAfterLabel(text, "school", 1);
        }
        if (collegeDetails == null) {
            collegeDetails = extractBlockAfterLabel(text, "institution", 1);
        }

        if (collegeDetails != null) {

            String stripped = collegeDetails.replaceFirst("^[A-Z]{1,4}\\d{3,6},\\s*", "");

            String[] parts = stripped.split(",", 2);

            setIfNotNull(cleanExtractedValue(parts[0]), education::setTwelfthInstitutionName);

            if (parts.length > 1) {
                setIfNotNull(cleanExtractedValue(parts[1]), education::setTwelfthLocation);
            }
        }
    }

    // ============================================================
    // DEGREE OCR
    // ============================================================

    private void extractDegreeData(Education education, String rawText) {

        if (rawText == null || rawText.isBlank()) {
            return;
        }

        String text = normalizeText(rawText);
        Map<String, String> kv = extractKeyValuePairs(text);

        setIfNotNull(extractDegreeName(text), education::setDegreeName);

        String college = getValue(kv, "name of the college", "college name");

        if (college == null) {
            college = extractBlockAfterLabel(text, "name of the college", 2);
        }

        setIfNotNull(stripTrailingParenthetical(college), education::setCollegeName);

        setIfNotNull(extractUniversityName(text), education::setUniversityName);

        setIfNotNull(
                validateSpecialization(resolveSubject(kv, text)),
                education::setSpecialization
        );

        String usn = getValue(kv, "university seat number", "seat number", "usn");
        usn = extractCleanIdValue(usn);

        if (usn != null) {
            education.setUsnNumber(usn.toUpperCase(Locale.ROOT));
        }

        String dateVal = getValue(kv, "date");
        Integer year = extractYearFromValue(dateVal);

        if (year == null) {
            year = extractYearFromValue(text);
        }

        setIfNotNull(year, education::setDegreeEndYear);

        Double percentage = extractPercentageInParens(text);
        if (percentage == null) {
            percentage = computePercentageFromTotalsRow(text);
        }

        setIfNotNull(percentage, education::setDegreePercentage);
    }

    // ============================================================
    // MASTER'S OCR
    // ============================================================

    private void extractMastersData(Education education, String rawText) {

        if (rawText == null || rawText.isBlank()) {
            return;
        }

        String text = normalizeText(rawText);
        Map<String, String> kv = extractKeyValuePairs(text);

        setIfNotNull(extractDegreeName(text), education::setMastersDegree);

        String college = getValue(kv, "name of the college", "college name");

        if (college == null) {
            college = extractBlockAfterLabel(text, "name of the college", 2);
        }

        setIfNotNull(stripTrailingParenthetical(college), education::setMastersCollege);

        setIfNotNull(extractUniversityName(text), education::setMastersUniversity);

        setIfNotNull(
                validateSpecialization(resolveSubject(kv, text)),
                education::setMastersSpecialization
        );

        String regNo = getValue(kv, "university seat number", "seat number", "usn");
        regNo = extractCleanIdValue(regNo);

        if (regNo != null) {
            education.setMastersRegistrationNumber(regNo.toUpperCase(Locale.ROOT));
        }

        String dateVal = getValue(kv, "date");
        Integer year = extractYearFromValue(dateVal);

        if (year == null) {
            year = extractYearFromValue(text);
        }

        setIfNotNull(year, education::setMastersEndYear);

        Double percentage = extractPercentageInParens(text);
        if (percentage == null) {
            percentage = computePercentageFromTotalsRow(text);
        }

        setIfNotNull(percentage, education::setMastersPercentage);
    }

    // ============================================================
    // SET ONLY IF VALUE EXISTS
    // ============================================================

    private <T> void setIfNotNull(T value, Consumer<T> setter) {

        if (value != null && !value.toString().isBlank()) {
            setter.accept(value);
        }
    }

    // ============================================================
    // MANUAL VALUES (override OCR)
    // ============================================================

    private void applyManualValues(Education education, EducationRequest request) {

        if (request == null) {
            return;
        }

        // 10TH
        setManualString(request.getTenthSchoolName(), education::setTenthSchoolName);
        setManualString(request.getTenthBoard(), education::setTenthBoard);
        setManualString(request.getTenthSchoolLocation(), education::setTenthSchoolLocation);
        setManualString(request.getTenthRollNumber(), education::setTenthRollNumber);
        setManualYear(request.getTenthPassingYear(), education::setTenthPassingYear);
        setManualPercentage(request.getTenthPercentage(), education::setTenthPercentage);

        // 12TH
        setManualString(request.getTwelfthInstitutionName(), education::setTwelfthInstitutionName);
        setManualString(request.getTwelfthLocation(), education::setTwelfthLocation);
        setManualString(request.getTwelfthBoardUniversity(), education::setTwelfthBoardUniversity);
        setManualString(request.getTwelfthRegistrationNumber(), education::setTwelfthRegistrationNumber);
        setManualYear(request.getTwelfthPassingYear(), education::setTwelfthPassingYear);
        setManualPercentage(request.getTwelfthPercentage(), education::setTwelfthPercentage);

        // DEGREE
        setManualString(request.getDegreeName(), education::setDegreeName);
        setManualString(request.getSpecialization(), education::setSpecialization);
        setManualString(request.getCollegeName(), education::setCollegeName);
        setManualString(request.getUniversityName(), education::setUniversityName);
        setManualString(request.getDegreeLocation(), education::setDegreeLocation);
        setManualString(request.getUsnNumber(), education::setUsnNumber);
        setManualYear(request.getDegreeStartYear(), education::setDegreeStartYear);
        setManualYear(request.getDegreeEndYear(), education::setDegreeEndYear);
        setManualPercentage(request.getDegreePercentage(), education::setDegreePercentage);

        // MASTER'S
        setManualString(request.getMastersDegree(), education::setMastersDegree);
        setManualString(request.getMastersSpecialization(), education::setMastersSpecialization);
        setManualString(request.getMastersCollege(), education::setMastersCollege);
        setManualString(request.getMastersUniversity(), education::setMastersUniversity);
        setManualString(request.getMastersLocation(), education::setMastersLocation);
        setManualString(request.getMastersRegistrationNumber(), education::setMastersRegistrationNumber);
        setManualYear(request.getMastersStartYear(), education::setMastersStartYear);
        setManualYear(request.getMastersEndYear(), education::setMastersEndYear);
        setManualPercentage(request.getMastersPercentage(), education::setMastersPercentage);

        // TECHNICAL SKILLS
        if (request.getTechnicalSkills() != null && !request.getTechnicalSkills().isEmpty()) {

            Candidate candidate = education.getCandidate();

            if (candidate != null) {
                candidate.setTechnicalSkills(request.getTechnicalSkills());
                candidateRepository.save(candidate);
            }
        }
    }

    private void setManualString(String value, Consumer<String> setter) {

        if (value == null) {
            return;
        }

        String cleaned = value.trim();

        // Ignore Swagger/OpenAPI placeholder values.
        if (cleaned.isBlank()
                || cleaned.equalsIgnoreCase("string")
                || cleaned.equalsIgnoreCase("null")) {
            return;
        }

        setter.accept(cleaned);
    }

    private void setManualYear(Integer value, Consumer<Integer> setter) {

        if (value == null) {
            return;
        }

        // Ignore Swagger's default 0.
        if (value >= 1900 && value <= 2100) {
            setter.accept(value);
        }
    }

    private void setManualPercentage(Double value, Consumer<Double> setter) {

        if (value == null) {
            return;
        }

        // Ignore Swagger's default 0.1 and invalid values.
        if (value > 0.1 && value <= 100.0) {
            setter.accept(value);
        }
    }

    // ============================================================
    // MAP REQUEST + OCR -> ENTITY
    // ============================================================

    private void mapRequestToEntity(Education education, EducationRequest request) throws IOException {

        if (request == null) {
            throw new BadRequestException("Education request is required.");
        }

        /*
         * IMPORTANT:
         * 1. Upload document
         * 2. OCR extracts available values (AWS Textract)
         * 3. Missing values stay unchanged/empty
         * 4. Manual values (applyManualValues) override OCR, applied last
         */

        // 10TH MARKS CARD
        if (request.getTenthMarksCard() != null && !request.getTenthMarksCard().isEmpty()) {

            saveFile(
                    request.getTenthMarksCard(),
                    education::setTenthMarksCard,
                    education::setTenthMarksCardName,
                    education::setTenthMarksCardContentType
            );

            String text = extractTextFromFile(request.getTenthMarksCard());
            extractTenthData(education, text);
        }

        // 12TH MARKS CARD
        if (request.getTwelfthMarksCard() != null && !request.getTwelfthMarksCard().isEmpty()) {

            saveFile(
                    request.getTwelfthMarksCard(),
                    education::setTwelfthMarksCard,
                    education::setTwelfthMarksCardName,
                    education::setTwelfthMarksCardContentType
            );

            String text = extractTextFromFile(request.getTwelfthMarksCard());
            extractTwelfthData(education, text);
        }

        // DEGREE CERTIFICATE
        if (request.getDegreeCertificate() != null && !request.getDegreeCertificate().isEmpty()) {

            saveFile(
                    request.getDegreeCertificate(),
                    education::setDegreeCertificate,
                    education::setDegreeCertificateName,
                    education::setDegreeCertificateContentType
            );

            String text = extractTextFromFile(request.getDegreeCertificate());
            extractDegreeData(education, text);
        }

        // MASTER'S DEGREE CERTIFICATE
        if (request.getMastersDegreeCertificate() != null
                && !request.getMastersDegreeCertificate().isEmpty()) {

            saveFile(
                    request.getMastersDegreeCertificate(),
                    education::setMastersDegreeCertificate,
                    education::setMastersDegreeCertificateName,
                    education::setMastersDegreeCertificateContentType
            );

            String text = extractTextFromFile(request.getMastersDegreeCertificate());
            extractMastersData(education, text);
        }

        // MANUAL VALUES OVERRIDE OCR - applied last
        applyManualValues(education, request);
    }

    // ============================================================
    // SAVE EDUCATION
    // ============================================================

    @Override
    public EducationResponse saveEducation(EducationRequest request) {

        try {

            Candidate candidate = getLoggedInCandidate();

            if (educationRepository.existsByCandidate(candidate)) {
                throw new BadRequestException("Education details already exist.");
            }

            Education education = new Education();
            education.setCandidate(candidate);

            mapRequestToEntity(education, request);

            Education savedEducation = educationRepository.save(education);

            log.info("Education details saved for candidate {}", candidate.getId());

            return mapToResponse(savedEducation);

        } catch (BadRequestException ex) {
            throw ex;

        } catch (IOException ex) {
            log.error("Failed to save education documents.", ex);
            throw new RuntimeException("Unable to save education documents.", ex);
        }
    }

    // ============================================================
    // UPDATE EDUCATION
    // ============================================================

    @Override
    public EducationResponse updateEducation(EducationRequest request) {

        try {

            Candidate candidate = getLoggedInCandidate();

            Education education = educationRepository
                    .findByCandidateId(candidate.getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Education details not found."));

            mapRequestToEntity(education, request);

            Education updatedEducation = educationRepository.save(education);

            return mapToResponse(updatedEducation);

        } catch (BadRequestException ex) {
            throw ex;

        } catch (IOException ex) {
            log.error("Failed to update education documents.", ex);
            throw new RuntimeException("Unable to update education documents.", ex);
        }
    }

    // ============================================================
    // GET MY EDUCATION
    // ============================================================

    @Override
    public EducationResponse getMyEducation() {

        Candidate candidate = getLoggedInCandidate();

        Education education = educationRepository
                .findByCandidateId(candidate.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Education details not found."));

        return mapToResponse(education);
    }

    // ============================================================
    // GET EDUCATION BY CANDIDATE ID
    // ============================================================

    @Override
    public EducationResponse getEducationByCandidateId(Long candidateId) {

        if (candidateId == null) {
            throw new BadRequestException("Candidate ID is required.");
        }

        Education education = educationRepository
                .findByCandidateId(candidateId)
                .orElseThrow(() -> new ResourceNotFoundException("Education details not found."));

        return mapToResponse(education);
    }

    // ============================================================
    // VIEW DOCUMENT
    // ============================================================

    @Override
    public Resource viewDocument(Long educationId, EducationDocumentType documentType) {

        if (educationId == null) {
            throw new BadRequestException("Education ID is required.");
        }

        if (documentType == null) {
            throw new BadRequestException("Document type is required.");
        }

        Education education = educationRepository
                .findById(educationId)
                .orElseThrow(() -> new ResourceNotFoundException("Education details not found."));

        byte[] fileData;

        switch (documentType) {

            case TENTH_MARKS_CARD:
                fileData = education.getTenthMarksCard();
                break;

            case TWELFTH_MARKS_CARD:
                fileData = education.getTwelfthMarksCard();
                break;

            case DEGREE_CERTIFICATE:
                fileData = education.getDegreeCertificate();
                break;

            case MASTERS_DEGREE_CERTIFICATE:
                fileData = education.getMastersDegreeCertificate();
                break;

            default:
                throw new BadRequestException("Invalid education document type.");
        }

        if (fileData == null || fileData.length == 0) {
            throw new ResourceNotFoundException("Document not found.");
        }

        return new ByteArrayResource(fileData);
    }

    // ============================================================
    // GET MY TECHNICAL SKILLS - DO NOT REMOVE
    // ============================================================

    @Override
    public List<TechnicalSkill> getMyTechnicalSkills() {

        Candidate candidate = getLoggedInCandidate();

        return candidate.getTechnicalSkills();
    }

    // ============================================================
    // DELETE EDUCATION
    // ============================================================

    @Override
    public void deleteEducation() {

        Candidate candidate = getLoggedInCandidate();

        Education education = educationRepository
                .findByCandidateId(candidate.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Education details not found."));

        educationRepository.delete(education);
    }

    // ============================================================
    // EXTRACT EDUCATION TEXT
    // ============================================================

    @Override
    public String extractEducationText(MultipartFile file) {

        return extractTextFromFile(file);
    }

    // ============================================================
    // EXTRACT AND POPULATE EDUCATION
    // ============================================================

    @Override
    public EducationResponse extractAndPopulateEducation(EducationRequest request) {

        if (request == null) {
            throw new BadRequestException("Education request is required.");
        }

        try {

            Candidate candidate = getLoggedInCandidate();

            Education education = educationRepository
                    .findByCandidateId(candidate.getId())
                    .orElse(null);

            if (education == null) {
                education = new Education();
                education.setCandidate(candidate);
            }

            mapRequestToEntity(education, request);

            Education savedEducation = educationRepository.save(education);

            log.info(
                    "Education extracted and populated successfully for candidate {}",
                    candidate.getId()
            );

            return mapToResponse(savedEducation);

        } catch (BadRequestException ex) {
            throw ex;

        } catch (IOException ex) {
            log.error("Failed to extract and populate education.", ex);
            throw new RuntimeException("Unable to process education documents.", ex);
        }
    }
}