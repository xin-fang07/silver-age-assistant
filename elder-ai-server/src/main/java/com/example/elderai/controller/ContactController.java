package com.example.elderai.controller;

import com.example.elderai.common.Result;
import com.example.elderai.dto.ContactMessageDTO;
import com.example.elderai.entity.ServiceTicket;
import com.example.elderai.mapper.ServiceTicketMapper;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/contact")
public class ContactController {
    @Resource private ServiceTicketMapper ticketMapper;

    @PostMapping("/message")
    public Result<ServiceTicket> submit(@Valid @RequestBody ContactMessageDTO dto) {
        ServiceTicket row = new ServiceTicket();
        row.setTicketNo("TK" + System.currentTimeMillis()); row.setType("CONTACT");
        row.setContactName(dto.getName().trim()); row.setContactPhone(dto.getPhone().trim());
        row.setContactEmail(dto.getEmail() == null || dto.getEmail().isBlank() ? null : dto.getEmail().trim());
        row.setSubject(dto.getSubject());
        row.setContent(dto.getMessage().trim()); row.setStatus("PENDING"); row.setPriority("NORMAL");
        row.setCreateTime(LocalDateTime.now());
        row.setUpdateTime(LocalDateTime.now());
        ticketMapper.insert(row);
        return Result.success("留言提交成功", row);
    }
}
