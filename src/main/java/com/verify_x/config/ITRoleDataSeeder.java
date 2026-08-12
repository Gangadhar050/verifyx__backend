package com.verify_x.config;

import com.verify_x.entity.ITRole;
import com.verify_x.repository.ITRoleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ITRoleDataSeeder implements CommandLineRunner {

    private final ITRoleRepository roleRepository;

    public ITRoleDataSeeder(ITRoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(String... args) {

        List<String> roles = List.of(
                "Frontend Developer",
                "React Developer",
                "Angular Developer",
                "Vue.js Developer",
                "UI Developer",
                "Backend Developer",
                "Java Developer",
                "Spring Boot Developer",
                ".NET Developer",
                "C# Developer",
                "Python Developer",
                "Node.js Developer",
                "PHP Developer",
                "Ruby on Rails Developer",
                "Go Developer",
                "Full Stack Developer",
                "Java Full Stack Developer",
                "MERN Stack Developer",
                "MEAN Stack Developer",
                "Mobile App Developer",
                "Android Developer",
                "iOS Developer",
                "Flutter Developer",
                "React Native Developer",
                "DevOps Engineer",
                "Cloud Engineer",
                "AWS Developer",
                "Azure Developer",
                "Data Analyst",
                "Data Scientist",
                "Machine Learning Engineer",
                "AI Engineer",
                "Data Engineer",
                "Database Developer",
                "SQL Developer",
                "QA Engineer",
                "Automation Tester",
                "Manual Tester",
                "Test Engineer",
                "Cybersecurity Engineer",
                "Security Engineer",
                "Blockchain Developer",
                "Embedded Systems Developer",
                "Game Developer",
                "Technical Support Engineer",
                "System Administrator",
                "Software Engineer",
                "Software Developer"
        );

        for (String roleName : roles) {

            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(new ITRole(roleName));
            }
        }
    }
}