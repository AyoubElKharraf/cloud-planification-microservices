package ma.ump.locaux.rest;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import ma.ump.locaux.entity.DisponibiliteLocal;
import ma.ump.locaux.entity.Local;
import ma.ump.locaux.entity.TypeLocal;
import ma.ump.locaux.service.LocalService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ressource REST exposant les opérations sur les locaux universitaires.
 */
@RequestScoped
@Path("/locaux")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class LocalResource {

    @Inject
    private LocalService localService;

    /**
     * Liste tous les locaux.
     *
     * @return réponse HTTP 200
     */
    @GET
    public Response listerTous() {
        try {
            List<Local> locaux = localService.listerTous();
            return Response.ok(locaux).build();
        } catch (Exception ex) {
            return erreurInterne(ex);
        }
    }

    /**
     * Recherche un local par identifiant.
     *
     * @param id identifiant du local
     * @return réponse HTTP 200 ou 404
     */
    @GET
    @Path("/{id}")
    public Response trouverParId(@PathParam("id") Long id) {
        try {
            return localService.trouverParId(id)
                    .map(local -> Response.ok(local).build())
                    .orElseGet(() -> erreur("Local introuvable avec l'id : " + id, Response.Status.NOT_FOUND));
        } catch (Exception ex) {
            return erreurInterne(ex);
        }
    }

    /**
     * Liste les locaux disponibles avec capacité minimale.
     *
     * @param capaciteMin capacité minimale (défaut 0)
     * @return réponse HTTP 200
     */
    @GET
    @Path("/disponibles")
    public Response listerDisponibles(@QueryParam("capaciteMin") @DefaultValue("0") int capaciteMin) {
        try {
            List<Local> locaux = localService.listerDisponibles(capaciteMin);
            return Response.ok(locaux).build();
        } catch (IllegalArgumentException ex) {
            return erreur(ex.getMessage(), Response.Status.BAD_REQUEST);
        } catch (Exception ex) {
            return erreurInterne(ex);
        }
    }

    /**
     * Liste les locaux filtrés par type.
     *
     * @param type type de local
     * @return réponse HTTP 200 ou 400
     */
    @GET
    @Path("/type/{type}")
    public Response listerParType(@PathParam("type") String type) {
        try {
            TypeLocal typeLocal = TypeLocal.valueOf(type.toUpperCase());
            List<Local> locaux = localService.listerParType(typeLocal);
            return Response.ok(locaux).build();
        } catch (IllegalArgumentException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("No enum constant")) {
                return erreur("Type de local invalide : " + type, Response.Status.BAD_REQUEST);
            }
            return erreur(ex.getMessage(), Response.Status.BAD_REQUEST);
        } catch (Exception ex) {
            return erreurInterne(ex);
        }
    }

    /**
     * Crée un nouveau local.
     *
     * @param local données du local
     * @return réponse HTTP 201 ou 400
     */
    @POST
    public Response creer(Local local) {
        try {
            Local cree = localService.creer(local);
            return Response.status(Response.Status.CREATED).entity(cree).build();
        } catch (IllegalArgumentException ex) {
            return erreur(ex.getMessage(), Response.Status.BAD_REQUEST);
        } catch (Exception ex) {
            return erreurInterne(ex);
        }
    }

    /**
     * Met à jour un local existant.
     *
     * @param id    identifiant du local
     * @param local données mises à jour
     * @return réponse HTTP 200, 400 ou 404
     */
    @PUT
    @Path("/{id}")
    public Response mettreAJour(@PathParam("id") Long id, Local local) {
        try {
            Local misAJour = localService.mettreAJour(id, local);
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
     * Met à jour la disponibilité d'un local.
     *
     * @param id     identifiant du local
     * @param valeur nouvelle disponibilité
     * @return réponse HTTP 200, 400 ou 404
     */
    @PATCH
    @Path("/{id}/disponibilite")
    public Response mettreAJourDisponibilite(@PathParam("id") Long id,
                                             @QueryParam("valeur") String valeur) {
        try {
            if (valeur == null || valeur.isBlank()) {
                return erreur("Le paramètre valeur est obligatoire", Response.Status.BAD_REQUEST);
            }
            DisponibiliteLocal dispo = DisponibiliteLocal.valueOf(valeur.toUpperCase());
            Local misAJour = localService.mettreAJourDisponibilite(id, dispo);
            return Response.ok(misAJour).build();
        } catch (IllegalArgumentException ex) {
            if (ex.getMessage() != null && ex.getMessage().contains("introuvable")) {
                return erreur(ex.getMessage(), Response.Status.NOT_FOUND);
            }
            if (ex.getMessage() != null && ex.getMessage().contains("No enum constant")) {
                return erreur("Valeur de disponibilité invalide : " + valeur, Response.Status.BAD_REQUEST);
            }
            return erreur(ex.getMessage(), Response.Status.BAD_REQUEST);
        } catch (Exception ex) {
            return erreurInterne(ex);
        }
    }

    /**
     * Supprime un local par identifiant.
     *
     * @param id identifiant du local
     * @return réponse HTTP 204 ou 404
     */
    @DELETE
    @Path("/{id}")
    public Response supprimer(@PathParam("id") Long id) {
        try {
            if (localService.supprimer(id)) {
                return Response.noContent().build();
            }
            return erreur("Local introuvable avec l'id : " + id, Response.Status.NOT_FOUND);
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
