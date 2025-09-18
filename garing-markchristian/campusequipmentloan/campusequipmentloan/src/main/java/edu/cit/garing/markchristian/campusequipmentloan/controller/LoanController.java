package edu.cit.garing.markchristian.campusequipmentloan.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import edu.cit.garing.markchristian.campusequipmentloan.model.Equipment;
import edu.cit.garing.markchristian.campusequipmentloan.model.Loan;
import edu.cit.garing.markchristian.campusequipmentloan.service.LoanService;

@RestController
@RequestMapping("/api")
@Validated
public class LoanController {

    private final LoanService loanService;

    public LoanController(LoanService loanService) {
        this.loanService = loanService;
    }

    @PostMapping("/loans")
        public ResponseEntity<Loan> createLoan(@RequestBody Map<String, Object> request) {
            Long equipmentId = request.get("equipmentId") != null ? Long.valueOf(request.get("equipmentId").toString()) : null;
            Long studentId = request.get("studentId") != null ? Long.valueOf(request.get("studentId").toString()) : null;
            String startDateStr = request.get("startDate") != null ? request.get("startDate").toString() : null;

            if (equipmentId == null || studentId == null) {
                return ResponseEntity.badRequest().build();
            }

            Loan loan = loanService.createLoan(equipmentId, studentId, startDateStr);
            return ResponseEntity.status(HttpStatus.CREATED).body(loan);
    }

    @PostMapping("/loans/{id}/return")
        public ResponseEntity<Map<String, Object>> returnLoan(@PathVariable("id") Long loanId, @RequestBody(required = false) Map<String, Object> request) {
            String returnDateStr = null;
            if (request != null && request.get("returnDate") != null) {
                returnDateStr = request.get("returnDate").toString();
            }
            Loan loan = loanService.returnLoan(loanId, returnDateStr);
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("loan", loan);
            response.put("penalty", loan.getPenaltyAmount());
            return ResponseEntity.ok(response);
    }

    @GetMapping("/equipment/available")
    public ResponseEntity<List<Equipment>> getAvailableEquipment() {
        List<Equipment> available = loanService.getAvailableEquipment();
        return ResponseEntity.ok(available);
    }
}
