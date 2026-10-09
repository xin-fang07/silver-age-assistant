package com.example.elderai.controller;

import com.example.elderai.common.PageResult;
import com.example.elderai.common.Result;
import com.example.elderai.dto.NewsDTO;
import com.example.elderai.dto.EmergencyTransitionDTO;
import com.example.elderai.dto.PageQueryDTO;
import com.example.elderai.entity.EmergencyHelp;
import com.example.elderai.entity.News;
import com.example.elderai.entity.NewsRevision;
import com.example.elderai.entity.SystemLog;
import com.example.elderai.entity.User;
import com.example.elderai.entity.ContactMessage;
import com.example.elderai.entity.AccountDeletionRequest;
import com.example.elderai.mapper.ContactMessageMapper;
import com.example.elderai.mapper.AccountDeletionRequestMapper;
import com.example.elderai.mapper.UserMapper;
import com.example.elderai.mapper.ServiceTicketMapper;
import com.example.elderai.mapper.FamilyBindingMapper;
import com.example.elderai.mapper.HealthRecordMapper;
import com.example.elderai.mapper.ReminderMapper;
import com.example.elderai.mapper.EmergencyHelpMapper;
import com.example.elderai.mapper.SystemLogMapper;
import com.example.elderai.mapper.NotificationMapper;
import com.example.elderai.mapper.ChatRecordMapper;
import com.example.elderai.mapper.NewsRevisionMapper;
import com.example.elderai.mapper.ReminderExecutionMapper;
import com.example.elderai.mapper.HealthWarningMapper;
import com.example.elderai.entity.Notification;
import com.example.elderai.entity.ServiceTicket;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.elderai.service.*;
import com.example.elderai.security.SecurityUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 管理员控制器
 * <p>
 * 所有接口需要 ADMIN 角色，非管理员会收到 403 错误。
 * </p>
 *
 * @author elder-ai-team
 */
