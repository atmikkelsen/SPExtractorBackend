package com.example.SPExtractorBackend.repository;

import com.example.SPExtractorBackend.entity.File;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class FileRepositoryTest {

    @Autowired
    private FileRepository fileRepository;

    @Test
    void shouldFindRecentFilesByDriveId() {
        // Arrange
        String driveId = "drive123";
        File file1 = new File("id1", "file1", 1000L, "http://example.com", LocalDateTime.now(), "user1", driveId, LocalDateTime.now());
        File file2 = new File("id2", "file2", 2000L, "http://example.com", LocalDateTime.now(), "user2", driveId, LocalDateTime.now());
        fileRepository.save(file1);
        fileRepository.save(file2);

        // Act
        List<File> recentFiles = fileRepository.findRecentFilesByDriveId(driveId, LocalDateTime.now().minusDays(1));

        // Assert
        assertThat(recentFiles).hasSize(2);
    }

    @Test
    void shouldReturnEmptyIfNoRecentFiles() {
        // Arrange
        String driveId = "drive456";

        // Act
        List<File> recentFiles = fileRepository.findRecentFilesByDriveId(driveId, LocalDateTime.now());

        // Assert
        assertThat(recentFiles).isEmpty();
    }
}
