package com.example.SPExtractorBackend.response;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Setter
@Getter
public class GraphFilesResponse {
    private List<Item> value; // Ensure this matches the JSON response
    private String nextLink;

    @Setter
    @Getter
    public static class Item {
        private String id;
        private String name;
        private String webUrl;
        private long size;
        private File file;
        private Folder folder;
        private LocalDateTime lastModifiedDateTime;
        private User lastModifiedBy;
        private User createdBy;


        public boolean isFolder() {
            return folder != null;
        }

        public boolean isFile() {
            return file != null;
        }
        @Setter
        @Getter
        public static class User {
            private UserDetail user;
        }

        @Setter
        @Getter
        public static class UserDetail {
            private String email;
            private String id;
            private String displayName;
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