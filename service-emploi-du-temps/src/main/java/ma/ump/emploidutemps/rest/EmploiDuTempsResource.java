package ma.ump.emploidutemps.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ma.ump.emploidutemps.entity.JourSemaine;
import ma.ump.emploidutemps.entity.Seance;
import ma.ump.emploidutemps.exception.EmploiDuTempsException;
import ma.ump.emploidutemps.service.EmploiDuTempsService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ressource REST exposant les opérations de planification de l'emploi du temps.
 */
@RequestScoped
@Path("/emploi-du-temps")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EmploiDuTempsResource {

    @Inject
    private EmploiDuTempsService emploiDuTempsService;

    /**
     * Liste toutes les séances planifiées.
     *
     * @return réponse HTTP 200
     */
    @GET
    public Response listerToutes() {
        try {
            List<Seance> seances = emploiDuTempsService.listerToutes();
            return Response.ok(seances).build();
        } catch (Exception ex) {
            return erreurInterne(ex);
        }
    }

    /**
     * Recherche une séance par identifiant.
     *
     * @param id identifiant de la séance
     * @return réponse HTTP 200 ou 404
     */
    @GET
    @Path("/{id}")
    public Response trouverParId(@PathParam("id") Long id) {
        try {
            return emploiDuTempsService.trouverParId(id)
                    .map(seance -> Response.ok(seance).build())
                    .orElseGet(() -> erreur("Séance introuvable avec l'id : " + id, Response.Status.NOT_FOUND));
        } catch (Exception ex) {
            return erreurInterne(ex);
        }
    }

    /**
     * Liste les séances filtrées par jour de la semaine.
     *
     * @param jour nom du jour (LUNDI, MARDI, ...)
     * @return réponse HTTP 200 ou 400
     */
    @GET
    @Path("/jour/{jour}")
    public Response listerParJour(@PathParam("jour") String jour) {
        try {
            JourSemaine jourSemaine = JourSemaine.valueOf(jour.toUpperCase());
            List<Seance> seances = emploiDuTempsService.listerParJour(jourSemaine);
            return Response.ok(seances).build();
        } catch (IllegalArgumentException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("No enum constant")) {
                return erreur("Jour invalide : " + jour, Response.Status.BAD_REQUEST);
            }
            return erreur(ex.getMessage(), Response.Status.BAD_REQUEST);
        } catch (EmploiDuTempsException ex) {
            return erreur(ex.getMessage(), Response.Status.fromStatusCode(ex.getStatutHttp()));
        } catch (Exception ex) {
            return erreurInterne(ex);
        }
    }

    /**
     * Planifie une nouvelle séance (logique métier complète).
     *
     * @param seance données de la séance
     * @return réponse HTTP 201, 400, 409 ou 500
     */
    @POST
    public Response planifier(Seance seance) {
        try {
            Seance planifiee = emploiDuTempsService.planifierSeance(seance);
            return Response.status(Response.Status.CREATED).entity(planifiee).build();
        } catch (EmploiDuTempsException ex) {
            return erreur(ex.getMessage(), Response.Status.fromStatusCode(ex.getStatutHttp()));
        } catch (Exception ex) {
            return erreurInterne(ex);
        }
    }

    /**
     * Supprime une séance planifiée et libère la salle associée.
     *
     * @param id identifiant de la séance
     * @return réponse HTTP 204, 404 ou 500
     */
    @DELETE
    @Path("/{id}")
    public Response supprimer(@PathParam("id") Long id) {
        try {
            emploiDuTempsService.supprimerSeance(id);
            return Response.noContent().build();
        } catch (EmploiDuTempsException ex) {
            return erreur(ex.getMessage(), Response.Status.fromStatusCode(ex.getStatutHttp()));
        } catch (Exception ex) {
            return erreurInterne(ex);
        }
    }

    /**
     * Construit une réponse d'erreur JSON standardisée.
     *
     * @param message message d'erreur
     * @param status  statut HTTP
     * @return réponse HTTP
     */
    private Response erreur(String message, Response.Status status) {
        Map<String, Object> body = new HashMap<>();
        body.put("erreur", message);
        body.put("statut", status.getStatusCode());
        return Response.status(status).entity(body).build();
    }

    /**
     * Construit une réponse d'erreur interne 500.
     *
     * @param ex exception capturée
     * @return réponse HTTP 500
     */
    private Response erreurInterne(Exception ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("erreur", "Erreur interne du serveur");
        body.put("detail", ex.getMessage());
        body.put("statut", Response.Status.INTERNAL_SERVER_ERROR.getStatusCode());
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(body).build();
    }
}
