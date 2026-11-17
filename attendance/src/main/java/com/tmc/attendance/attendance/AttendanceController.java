package com.tmc.attendance.attendance;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.tmc.attendance.attendance.dto.AttendanceResponse;
import com.tmc.attendance.attendance.dto.CreateAttendanceRequest;

@RestController
@RequestMapping("/attendances")
public class AttendanceController {
    private final AttendanceService attendanceService;

    public AttendanceController(AttendanceService attendanceService) {
        this.attendanceService = attendanceService;
    }

    @PostMapping
    public AttendanceResponse createAttendance(@RequestBody @Valid CreateAttendanceRequest request) {
        return attendanceService.createAttendance(request);
    }

    @GetMapping
    public List<AttendanceResponse> getAttendances(@RequestParam Instant start, @RequestParam Instant end) {
        return attendanceService.getAttendances(start, end);
    }

    @GetMapping("/{userId}")
    public List<AttendanceResponse> getAttendancesByUserId(
            @PathVariable UUID userId,
            @RequestParam Instant start,
            @RequestParam Instant end,
            Authentication authentication
    ) {
        return attendanceService.getAttendancesByUserId(userId, start, end, authentication);
    }
}
