package fr.upec.episen.paas.db;

import fr.upec.episen.paas.db.entity.Employe;
import fr.upec.episen.paas.db.repository.EmployeRepository;
import fr.upec.episen.paas.db.service.CacheLoadingService;
import fr.upec.episen.paas.db.service.CacheService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class CacheInitTest {

    @Autowired
    EmployeRepository employeRepository;

    @Autowired
    CacheLoadingService cacheLoadingService;

    @Autowired
    CacheService cacheService;

    @Test
    @Transactional
    public void testCacheRefresh() {
        // prepare: save two employees
        Employe e1 = new Employe();
        e1.setId(1L);
        e1.setNom("Dupont");
        e1.setPrenom("Jean");
        e1.setMail("jean.dupont@example.com");
        e1.setValide(true);

        Employe e2 = new Employe();
        e2.setId(2L);
        e2.setNom("Martin");
        e2.setPrenom("Anne");
        e2.setMail("anne.martin@example.com");
        e2.setValide(false);

        employeRepository.save(e1);
        employeRepository.save(e2);

        // run the cache loader
        cacheLoadingService.refreshCache();

        // assert cache has been populated
        assertThat(cacheService.getAll()).hasSize(2);
        assertThat(cacheService.getEmploye(1L).getNom()).isEqualTo("Dupont");
    }
}
