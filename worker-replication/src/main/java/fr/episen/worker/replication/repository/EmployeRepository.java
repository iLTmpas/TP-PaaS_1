package fr.episen.worker.replication.repository;

import fr.episen.worker.replication.entity.EmployeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeRepository extends JpaRepository<EmployeEntity, Long> {
}
