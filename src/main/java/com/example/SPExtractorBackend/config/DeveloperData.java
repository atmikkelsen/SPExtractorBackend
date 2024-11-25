package com.example.SPExtractorBackend.config;

import com.example.SPExtractorBackend.entity.Site;
import com.example.SPExtractorBackend.repository.UserRepository;
import com.example.SPExtractorBackend.repository.SiteRepository;
import com.example.SPExtractorBackend.repository.FileRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;

import java.util.List;
/*
@Configuration
public class DeveloperData  ApplicationRunner {

    UserRepository userRepository;
    SiteRepository siteRepository;
    FileRepository fileRepository;

    public DeveloperData(UserRepository userRepository, SiteRepository siteRepository, FileRepository fileRepository) {
        this.userRepository = userRepository;
        this.siteRepository = siteRepository;
        this.fileRepository = fileRepository;
    }


    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<Site> sites = HotelTestDataFactory.generateTestHotels(250);
        siteRepository.saveAll(sites);

        System.out.println("Developer data created " + sites.size() + " sites");
    }

}


 */