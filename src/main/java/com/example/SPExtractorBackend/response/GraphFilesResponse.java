package com.example.SPExtractorBackend.response;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
public class GraphFilesResponse {
    private List<Item> value; // Ensure this matches the JSON response

    @Setter
    @Getter
    public static class Item {
        private String id;
        private String name;
        private String webUrl;
        private int size;
        private File file;
        private Folder folder;
        private LocalDateTime lastModifiedDateTime;


        public boolean isFolder() {
            return folder != null;
        }

        public boolean isFile() {
            return file != null;
        }
    }

    @Setter
    @Getter
    public static class File {
        private String mimeType;
    }

    @Setter
    @Getter
    public static class Folder {
        private int childCount;
    }
}