package com.verify_x.config;

import com.verify_x.entity.ITRole;
import com.verify_x.entity.ITRoleTechnology;
import com.verify_x.entity.Technology;
import com.verify_x.repository.ITRoleRepository;
import com.verify_x.repository.ITRoleTechnologyRepository;
import com.verify_x.repository.TechnologyRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoleTechnologyDataSeeder implements CommandLineRunner {

    private final ITRoleRepository roleRepository;
    private final TechnologyRepository technologyRepository;
    private final ITRoleTechnologyRepository roleTechnologyRepository;

    public RoleTechnologyDataSeeder(
            ITRoleRepository roleRepository,
            TechnologyRepository technologyRepository,
            ITRoleTechnologyRepository roleTechnologyRepository) {

        this.roleRepository = roleRepository;
        this.technologyRepository = technologyRepository;
        this.roleTechnologyRepository = roleTechnologyRepository;
    }

    @Override
    public void run(String... args) {

        addSkills("Java Developer",
                "Java",
                "Spring Boot",
                "Spring MVC",
                "Spring Security",
                "Hibernate",
                "JPA",
                "SQL",
                "REST API",
                "Microservices");

        addSkills("Java Full Stack Developer",
                "Java",
                "Spring Boot",
                "Spring MVC",
                "Spring Security",
                "Hibernate",
                "JPA",
                "SQL",
                "REST API",
                "React",
                "HTML",
                "CSS",
                "JavaScript");

        addSkills("React Developer",
                "HTML",
                "CSS",
                "JavaScript",
                "React",
                "REST API");

        addSkills("Angular Developer",
                "HTML",
                "CSS",
                "JavaScript",
                "Angular",
                "TypeScript",
                "REST API");

        addSkills("Python Developer",
                "Python",
                "Django",
                "Flask",
                "FastAPI",
                "SQL",
                "REST API");

        addSkills("Node.js Developer",
                "JavaScript",
                "Node.js",
                "Express.js",
                "REST API",
                "MongoDB",
                "SQL");

        addSkills("Full Stack Developer",
                "HTML",
                "CSS",
                "JavaScript",
                "React",
                "Node.js",
                "SQL",
                "REST API");

        addSkills("DevOps Engineer",
                "Linux",
                "Docker",
                "Kubernetes",
                "Jenkins",
                "Git",
                "AWS",
                "CI/CD");

        addSkills("Data Scientist",
                "Python",
                "SQL",
                "Pandas",
                "NumPy",
                "Machine Learning");

        addSkills("Machine Learning Engineer",
                "Python",
                "NumPy",
                "Pandas",
                "Machine Learning",
                "TensorFlow");

        addSkills("Android Developer",
                "Java",
                "Kotlin",
                "Android");

        addSkills("Flutter Developer",
                "Dart",
                "Flutter");

        addSkills("SQL Developer",
                "SQL",
                "MySQL",
                "PostgreSQL");

        addSkills("Software Developer",
                "Java",
                "Python",
                "SQL",
                "Git",
                "REST API");
    }

    private void addSkills(String roleName, String... technologyNames) {

        ITRole role = roleRepository
                .findByName(roleName)
                .orElse(null);

        if (role == null) {
            return;
        }

        for (String technologyName : technologyNames) {

            Technology technology = technologyRepository
                    .findByName(technologyName)
                    .orElseGet(() ->
                            technologyRepository.save(
                                    new Technology(technologyName)
                            )
                    );

            boolean exists = roleTechnologyRepository
                    .findByRoleId(role.getId())
                    .stream()
                    .anyMatch(mapping ->
                            mapping.getTechnology()
                                    .getId()
                                    .equals(technology.getId()));

            if (!exists) {
                roleTechnologyRepository.save(
                        new ITRoleTechnology(role, technology)
                );
            }
        }
    }
}