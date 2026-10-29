package com.tmc.attendance.attendance;

import org.springframework.stereotype.Component;

import com.tmc.attendance.attendance.dto.AttendanceResponse;
import com.tmc.attendance.attendance.dto.CreateAttendanceRequest;
import com.tmc.attendance.user.AppUser;

@Component
public class AttendanceMapper {
    public Attendance toEntity(CreateAttendanceRequest request, AppUser appUser) {
        Attendance attendance = new Attendance();
        attendance.setAppUser(appUser);
        attendance.setEvent(request.event());
        return attendance;
    }

    public AttendanceResponse toResponse(Attendance attendance) {
        return new AttendanceResponse(
                attendance.getId(),
                attendance.getAppUser().getId(),
                attendance.getEvent(),
                attendance.getCreatedAt()
        );
    }
}
