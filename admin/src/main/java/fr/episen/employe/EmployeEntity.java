package fr.episen.employe;

import jakarta.persistence.*;

import java.util.Random;
import java.util.UUID;

@Entity
@Table(name = "employes")
public class EmployeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_employe")
    private Long idEmploye;

    @Column(name = "nom", nullable = false, length = 50)
    private String nom;

    @Column(name = "prenom", nullable = false, length = 50)
    private String prenom;

    @Column(name = "mail", nullable = false, length = 100, unique = true)
    private String mail;

    @Column(name = "badge_id", nullable = false)
    private UUID badgeId;

    @Column(name = "valide", nullable = false)
    private Boolean valide = false;

    private Long id;

    protected EmployeEntity() {
        // For Hibernate
    }

    public EmployeEntity(String nom, String prenom, String mail) {
        this.nom = nom;
        this.prenom = prenom;
        this.mail = mail;
        this.badgeId = UUID.randomUUID();
        this.valide = false;
        this.id = new Random().nextLong();

    }

    public Long getIdEmploye() {
        return idEmploye;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public UUID getBadgeId() {
        return badgeId;
    }

    public Boolean getValide() {
        return valide;
    }

    public void setValide(Boolean valide) {
        this.valide = valide;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !getClass().isAssignableFrom(obj.getClass())) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        EmployeEntity other = (EmployeEntity) obj;
        return getIdEmploye() != null && getIdEmploye().equals(other.getIdEmploye());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
