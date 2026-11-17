package com.tmc.attendance.attendance;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRepository extends JpaRepository<Attendance, UUID> {
    List<Attendance> findByCreatedAtBetweenOrderByCreatedAtAsc(Instant start, Instant end);

    List<Attendance> findByAppUserIdAndCreatedAtBetweenOrderByCreatedAtAsc(UUID userId, Instant start, Instant end);
}
