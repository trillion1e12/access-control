package com.tmc.access.qrcode.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import com.tmc.access.attendance.AttendanceType;

public record VerifyQrCodeRequest(
        @NotBlank String content,
        @NotNull AttendanceType event
) {

}
