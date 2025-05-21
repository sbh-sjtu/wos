package com.example.backend.controller;

import com.example.backend.model.main2022;
import com.example.backend.service.DownloadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/download")
@CrossOrigin
public class DownloadController {

    private final DownloadService downloadService;

    @Autowired
    public DownloadController(DownloadService downloadService) {
        this.downloadService = downloadService;
    }

    @PostMapping("/csv")
    public ResponseEntity<byte[]> downloadCSV(@RequestBody List<main2022> data){
        return downloadService.downloadCSV(data);
    }
}
