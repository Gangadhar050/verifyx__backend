package com.verify_x.config;

import com.verify_x.entity.Technology;
import com.verify_x.repository.TechnologyRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TechnologyDataSeeder implements CommandLineRunner {

    private final TechnologyRepository technologyRepository;

    public TechnologyDataSeeder(TechnologyRepository technologyRepository) {
        this.technologyRepository = technologyRepository;
    }

    @Override
    public void run(String... args) {

        List<String> technologies = List.of(
                "C",
                "C++",
                "Python",
                "HTML",
                "CSS",
                "JavaScript",
                "React",
                "Angular",
                "TypeScript",
                "Node.js",
                "SQL",
                "Git",
                "Docker",
                "AWS",
                "Kotlin",
                "Dart",
                "Flutter",
                "Spring Boot",
                "Spring Security",
                "Spring MVC",
                "Hibernate",
                "JPA",
                "REST API",
                "Microservices",
                "Django",
                "Flask",
                "FastAPI",
                "MongoDB",
                "PostgreSQL",
                "MySQL",
                "Linux",
                "Kubernetes",
                "Jenkins",
                "TensorFlow",
                "Pandas",
                "NumPy",
                "Machine Learning"
        );

        for (String technologyName : technologies) {

            if (technologyRepository
                    .findByName(technologyName)
                    .isEmpty()) {

                technologyRepository.save(
                        new Technology(technologyName)
                );
            }
        }
    }
}