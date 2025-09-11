package edu.cit.garing.markchristian.campusequipmentloan.strategy;

import java.time.LocalDate;

public interface PenaltyStrategy {
    long calculatePenalty(LocalDate dueDate, LocalDate returnDate);
}
