package com.example.SPExtractorBackend.response;
import com.example.SPExtractorBackend.dto.FileDTO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
public class GraphFilesResponse {
    private List<FileDTO> value;

    @Getter
    @Setter
    public static class FileDTO {
        private String name;
        private int size;
        private String libraryName;
        private String webUrl;
        private LocalDateTime lastModifiedDateTime;
    }


}
