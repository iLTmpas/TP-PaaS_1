package fr.upec.episen.paas.core.entity;

public class Employe {
    private Long id;
    private String nom;
    private String prenom;
    private String mail;
    private boolean valide;

    // Getters et setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getMail() { return mail; }
    public void setMail(String mail) { this.mail = mail; }
    public boolean isValide() { return valide; }
    public void setValide(boolean valide) { this.valide = valide; }
}
