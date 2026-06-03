package ma.ump.emploidutemps.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.io.Serializable;

/**
 * Entité JPA représentant une séance planifiée dans l'emploi du temps UMP Oujda.
 */
@Entity
@Table(name = "seances")
public class Seance implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cours_id", nullable = false)
    private Long coursId;

    @Column(name = "local_id", nullable = false)
    private Long localId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private JourSemaine jour;

    @Column(name = "heure_debut", nullable = false, length = 5)
    private String heureDebut;

    @Column(name = "heure_fin", nullable = false, length = 5)
    private String heureFin;

    @Column(nullable = false, length = 20)
    private String semestre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeSeance type;

    /**
     * Constructeur par défaut requis par JPA.
     */
    public Seance() {
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
     * @return identifiant du cours (service catalogue)
     */
    public Long getCoursId() {
        return coursId;
    }

    /**
     * @param coursId identifiant du cours
     */
    public void setCoursId(Long coursId) {
        this.coursId = coursId;
    }

    /**
     * @return identifiant du local (service locaux)
     */
    public Long getLocalId() {
        return localId;
    }

    /**
     * @param localId identifiant du local
     */
    public void setLocalId(Long localId) {
        this.localId = localId;
    }

    /**
     * @return jour de la semaine
     */
    public JourSemaine getJour() {
        return jour;
    }

    /**
     * @param jour jour de la semaine
     */
    public void setJour(JourSemaine jour) {
        this.jour = jour;
    }

    /**
     * @return heure de début (format HH:mm)
     */
    public String getHeureDebut() {
        return heureDebut;
    }

    /**
     * @param heureDebut heure de début
     */
    public void setHeureDebut(String heureDebut) {
        this.heureDebut = heureDebut;
    }

    /**
     * @return heure de fin (format HH:mm)
     */
    public String getHeureFin() {
        return heureFin;
    }

    /**
     * @param heureFin heure de fin
     */
    public void setHeureFin(String heureFin) {
        this.heureFin = heureFin;
    }

    /**
     * @return semestre académique
     */
    public String getSemestre() {
        return semestre;
    }

    /**
     * @param semestre semestre académique
     */
    public void setSemestre(String semestre) {
        this.semestre = semestre;
    }

    /**
     * @return type de séance
     */
    public TypeSeance getType() {
        return type;
    }

    /**
     * @param type type de séance
     */
    public void setType(TypeSeance type) {
        this.type = type;
    }
}
