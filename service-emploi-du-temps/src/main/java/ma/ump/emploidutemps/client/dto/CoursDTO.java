package ma.ump.emploidutemps.client.dto;

/**
 * DTO représentant un cours retourné par le service catalogue.
 */
public class CoursDTO {

    private Long id;
    private String code;
    private String intitule;
    private String syllabus;
    private String prerequis;
    private int credits;

    /**
     * Constructeur par défaut.
     */
    public CoursDTO() {
    }

    /**
     * @return identifiant du cours
     */
    public Long getId() {
        return id;
    }

    /**
     * @param id identifiant du cours
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
     * @return syllabus
     */
    public String getSyllabus() {
        return syllabus;
    }

    /**
     * @param syllabus syllabus
     */
    public void setSyllabus(String syllabus) {
        this.syllabus = syllabus;
    }

    /**
     * @return prérequis
     */
    public String getPrerequis() {
        return prerequis;
    }

    /**
     * @param prerequis prérequis
     */
    public void setPrerequis(String prerequis) {
        this.prerequis = prerequis;
    }

    /**
     * @return crédits
     */
    public int getCredits() {
        return credits;
    }

    /**
     * @param credits crédits
     */
    public void setCredits(int credits) {
        this.credits = credits;
    }
}
