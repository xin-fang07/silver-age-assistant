package com.example.elderai.controller;

import com.example.elderai.common.PageResult;
import com.example.elderai.common.Result;
import com.example.elderai.entity.MedicalRecord;
import com.example.elderai.security.SecurityUtils;
import com.example.elderai.service.MedicalRecordService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/medical-record")
public class MedicalRecordController {

    @Resource
    private MedicalRecordService medicalRecordService;

    @GetMapping("/list")
    public PageResult<MedicalRecord> list(
            @RequestParam(required = false) Long elderInfoId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return medicalRecordService.pageByFamily(SecurityUtils.currentUserId(), elderInfoId, pageNum, pageSize);
    }

    @GetMapping("/{id}")
    public Result<MedicalRecord> detail(@PathVariable Long id) {
        return Result.success(medicalRecordService.detail(id, SecurityUtils.currentUserId()));
    }

    @PostMapping
    public Result<Long> create(@RequestBody MedicalRecord entity) {
        return Result.success(medicalRecordService.create(entity, SecurityUtils.currentUserId()));
    }

    @PutMapping
    public Result<Void> update(@RequestBody MedicalRecord entity) {
        medicalRecordService.update(entity, SecurityUtils.currentUserId());
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        medicalRecordService.deleteOwn(id, SecurityUtils.currentUserId());
        return Result.success();
    }
}
