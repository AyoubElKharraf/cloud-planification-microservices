package ma.ump.emploidutemps.client.dto;

/**
 * DTO représentant un local retourné par le service locaux.
 */
public class LocalDTO {

    private Long id;
    private String code;
    private String nom;
    private String type;
    private int capacite;
    private String batiment;
    private int etage;
    private boolean projecteur;
    private boolean tableauNumerique;
    private boolean climatisation;
    private boolean accessiblePMR;
    private String disponibilite;

    /**
     * Constructeur par défaut.
     */
    public LocalDTO() {
    }

    /**
     * @return identifiant du local
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id identifiant du local
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * @return code du local
     */
    public String getCode() {
        return code;
    }

    /**
     * @param code code du local
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
     * @return type du local
     */
    public String getType() {
        return type;
    }

    /**
     * @param type type du local
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return capacité
     */
    public int getCapacite() {
        return capacite;
    }

    /**
     * @param capacite capacité
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
     * @return projecteur
     */
    public boolean isProjecteur() {
        return projecteur;
    }

    /**
     * @param projecteur projecteur
     */
    public void setProjecteur(boolean projecteur) {
        this.projecteur = projecteur;
    }

    /**
     * @return tableau numérique
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
     * @return climatisation
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
     * @return accessibilité PMR
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
     * @return disponibilité du local
     */
    public String getDisponibilite() {
        return disponibilite;
    }

    /**
     * @param disponibilite disponibilité
     */
    public void setDisponibilite(String disponibilite) {
        this.disponibilite = disponibilite;
    }
}
