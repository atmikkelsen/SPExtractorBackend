package com.example.SPExtractorBackend.service;

import com.example.SPExtractorBackend.dto.SiteDTO;
import com.example.SPExtractorBackend.entity.Site;
import com.example.SPExtractorBackend.repository.SiteRepository;
import com.example.SPExtractorBackend.response.GraphSitesResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SiteService {
    private final RestTemplate restTemplate;

    @Value("${graph.api.base-url}")
    private String graphApiBaseUrl;

    @Autowired
    public SiteService(RestTemplateBuilder restTemplateBuilder) {
        this.restTemplate = restTemplateBuilder.build();
    }

    // Hardcoded Bearer Token (Replace this with your actual token)
    private static final String BEARER_TOKEN = "eyJ0eXAiOiJKV1QiLCJub25jZSI6ImQzWkxvNHFydkZSN0Y4ZVhZempIMHdoMm1GTG1nNXBYVVR5eDdCS1VoNDAiLCJhbGciOiJSUzI1NiIsIng1dCI6Inp4ZWcyV09OcFRrd041R21lWWN1VGR0QzZKMCIsImtpZCI6Inp4ZWcyV09OcFRrd041R21lWWN1VGR0QzZKMCJ9.eyJhdWQiOiIwMDAwMDAwMy0wMDAwLTAwMDAtYzAwMC0wMDAwMDAwMDAwMDAiLCJpc3MiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC9kN2ZlMTNmZS05MWY4LTQ2MjUtODI2ZC04YjExZDBkNTc4NTIvIiwiaWF0IjoxNzMyNTUyMjI0LCJuYmYiOjE3MzI1NTIyMjQsImV4cCI6MTczMjYzODkyNSwiYWNjdCI6MCwiYWNyIjoiMSIsImFpbyI6IkFWUUFxLzhZQUFBQXo2cGIzbzNDVlIxVlJOeDBVUWtoaFA0VVZhR3k0Ti9jVjd6enRaVEZmdjR3MWxlRzd3aVdueCsxcjNiL0hJOHF6NWwyazJ3T25aWXM2cmJmaTB5YjA2SzN5ZFJqdEpqM0ZDTzRQSXo4U1pNPSIsImFtciI6WyJwd2QiLCJtZmEiXSwiYXBwX2Rpc3BsYXluYW1lIjoiR3JhcGggRXhwbG9yZXIiLCJhcHBpZCI6ImRlOGJjOGI1LWQ5ZjktNDhiMS1hOGFkLWI3NDhkYTcyNTA2NCIsImFwcGlkYWNyIjoiMCIsImZhbWlseV9uYW1lIjoiVG9yIE1pa2tlbHNlbiIsImdpdmVuX25hbWUiOiJBc2dlciIsImlkdHlwIjoidXNlciIsImlwYWRkciI6IjE4NS4xNy4xOTMuMjEyIiwibmFtZSI6IkFzZ2VyIFRvciBNaWtrZWxzZW4iLCJvaWQiOiIwMzIzNTQyNS03YWQ4LTQxZDctYmYyOS05ZmI0NTA0MzY4ODAiLCJvbnByZW1fc2lkIjoiUy0xLTUtMjEtNTE1OTY3ODk5LTEzOTAwNjczNTctODM5NTIyMTE1LTE0NjYwIiwicGxhdGYiOiI1IiwicHVpZCI6IjEwMDMyMDAxMEVCNEQzMkUiLCJyaCI6IjEuQVI4QV9oUC0xX2lSSlVhQ2JZc1IwTlY0VWdNQUFBQUFBQUFBd0FBQUFBQUFBQUNGQUVBZkFBLiIsInNjcCI6IkRpcmVjdG9yeS5SZWFkLkFsbCBEaXJlY3RvcnkuUmVhZFdyaXRlLkFsbCBHcm91cC5SZWFkLkFsbCBHcm91cC5SZWFkV3JpdGUuQWxsIEdyb3VwTWVtYmVyLlJlYWQuQWxsIEdyb3VwTWVtYmVyLlJlYWRXcml0ZS5BbGwgb3BlbmlkIHByb2ZpbGUgU2l0ZXMuRnVsbENvbnRyb2wuQWxsIFVzZXIuUmVhZCBlbWFpbCIsInN1YiI6ImdmaGJwODA2VU9Cb3N0aEZKTUJodmdRWnZMU2ZpY1pZdUdsem1sQUZQRVUiLCJ0ZW5hbnRfcmVnaW9uX3Njb3BlIjoiRVUiLCJ0aWQiOiJkN2ZlMTNmZS05MWY4LTQ2MjUtODI2ZC04YjExZDBkNTc4NTIiLCJ1bmlxdWVfbmFtZSI6ImF0bWlra2Vsc2VuQGhqZXJ0ZWZvcmVuaW5nZW4uZGsiLCJ1cG4iOiJhdG1pa2tlbHNlbkBoamVydGVmb3JlbmluZ2VuLmRrIiwidXRpIjoiSFZGQ0hxTDNHRUdELTV0M3V4SXJBQSIsInZlciI6IjEuMCIsIndpZHMiOlsiZTM5NzNiZGYtNDk4Ny00OWFlLTgzN2EtYmE4ZTIzMWM3Mjg2IiwiYjc5ZmJmNGQtM2VmOS00Njg5LTgxNDMtNzZiMTk0ZTg1NTA5Il0sInhtc19jYyI6WyJDUDEiXSwieG1zX2lkcmVsIjoiMSAyMCIsInhtc19zc20iOiIxIiwieG1zX3N0Ijp7InN1YiI6IjJ3bzRrNzRDbnZRQkthbXNsRF9pMzVwSUlzcjJpUXU3aXJxQjNuWWlncW8ifSwieG1zX3RjZHQiOjEzOTYzNTQ1MzQsInhtc190ZGJyIjoiRVUifQ.YEAX7H78Y7PgRV0esRU3_z86UG4RLzs6r_gR9ttW9chawpVwJl0sfO52wbP7tbuS7-hjg54_u8nEvxPmzTRZhpV9C3A8j4he6KJjwp-5J1x9z7wlGQ7NPOw7bRTeeZJaaYwNd9cei-8MZUyjrjCDK5ygFXCtqVhHquVGHkL1khOvYzIHkB8KW34R_gooFoOP8oDwuzEwHImHxO87i0o5xDZBjfEDIkw4o1BQ4phRr1MoVfZKdPP3SdI6kD_xSDtVahHcfYjYN1g4mDGdLatQ2Q-9C66w1m_exw8JZWzL5mZSZiH2dJRiE4tnF6ighAZcaH_n-4Ye7dRqNyCHgWIcig";

    public List<SiteDTO> fetchAllSites() {
        String url = graphApiBaseUrl + "/sites?search=*";

        // Set the Authorization header with the hardcoded token
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(BEARER_TOKEN); // Add Bearer token
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<GraphSitesResponse> response = restTemplate.exchange(
                url, HttpMethod.GET, requestEntity, GraphSitesResponse.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return response.getBody().getValue().stream()
                    .map(site -> new SiteDTO(site.getId(), site.getName(), site.getWebUrl(), site.getDisplayName()))
                    .collect(Collectors.toList());
        } else {
            throw new RuntimeException("Failed to fetch sites from Microsoft Graph API");
        }
    }
}
