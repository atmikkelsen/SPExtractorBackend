package com.example.SPExtractorBackend.repository;

import com.example.SPExtractorBackend.entity.File;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FileRepository extends JpaRepository<File, Integer> {

}
