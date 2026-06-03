package ma.ump.catalogue.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ma.ump.catalogue.entity.Cours;
import ma.ump.catalogue.service.CoursService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ressource REST exposant les opérations CRUD sur le catalogue des cours.
 */
@RequestScoped
@Path("/cours")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CoursResource {

    @Inject
    private CoursService coursService;

    /**
     * Liste tous les cours du catalogue.
     *
     * @return réponse HTTP 200 avec la liste
     */
    @GET
    public Response listerTous() {
        try {
            List<Cours> cours = coursService.listerTous();
            return Response.ok(cours).build();
        } catch (Exception ex) {
            return erreurInterne(ex);
        }
    }

    /**
     * Recherche un cours par identifiant.
     *
     * @param id identifiant du cours
     * @return réponse HTTP 200 ou 404
     */
    @GET
    @Path("/{id}")
    public Response trouverParId(@PathParam("id") Long id) {
        try {
            return coursService.trouverParId(id)
                    .map(cours -> Response.ok(cours).build())
                    .orElseGet(() -> erreur("Cours introuvable", Response.Status.NOT_FOUND));
        } catch (Exception ex) {
            return erreurInterne(ex);
        }
    }

    /**
     * Recherche un cours par code unique.
     *
     * @param code code du cours
     * @return réponse HTTP 200 ou 404
     */
    @GET
    @Path("/code/{code}")
    public Response trouverParCode(@PathParam("code") String code) {
        try {
            return coursService.trouverParCode(code)
                    .map(cours -> Response.ok(cours).build())
                    .orElseGet(() -> erreur("Cours introuvable pour le code : " + code, Response.Status.NOT_FOUND));
        } catch (Exception ex) {
            return erreurInterne(ex);
        }
    }

    /**
     * Crée un nouveau cours.
     *
     * @param cours données du cours
     * @return réponse HTTP 201 ou 400
     */
    @POST
    public Response creer(Cours cours) {
        try {
            Cours cree = coursService.creer(cours);
            return Response.status(Response.Status.CREATED).entity(cree).build();
        } catch (IllegalArgumentException ex) {
            return erreur(ex.getMessage(), Response.Status.BAD_REQUEST);
        } catch (Exception ex) {
            return erreurInterne(ex);
        }
    }

    /**
     * Met à jour un cours existant.
     *
     * @param id    identifiant du cours
     * @param cours données mises à jour
     * @return réponse HTTP 200, 400 ou 404
     */
    @PUT
    @Path("/{id}")
    public Response mettreAJour(@PathParam("id") Long id, Cours cours) {
        try {
            Cours misAJour = coursService.mettreAJour(id, cours);
            return Response.ok(misAJour).build();
        } catch (IllegalArgumentException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("introuvable")) {
                return erreur(ex.getMessage(), Response.Status.NOT_FOUND);
            }
            return erreur(ex.getMessage(), Response.Status.BAD_REQUEST);
        } catch (Exception ex) {
            return erreurInterne(ex);
        }
    }

    /**
     * Supprime un cours par identifiant.
     *
     * @param id identifiant du cours
     * @return réponse HTTP 204 ou 404
     */
    @DELETE
    @Path("/{id}")
    public Response supprimer(@PathParam("id") Long id) {
        try {
            if (coursService.supprimer(id)) {
                return Response.noContent().build();
            }
            return erreur("Cours introuvable avec l'id : " + id, Response.Status.NOT_FOUND);
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
