package edu.cit.garing.markchristian.campusequipmentloan.repository;

import edu.cit.garing.markchristian.campusequipmentloan.model.Loan;
import edu.cit.garing.markchristian.campusequipmentloan.model.Loan.Status;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByStudentIdAndStatus(Long studentId, Status status);
}
