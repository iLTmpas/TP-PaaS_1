package fr.episen.employe;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface EmployeRepository extends JpaRepository<EmployeEntity, Long>, JpaSpecificationExecutor<EmployeEntity> {

    Slice<EmployeEntity> findAllBy(Pageable pageable);

    Optional<EmployeEntity> findByMail(String mail);

    boolean existsByMail(String mail);
}
