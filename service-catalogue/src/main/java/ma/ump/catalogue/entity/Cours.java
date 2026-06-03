package ma.ump.catalogue.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.io.Serializable;

/**
 * Entité JPA représentant un cours du catalogue universitaire UMP Oujda.
 */
@Entity
@Table(name = "cours", uniqueConstraints = {
        @UniqueConstraint(name = "uk_cours_code", columnNames = "code")
})
public class Cours implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 255)
    private String intitule;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String syllabus;

    @Column(length = 500)
    private String prerequis;

    private int credits;

    /**
     * Constructeur par défaut requis par JPA.
     */
    public Cours() {
    }

    /**
     * Constructeur complet pour la création d'un cours.
     *
     * @param code      code unique du cours
     * @param intitule  intitulé du cours
     * @param syllabus  contenu du syllabus
     * @param prerequis prérequis éventuels
     * @param credits   nombre de crédits
     */
    public Cours(String code, String intitule, String syllabus, String prerequis, int credits) {
        this.code = code;
        this.intitule = intitule;
        this.syllabus = syllabus;
        this.prerequis = prerequis;
        this.credits = credits;
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
     * @return code du cours
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code code du cours
     */
    public void setCode(String code) {
        this.code = code;
    }

    /**
     * @return intitulé du cours
     */
    public String getIntitule() {
        return intitule;
    }

    /**
     * @param intitule intitulé du cours
     */
    public void setIntitule(String intitule) {
        this.intitule = intitule;
    }

    /**
     * @return syllabus du cours
     */
    public String getSyllabus() {
        return syllabus;
    }

    /**
     * @param syllabus syllabus du cours
     */
    public void setSyllabus(String syllabus) {
        this.syllabus = syllabus;
    }

    /**
     * @return prérequis du cours
     */
    public String getPrerequis() {
        return prerequis;
    }

    /**
     * @param prerequis prérequis du cours
     */
    public void setPrerequis(String prerequis) {
        this.prerequis = prerequis;
    }

    /**
     * @return nombre de crédits
     */
    public int getCredits() {
        return credits;
    }

    /**
     * @param credits nombre de crédits
     */
    public void setCredits(int credits) {
        this.credits = credits;
    }
}