@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @Resource
    private AdminService adminService;

    @Resource
    private NewsService newsService;

    @Resource
    private EmergencyHelpService emergencyHelpService;

    @Resource
    private SystemLogService systemLogService;
    @Resource private ContactMessageMapper contactMessageMapper;
    @Resource private AccountDeletionRequestMapper deletionRequestMapper;
    @Resource private UserMapper userMapper;
    @Resource private ServiceTicketMapper ticketMapper;
    @Resource private FamilyBindingMapper familyBindingMapper;
    @Resource private HealthRecordMapper healthRecordMapper;
    @Resource private ReminderMapper reminderMapper;
    @Resource private EmergencyHelpMapper emergencyHelpMapper;
    @Resource private SystemLogMapper systemLogMapper;
    @Resource private NotificationMapper notificationMapper;
    @Resource private ChatRecordMapper chatRecordMapper;
    @Resource private ReminderExecutionMapper reminderExecutionMapper;
    @Resource private HealthWarningMapper healthWarningMapper;
    @Resource private NewsRevisionMapper newsRevisionMapper;

    @GetMapping("/analytics")
    public Result<Map<String,Object>> analytics(){
      Map<String,Object> r=new java.util.LinkedHashMap<>();
      r.put("chat",chatRecordMapper.selectMaps(new QueryWrapper<com.example.elderai.entity.ChatRecord>().select("DATE(create_time) day","COUNT(*) total","COUNT(DISTINCT user_id) users","SUM(is_fallback) fallback").ge("create_time",java.time.LocalDate.now().minusDays(29).atStartOfDay()).groupBy("DATE(create_time)").orderByAsc("day")));
      r.put("reminder",reminderExecutionMapper.selectMaps(new QueryWrapper<com.example.elderai.entity.ReminderExecution>().select("action","COUNT(*) count").ge("create_time",java.time.LocalDate.now().minusDays(29).atStartOfDay()).groupBy("action")));
      r.put("warning",healthWarningMapper.selectMaps(new QueryWrapper<com.example.elderai.entity.HealthWarning>().select("warning_type","COUNT(*) total","SUM(CASE WHEN status=2 THEN 1 ELSE 0 END) handled").groupBy("warning_type")));
      r.put("emergency",emergencyHelpMapper.selectMaps(new QueryWrapper<EmergencyHelp>().select("COUNT(*) total","AVG(TIMESTAMPDIFF(SECOND,create_time,acknowledged_at)) avg_ack_seconds","AVG(TIMESTAMPDIFF(SECOND,create_time,processing_at)) avg_process_seconds","AVG(TIMESTAMPDIFF(SECOND,create_time,completed_at)) avg_complete_seconds")));
      r.put("binding",familyBindingMapper.selectMaps(new QueryWrapper<com.example.elderai.entity.FamilyBinding>().select("status","COUNT(*) count").groupBy("status")));
      return Result.success(r);
    }

    @GetMapping("/tickets")
    public PageResult<ServiceTicket> tickets(@RequestParam(defaultValue="1") Integer pageNum,
      @RequestParam(defaultValue="20") Integer pageSize, @RequestParam(required=false) String type,
      @RequestParam(required=false) String status, @RequestParam(required=false) String keyword) {
        Page<ServiceTicket> page = new Page<>(pageNum, Math.min(pageSize,100));
        LambdaQueryWrapper<ServiceTicket> w = new LambdaQueryWrapper<ServiceTicket>()
          .eq(type!=null&&!type.isBlank(), ServiceTicket::getType,type)
          .eq(status!=null&&!status.isBlank(), ServiceTicket::getStatus,status)
          .and(keyword!=null&&!keyword.isBlank(), q->q.like(ServiceTicket::getTicketNo,keyword).or().like(ServiceTicket::getSubject,keyword).or().like(ServiceTicket::getContent,keyword))
          .orderByDesc(ServiceTicket::getCreateTime);
        Page<ServiceTicket> result=ticketMapper.selectPage(page,w);
        return PageResult.pageSuccess(result.getRecords(),result.getTotal(),result.getCurrent(),result.getSize());
    }

    @PutMapping("/tickets/{id}/status")
    public Result<Void> transitionTicket(@PathVariable Long id,@RequestBody Map<String,String> body){
      ServiceTicket t=ticketMapper.selectById(id); if(t==null) throw new com.example.elderai.common.BusinessException(404,"工单不存在");
      String status=body.get("status"); if(!java.util.Set.of("PENDING","PROCESSING","RESOLVED","REJECTED","ARCHIVED").contains(status)) throw new com.example.elderai.common.BusinessException(400,"无效工单状态");
      t.setStatus(status);t.setHandlerId(getCurrentUserId());t.setProcessRemark(body.get("remark"));t.setUpdateTime(java.time.LocalDateTime.now());
      if(java.util.Set.of("RESOLVED","REJECTED").contains(status))t.setResolvedAt(java.time.LocalDateTime.now()); if("ARCHIVED".equals(status))t.setArchivedAt(java.time.LocalDateTime.now());
      ticketMapper.updateById(t);
      if("DELETION".equals(t.getType())&&"RESOLVED".equals(status)&&t.getUserId()!=null){User u=userMapper.selectById(t.getUserId());if(u!=null&&!"ADMIN".equals(u.getRole())){u.setStatus(0);userMapper.updateById(u);}}
      return Result.success("工单状态已更新",null);
    }

    // ==================== 权限验证 ====================

    /** 从 Token 获取当前用户 ID */
    private Long getCurrentUserId() {
        return SecurityUtils.currentUserId();
    }

    /** 验证管理员权限，非 ADMIN 抛出 BusinessException(403) */
    private void checkAdminRole() {
        SecurityUtils.currentUser();
    }

    // ==================== 控制台统计 ====================

    @GetMapping("/dashboard")
    public Result<Map<String, Object>> getDashboardStats() {
        checkAdminRole();
        Map<String, Object> stats = adminService.getDashboardStats();
        return Result.success(stats);
    }

    // ==================== 用户管理 ====================

    @GetMapping("/users")
    public PageResult<User> listUsers(@RequestParam(defaultValue = "1") Integer pageNum,
                                       @RequestParam(defaultValue = "10") Integer pageSize,
                                       @RequestParam(required = false) String keyword) {
        checkAdminRole();
        PageQueryDTO dto = new PageQueryDTO();
        dto.setPageNum(pageNum);
        dto.setPageSize(pageSize);
        dto.setKeyword(keyword);
        return adminService.listUsers(dto);
    }

    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id,
                                          @RequestParam String status,
                                          @RequestParam(required=false) String reason) {
        checkAdminRole();
        int value = java.util.Set.of("1","ACTIVE","ENABLED").contains(status.toUpperCase()) ? 1 : 0;
        if(value==0 && (reason==null || reason.isBlank())) throw new com.example.elderai.common.BusinessException(400,"禁用用户必须填写原因");
        adminService.updateUserStatus(id, value);
        User target=userMapper.selectById(id); target.setDisabledReason(value==0?reason.trim():null); userMapper.updateById(target);
        Notification n=new Notification();n.setUserId(id);n.setType("ACCOUNT");n.setTitle(value==0?"账号已被禁用":"账号已恢复启用");n.setContent(value==0?"禁用原因："+reason:"管理员已恢复您的账号使用权限");n.setIsRead(0);n.setCreateTime(java.time.LocalDateTime.now());notificationMapper.insert(n);
        return Result.success();
    }

    @DeleteMapping("/users/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        checkAdminRole();
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new com.example.elderai.common.BusinessException(404, "用户不存在");
        }
        if ("ADMIN".equals(user.getRole())) {
            throw new com.example.elderai.common.BusinessException(400, "不能删除管理员账户");
        }
        userMapper.deleteById(id);
        return Result.success("用户已删除");
    }

    @GetMapping("/users/{id}/detail")
    public Result<Map<String,Object>> userDetail(@PathVariable Long id){
      User u=userMapper.selectById(id);if(u==null)throw new com.example.elderai.common.BusinessException(404,"用户不存在");u.setPassword(null);
      Map<String,Object> d=new java.util.LinkedHashMap<>();d.put("user",u);
      d.put("bindings",familyBindingMapper.selectMaps(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.example.elderai.entity.FamilyBinding>().and(w->w.eq("family_user_id",id).or().eq("elder_info_id",id)).orderByDesc("update_time")));
      d.put("healthCount",healthRecordMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.example.elderai.entity.HealthRecord>().eq("user_id",id)));
      d.put("reminderCount",reminderMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.example.elderai.entity.Reminder>().eq("user_id",id)));
      d.put("emergencyCount",emergencyHelpMapper.selectCount(new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<EmergencyHelp>().eq("user_id",id)));
      d.put("deletionTicket",ticketMapper.selectOne(new LambdaQueryWrapper<ServiceTicket>().eq(ServiceTicket::getUserId,id).eq(ServiceTicket::getType,"DELETION").orderByDesc(ServiceTicket::getCreateTime).last("LIMIT 1")));
      d.put("logs",systemLogMapper.selectList(new LambdaQueryWrapper<SystemLog>().eq(SystemLog::getUserId,id).orderByDesc(SystemLog::getCreateTime).last("LIMIT 20")));
      return Result.success(d);
    }

    // ==================== 求助管理 ====================

    @GetMapping("/emergency")
    public PageResult<EmergencyHelp> listEmergency(@RequestParam(defaultValue = "1") Integer pageNum,
                                                     @RequestParam(defaultValue = "10") Integer pageSize,
                                                     @RequestParam(required = false) String keyword,
                                                     @RequestParam(required = false) Integer status) {
        checkAdminRole();
        PageQueryDTO dto = new PageQueryDTO();
        dto.setPageNum(pageNum);
        dto.setPageSize(pageSize);
        dto.setKeyword(keyword);
        return emergencyHelpService.listAll(dto, status);
    }

    @PutMapping("/emergency/{id}/status")
    public Result<Void> transitionEmergency(@PathVariable Long id,
                                            @Valid @RequestBody EmergencyTransitionDTO dto) {
        emergencyHelpService.transition(id, getCurrentUserId(), dto.getStatus(), dto.getRemark());
        return Result.success("求助状态已更新", null);
    }

    // ==================== 资讯管理 ====================

    @GetMapping("/news")
    public PageResult<News> listAdminNews(@RequestParam(defaultValue = "1") Integer pageNum,
                                          @RequestParam(defaultValue = "10") Integer pageSize,
                                          @RequestParam(required = false) String keyword) {
        checkAdminRole();
        PageQueryDTO dto = new PageQueryDTO();
        dto.setPageNum(pageNum);
        dto.setPageSize(pageSize);
        dto.setKeyword(keyword);
        return newsService.listAll(dto);
    }

    @PostMapping("/news")
    public Result<News> createNews(@Valid @RequestBody NewsDTO dto) {
        checkAdminRole();
        Long publisherId = getCurrentUserId();
        News news = newsService.create(publisherId, dto);
        return Result.success("资讯发布成功", news);
    }

    @PutMapping("/news/{id}")
    public Result<News> updateNews(@PathVariable Long id,
                                    @Valid @RequestBody NewsDTO dto) {
        checkAdminRole();
        News news = newsService.update(id, getCurrentUserId(), dto);
        return Result.success("资讯编辑成功", news);
    }

    @PutMapping("/news/{id}/status")
    public Result<News> changeNewsStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        checkAdminRole();
        News news = newsService.changeStatus(id, getCurrentUserId(), body.get("status"));
        return Result.success("资讯状态已更新", news);
    }

    @GetMapping("/news/{id}/revisions")
    public Result<List<NewsRevision>> newsRevisions(@PathVariable Long id) {
        checkAdminRole();
        List<NewsRevision> rows = newsRevisionMapper.selectList(new LambdaQueryWrapper<NewsRevision>()
                .eq(NewsRevision::getNewsId, id).orderByDesc(NewsRevision::getCreateTime).last("LIMIT 20"));
        return Result.success(rows);
    }

    @DeleteMapping("/news/{id}")
    public Result<Void> deleteNews(@PathVariable Long id) {
        checkAdminRole();
        newsService.delete(id);
        return Result.success();
    }

    // ==================== 系统日志 ====================

    @GetMapping("/logs")
    public PageResult<SystemLog> listLogs(@RequestParam(defaultValue = "1") Integer pageNum,
                                           @RequestParam(defaultValue = "10") Integer pageSize,
                                           @RequestParam(required = false) String keyword,
                                           @RequestParam(required = false) String operation,
                                           @RequestParam(required = false) String userRole,
                                           @RequestParam(required = false) Integer statusCode,
                                           @RequestParam(required = false) String traceId,
                                           @RequestParam(required = false) String ip,
                                           @RequestParam(required = false) Boolean exceptionOnly,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
                                           @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        checkAdminRole();
        Page<SystemLog> page = new Page<>(pageNum, Math.min(pageSize, 100));
        Page<SystemLog> result = systemLogMapper.selectPage(page, buildLogQuery(
                keyword, operation, userRole, statusCode, traceId, ip, exceptionOnly, startTime, endTime));
        return PageResult.pageSuccess(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @GetMapping("/logs/export")
    public ResponseEntity<byte[]> exportLogs(@RequestParam(required = false) String keyword,
                                              @RequestParam(required = false) String operation,
                                              @RequestParam(required = false) String userRole,
                                              @RequestParam(required = false) Integer statusCode,
                                              @RequestParam(required = false) String traceId,
                                              @RequestParam(required = false) String ip,
                                              @RequestParam(required = false) Boolean exceptionOnly,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
                                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) throws IOException {
        checkAdminRole();
        Page<SystemLog> page = systemLogMapper.selectPage(new Page<>(1, 5000, false), buildLogQuery(
                keyword, operation, userRole, statusCode, traceId, ip, exceptionOnly, startTime, endTime));
        byte[] content = createLogWorkbook(page.getRecords());
        String filename = "system-logs-" + java.time.LocalDate.now() + ".xlsx";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename(filename, StandardCharsets.UTF_8).build());
        headers.setContentType(MediaType.parseMediaType(
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        return ResponseEntity.ok().headers(headers).body(content);
    }

    private QueryWrapper<SystemLog> buildLogQuery(String keyword, String operation, String userRole,
                                                   Integer statusCode, String traceId, String ip,
                                                   Boolean exceptionOnly, LocalDateTime startTime,
                                                   LocalDateTime endTime) {
        QueryWrapper<SystemLog> query = new QueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            query.and(q -> q.like("username", keyword).or().like("operation", keyword)
                    .or().like("method", keyword).or().like("trace_id", keyword)
                    .or().like("error_message", keyword));
        }
        query.like(operation != null && !operation.isBlank(), "operation", operation)
                .eq(userRole != null && !userRole.isBlank(), "user_role", userRole)
                .eq(statusCode != null, "status_code", statusCode)
                .like(traceId != null && !traceId.isBlank(), "trace_id", traceId)
                .like(ip != null && !ip.isBlank(), "ip", ip)
                .eq(Boolean.TRUE.equals(exceptionOnly), "success", 0)
                .ge(startTime != null, "create_time", startTime)
                .le(endTime != null, "create_time", endTime)
                .orderByDesc("create_time");
        return query;
    }

    private byte[] createLogWorkbook(List<SystemLog> logs) throws IOException {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("系统日志");
            String[] headers = {"ID", "用户", "角色", "操作", "HTTP状态", "是否成功", "请求ID",
                    "IP地址", "耗时(ms)", "错误摘要", "操作时间"};
            Row header = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) header.createCell(i).setCellValue(headers[i]);
            int rowIndex = 1;
            for (SystemLog log : logs) {
                Row row = sheet.createRow(rowIndex++);
                setCell(row, 0, log.getId());
                setCell(row, 1, log.getUsername());
                setCell(row, 2, log.getUserRole());
                setCell(row, 3, log.getOperation());
                setCell(row, 4, log.getStatusCode());
                setCell(row, 5, Integer.valueOf(1).equals(log.getSuccess()) ? "成功" : "失败");
                setCell(row, 6, log.getTraceId());
                setCell(row, 7, log.getIp());
                setCell(row, 8, log.getDuration());
                setCell(row, 9, log.getErrorMessage());
                setCell(row, 10, log.getCreateTime());
            }
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, Math.min(sheet.getColumnWidth(i) + 512, 16000));
            }
            workbook.write(output);
            return output.toByteArray();
        }
    }

    private void setCell(Row row, int column, Object value) {
        row.createCell(column).setCellValue(value == null ? "" : String.valueOf(value));
    }

    @GetMapping("/contact-messages")
    public PageResult<ContactMessage> contactMessages(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {
        Page<ContactMessage> page = new Page<>(pageNum, Math.min(pageSize, 100));
        LambdaQueryWrapper<ContactMessage> wrapper = new LambdaQueryWrapper<ContactMessage>()
                .eq(status != null && !status.isBlank(), ContactMessage::getStatus, status)
                .orderByDesc(ContactMessage::getCreateTime);
        Page<ContactMessage> result = contactMessageMapper.selectPage(page, wrapper);
        return PageResult.pageSuccess(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @PutMapping("/contact-messages/{id}/status")
    public Result<Void> updateContactStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        ContactMessage row = contactMessageMapper.selectById(id);
        if (row == null) throw new com.example.elderai.common.BusinessException(404, "留言不存在");
        String status = body.get("status");
        if (!java.util.Set.of("PENDING", "PROCESSING", "REPLIED").contains(status))
            throw new com.example.elderai.common.BusinessException(400, "无效处理状态");
        row.setStatus(status);
        row.setUpdateTime(java.time.LocalDateTime.now());
        contactMessageMapper.updateById(row);
        return Result.success("留言状态已更新", null);
    }

    @GetMapping("/deletion-requests")
    public PageResult<AccountDeletionRequest> deletionRequests(@RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String status) {
        Page<AccountDeletionRequest> page = new Page<>(pageNum, Math.min(pageSize, 100));
        LambdaQueryWrapper<AccountDeletionRequest> wrapper = new LambdaQueryWrapper<AccountDeletionRequest>()
                .eq(status != null && !status.isBlank(), AccountDeletionRequest::getStatus, status)
                .orderByDesc(AccountDeletionRequest::getRequestedAt);
        Page<AccountDeletionRequest> result = deletionRequestMapper.selectPage(page, wrapper);
        return PageResult.pageSuccess(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @PutMapping("/deletion-requests/{id}/review")
    public Result<Void> reviewDeletion(@PathVariable Long id, @RequestBody Map<String, String> body) {
        AccountDeletionRequest row = deletionRequestMapper.selectById(id);
        if (row == null || !"PENDING".equals(row.getStatus()))
            throw new com.example.elderai.common.BusinessException(404, "待审核申请不存在");
        String decision = body.get("decision");
        if (!java.util.Set.of("APPROVED", "REJECTED").contains(decision))
            throw new com.example.elderai.common.BusinessException(400, "无效审核结果");
        row.setStatus(decision);
        row.setProcessedBy(getCurrentUserId());
        row.setProcessedAt(java.time.LocalDateTime.now());
        row.setProcessRemark(body.get("remark"));
        deletionRequestMapper.updateById(row);
        if ("APPROVED".equals(decision)) {
            User target = userMapper.selectById(row.getUserId());
            if (target != null && !"ADMIN".equals(target.getRole())) {
                target.setStatus(0);
                userMapper.updateById(target);
            }
        }
        return Result.success("注销申请已审核", null);
    }
}
