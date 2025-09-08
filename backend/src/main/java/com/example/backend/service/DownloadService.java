package com.example.backend.service;

import com.example.backend.model.main2022;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface DownloadService {
    /**
     * 下载CSV文件
     */
    ResponseEntity<byte[]> downloadCSV(List<main2022> data);

    /**
     * 生成CSV字节数组
     */
    byte[] generateCSVBytes(List<main2022> data);

    /**
     * 创建下载响应
     */
    ResponseEntity<byte[]> createDownloadResponse(byte[] data, String fileName);

    /**
     * 存储临时文件
     */
    void storeTemporaryFile(String taskId, byte[] data);

    /**
     * 获取临时文件
     */
    byte[] getTemporaryFile(String taskId);

    /**
     * 清理临时文件
     */
    void cleanupTemporaryFile(String taskId);
}