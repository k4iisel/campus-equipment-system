package edu.cit.garing.markchristian.campusequipmentloan.strategy;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Component
public class FixedPenaltyStrategy implements PenaltyStrategy {

    private static final long PENALTY_PER_DAY = 50;

    @Override
    public long calculatePenalty(LocalDate dueDate, LocalDate returnDate) {
        if (returnDate == null || !returnDate.isAfter(dueDate)) {
            return 0;
        }
        long daysLate = ChronoUnit.DAYS.between(dueDate, returnDate);
        return daysLate * PENALTY_PER_DAY;
    }
}
