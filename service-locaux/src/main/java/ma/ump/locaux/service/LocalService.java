package ma.ump.locaux.service;

import jakarta.ejb.Stateless;
import jakarta.inject.Inject;
import ma.ump.locaux.entity.DisponibiliteLocal;
import ma.ump.locaux.entity.Local;
import ma.ump.locaux.entity.TypeLocal;
import ma.ump.locaux.repository.LocalRepository;

import java.util.List;
import java.util.Optional;

/**
 * Service métier pour la gestion des locaux universitaires.
 */
@Stateless
public class LocalService {

    @Inject
    private LocalRepository localRepository;

    /**
     * Liste tous les locaux.
     *
     * @return liste complète
     */
    public List<Local> listerTous() {
        return localRepository.findAll();
    }

    /**
     * Recherche un local par identifiant.
     *
     * @param id identifiant du local
     * @return local éventuellement trouvé
     */
    public Optional<Local> trouverParId(Long id) {
        return localRepository.findById(id);
    }

    /**
     * Liste les locaux disponibles filtrés par capacité minimale.
     *
     * @param capaciteMin capacité minimale
     * @return locaux disponibles
     */
    public List<Local> listerDisponibles(int capaciteMin) {
        if (capaciteMin < 0) {
            throw new IllegalArgumentException("La capacité minimale doit être positive ou nulle");
        }
        return localRepository.findDisponibles(capaciteMin);
    }

    /**
     * Liste les locaux par type.
     *
     * @param type type de local
     * @return liste filtrée
     */
    public List<Local> listerParType(TypeLocal type) {
        if (type == null) {
            throw new IllegalArgumentException("Le type de local est obligatoire");
        }
        return localRepository.findByType(type);
    }

    /**
     * Crée un nouveau local.
     *
     * @param local données du local
     * @return local créé
     */
    public Local creer(Local local) {
        validerLocal(local, null);
        if (localRepository.existsByCode(local.getCode(), null)) {
            throw new IllegalArgumentException("Le code local existe déjà : " + local.getCode());
        }
        if (local.getDisponibilite() == null) {
            local.setDisponibilite(DisponibiliteLocal.DISPONIBLE);
        }
        return localRepository.create(local);
    }

    /**
     * Met à jour un local existant.
     *
     * @param id    identifiant du local
     * @param local données mises à jour
     * @return local mis à jour
     */
    public Local mettreAJour(Long id, Local local) {
        Local existant = localRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Local introuvable avec l'id : " + id));
        validerLocal(local, id);
        if (localRepository.existsByCode(local.getCode(), id)) {
            throw new IllegalArgumentException("Le code local existe déjà : " + local.getCode());
        }
        existant.setCode(local.getCode());
        existant.setNom(local.getNom());
        existant.setType(local.getType());
        existant.setCapacite(local.getCapacite());
        existant.setBatiment(local.getBatiment());
        existant.setEtage(local.getEtage());
        existant.setProjecteur(local.isProjecteur());
        existant.setTableauNumerique(local.isTableauNumerique());
        existant.setClimatisation(local.isClimatisation());
        existant.setAccessiblePMR(local.isAccessiblePMR());
        if (local.getDisponibilite() != null) {
            existant.setDisponibilite(local.getDisponibilite());
        }
        return localRepository.update(existant);
    }

    /**
     * Met à jour uniquement la disponibilité d'un local.
     *
     * @param id     identifiant du local
     * @param valeur nouvelle disponibilité
     * @return local mis à jour
     */
    public Local mettreAJourDisponibilite(Long id, DisponibiliteLocal valeur) {
        if (valeur == null) {
            throw new IllegalArgumentException("La valeur de disponibilité est obligatoire");
        }
        Local existant = localRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Local introuvable avec l'id : " + id));
        existant.setDisponibilite(valeur);
        return localRepository.update(existant);
    }

    /**
     * Supprime un local par identifiant.
     *
     * @param id identifiant du local
     * @return true si supprimé
     */
    public boolean supprimer(Long id) {
        return localRepository.delete(id);
    }

    /**
     * Valide les champs obligatoires d'un local.
     *
     * @param local local à valider
     * @param id    identifiant pour mise à jour
     */
    private void validerLocal(Local local, Long id) {
        if (local == null) {
            throw new IllegalArgumentException("Le corps de la requête est obligatoire");
        }
        if (local.getCode() == null || local.getCode().isBlank()) {
            throw new IllegalArgumentException("Le code du local est obligatoire");
        }
        if (local.getNom() == null || local.getNom().isBlank()) {
            throw new IllegalArgumentException("Le nom du local est obligatoire");
        }
        if (local.getType() == null) {
            throw new IllegalArgumentException("Le type du local est obligatoire");
        }
        if (local.getCapacite() <= 0) {
            throw new IllegalArgumentException("La capacité doit être strictement positive");
        }
    }
}
