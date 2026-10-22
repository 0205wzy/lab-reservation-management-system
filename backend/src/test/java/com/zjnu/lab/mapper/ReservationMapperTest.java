package com.zjnu.lab.mapper;

import com.zjnu.lab.model.Reservation;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class ReservationMapperTest {
    @Autowired
    private ReservationMapper reservations;

    @Test
    void overlappingPendingReservationsDoNotConflictUntilOneIsApproved() {
        LocalDate date = LocalDate.now().plusDays(30);
        Reservation first = insertPending(1L, date);
        Reservation second = insertPending(2L, date);

        assertThat(reservations.conflict(1L, date, time("10:00"), time("11:00"), null)).isZero();
        assertThat(reservations.calendar(1L, date, date)).isEmpty();

        reservations.audit(first.getId(), "APPROVED", null, 3L);

        assertThat(reservations.conflict(1L, date, time("10:00"), time("11:00"), second.getId())).isEqualTo(1);
        List<Reservation> calendar = reservations.calendar(1L, date, date);
        assertThat(calendar).hasSize(1);
        assertThat(calendar.get(0).getStatus()).isEqualTo("APPROVED");
    }

    @Test
    void rejectingPendingReservationLeavesItsTimeAvailable() {
        LocalDate date = LocalDate.now().plusDays(31);
        Reservation rejected = insertPending(1L, date);
        Reservation accepted = insertPending(2L, date);

        reservations.audit(rejected.getId(), "REJECTED", "测试拒绝", 3L);
        assertThat(reservations.conflict(1L, date, time("10:00"), time("11:00"), accepted.getId())).isZero();

        reservations.audit(accepted.getId(), "APPROVED", null, 3L);

        assertThat(reservations.conflict(1L, date, time("10:00"), time("11:00"), rejected.getId())).isEqualTo(1);
    }

    @Test
    void cancelledAndCompletedReservationsDoNotOccupyTheirTime() {
        LocalDate date = LocalDate.now().plusDays(32);
        Reservation cancelled = insertPending(1L, date);
        Reservation completed = insertPending(2L, date);
        reservations.cancel(cancelled.getId());
        reservations.audit(completed.getId(), "COMPLETED", null, 3L);

        assertThat(reservations.conflict(1L, date, time("10:00"), time("11:00"), null)).isZero();
        assertThat(reservations.calendar(1L, date, date)).isEmpty();
        assertThat(reservations.busyPeriods(1L, date)).isEmpty();
    }

    @Test
    void adjacentApprovedReservationsDoNotConflict() {
        LocalDate date = LocalDate.now().plusDays(33);
        Reservation existing = insertPending(1L, date, "09:00", "10:00");
        reservations.audit(existing.getId(), "APPROVED", null, 3L);

        assertThat(reservations.conflict(1L, date, time("10:00"), time("11:00"), null)).isZero();
        assertThat(reservations.conflict(1L, date, time("08:30"), time("09:00"), null)).isZero();
    }

    private Reservation insertPending(Long applicantId, LocalDate date) {
        return insertPending(applicantId, date, "10:00", "11:00");
    }

    private Reservation insertPending(Long applicantId, LocalDate date, String startTime, String endTime) {
        Reservation reservation = new Reservation();
        reservation.setApplicantId(applicantId);
        reservation.setLabId(1L);
        reservation.setReserveDate(date);
        reservation.setStartTime(time(startTime));
        reservation.setEndTime(time(endTime));
        reservation.setPurpose("预约冲突规则测试");
        reservation.setParticipants(1);
        reservations.insert(reservation);
        return reservation;
    }

    private LocalTime time(String value) {
        return LocalTime.parse(value);
    }
}
