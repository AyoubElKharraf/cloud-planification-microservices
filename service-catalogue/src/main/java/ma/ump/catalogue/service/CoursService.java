package ma.ump.catalogue.service;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import ma.ump.catalogue.entity.Cours;
import ma.ump.catalogue.repository.CoursRepository;

import java.util.List;
import java.util.Optional;

/**
 * Service métier pour la gestion du catalogue des cours.
 */
@Stateless
public class CoursService {

    @Inject
    private CoursRepository coursRepository;

    /**
     * Liste tous les cours du catalogue.
     *
     * @return liste des cours
     */
    public List<Cours> listerTous() {
        return coursRepository.findAll();
    }

    /**
     * Recherche un cours par identifiant.
     *
     * @param id identifiant du cours
     * @return cours éventuellement trouvé
     */
    public Optional<Cours> trouverParId(Long id) {
        return coursRepository.findById(id);
    }

    /**
     * Recherche un cours par code.
     *
     * @param code code du cours
     * @return cours éventuellement trouvé
     */
    public Optional<Cours> trouverParCode(String code) {
        return coursRepository.findByCode(code);
    }

    /**
     * Crée un nouveau cours après validation.
     *
     * @param cours cours à créer
     * @return cours créé
     * @throws IllegalArgumentException si les données sont invalides
     */
    public Cours creer(Cours cours) {
        validerCours(cours, null);
        if (coursRepository.existsByCode(cours.getCode(), null)) {
            throw new IllegalArgumentException("Le code cours existe déjà : " + cours.getCode());
        }
        return coursRepository.create(cours);
    }

    /**
     * Met à jour un cours existant.
     *
     * @param id    identifiant du cours
     * @param cours données mises à jour
     * @return cours mis à jour
     * @throws IllegalArgumentException si les données sont invalides
     */
    public Cours mettreAJour(Long id, Cours cours) {
        Cours existant = coursRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cours introuvable avec l'id : " + id));
        validerCours(cours, id);
        if (coursRepository.existsByCode(cours.getCode(), id)) {
            throw new IllegalArgumentException("Le code cours existe déjà : " + cours.getCode());
        }
        existant.setCode(cours.getCode());
        existant.setIntitule(cours.getIntitule());
        existant.setSyllabus(cours.getSyllabus());
        existant.setPrerequis(cours.getPrerequis());
        existant.setCredits(cours.getCredits());
        return coursRepository.update(existant);
    }

    /**
     * Supprime un cours par identifiant.
     *
     * @param id identifiant du cours
     * @return true si supprimé
     */
    public boolean supprimer(Long id) {
        return coursRepository.delete(id);
    }

    /**
     * Valide les champs obligatoires d'un cours.
     *
     * @param cours cours à valider
     * @param id    identifiant pour mise à jour (null en création)
     */
    private void validerCours(Cours cours, Long id) {
        if (cours == null) {
            throw new IllegalArgumentException("Le corps de la requête est obligatoire");
        }
        if (cours.getCode() == null || cours.getCode().isBlank()) {
            throw new IllegalArgumentException("Le code du cours est obligatoire");
        }
        if (cours.getIntitule() == null || cours.getIntitule().isBlank()) {
            throw new IllegalArgumentException("L'intitulé du cours est obligatoire");
        }
        if (cours.getCredits() < 0) {
            throw new IllegalArgumentException("Le nombre de crédits doit être positif ou nul");
        }
    }
}
