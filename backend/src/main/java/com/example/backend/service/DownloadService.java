package com.example.backend.service;
import com.example.backend.model.main2022;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface DownloadService {
    ResponseEntity<byte[]> downloadCSV(List<main2022> data);
}
