package com.tmc.attendance.attendance;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
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

}
