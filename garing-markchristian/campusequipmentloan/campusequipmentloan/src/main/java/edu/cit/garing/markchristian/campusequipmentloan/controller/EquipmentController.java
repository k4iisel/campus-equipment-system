package edu.cit.garing.markchristian.campusequipmentloan.controller;

import edu.cit.garing.markchristian.campusequipmentloan.model.Equipment;
import edu.cit.garing.markchristian.campusequipmentloan.repository.EquipmentRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/equipment")
@Validated
public class EquipmentController {

    private final EquipmentRepository equipmentRepository;

    public EquipmentController(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    // Get all equipment
    @GetMapping
    public ResponseEntity<List<Equipment>> getAllEquipment() {
        return ResponseEntity.ok(equipmentRepository.findAll());
    }

    // Get equipment by id
    @GetMapping("/{id}")
    public ResponseEntity<Equipment> getEquipmentById(@PathVariable Long id) {
        return equipmentRepository.findById(id)
                .map(equipment -> ResponseEntity.ok(equipment))
                .orElse(ResponseEntity.notFound().build());
    }

    // Create new equipment
    @PostMapping
    public ResponseEntity<Equipment> createEquipment(@Valid @RequestBody Equipment equipment) {
        Equipment saved = equipmentRepository.save(equipment);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    // Update existing equipment
    @PutMapping("/{id}")
    public ResponseEntity<Equipment> updateEquipment(@PathVariable Long id, @Valid @RequestBody Equipment updatedEquipment) {
        return equipmentRepository.findById(id).map(equipment -> {
            equipment.setName(updatedEquipment.getName());
            equipment.setType(updatedEquipment.getType());
            equipment.setSerialNumber(updatedEquipment.getSerialNumber());
            equipment.setAvailability(updatedEquipment.isAvailability());
            Equipment saved = equipmentRepository.save(equipment);
            return ResponseEntity.ok(saved);
        }).orElse(ResponseEntity.notFound().build());
    }

    // Delete equipment
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEquipment(@PathVariable Long id) {
        if (!equipmentRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        equipmentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
