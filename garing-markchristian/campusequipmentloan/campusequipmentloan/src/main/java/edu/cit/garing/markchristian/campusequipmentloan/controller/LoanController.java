package edu.cit.garing.markchristian.campusequipmentloan.controller;

import edu.cit.garing.markchristian.campusequipmentloan.model.Equipment;
import edu.cit.garing.markchristian.campusequipmentloan.model.Loan;
import edu.cit.garing.markchristian.campusequipmentloan.service.LoanService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Validated
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping("/loans")
    public ResponseEntity<Loan> createLoan(@RequestBody Map<String, Long> request) {
        Long equipmentId = request.get("equipmentId");
        Long studentId = request.get("studentId");

        if (equipmentId == null || studentId == null) {
            return ResponseEntity.badRequest().build();
        }

        Loan loan = loanService.createLoan(equipmentId, studentId);
        return ResponseEntity.status(HttpStatus.CREATED).body(loan);
    }

    @PostMapping("/loans/{id}/return")
    public ResponseEntity<Loan> returnLoan(@PathVariable("id") Long loanId) {
        Loan loan = loanService.returnLoan(loanId);
        return ResponseEntity.ok(loan);
    }

    @GetMapping("/equipment/available")
    public ResponseEntity<List<Equipment>> getAvailableEquipment() {
        List<Equipment> available = loanService.getAvailableEquipment();
        return ResponseEntity.ok(available);
    }
}
