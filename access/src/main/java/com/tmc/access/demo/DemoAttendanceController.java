package com.tmc.access.demo;

import java.time.Clock;
import java.util.UUID;

import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tmc.access.attendance.AttendanceEventPublisher;
import com.tmc.access.attendance.AttendanceMessage;
import com.tmc.access.attendance.AttendanceType;
import com.tmc.access.user.AppUser;
import com.tmc.access.user.UserRepository;

/**
 * Publishes a made-up check-in to try out the messaging path. Only available
 * under the demo profile.
 */
@RestController
@Profile("demo")
@RequestMapping("/qr-codes/test-kafka")
public class DemoAttendanceController {
    private final UserRepository userRepository;
    private final AttendanceEventPublisher attendanceEventPublisher;
    private final Clock clock;

    public DemoAttendanceController(
            UserRepository userRepository,
            AttendanceEventPublisher attendanceEventPublisher,
            Clock clock
    ) {
        this.userRepository = userRepository;
        this.attendanceEventPublisher = attendanceEventPublisher;
        this.clock = clock;
    }

    @GetMapping
    public String testKafka() {
        AppUser anyUser = userRepository.findFirstByActiveTrue().orElseThrow();
        attendanceEventPublisher.publish(
                new AttendanceMessage(UUID.randomUUID(), anyUser.getId(), AttendanceType.CHECK_IN, clock.instant())
        );
        return "Check database for new attendance of user " + anyUser.getId();
    }
}
