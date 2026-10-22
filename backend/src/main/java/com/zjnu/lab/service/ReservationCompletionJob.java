package com.zjnu.lab.service;

import com.zjnu.lab.mapper.ReservationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ReservationCompletionJob {
    private final ReservationMapper reservations;

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void completeExpiredReservations() {
        reservations.completeDue();
    }
}
