package fr.episen.employe;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class EmployeService {

    private final EmployeRepository employeRepository;

    public EmployeService(EmployeRepository employeRepository) {
        this.employeRepository = employeRepository;
    }

    @Transactional
    public EmployeEntity createEmploye(String nom, String prenom, String mail, Boolean valide) {
        if (employeRepository.existsByMail(mail)) {
            throw new IllegalArgumentException("Un employé avec cet email existe déjà");
        }
        EmployeEntity employe = new EmployeEntity(nom, prenom, mail);
        employe.setValide(valide);
        return employeRepository.saveAndFlush(employe);
    }

    @Transactional(readOnly = true)
    public List<EmployeEntity> list(Pageable pageable) {
        return employeRepository.findAllBy(pageable).toList();
    }

    @Transactional(readOnly = true)
    public Optional<EmployeEntity> findById(Long id) {
        return employeRepository.findById(id);
    }

    @Transactional
    public EmployeEntity updateEmploye(Long id, String nom, String prenom, String mail, Boolean valide) {
        EmployeEntity employe = employeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employé non trouvé"));

        // Check if email is being changed and if new email already exists
        if (!employe.getMail().equals(mail) && employeRepository.existsByMail(mail)) {
            throw new IllegalArgumentException("Un employé avec cet email existe déjà");
        }

        employe.setNom(nom);
        employe.setPrenom(prenom);
        employe.setMail(mail);
        employe.setValide(valide);
        return employeRepository.saveAndFlush(employe);
    }

    @Transactional
    public void deleteEmploye(Long id) {
        if (!employeRepository.existsById(id)) {
            throw new IllegalArgumentException("Employé non trouvé");
        }
        employeRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public long count() {
        return employeRepository.count();
    }
}
