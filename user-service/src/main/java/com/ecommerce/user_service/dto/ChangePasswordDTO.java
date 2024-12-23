package com.ecommerce.user_service.dto;

import lombok.Data;

@Data
public class ChangePasswordDTO {
    private String currentPassword;
    private String newPassword;
    private String confirmPassword;
} 