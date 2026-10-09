package com.example.elderai.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
@Data
public class ContactMessageDTO {
    @NotBlank @Size(max = 50) private String name;
    @NotBlank @Pattern(regexp = "^1[3-9]\\d{9}$", message = "联系电话格式不正确") private String phone;
    @Email @Size(max = 120) private String email;
    @NotBlank @Pattern(regexp = "product|technical|cooperation|other", message = "咨询类型不正确") private String subject;
    @NotBlank @Size(max = 1000) private String message;
}
