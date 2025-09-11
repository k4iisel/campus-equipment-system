package edu.cit.garing.markchristian.campusequipmentloan.service;

import edu.cit.garing.markchristian.campusequipmentloan.model.*;
import edu.cit.garing.markchristian.campusequipmentloan.model.Loan.Status;
import edu.cit.garing.markchristian.campusequipmentloan.repository.*;
import edu.cit.garing.markchristian.campusequipmentloan.strategy.PenaltyStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class LoanService {

    private static final Logger logger = LoggerFactory.getLogger(LoanService.class);

    private final LoanRepository loanRepository;
    private final EquipmentRepository equipmentRepository;
    private final StudentRepository studentRepository;
    private final PenaltyStrategy penaltyStrategy;

    private static final int MAX_ACTIVE_LOANS_PER_STUDENT = 2;
    private static final int LOAN_DURATION_DAYS = 7;

    public LoanService(LoanRepository loanRepository,
                       EquipmentRepository equipmentRepository,
                       StudentRepository studentRepository,
                       PenaltyStrategy penaltyStrategy) {
        this.loanRepository = loanRepository;
        this.equipmentRepository = equipmentRepository;
        this.studentRepository = studentRepository;
        this.penaltyStrategy = penaltyStrategy;
    }

    @Transactional
    public Loan createLoan(Long equipmentId, Long studentId) {
        logger.info("Creating loan for equipment {} and student {}", equipmentId, studentId);

        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Equipment not found"));

        if (!equipment.isAvailability()) {
            throw new IllegalStateException("Equipment not available");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Student not found"));

        List<Loan> activeLoans = loanRepository.findByStudentIdAndStatus(student.getId(), Status.ACTIVE);
        if (activeLoans.size() >= MAX_ACTIVE_LOANS_PER_STUDENT) {
            throw new IllegalStateException("Student has reached max active loans");
        }

        Loan loan = new Loan();
        loan.setEquipment(equipment);
        loan.setStudent(student);
        loan.setStartDate(LocalDate.now());
        loan.setDueDate(LocalDate.now().plusDays(LOAN_DURATION_DAYS));
        loan.setStatus(Status.ACTIVE);

        equipment.setAvailability(false);
        equipmentRepository.save(equipment);

        return loanRepository.save(loan);
    }

    @Transactional
    public Loan returnLoan(Long loanId) {
        logger.info("Returning loan with ID {}", loanId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));

        if (loan.getStatus() != Status.ACTIVE) {
            throw new IllegalStateException("Loan is not active");
        }

        LocalDate returnDate = LocalDate.now();
        loan.setReturnDate(returnDate);

        if (returnDate.isAfter(loan.getDueDate())) {
            loan.setStatus(Status.OVERDUE);
        } else {
            loan.setStatus(Status.RETURNED);
        }

        Equipment equipment = loan.getEquipment();
        equipment.setAvailability(true);
        equipmentRepository.save(equipment);

        return loanRepository.save(loan);
    }

    public List<Equipment> getAvailableEquipment() {
        logger.info("Fetching available equipment");
        return equipmentRepository.findByAvailabilityTrue();
    }

    public long calculatePenalty(Long loanId) {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));

        LocalDate returnDate = loan.getReturnDate();
        if (returnDate == null) {
            returnDate = LocalDate.now();
        }

        return penaltyStrategy.calculatePenalty(loan.getDueDate(), returnDate);
    }
}
