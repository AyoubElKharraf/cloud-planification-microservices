package ma.ump.emploidutemps.exception;

import jakarta.ejb.ApplicationException;

/**
 * Exception métier portant un code HTTP associé pour l'API emploi du temps.
 * Propagée telle quelle depuis la couche EJB vers JAX-RS.
 */
@ApplicationException(rollback = true)
public class EmploiDuTempsException extends RuntimeException {

    private final int statutHttp;

    /**
     * Crée une exception métier avec statut HTTP.
     *
     * @param message    message d'erreur
     * @param statutHttp code HTTP (400, 404, 409, etc.)
     */
    public EmploiDuTempsException(String message, int statutHttp) {
        super(message);
        this.statutHttp = statutHttp;
    }

    /**
     * @return code HTTP associé à l'erreur
     */
    public int getStatutHttp() {
        return statutHttp;
    }
}
