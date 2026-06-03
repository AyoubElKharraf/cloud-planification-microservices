package ma.ump.emploidutemps.exception;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

import java.util.HashMap;
import java.util.Map;

/**
 * Mappe les exceptions métier vers des réponses HTTP JSON cohérentes.
 */
@Provider
public class EmploiDuTempsExceptionMapper implements ExceptionMapper<EmploiDuTempsException> {

    /**
     * Convertit une {@link EmploiDuTempsException} en réponse HTTP.
     *
     * @param ex exception métier
     * @return réponse JAX-RS avec le statut approprié
     */
    @Override
    public Response toResponse(EmploiDuTempsException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("erreur", ex.getMessage());
        body.put("statut", ex.getStatutHttp());
        return Response.status(ex.getStatutHttp())
                .type(MediaType.APPLICATION_JSON)
                .entity(body)
                .build();
    }
}
