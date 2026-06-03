package ma.ump.emploidutemps.client;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.json.JsonReader;
import ma.ump.emploidutemps.client.dto.CoursDTO;
import ma.ump.emploidutemps.exception.EmploiDuTempsException;

import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

/**
 * Client HTTP pour communiquer avec le microservice catalogue des cours.
 */
@ApplicationScoped
public class CatalogueClient {

    private static final int HTTP_BAD_REQUEST = 400;
    private static final int HTTP_INTERNAL_ERROR = 500;

    private HttpClient httpClient;
    private String baseUrl;

    /**
     * Initialise le client HTTP et l'URL de base du service catalogue.
     */
    @PostConstruct
    public void init() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.baseUrl = System.getenv().getOrDefault("CATALOGUE_BASE_URL", "http://service-catalogue:8080");
    }

    /**
     * Vérifie qu'un cours existe dans le catalogue.
     *
     * @param coursId identifiant du cours
     * @return DTO du cours trouvé
     * @throws EmploiDuTempsException si le cours n'existe pas (400) ou erreur technique (500)
     */
    public CoursDTO verifierCoursExiste(Long coursId) {
        if (coursId == null) {
            throw new EmploiDuTempsException("L'identifiant du cours est obligatoire", HTTP_BAD_REQUEST);
        }
        try {
            String url = baseUrl + "/api/cours/" + coursId;
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .timeout(Duration.ofSeconds(15))
                    .header("Accept", "application/json")
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 404) {
                throw new EmploiDuTempsException("Le cours avec l'id " + coursId + " n'existe pas", HTTP_BAD_REQUEST);
            }
            if (response.statusCode() != 200) {
                throw new EmploiDuTempsException(
                        "Erreur lors de la vérification du cours (HTTP " + response.statusCode() + ")",
                        HTTP_INTERNAL_ERROR);
            }

            return parseCours(response.body());
        } catch (EmploiDuTempsException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new EmploiDuTempsException(
                    "Impossible de contacter le service catalogue : " + ex.getMessage(),
                    HTTP_INTERNAL_ERROR);
        }
    }

    /**
     * Recherche optionnelle d'un cours par identifiant.
     *
     * @param coursId identifiant du cours
     * @return cours éventuellement trouvé
     */
    public Optional<CoursDTO> trouverCours(Long coursId) {
        try {
            return Optional.of(verifierCoursExiste(coursId));
        } catch (EmploiDuTempsException ex) {
            if (ex.getStatutHttp() == HTTP_BAD_REQUEST) {
                return Optional.empty();
            }
            throw ex;
        }
    }

    /**
     * Parse la réponse JSON d'un cours.
     *
     * @param json corps JSON
     * @return DTO cours
     */
    private CoursDTO parseCours(String json) {
        try (JsonReader reader = Json.createReader(new StringReader(json))) {
            JsonObject obj = reader.readObject();
            CoursDTO dto = new CoursDTO();
            if (obj.containsKey("id") && !obj.isNull("id")) {
                dto.setId(obj.getJsonNumber("id").longValue());
            }
            if (obj.containsKey("code")) {
                dto.setCode(obj.getString("code", null));
            }
            if (obj.containsKey("intitule")) {
                dto.setIntitule(obj.getString("intitule", null));
            }
            if (obj.containsKey("syllabus")) {
                dto.setSyllabus(obj.getString("syllabus", null));
            }
            if (obj.containsKey("prerequis")) {
                dto.setPrerequis(obj.getString("prerequis", null));
            }
            if (obj.containsKey("credits")) {
                dto.setCredits(obj.getInt("credits"));
            }
            return dto;
        }
    }
}
