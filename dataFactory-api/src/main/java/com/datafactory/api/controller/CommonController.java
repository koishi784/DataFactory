package com.datafactory.api.controller;

import com.datafactory.common.model.vo.common.FileUploadVo;
import com.datafactory.common.response.Result;
import com.datafactory.core.domain.entity.SysFile;
import com.datafactory.core.domain.mapper.SysFileMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;

/**
 * 通用接口控制器
 *
 * 提供文件上传、文件下载等通用 REST 接口。
 */
@Slf4j
@Tag(name = "通用接口", description = "文件上传、文件下载等通用功能")
@RestController
@RequestMapping("/api/v1/common")
@RequiredArgsConstructor
public class CommonController {

    private final SysFileMapper sysFileMapper;

    /** 文件上传存储根路径 */
    @Value("${script.file.storage-path:./upload/script}")
    private String fileStoragePath;

    @Operation(summary = "文件上传", description = "上传文件（multipart/form-data），返回文件ID、文件名、访问URL和文件大小。支持最大 100MB 的文件")
    @PostMapping("/upload")
    public Result<FileUploadVo> uploadFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(required = false) String bizType) {

        if (file.isEmpty()) {
            return Result.error(100400, "上传文件不能为空");
        }

        try {
            // 1. 创建文件记录，获取自增ID
            SysFile sysFile = new SysFile();
            sysFile.setOriginalName(file.getOriginalFilename());
            sysFile.setFileSize(file.getSize());
            sysFile.setBizType(bizType);
            sysFile.setStoredPath("");    // 临时占位，后续更新为文件ID
            sysFile.setCreateTime(LocalDateTime.now());
            sysFileMapper.insert(sysFile);

            // 2. 保存文件内容到磁盘
            Long fileId = sysFile.getId();
            Path targetPath = Paths.get(fileStoragePath, String.valueOf(fileId));
            Files.createDirectories(targetPath.getParent());
            Files.write(targetPath, file.getBytes());

            // 3. 更新存储路径为文件ID（作为磁盘文件名）
            sysFile.setStoredPath(String.valueOf(fileId));
            sysFileMapper.updateById(sysFile);

            // 4. 构建返回结果
            FileUploadVo vo = new FileUploadVo();
            vo.setFileId(fileId);
            vo.setFileName(file.getOriginalFilename());
            vo.setFileUrl("/api/v1/common/download/" + fileId);
            vo.setFileSize(file.getSize());

            log.info("文件上传成功：fileId={}, fileName={}, size={}", fileId, file.getOriginalFilename(), file.getSize());
            return Result.success(vo);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error(100500, "文件上传失败：" + e.getMessage());
        }
    }

    @Operation(summary = "文件下载", description = "根据文件ID下载已上传的文件")
    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        // 1. 查询文件记录
        SysFile sysFile = sysFileMapper.selectById(fileId);
        if (sysFile == null) {
            return ResponseEntity.notFound().build();
        }

        // 2. 读取文件
        Path filePath = Paths.get(fileStoragePath, sysFile.getStoredPath());
        java.io.File file = filePath.toFile();
        if (!file.exists()) {
            log.warn("文件不存在：fileId={}, path={}", fileId, filePath);
            return ResponseEntity.notFound().build();
        }

        // 3. 构建响应
        Resource resource = new FileSystemResource(file);
        String encodedFileName = URLEncoder.encode(sysFile.getOriginalName(), StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedFileName)
                .body(resource);
    }
}
