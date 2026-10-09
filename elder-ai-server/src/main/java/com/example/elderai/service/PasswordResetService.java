package com.example.elderai.service;

import com.example.elderai.dto.ForgotPasswordDTO;
import com.example.elderai.dto.ResetPasswordDTO;

public interface PasswordResetService {

    void sendResetCode(ForgotPasswordDTO dto);

    void resetPassword(ResetPasswordDTO dto);
}