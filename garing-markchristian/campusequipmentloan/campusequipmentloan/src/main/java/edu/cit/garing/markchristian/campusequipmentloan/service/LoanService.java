package edu.cit.garing.markchristian.campusequipmentloan.service;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import edu.cit.garing.markchristian.campusequipmentloan.model.Equipment;
import edu.cit.garing.markchristian.campusequipmentloan.model.Loan;
import edu.cit.garing.markchristian.campusequipmentloan.model.Loan.Status;
import edu.cit.garing.markchristian.campusequipmentloan.model.Student;
import edu.cit.garing.markchristian.campusequipmentloan.repository.EquipmentRepository;
import edu.cit.garing.markchristian.campusequipmentloan.repository.LoanRepository;
import edu.cit.garing.markchristian.campusequipmentloan.repository.StudentRepository;
import edu.cit.garing.markchristian.campusequipmentloan.strategy.PenaltyStrategy;

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
    public Loan createLoan(Long equipmentId, Long studentId, String startDateStr) {
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

        LocalDate startDate = startDateStr != null ? LocalDate.parse(startDateStr) : LocalDate.now();
        LocalDate dueDate = startDate.plusDays(LOAN_DURATION_DAYS);

        Loan loan = new Loan();
        loan.setEquipment(equipment);
        loan.setStudent(student);
        loan.setStartDate(startDate);
        loan.setDueDate(dueDate);
        loan.setStatus(Status.ACTIVE);

        equipment.setAvailability(false);
        equipmentRepository.save(equipment);

        return loanRepository.save(loan);
    }

    @Transactional
    public Loan returnLoan(Long loanId, String returnDateStr) {
        logger.info("Returning loan with ID {}", loanId);

        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));

        if (loan.getStatus() != Status.ACTIVE) {
            throw new IllegalStateException("Loan is not active");
        }

        LocalDate returnDate = returnDateStr != null ? LocalDate.parse(returnDateStr) : LocalDate.now();
        loan.setReturnDate(returnDate);

        long penalty = penaltyStrategy.calculatePenalty(loan.getDueDate(), returnDate);
        loan.setPenaltyAmount(penalty);

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
