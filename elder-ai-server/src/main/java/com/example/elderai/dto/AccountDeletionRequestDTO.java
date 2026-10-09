package com.example.elderai.dto;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Data
public class AccountDeletionRequestDTO {
    @Size(max = 500, message = "注销原因不能超过500字")
    private String reason;
}
