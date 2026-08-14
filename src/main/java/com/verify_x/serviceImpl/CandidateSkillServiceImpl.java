package com.verify_x.serviceImpl;

import com.verify_x.dto.TechnicalSkillDto;
import com.verify_x.dto.TechnicalSkillResponseDto;
import com.verify_x.dto.UpdateTechnicalSkillRequestDto;
import com.verify_x.entity.Candidate;
import com.verify_x.enums.TechnicalSkill;
import com.verify_x.exception.ResourceNotFoundException;
import com.verify_x.jwt.UserPrincipal;
import com.verify_x.repository.CandidateRepository;
import com.verify_x.services.CandidateSkillService;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CandidateSkillServiceImpl implements CandidateSkillService {

    private final CandidateRepository candidateRepository;


    // =========================================================
    // GET ALL TECHNICAL SKILLS
    // =========================================================

    @Override
    public TechnicalSkillResponseDto getTechnicalSkills() {

        Candidate candidate = getLoggedInCandidate();

        List<TechnicalSkill> selectedSkills =
                candidate.getTechnicalSkills() != null
                        ? candidate.getTechnicalSkills()
                        : Collections.emptyList();

        List<TechnicalSkill> recommendedSkills =
                getRecommendations(candidate.getAppliedRole());

        List<TechnicalSkillDto> skills =
                Arrays.stream(TechnicalSkill.values())
                        .map(skill -> TechnicalSkillDto.builder()
                                .value(skill.name())
                                .name(formatSkillName(skill))
                                .selected(selectedSkills.contains(skill))
                                .recommended(recommendedSkills.contains(skill))
                                .build())
                        .toList();

        return TechnicalSkillResponseDto.builder()
                .candidateId(candidate.getId())
                .appliedRole(
                        candidate.getAppliedRole() != null
                                ? candidate.getAppliedRole().name()
                                : null
                )
                .skills(skills)
                .build();
    }


 // =========================================================
 // UPDATE ALL SELECTED TECHNICAL SKILLS
 // =========================================================

 @Override
 public TechnicalSkillResponseDto updateTechnicalSkills(
         UpdateTechnicalSkillRequestDto request) {

     Candidate candidate = getLoggedInCandidate();

     List<TechnicalSkill> requestedSkills =
             request.getTechnicalSkills() != null
                     ? request.getTechnicalSkills()
                     : Collections.emptyList();

     List<TechnicalSkill> currentSkills =
             candidate.getTechnicalSkills();

     // Initialize collection if it is null
     if (currentSkills == null) {
         currentSkills = new ArrayList<>();
         candidate.setTechnicalSkills(currentSkills);
     } else {
         // Modify the existing JPA collection
         currentSkills.clear();
     }

     // Add all requested skills
     currentSkills.addAll(
             requestedSkills.stream()
                     .distinct()
                     .toList()
     );

     candidateRepository.save(candidate);

     return getTechnicalSkills();
 }
 // =========================================================
 // REMOVE SINGLE TECHNICAL SKILL
 // =========================================================

 @Override
 public TechnicalSkillResponseDto removeTechnicalSkill(
         TechnicalSkill skill) {

     Candidate candidate = getLoggedInCandidate();

     List<TechnicalSkill> selectedSkills =
             candidate.getTechnicalSkills();

     if (selectedSkills != null) {
         selectedSkills.remove(skill);
         candidate.setTechnicalSkills(selectedSkills);
     }

     candidateRepository.save(candidate);

     return getTechnicalSkills();
 }


    // =========================================================
    // SEARCH TECHNICAL SKILLS
    // =========================================================

    @Override
    public List<TechnicalSkill> searchTechnicalSkills(
            String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {

            return Arrays.stream(TechnicalSkill.values())
                    .toList();
        }

        String searchKeyword =
                keyword.trim()
                        .toLowerCase(Locale.ROOT);

        return Arrays.stream(TechnicalSkill.values())
                .filter(skill -> {

                    String enumName =
                            skill.name()
                                    .toLowerCase(Locale.ROOT);

                    String displayName =
                            formatSkillName(skill)
                                    .toLowerCase(Locale.ROOT);

                    return enumName.contains(searchKeyword)
                            || displayName.contains(searchKeyword);
                })
                .collect(Collectors.toList());
    }


    // =========================================================
    // GET RECOMMENDED SKILLS
    // =========================================================

    @Override
    public TechnicalSkillResponseDto getRecommendedSkills() {

        Candidate candidate = getLoggedInCandidate();

        List<TechnicalSkill> selectedSkills =
                candidate.getTechnicalSkills() != null
                        ? candidate.getTechnicalSkills()
                        : Collections.emptyList();

        List<TechnicalSkill> recommendedSkills =
                getRecommendations(candidate.getAppliedRole());

        /*
         * Same combined skill list used by the main
         * technical-skills endpoint.
         */
        List<TechnicalSkillDto> skills =
                Arrays.stream(TechnicalSkill.values())
                        .map(skill -> TechnicalSkillDto.builder()
                                .value(skill.name())
                                .name(formatSkillName(skill))
                                .selected(selectedSkills.contains(skill))
                                .recommended(recommendedSkills.contains(skill))
                                .build())
                        .toList();

        return TechnicalSkillResponseDto.builder()
                .candidateId(candidate.getId())
                .appliedRole(
                        candidate.getAppliedRole() != null
                                ? candidate.getAppliedRole().name()
                                : null
                )
                .skills(skills)
                .build();
    }


    // =========================================================
    // GET LOGGED-IN CANDIDATE
    // =========================================================

    private Candidate getLoggedInCandidate() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        if (authentication == null ||
                !(authentication.getPrincipal()
                        instanceof UserPrincipal)) {

            throw new ResourceNotFoundException(
                    "Logged-in candidate not found."
            );
        }

        UserPrincipal principal =
                (UserPrincipal) authentication.getPrincipal();

        Long userId = principal.getUserId();

        return candidateRepository.findById(userId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Candidate not found."
                        )
                );
    }


    // =========================================================
    // ROLE BASED RECOMMENDATIONS
    // =========================================================

    private List<TechnicalSkill> getRecommendations(
            Candidate.AppliedRole appliedRole) {

        if (appliedRole == null) {
            return Collections.emptyList();
        }

        return switch (appliedRole) {

            case FRONTEND_DEVELOPER ->
                    List.of(
                            TechnicalSkill.HTML,
                            TechnicalSkill.CSS,
                            TechnicalSkill.JAVASCRIPT,
                            TechnicalSkill.TYPESCRIPT,
                            TechnicalSkill.REACT_JS,
                            TechnicalSkill.ANGULAR,
                            TechnicalSkill.SQL
                    );

            case REACT_DEVELOPER ->
                    List.of(
                            TechnicalSkill.HTML,
                            TechnicalSkill.CSS,
                            TechnicalSkill.JAVASCRIPT,
                            TechnicalSkill.TYPESCRIPT,
                            TechnicalSkill.REACT_JS,
                            TechnicalSkill.NODE_JS,
                            TechnicalSkill.MONGODB
                    );

            case ANGULAR_DEVELOPER ->
                    List.of(
                            TechnicalSkill.HTML,
                            TechnicalSkill.CSS,
                            TechnicalSkill.JAVASCRIPT,
                            TechnicalSkill.TYPESCRIPT,
                            TechnicalSkill.ANGULAR,
                            TechnicalSkill.NODE_JS,
                            TechnicalSkill.SQL
                    );

            case VUE_JS_DEVELOPER ->
                    List.of(
                            TechnicalSkill.HTML,
                            TechnicalSkill.CSS,
                            TechnicalSkill.JAVASCRIPT,
                            TechnicalSkill.TYPESCRIPT,
                            TechnicalSkill.SQL
                    );

            case UI_DEVELOPER ->
                    List.of(
                            TechnicalSkill.HTML,
                            TechnicalSkill.CSS,
                            TechnicalSkill.JAVASCRIPT,
                            TechnicalSkill.TYPESCRIPT,
                            TechnicalSkill.REACT_JS,
                            TechnicalSkill.ANGULAR
                    );

            case BACKEND_DEVELOPER ->
                    List.of(
                            TechnicalSkill.JAVA,
                            TechnicalSkill.PYTHON,
                            TechnicalSkill.NODE_JS,
                            TechnicalSkill.SQL,
                            TechnicalSkill.MYSQL,
                            TechnicalSkill.MONGODB,
                            TechnicalSkill.DOCKER
                    );

            case JAVA_DEVELOPER ->
                    List.of(
                            TechnicalSkill.JAVA,
                            TechnicalSkill.SPRING_BOOT,
                            TechnicalSkill.SQL,
                            TechnicalSkill.MYSQL,
                            TechnicalSkill.ORACLE,
                            TechnicalSkill.GIT,
                            TechnicalSkill.DOCKER
                    );

            case SPRING_BOOT_DEVELOPER ->
                    List.of(
                            TechnicalSkill.JAVA,
                            TechnicalSkill.SPRING_BOOT,
                            TechnicalSkill.SQL,
                            TechnicalSkill.MYSQL,
                            TechnicalSkill.MONGODB,
                            TechnicalSkill.GIT,
                            TechnicalSkill.DOCKER
                    );

            case DOT_NET_DEVELOPER ->
                    List.of(
                            TechnicalSkill.DOT_NET,
                            TechnicalSkill.CSHARP,
                            TechnicalSkill.ASP_NET_MVC,
                            TechnicalSkill.SQL_SERVER,
                            TechnicalSkill.GIT
                    );

            case C_SHARP_DEVELOPER ->
                    List.of(
                            TechnicalSkill.CSHARP,
                            TechnicalSkill.DOT_NET,
                            TechnicalSkill.ASP_NET_MVC,
                            TechnicalSkill.SQL_SERVER,
                            TechnicalSkill.GIT
                    );

            case PYTHON_DEVELOPER ->
                    List.of(
                            TechnicalSkill.PYTHON,
                            TechnicalSkill.SQL,
                            TechnicalSkill.MYSQL,
                            TechnicalSkill.MONGODB,
                            TechnicalSkill.GIT,
                            TechnicalSkill.DOCKER
                    );

            case NODE_JS_DEVELOPER ->
                    List.of(
                            TechnicalSkill.NODE_JS,
                            TechnicalSkill.JAVASCRIPT,
                            TechnicalSkill.TYPESCRIPT,
                            TechnicalSkill.REACT_JS,
                            TechnicalSkill.MONGODB,
                            TechnicalSkill.SQL,
                            TechnicalSkill.GIT
                    );

            case PHP_DEVELOPER ->
                    List.of(
                            TechnicalSkill.PHP,
                            TechnicalSkill.HTML,
                            TechnicalSkill.CSS,
                            TechnicalSkill.JAVASCRIPT,
                            TechnicalSkill.MYSQL,
                            TechnicalSkill.GIT
                    );

            case RUBY_ON_RAILS_DEVELOPER ->
                    List.of(
                            TechnicalSkill.RUBY,
                            TechnicalSkill.SQL,
                            TechnicalSkill.MYSQL,
                            TechnicalSkill.MONGODB,
                            TechnicalSkill.HTML,
                            TechnicalSkill.CSS,
                            TechnicalSkill.GIT
                    );

            case GO_DEVELOPER ->
                    List.of(
                            TechnicalSkill.GO,
                            TechnicalSkill.SQL,
                            TechnicalSkill.MYSQL,
                            TechnicalSkill.MONGODB,
                            TechnicalSkill.DOCKER,
                            TechnicalSkill.GIT
                    );
        };
    }


    // =========================================================
    // TECHNICAL SKILL DISPLAY NAME
    // =========================================================

    private String formatSkillName(TechnicalSkill skill) {

        return switch (skill) {

            case C -> "C";
            case CPP -> "C++";
            case CSHARP -> "C#";
            case DOT_NET -> ".NET";
            case ASP_NET -> "ASP.NET";
            case ASP_NET_CORE -> "ASP.NET Core";
            case ASP_NET_MVC -> "ASP.NET MVC";

            case JAVA -> "Java";
            case JAVASCRIPT -> "JavaScript";
            case TYPESCRIPT -> "TypeScript";
            case PYTHON -> "Python";
            case GO -> "Go";
            case RUST -> "Rust";
            case KOTLIN -> "Kotlin";
            case SWIFT -> "Swift";
            case DART -> "Dart";

            case HTML -> "HTML";
            case CSS -> "CSS";
            case REACT_JS -> "React.js";
            case NEXT_JS -> "Next.js";
            case ANGULAR -> "Angular";
            case VUE_JS -> "Vue.js";
            case NUXT_JS -> "Nuxt.js";
            case SVELTE -> "Svelte";
            case BOOTSTRAP -> "Bootstrap";
            case TAILWIND_CSS -> "Tailwind CSS";
            case MATERIAL_UI -> "Material UI";

            case SPRING -> "Spring";
            case SPRING_BOOT -> "Spring Boot";
            case SPRING_MVC -> "Spring MVC";
            case SPRING_SECURITY -> "Spring Security";
            case SPRING_CLOUD -> "Spring Cloud";
            case HIBERNATE -> "Hibernate";
            case JPA -> "JPA";

            case NODE_JS -> "Node.js";
            case EXPRESS_JS -> "Express.js";
            case NEST_JS -> "NestJS";
            case DJANGO -> "Django";
            case FLASK -> "Flask";
            case FAST_API -> "FastAPI";
            case LARAVEL -> "Laravel";
            case RUBY_ON_RAILS -> "Ruby on Rails";
            case GIN -> "Gin";
            case ECHO -> "Echo";

            case SQL -> "SQL";
            case MYSQL -> "MySQL";
            case POSTGRESQL -> "PostgreSQL";
            case SQL_SERVER -> "SQL Server";
            case ORACLE -> "Oracle";
            case SQLITE -> "SQLite";
            case MARIADB -> "MariaDB";
            case MONGODB -> "MongoDB";
            case FIREBASE -> "Firebase";
            case FIRESTORE -> "Firestore";
            case DYNAMODB -> "DynamoDB";
            case CASSANDRA -> "Cassandra";
            case NEO4J -> "Neo4j";
            case REDIS -> "Redis";
            case MEMCACHED -> "Memcached";

            case AWS -> "AWS";
            case AWS_EC2 -> "AWS EC2";
            case AWS_S3 -> "AWS S3";
            case AWS_RDS -> "AWS RDS";
            case AWS_LAMBDA -> "AWS Lambda";
            case AZURE -> "Azure";
            case AZURE_APP_SERVICE -> "Azure App Service";
            case AZURE_FUNCTIONS -> "Azure Functions";
            case AZURE_SQL_DATABASE -> "Azure SQL Database";
            case GOOGLE_CLOUD -> "Google Cloud";
            case GOOGLE_CLOUD_RUN -> "Google Cloud Run";

            case GIT -> "Git";
            case GITHUB -> "GitHub";
            case GITLAB -> "GitLab";
            case BITBUCKET -> "Bitbucket";
            case DOCKER -> "Docker";
            case KUBERNETES -> "Kubernetes";
            case HELM -> "Helm";
            case JENKINS -> "Jenkins";
            case GITHUB_ACTIONS -> "GitHub Actions";
            case GITLAB_CI_CD -> "GitLab CI/CD";
            case TERRAFORM -> "Terraform";
            case ANSIBLE -> "Ansible";
            case NGINX -> "Nginx";
            case APACHE -> "Apache";

            case REST_API -> "REST API Development";
            case GRAPHQL -> "GraphQL";
            case SOAP -> "SOAP";
            case WEBSOCKET -> "WebSocket";

            case APACHE_KAFKA -> "Apache Kafka";
            case RABBITMQ -> "RabbitMQ";
            case APACHE_ACTIVEMQ -> "Apache ActiveMQ";

            case MANUAL_TESTING -> "Manual Testing";
            case AUTOMATION_TESTING -> "Automation Testing";
            case JUNIT -> "JUnit";
            case TESTNG -> "TestNG";
            case MOCKITO -> "Mockito";
            case SELENIUM -> "Selenium";
            case CYPRESS -> "Cypress";
            case PLAYWRIGHT -> "Playwright";
            case POSTMAN -> "Postman";
            case REST_ASSURED -> "REST Assured";

            case MACHINE_LEARNING -> "Machine Learning";
            case DEEP_LEARNING -> "Deep Learning";
            case ARTIFICIAL_INTELLIGENCE -> "Artificial Intelligence";
            case NATURAL_LANGUAGE_PROCESSING -> "Natural Language Processing";
            case COMPUTER_VISION -> "Computer Vision";
            case TENSORFLOW -> "TensorFlow";
            case PYTORCH -> "PyTorch";
            case SCIKIT_LEARN -> "Scikit-learn";
            case NUMPY -> "NumPy";
            case PANDAS -> "Pandas";
            case MATPLOTLIB -> "Matplotlib";
            case JUPYTER -> "Jupyter";
            case GENERATIVE_AI -> "Generative AI";
            case LARGE_LANGUAGE_MODELS -> "Large Language Models";
            case PROMPT_ENGINEERING -> "Prompt Engineering";
            case RAG -> "RAG";
            case AI_AGENTS -> "AI Agents";
            case LANGCHAIN -> "LangChain";
            case LLAMA_INDEX -> "LlamaIndex";
            case PINECONE -> "Pinecone";
            case MILVUS -> "Milvus";
            case WEAVIATE -> "Weaviate";
            case CHROMA_DB -> "ChromaDB";

            case ANDROID -> "Android";
            case ANDROID_STUDIO -> "Android Studio";
            case FLUTTER -> "Flutter";
            case REACT_NATIVE -> "React Native";
            case SWIFT_IOS -> "Swift iOS";

            case MICROSERVICES -> "Microservices";
            case SYSTEM_DESIGN -> "System Design";
            case EVENT_DRIVEN_ARCHITECTURE -> "Event-Driven Architecture";
            case API_GATEWAY -> "API Gateway";
            case SERVICE_DISCOVERY -> "Service Discovery";

            case JWT -> "JWT";
            case OAUTH2 -> "OAuth 2.0";
            case OPENID_CONNECT -> "OpenID Connect";

            case MAVEN -> "Maven";
            case GRADLE -> "Gradle";
            case NPM -> "npm";
            case YARN -> "Yarn";

            case JIRA -> "Jira";
            case CONFLUENCE -> "Confluence";

            case LINUX -> "Linux";
            case BASH -> "Bash";
            case POWERSHELL -> "PowerShell";

            case POWER_BI -> "Power BI";
            case TABLEAU -> "Tableau";

            case PHP -> "PHP";
            case RUBY -> "Ruby";

            default -> skill.name();
        };
    }
}