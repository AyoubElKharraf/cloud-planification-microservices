package ma.ump.emploidutemps.client;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import jakarta.json.JsonValue;
import ma.ump.emploidutemps.client.dto.LocalDTO;
import ma.ump.emploidutemps.exception.EmploiDuTempsException;

import java.io.StringReader;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Client HTTP pour communiquer avec le microservice gestion des locaux.
 */
@ApplicationScoped
public class LocalClient {

    private static final int HTTP_BAD_REQUEST = 400;
    private static final int HTTP_INTERNAL_ERROR = 500;

    private HttpClient httpClient;
    private String baseUrl;

    /**
     * Initialise le client HTTP et l'URL de base du service locaux.
     */
    @PostConstruct
    public void init() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.baseUrl = System.getenv().getOrDefault("LOCAUX_BASE_URL", "http://service-locaux:8080");
    }

    /**
     * Vérifie qu'une salle est disponible (présente dans la liste des locaux disponibles).
     *
     * @param localId identifiant du local
     * @throws EmploiDuTempsException si la salle n'est pas disponible (400)
     */
    public void verifierSalleDisponible(Long localId) {
        if (localId == null) {
            throw new EmploiDuTempsException("L'identifiant du local est obligatoire", HTTP_BAD_REQUEST);
        }
        List<LocalDTO> disponibles = listerLocauxDisponibles(0);
        boolean trouve = disponibles.stream()
                .anyMatch(l -> localId.equals(l.getId()));
        if (!trouve) {
            throw new EmploiDuTempsException(
                    "La salle avec l'id " + localId + " n'est pas disponible (DISPONIBLE)",
                    HTTP_BAD_REQUEST);
        }
    }

    /**
     * Réserve une salle en passant sa disponibilité à OCCUPE.
     *
     * @param localId identifiant du local
     * @throws EmploiDuTempsException en cas d'échec de réservation
     */
    public void reserverSalle(Long localId) {
        mettreAJourDisponibilite(localId, "OCCUPE");
    }

    /**
     * Libère une salle en passant sa disponibilité à DISPONIBLE.
     *
     * @param localId identifiant du local
     * @throws EmploiDuTempsException en cas d'échec de libération
     */
    public void libererSalle(Long localId) {
        mettreAJourDisponibilite(localId, "DISPONIBLE");
    }

    /**
     * Liste les locaux disponibles avec une capacité minimale.
     *
     * @param capaciteMin capacité minimale
     * @return liste des locaux disponibles
     */
    public List<LocalDTO> listerLocauxDisponibles(int capaciteMin) {
        try {
            String url = baseUrl + "/api/locaux/disponibles?capaciteMin=" + capaciteMin;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(15))
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new EmploiDuTempsException(
                        "Erreur lors de la récupération des locaux disponibles (HTTP " + response.statusCode() + ")",
                        HTTP_INTERNAL_ERROR);
            }

            return parseLocauxListe(response.body());
        } catch (EmploiDuTempsException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new EmploiDuTempsException(
                    "Impossible de contacter le service locaux : " + ex.getMessage(),
                    HTTP_INTERNAL_ERROR);
        }
    }

    /**
     * Met à jour la disponibilité d'un local via PATCH.
     *
     * @param localId identifiant du local
     * @param valeur  nouvelle disponibilité (OCCUPE, DISPONIBLE, etc.)
     */
    private void mettreAJourDisponibilite(Long localId, String valeur) {
        if (localId == null) {
            throw new EmploiDuTempsException("L'identifiant du local est obligatoire", HTTP_BAD_REQUEST);
        }
        try {
            String encoded = URLEncoder.encode(valeur, StandardCharsets.UTF_8);
            String url = baseUrl + "/api/locaux/" + localId + "/disponibilite?valeur=" + encoded;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .method("PATCH", HttpRequest.BodyPublishers.noBody())
                    .timeout(Duration.ofSeconds(15))
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 404) {
                throw new EmploiDuTempsException("Local introuvable avec l'id : " + localId, HTTP_BAD_REQUEST);
            }
            if (response.statusCode() != 200) {
                throw new EmploiDuTempsException(
                        "Erreur lors de la mise à jour de disponibilité (HTTP " + response.statusCode() + ")",
                        HTTP_INTERNAL_ERROR);
            }
        } catch (EmploiDuTempsException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new EmploiDuTempsException(
                    "Impossible de mettre à jour la disponibilité du local : " + ex.getMessage(),
                    HTTP_INTERNAL_ERROR);
        }
    }

    /**
     * Parse un tableau JSON de locaux.
     *
     * @param json corps JSON
     * @return liste de DTO locaux
     */
    private List<LocalDTO> parseLocauxListe(String json) {
        List<LocalDTO> result = new ArrayList<>();
        try (JsonReader reader = Json.createReader(new StringReader(json))) {
            JsonArray array = reader.readArray();
            for (JsonValue value : array) {
                result.add(parseLocal(value.asJsonObject()));
            }
        }
        return result;
    }

    /**
     * Parse un objet JSON local.
     *
     * @param obj objet JSON
     * @return DTO local
     */
    private LocalDTO parseLocal(JsonObject obj) {
        LocalDTO dto = new LocalDTO();
        if (obj.containsKey("id") && !obj.isNull("id")) {
            dto.setId(obj.getJsonNumber("id").longValue());
        }
        if (obj.containsKey("code")) {
            dto.setCode(obj.getString("code", null));
        }
        if (obj.containsKey("nom")) {
            dto.setNom(obj.getString("nom", null));
        }
        if (obj.containsKey("type")) {
            dto.setType(obj.getString("type", null));
        }
        if (obj.containsKey("capacite")) {
            dto.setCapacite(obj.getInt("capacite"));
        }
        if (obj.containsKey("batiment")) {
            dto.setBatiment(obj.getString("batiment", null));
        }
        if (obj.containsKey("etage")) {
            dto.setEtage(obj.getInt("etage"));
        }
        if (obj.containsKey("projecteur")) {
            dto.setProjecteur(obj.getBoolean("projecteur"));
        }
        if (obj.containsKey("tableauNumerique")) {
            dto.setTableauNumerique(obj.getBoolean("tableauNumerique"));
        }
        if (obj.containsKey("climatisation")) {
            dto.setClimatisation(obj.getBoolean("climatisation"));
        }
        if (obj.containsKey("accessiblePMR")) {
            dto.setAccessiblePMR(obj.getBoolean("accessiblePMR"));
        }
        if (obj.containsKey("disponibilite")) {
            dto.setDisponibilite(obj.getString("disponibilite", null));
        }
        return dto;
    }
}
