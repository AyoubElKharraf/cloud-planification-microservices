package ma.ump.emploidutemps.service;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import ma.ump.emploidutemps.client.CatalogueClient;
import ma.ump.emploidutemps.client.LocalClient;
import ma.ump.emploidutemps.entity.JourSemaine;
import ma.ump.emploidutemps.entity.Seance;
import ma.ump.emploidutemps.exception.EmploiDuTempsException;
import ma.ump.emploidutemps.repository.SeanceRepository;
import ma.ump.emploidutemps.util.HeureUtil;

import java.util.List;
import java.util.Optional;

/**
 * Service métier de planification et gestion de l'emploi du temps.
 */
@Stateless
public class EmploiDuTempsService {

    private static final int HTTP_BAD_REQUEST = 400;
    private static final int HTTP_NOT_FOUND = 404;
    private static final int HTTP_CONFLICT = 409;

    @Inject
    private SeanceRepository seanceRepository;

    @Inject
    private CatalogueClient catalogueClient;

    @Inject
    private LocalClient localClient;

    /**
     * Liste toutes les séances planifiées.
     *
     * @return liste des séances
     */
    public List<Seance> listerToutes() {
        return seanceRepository.findAll();
    }

    /**
     * Recherche une séance par identifiant.
     *
     * @param id identifiant de la séance
     * @return séance éventuellement trouvée
     */
    public Optional<Seance> trouverParId(Long id) {
        return seanceRepository.findById(id);
    }

    /**
     * Liste les séances pour un jour donné.
     *
     * @param jour jour de la semaine
     * @return séances du jour
     */
    public List<Seance> listerParJour(JourSemaine jour) {
        if (jour == null) {
            throw new EmploiDuTempsException("Le jour est obligatoire", HTTP_BAD_REQUEST);
        }
        return seanceRepository.findByJour(jour);
    }

    /**
     * Planifie une nouvelle séance selon la logique métier ordonnée :
     * 1) vérifier cours, 2) vérifier salle disponible, 3) détecter conflit,
     * 4) réserver salle, 5) persister séance.
     *
     * @param seance données de la séance à planifier
     * @return séance créée
     */
    public Seance planifierSeance(Seance seance) {
        validerSeance(seance);

        // Étape 1 : vérifier que le cours existe
        catalogueClient.verifierCoursExiste(seance.getCoursId());

        // Étape 2 : vérifier que la salle est DISPONIBLE
        localClient.verifierSalleDisponible(seance.getLocalId());

        // Étape 3 : vérifier l'absence de conflit horaire sur la même salle et le même jour
        if (seanceRepository.existeConflit(
                seance.getLocalId(),
                seance.getJour(),
                seance.getHeureDebut(),
                seance.getHeureFin(),
                null)) {
            throw new EmploiDuTempsException(
                    "Conflit de planification : la salle est déjà occupée sur ce créneau horaire",
                    HTTP_CONFLICT);
        }

        // Étape 4 : réserver la salle (OCCUPE)
        localClient.reserverSalle(seance.getLocalId());

        // Étape 5 : persister la séance
        try {
            return seanceRepository.create(seance);
        } catch (RuntimeException ex) {
            try {
                localClient.libererSalle(seance.getLocalId());
            } catch (Exception rollbackEx) {
                // journalisation implicite via message d'erreur enrichi
            }
            throw new EmploiDuTempsException(
                    "Erreur lors de la persistance de la séance : " + ex.getMessage(),
                    500);
        }
    }

    /**
     * Supprime une séance planifiée :
     * 1) trouver la séance, 2) libérer la salle, 3) supprimer en base.
     *
     * @param id identifiant de la séance
     */
    public void supprimerSeance(Long id) {
        // Étape 1 : trouver la séance
        Seance seance = seanceRepository.findById(id)
                .orElseThrow(() -> new EmploiDuTempsException(
                        "Séance introuvable avec l'id : " + id,
                        HTTP_NOT_FOUND));

        // Étape 2 : libérer la salle
        localClient.libererSalle(seance.getLocalId());

        // Étape 3 : supprimer la séance
        seanceRepository.delete(seance);
    }

    /**
     * Valide les champs obligatoires d'une séance.
     *
     * @param seance séance à valider
     */
    private void validerSeance(Seance seance) {
        if (seance == null) {
            throw new EmploiDuTempsException("Le corps de la requête est obligatoire", HTTP_BAD_REQUEST);
        }
        if (seance.getCoursId() == null) {
            throw new EmploiDuTempsException("L'identifiant du cours est obligatoire", HTTP_BAD_REQUEST);
        }
        if (seance.getLocalId() == null) {
            throw new EmploiDuTempsException("L'identifiant du local est obligatoire", HTTP_BAD_REQUEST);
        }
        if (seance.getJour() == null) {
            throw new EmploiDuTempsException("Le jour de la séance est obligatoire", HTTP_BAD_REQUEST);
        }
        if (seance.getHeureDebut() == null || seance.getHeureDebut().isBlank()) {
            throw new EmploiDuTempsException("L'heure de début est obligatoire", HTTP_BAD_REQUEST);
        }
        if (seance.getHeureFin() == null || seance.getHeureFin().isBlank()) {
            throw new EmploiDuTempsException("L'heure de fin est obligatoire", HTTP_BAD_REQUEST);
        }
        if (!HeureUtil.intervalleValide(seance.getHeureDebut(), seance.getHeureFin())) {
            throw new EmploiDuTempsException(
                    "L'heure de fin doit être postérieure à l'heure de début",
                    HTTP_BAD_REQUEST);
        }
        if (seance.getSemestre() == null || seance.getSemestre().isBlank()) {
            throw new EmploiDuTempsException("Le semestre est obligatoire", HTTP_BAD_REQUEST);
        }
        if (seance.getType() == null) {
            throw new EmploiDuTempsException("Le type de séance est obligatoire", HTTP_BAD_REQUEST);
        }
    }
}
