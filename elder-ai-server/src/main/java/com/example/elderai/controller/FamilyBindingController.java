package com.example.elderai.controller;

import com.example.elderai.common.BusinessException;
import com.example.elderai.common.Result;
import com.example.elderai.security.AuthenticatedUser;
import com.example.elderai.security.SecurityUtils;
import com.example.elderai.service.FamilyBindingService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/family-binding")
public class FamilyBindingController {

    @Resource
    private FamilyBindingService familyBindingService;

    @PostMapping("/bind")
    public Result<Void> bind(@RequestBody Map<String, String> body) {
        AuthenticatedUser user = SecurityUtils.currentUser();
        if (!"FAMILY".equals(user.role())) {
            throw new BusinessException(403, "仅家属账号可发起绑定");
        }
        Long elderInfoId = body.get("elderInfoId") != null ? Long.valueOf(body.get("elderInfoId")) : null;
        familyBindingService.bind(user.userId(), elderInfoId, body.get("relation"), body.get("applicationNote"));
        return Result.success("绑定成功");
    }

    @GetMapping("/elders")
    public Result<List<Map<String, Object>>> myElders() {
        return Result.success(familyBindingService.myElders(SecurityUtils.currentUserId()));
    }

    @GetMapping("/available-elders")
    public Result<List<Map<String, Object>>> availableElders() {
        AuthenticatedUser user = SecurityUtils.currentUser();
        if (!"FAMILY".equals(user.role())) {
            throw new BusinessException(403, "仅家属账号可查看可绑定老人");
        }
        return Result.success(familyBindingService.availableElders(user.userId()));
    }

    @GetMapping("/families")
    public Result<List<Map<String, Object>>> myFamilies() {
        return Result.success(familyBindingService.myFamilies(SecurityUtils.currentUserId()));
    }

    @GetMapping("/binding-requests/mine")
    public Result<List<Map<String, Object>>> myRequests() {
        AuthenticatedUser user = SecurityUtils.currentUser();
        if (!"FAMILY".equals(user.role())) throw new BusinessException(403, "仅家属账号可查看申请");
        return Result.success(familyBindingService.myRequests(user.userId()));
    }

    @PostMapping("/unbind")
    public Result<Void> unbind(@RequestBody Map<String, Long> body) {
        AuthenticatedUser user = SecurityUtils.currentUser();
        familyBindingService.unbind(user.userId(), user.role(),
                body.get("elderInfoId"), body.get("familyUserId"));
        return Result.success("已解除绑定");
    }

    @GetMapping("/elder/{elderInfoId}")
    public Result<Map<String, Object>> elderDetail(@PathVariable Long elderInfoId) {
        Long familyUserId = SecurityUtils.currentUserId();
        return Result.success(familyBindingService.detail(familyUserId, elderInfoId));
    }
}
