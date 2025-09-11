package edu.cit.garing.markchristian.campusequipmentloan.repository;

import edu.cit.garing.markchristian.campusequipmentloan.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudentRepository extends JpaRepository<Student, Long> {

}
