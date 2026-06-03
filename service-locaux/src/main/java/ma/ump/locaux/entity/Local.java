package ma.ump.locaux.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.io.Serializable;

/**
 * Entité JPA représentant un local (salle, amphi, labo) de l'UMP Oujda.
 */
@Entity
@Table(name = "locaux", uniqueConstraints = {
        @UniqueConstraint(name = "uk_local_code", columnNames = "code")
})
public class Local implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 30)
    private String code;

    @Column(nullable = false, length = 255)
    private String nom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeLocal type;

    @Column(nullable = false)
    private int capacite;

    @Column(length = 100)
    private String batiment;

    private int etage;

    private boolean projecteur;

    private boolean tableauNumerique;

    private boolean climatisation;

    private boolean accessiblePMR;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private DisponibiliteLocal disponibilite = DisponibiliteLocal.DISPONIBLE;

    /**
     * Constructeur par défaut requis par JPA.
     */
    public Local() {
    }

    /**
     * @return identifiant technique
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id identifiant technique
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return code unique du local
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code code unique du local
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return nom du local
     */
    public String getNom() {
        return nom;
    }

    /**
     * @param nom nom du local
     */
    public void setNom(String nom) {
        this.nom = nom;
    }

    /**
     * @return type de local
     */
    public TypeLocal getType() {
        return type;
    }

    /**
     * @param type type de local
     */
    public void setType(TypeLocal type) {
        this.type = type;
    }

    /**
     * @return capacité d'accueil
     */
    public int getCapacite() {
        return capacite;
    }

    /**
     * @param capacite capacité d'accueil
     */
    public void setCapacite(int capacite) {
        this.capacite = capacite;
    }

    /**
     * @return bâtiment
     */
    public String getBatiment() {
        return batiment;
    }

    /**
     * @param batiment bâtiment
     */
    public void setBatiment(String batiment) {
        this.batiment = batiment;
    }

    /**
     * @return étage
     */
    public int getEtage() {
        return etage;
    }

    /**
     * @param etage étage
     */
    public void setEtage(int etage) {
        this.etage = etage;
    }

    /**
     * @return true si équipé d'un projecteur
     */
    public boolean isProjecteur() {
        return projecteur;
    }

    /**
     * @param projecteur équipement projecteur
     */
    public void setProjecteur(boolean projecteur) {
        this.projecteur = projecteur;
    }

    /**
     * @return true si équipé d'un tableau numérique
     */
    public boolean isTableauNumerique() {
        return tableauNumerique;
    }

    /**
     * @param tableauNumerique tableau numérique
     */
    public void setTableauNumerique(boolean tableauNumerique) {
        this.tableauNumerique = tableauNumerique;
    }

    /**
     * @return true si climatisé
     */
    public boolean isClimatisation() {
        return climatisation;
    }

    /**
     * @param climatisation climatisation
     */
    public void setClimatisation(boolean climatisation) {
        this.climatisation = climatisation;
    }

    /**
     * @return true si accessible PMR
     */
    public boolean isAccessiblePMR() {
        return accessiblePMR;
    }

    /**
     * @param accessiblePMR accessibilité PMR
     */
    public void setAccessiblePMR(boolean accessiblePMR) {
        this.accessiblePMR = accessiblePMR;
    }

    /**
     * @return état de disponibilité
     */
    public DisponibiliteLocal getDisponibilite() {
        return disponibilite;
    }

    /**
     * @param disponibilite état de disponibilité
     */
    public void setDisponibilite(DisponibiliteLocal disponibilite) {
        this.disponibilite = disponibilite;
    }
}
