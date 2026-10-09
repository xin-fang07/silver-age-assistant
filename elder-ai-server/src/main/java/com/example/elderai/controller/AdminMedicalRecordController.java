package com.example.elderai.controller;

import com.example.elderai.common.PageResult;
import com.example.elderai.common.Result;
import com.example.elderai.entity.MedicalRecord;
import com.example.elderai.service.MedicalRecordService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/medical-record")
public class AdminMedicalRecordController {

    @Resource
    private MedicalRecordService medicalRecordService;

    @GetMapping("/list")
    public PageResult<MedicalRecord> list(
            @RequestParam(required = false) Long elderInfoId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return medicalRecordService.pageAll(elderInfoId, keyword, pageNum, pageSize);
    }

    @GetMapping("/{id}")
    public Result<MedicalRecord> detail(@PathVariable Long id) {
        return Result.success(medicalRecordService.adminDetail(id));
    }

    @PutMapping
    public Result<Void> update(@RequestBody MedicalRecord entity) {
        medicalRecordService.adminUpdate(entity);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        medicalRecordService.adminDelete(id);
        return Result.success();
    }
}
