package com.example.SPExtractorBackend.api;

import com.example.SPExtractorBackend.dto.DriveDTO;
import com.example.SPExtractorBackend.service.SiteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/drives")
@CrossOrigin(origins = "http://127.0.0.1:5500")
public class DriveController {

    private final SiteService siteService;
    @Value("${bearer.token}")
    private String BEARER_TOKEN;

    @Autowired
    public DriveController(SiteService siteService) {
        this.siteService = siteService;
    }

    @GetMapping
    public ResponseEntity<List<DriveDTO>> getAllDrives(@RequestHeader("Authorization") String authorizationHeader, @RequestParam String siteId) {
        try {
            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.emptyList());
            }
            String token = authorizationHeader.substring(7);

            List<DriveDTO> sites = siteService.fetchAllDrives(token, siteId);
            System.out.printf("Sites fetched successfully from Microsoft Graph API%n");
            return ResponseEntity.ok(sites);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.emptyList());
        }
    }

    @GetMapping("/no-header")
    public ResponseEntity<List<DriveDTO>> getAllDrives() {
        String token = BEARER_TOKEN;
        String siteId = "hjerteforeningen.sharepoint.com,c68e317f-7947-4eb3-a895-abdbb2fab90c,ddb0d5a1-43e4-4e22-a682-fc418014c56f";


        List<DriveDTO> drives = siteService.fetchAllDrives(token, siteId);
        System.out.printf("Sites fetched successfully from Microsoft Graph API without header %n");
        //System.out.println(sites.get(10).getId());

        return ResponseEntity.ok(drives);


    }
}
