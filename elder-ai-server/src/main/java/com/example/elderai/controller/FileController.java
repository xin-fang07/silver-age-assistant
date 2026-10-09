package com.example.elderai.controller;

import com.example.elderai.common.BusinessException;
import com.example.elderai.common.Result;
import com.example.elderai.security.SecurityUtils;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 文件上传控制器
 * <p>
 * 安全措施：魔数校验、扩展名白名单、大小限制、JWT认证
 * </p>
 */
@RestController
@RequestMapping("/api/common")
public class FileController {

    @Value("${app.upload.path:uploads}")
    private String uploadPath;

    @Value("${app.upload.max-size:5242880}")
    private long maxSize;

    /** 允许的扩展名白名单 */
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(".jpg", ".jpeg", ".png", ".gif", ".webp", ".bmp");
    /** 允许的 MIME 类型白名单 */
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
        "image/jpeg", "image/png", "image/gif", "image/webp", "image/bmp"
    );

    // ============ 常见图片文件魔数 ============
    private static final byte[][] JPEG_MAGIC = {{(byte)0xFF, (byte)0xD8, (byte)0xFF}};
    private static final byte[]   PNG_MAGIC  = {(byte)0x89, 0x50, 0x4E, 0x47};
    private static final byte[]   GIF_MAGIC  = {0x47, 0x49, 0x46, 0x38};  // GIF89a or GIF87a
    private static final byte[]   WEBP_MAGIC = {0x52, 0x49, 0x46, 0x46};  // RIFF
    private static final byte[]   BMP_MAGIC  = {0x42, 0x4D};              // BM

    // ==================== JWT 解析 ====================
    private Long getCurrentUserId() {
        return SecurityUtils.currentUserId();
    }

    // ==================== 上传接口 ====================

    /**
     * 通用图片上传
     */
    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        getCurrentUserId();
        return doUpload(file);
    }

    /**
     * 头像上传（与通用上传逻辑相同，独立接口方便后续差异化处理）
     */
    @PostMapping("/upload-avatar")
    public Result<Map<String, String>> uploadAvatar(@RequestParam("file") MultipartFile file) {
        getCurrentUserId();
        return doUpload(file);
    }

    // ==================== 核心上传逻辑 ====================

    private Result<Map<String, String>> doUpload(MultipartFile file) {
        // 1. 非空检查
        if (file.isEmpty()) {
            throw new BusinessException(400, "请选择要上传的文件");
        }
        // 2. 文件大小检查
        if (file.getSize() > maxSize) {
            throw new BusinessException(400, "文件大小不能超过 " + (maxSize / 1024 / 1024) + "MB");
        }
        // 3. 获取原始文件名与扩展名
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        }
        // 4. 扩展名白名单校验
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new BusinessException(400, "不支持的文件类型，仅允许 jpg/jpeg/png/gif/webp/bmp");
        }
        // 5. MIME 类型白名单校验
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException(400, "不支持的文件格式");
        }
        // 6. 魔数校验（最可靠的文件类型检测）
        byte[] header = readHeader(file, 8);
        if (!isValidImageHeader(header, ext)) {
            throw new BusinessException(400, "文件内容与扩展名不匹配");
        }
        // 7. 生成安全文件名
        String dateDir = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String savedName = UUID.randomUUID().toString().replace("-", "") + ext;
        Path dirPath = Paths.get(uploadPath, dateDir);
        try {
            Files.createDirectories(dirPath);
            Path filePath = dirPath.resolve(savedName);
            file.transferTo(filePath.toFile());
            String url = "/uploads/" + dateDir + "/" + savedName;
            return Result.success("上传成功", Map.of("url", url, "name", originalName != null ? originalName : ""));
        } catch (IOException e) {
            throw new BusinessException(500, "文件保存失败");
        }
    }

    // ==================== 魔数检测 ====================

    /** 读取文件头部指定字节数 */
    private byte[] readHeader(MultipartFile file, int length) {
        try (InputStream is = file.getInputStream()) {
            byte[] header = new byte[Math.min(length, (int) file.getSize())];
            int read = is.read(header);
            if (read < header.length) {
                byte[] actual = new byte[read];
                System.arraycopy(header, 0, actual, 0, read);
                return actual;
            }
            return header;
        } catch (IOException e) {
            throw new BusinessException(400, "无法读取文件内容");
        }
    }

    /** 根据扩展名 + 文件头魔数判断是否为合法图片 */
    private boolean isValidImageHeader(byte[] header, String ext) {
        if (header == null || header.length < 2) return false;
        switch (ext) {
            case ".jpg":
            case ".jpeg":
                return matches(header, JPEG_MAGIC);
            case ".png":
                return matches(header, PNG_MAGIC);
            case ".gif":
                return matches(header, GIF_MAGIC);
            case ".webp":
                return matches(header, WEBP_MAGIC);
            case ".bmp":
                return matches(header, BMP_MAGIC);
            default:
                return false;
        }
    }

    private boolean matches(byte[] header, byte[] magic) {
        if (header.length < magic.length) return false;
        for (int i = 0; i < magic.length; i++) {
            if (header[i] != magic[i]) return false;
        }
        return true;
    }

    private boolean matches(byte[] header, byte[][] magics) {
        for (byte[] magic : magics) {
            if (matches(header, magic)) return true;
        }
        return false;
    }
}
