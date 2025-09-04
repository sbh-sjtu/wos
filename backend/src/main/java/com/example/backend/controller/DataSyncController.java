package com.example.backend.controller;

import com.example.backend.service.impl.DataSyncService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/sync")
@CrossOrigin
public class DataSyncController {

    @Autowired
    private DataSyncService dataSyncService;

    /**
     * 检查MySQL数据
     */
    @GetMapping("/check-mysql")
    public ResponseEntity<Map<String, Object>> checkMySQLData() {
        Map<String, Object> result = new HashMap<>();

        try {
            String checkResult = dataSyncService.checkMySQLData();
            result.put("status", "SUCCESS");
            result.put("message", checkResult);
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 同步数据到Elasticsearch - GET方法（用于浏览器测试）
     */
    @GetMapping("/to-elasticsearch-get")
    public ResponseEntity<Map<String, Object>> syncToElasticsearchGet() {
        return syncToElasticsearch();
    }

    /**
     * 同步数据到Elasticsearch
     */
    @PostMapping("/to-elasticsearch")
    public ResponseEntity<Map<String, Object>> syncToElasticsearch() {
        Map<String, Object> result = new HashMap<>();

        try {
            String syncResult = dataSyncService.syncDataToElasticsearch();
            result.put("status", "SUCCESS");
            result.put("message", syncResult);
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
            e.printStackTrace();
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 清空并重建Elasticsearch索引
     */
    @PostMapping("/clear-elasticsearch")
    public ResponseEntity<Map<String, Object>> clearElasticsearch() {
        Map<String, Object> result = new HashMap<>();

        try {
            String clearResult = dataSyncService.clearElasticsearchIndex();
            result.put("status", "SUCCESS");
            result.put("message", clearResult);
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
        }

        return ResponseEntity.ok(result);
    }

    /**
     * 完整的重新同步流程 - GET方法（用于浏览器测试）
     */
    @GetMapping("/full-resync-get")
    public ResponseEntity<Map<String, Object>> fullResyncGet() {
        return fullResync();
    }

    /**
     * 完整的重新同步流程
     */
    @PostMapping("/full-resync")
    public ResponseEntity<Map<String, Object>> fullResync() {
        Map<String, Object> result = new HashMap<>();

        try {
            // 1. 检查MySQL数据
            String mysqlCheck = dataSyncService.checkMySQLData();

            // 2. 清空ES索引
            String clearResult = dataSyncService.clearElasticsearchIndex();

            // 3. 重新同步数据
            String syncResult = dataSyncService.syncDataToElasticsearch();

            result.put("status", "SUCCESS");
            result.put("mysqlCheck", mysqlCheck);
            result.put("clearResult", clearResult);
            result.put("syncResult", syncResult);

        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
            e.printStackTrace();
        }

        return ResponseEntity.ok(result);
    }
}