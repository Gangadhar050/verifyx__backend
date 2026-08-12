package com.verify_x.config;

import com.verify_x.entity.CareerTrack;
import com.verify_x.entity.CareerTrackTechnology;
import com.verify_x.entity.SkillDomain;
import com.verify_x.entity.Technology;
import com.verify_x.repository.CareerTrackRepository;
import com.verify_x.repository.CareerTrackTechnologyRepository;
import com.verify_x.repository.SkillDomainRepository;
import com.verify_x.repository.TechnologyRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SkillDataSeeder implements CommandLineRunner {

    private final SkillDomainRepository skillDomainRepository;
    private final CareerTrackRepository careerTrackRepository;
    private final TechnologyRepository technologyRepository;
    private final CareerTrackTechnologyRepository
            careerTrackTechnologyRepository;

    public SkillDataSeeder(
            SkillDomainRepository skillDomainRepository,
            CareerTrackRepository careerTrackRepository,
            TechnologyRepository technologyRepository,
            CareerTrackTechnologyRepository careerTrackTechnologyRepository) {

        this.skillDomainRepository = skillDomainRepository;
        this.careerTrackRepository = careerTrackRepository;
        this.technologyRepository = technologyRepository;
        this.careerTrackTechnologyRepository =
                careerTrackTechnologyRepository;
    }

    @Override
    public void run(String... args) {

        SkillDomain development = createDomain("Development Domain");

        CareerTrack javaFullStack =
                createCareerTrack("Java Full Stack", development);

        CareerTrack pythonFullStack =
                createCareerTrack("Python Full Stack", development);

        CareerTrack mernStack =
                createCareerTrack("MERN Stack", development);

        CareerTrack dotNetFullStack =
                createCareerTrack(".NET Full Stack", development);

        // Java Full Stack
        addTechnology(javaFullStack, "Java");
        addTechnology(javaFullStack, "Spring Boot");
        addTechnology(javaFullStack, "Spring Security");
        addTechnology(javaFullStack, "Hibernate");
        addTechnology(javaFullStack, "REST API");
        addTechnology(javaFullStack, "Microservices");
        addTechnology(javaFullStack, "React");
        addTechnology(javaFullStack, "MySQL");
        addTechnology(javaFullStack, "Kafka");
        addTechnology(javaFullStack, "Redis");
        addTechnology(javaFullStack, "Docker");
        addTechnology(javaFullStack, "Git");
        addTechnology(javaFullStack, "HTML");
        addTechnology(javaFullStack, "CSS");
        addTechnology(javaFullStack, "Java Script");


        // Python Full Stack
        addTechnology(pythonFullStack, "Python");
        addTechnology(pythonFullStack, "Django");
        addTechnology(pythonFullStack, "Flask");
        addTechnology(pythonFullStack, "FastAPI");
        addTechnology(pythonFullStack, "REST API");
        addTechnology(pythonFullStack, "Microservices");
        addTechnology(pythonFullStack, "React");
        addTechnology(pythonFullStack, "PostgreSQL");
        addTechnology(pythonFullStack, "Redis");
        addTechnology(pythonFullStack, "Docker");
        addTechnology(pythonFullStack, "Git");

        // MERN Stack
        addTechnology(mernStack, "JavaScript");
        addTechnology(mernStack, "TypeScript");
        addTechnology(mernStack, "MongoDB");
        addTechnology(mernStack, "Express.js");
        addTechnology(mernStack, "React");
        addTechnology(mernStack, "Node.js");
        addTechnology(mernStack, "REST API");
        addTechnology(mernStack, "Redis");
        addTechnology(mernStack, "Docker");
        addTechnology(mernStack, "Git");

        // .NET Full Stack
        addTechnology(dotNetFullStack, "C#");
        addTechnology(dotNetFullStack, ".NET");
        addTechnology(dotNetFullStack, "ASP.NET Core");
        addTechnology(dotNetFullStack, "Entity Framework");
        addTechnology(dotNetFullStack, "REST API");
        addTechnology(dotNetFullStack, "React");
        addTechnology(dotNetFullStack, "Angular");
        addTechnology(dotNetFullStack, "SQL Server");
        addTechnology(dotNetFullStack, "Redis");
        addTechnology(dotNetFullStack, "Docker");
        addTechnology(dotNetFullStack, "Git");
    }

    private SkillDomain createDomain(String name) {

        return skillDomainRepository
                .findByName(name)
                .orElseGet(() ->
                        skillDomainRepository.save(
                                new SkillDomain(name)
                        )
                );
    }

    private CareerTrack createCareerTrack(
            String name,
            SkillDomain domain) {

        return careerTrackRepository
                .findByNameAndDomainId(
                        name,
                        domain.getId()
                )
                .orElseGet(() ->
                        careerTrackRepository.save(
                                new CareerTrack(name, domain)
                        )
                );
    }

    private Technology createTechnology(String name) {

        return technologyRepository
                .findByName(name)
                .orElseGet(() ->
                        technologyRepository.save(
                                new Technology(name)
                        )
                );
    }

    private void addTechnology(
            CareerTrack careerTrack,
            String technologyName) {

        Technology technology =
                createTechnology(technologyName);

        boolean exists =
                careerTrackTechnologyRepository
                        .existsByCareerTrackIdAndTechnologyId(
                                careerTrack.getId(),
                                technology.getId()
                        );

        if (!exists) {

            careerTrackTechnologyRepository.save(
                    new CareerTrackTechnology(
                            careerTrack,
                            technology
                    )
            );
        }
    }
}