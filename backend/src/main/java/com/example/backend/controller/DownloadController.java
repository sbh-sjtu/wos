package com.example.backend.controller;

import com.example.backend.config.SearchFilter;
import com.example.backend.service.DownloadService;
import com.example.backend.service.Main2022Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.backend.model.main2022;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/download")
@CrossOrigin
public class DownloadController {

    private final DownloadService downloadService;
    private final Main2022Service main2022Service;

    // 最大下载数量限制（避免tempdb问题）
    private static final int MAX_DOWNLOAD_LIMIT = 50000;

    // 使用线程安全的Map存储下载进度
    private final ConcurrentHashMap<String, ConcurrentHashMap<String, Object>> downloadProgress = new ConcurrentHashMap<>();

    @Autowired
    public DownloadController(DownloadService downloadService, Main2022Service main2022Service) {
        this.downloadService = downloadService;
        this.main2022Service = main2022Service;
    }

    /**
     * 下载传入的数据（当前页面数据）
     */
    @PostMapping("/csv")
    public ResponseEntity<byte[]> downloadCSV(@RequestBody List<main2022> data){
        return downloadService.downloadCSV(data);
    }

    /**
     * 获取下载进度的SSE连接
     */
    @GetMapping("/progress/{taskId}")
    public SseEmitter getDownloadProgress(@PathVariable String taskId) {
        SseEmitter emitter = new SseEmitter(300000L); // 5分钟超时

        try {
            // 定期发送进度更新
            CompletableFuture.runAsync(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        ConcurrentHashMap<String, Object> progress = downloadProgress.get(taskId);
                        if (progress != null) {
                            emitter.send(SseEmitter.event()
                                    .name("progress")
                                    .data(progress));

                            // 如果完成了，结束SSE连接
                            Boolean completed = (Boolean) progress.get("completed");
                            if (completed != null && completed) {
                                emitter.complete();
                                // 延迟清理进度信息
                                CompletableFuture.runAsync(() -> {
                                    try {
                                        Thread.sleep(5000); // 5秒后清理
                                        downloadProgress.remove(taskId);
                                    } catch (InterruptedException e) {
                                        Thread.currentThread().interrupt();
                                    }
                                });
                                break;
                            }
                        }
                        Thread.sleep(1000); // 每秒更新一次
                    }
                } catch (Exception e) {
                    emitter.completeWithError(e);
                }
            });

        } catch (Exception e) {
            emitter.completeWithError(e);
        }

        return emitter;
    }

    /**
     * 启动下载所有符合条件的数据（异步，有数量限制）
     */
    @PostMapping("/csv/all/start")
    public ResponseEntity<Map<String, Object>> startDownloadAll(@RequestBody List<SearchFilter> searchFilter) {
        try {
            // 验证输入参数
            if (searchFilter == null || searchFilter.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "搜索条件不能为空"));
            }

            String taskId = "download_" + System.currentTimeMillis();
            System.out.println("启动下载任务: " + taskId);

            // 创建进度对象
            ConcurrentHashMap<String, Object> progress = new ConcurrentHashMap<>();
            progress.put("taskId", taskId);
            progress.put("status", "started");
            progress.put("processedCount", 0);
            progress.put("totalCount", 0);
            progress.put("completed", false);
            progress.put("error", "");
            progress.put("warning", "");

            // 安全地添加到进度Map中
            downloadProgress.put(taskId, progress);
            System.out.println("进度对象已创建");

            // 异步执行下载
            CompletableFuture.runAsync(() -> {
                try {
                    System.out.println("开始异步下载任务: " + taskId);

                    // 更新状态：正在查询数据
                    updateProgress(taskId, "status", "querying");

                    // 首先尝试获取数据计数（有超时保护）
                    int estimatedCount = 0;
                    try {
                        System.out.println("尝试获取数据计数...");
                        estimatedCount = main2022Service.countAdvancedSearch(searchFilter);
                        System.out.println("预估数据量: " + estimatedCount);

                        // 检查是否超过限制
                        if (estimatedCount > MAX_DOWNLOAD_LIMIT) {
                            updateProgress(taskId, "warning",
                                    String.format("数据量过大（约%d条），将限制下载前%d条记录。建议使用更具体的搜索条件。",
                                            estimatedCount, MAX_DOWNLOAD_LIMIT));
                        }

                        updateProgress(taskId, "totalCount", Math.min(estimatedCount, MAX_DOWNLOAD_LIMIT));

                    } catch (Exception countError) {
                        System.err.println("获取计数失败，直接查询数据: " + countError.getMessage());
                        updateProgress(taskId, "warning", "无法预估数据量，直接开始下载");
                        updateProgress(taskId, "totalCount", MAX_DOWNLOAD_LIMIT);
                    }

                    updateProgress(taskId, "status", "downloading");

                    // 获取数据（使用限制版本的查询）
                    List<main2022> allData = main2022Service.advancedSearchAllWithProgress(
                            searchFilter,
                            (processedCount, totalCount) -> {
                                updateProgress(taskId, "processedCount", processedCount != null ? processedCount : 0);
                                updateProgress(taskId, "status", "processing");
                            }
                    );

                    if (allData == null || allData.isEmpty()) {
                        updateProgress(taskId, "status", "no_data");
                        updateProgress(taskId, "completed", true);
                        return;
                    }

                    // 如果数据量超过限制，截取前N条
                    if (allData.size() > MAX_DOWNLOAD_LIMIT) {
                        allData = allData.subList(0, MAX_DOWNLOAD_LIMIT);
                        updateProgress(taskId, "warning",
                                String.format("数据量超过限制，已截取前%d条记录", MAX_DOWNLOAD_LIMIT));
                    }

                    System.out.println("实际获取数据: " + allData.size() + " 条");

                    // 更新状态：正在生成CSV
                    updateProgress(taskId, "status", "generating_csv");
                    updateProgress(taskId, "totalCount", allData.size());
                    updateProgress(taskId, "processedCount", allData.size());

                    // 生成CSV文件
                    byte[] csvData = downloadService.generateCSVBytes(allData);

                    if (csvData == null || csvData.length == 0) {
                        updateProgress(taskId, "status", "error");
                        updateProgress(taskId, "error", "生成CSV文件失败");
                        updateProgress(taskId, "completed", true);
                        return;
                    }

                    System.out.println("CSV文件生成完成，大小: " + csvData.length + " 字节");

                    // 存储文件数据
                    downloadService.storeTemporaryFile(taskId, csvData);

                    // 更新状态为完成
                    updateProgress(taskId, "status", "completed");
                    updateProgress(taskId, "completed", true);
                    updateProgress(taskId, "downloadUrl", "/download/csv/file/" + taskId);
                    updateProgress(taskId, "fileName", "wos_data_" + taskId + ".csv");

                    System.out.println("下载任务完成: " + taskId);

                } catch (Exception e) {
                    System.err.println("下载任务错误: " + e.getMessage());
                    e.printStackTrace();
                    updateProgress(taskId, "status", "error");
                    updateProgress(taskId, "error",
                            e.getMessage() != null ? e.getMessage() : "下载过程中发生未知错误");
                    updateProgress(taskId, "completed", true);
                }
            });

            return ResponseEntity.ok(Map.of(
                    "taskId", taskId,
                    "maxLimit", MAX_DOWNLOAD_LIMIT,
                    "message", "下载任务已启动"
            ));

        } catch (Exception e) {
            System.err.println("启动下载任务失败: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "error", "启动下载失败: " + e.getMessage(),
                    "maxLimit", MAX_DOWNLOAD_LIMIT
            ));
        }
    }

    /**
     * 安全地更新进度信息
     */
    private void updateProgress(String taskId, String key, Object value) {
        try {
            ConcurrentHashMap<String, Object> progress = downloadProgress.get(taskId);
            if (progress != null) {
                progress.put(key, value);
            }
        } catch (Exception e) {
            System.err.println("更新进度失败: " + e.getMessage());
        }
    }

    /**
     * 下载生成的文件
     */
    @GetMapping("/csv/file/{taskId}")
    public ResponseEntity<byte[]> downloadGeneratedFile(@PathVariable String taskId) {
        try {
            byte[] fileData = downloadService.getTemporaryFile(taskId);
            if (fileData != null) {
                return downloadService.createDownloadResponse(fileData, "wos_data_" + taskId + ".csv");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取下载进度（RESTful API方式，作为SSE的备选）
     */
    @GetMapping("/progress/{taskId}/status")
    public ResponseEntity<Map<String, Object>> getDownloadStatus(@PathVariable String taskId) {
        try {
            ConcurrentHashMap<String, Object> progress = downloadProgress.get(taskId);
            if (progress != null) {
                return ResponseEntity.ok(new HashMap<>(progress));
            } else {
                return ResponseEntity.ok(Map.of(
                        "error", "任务不存在或已完成",
                        "status", "not_found"
                ));
            }
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "error", e.getMessage(),
                    "status", "error"
            ));
        }
    }

    /**
     * 取消下载任务
     */
    @PostMapping("/cancel/{taskId}")
    public ResponseEntity<Void> cancelDownload(@PathVariable String taskId) {
        try {
            updateProgress(taskId, "status", "cancelled");
            updateProgress(taskId, "completed", true);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}