package fr.upec.episen.paas.db.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import fr.upec.episen.paas.db.entity.Employe;

@Repository
public interface EmployeRepository extends JpaRepository<Employe, Long> {

}
