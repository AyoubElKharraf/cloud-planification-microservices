package ma.ump.locaux.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import ma.ump.locaux.entity.DisponibiliteLocal;
import ma.ump.locaux.entity.Local;
import ma.ump.locaux.entity.TypeLocal;

import java.util.List;
import java.util.Optional;

/**
 * Couche d'accès aux données pour l'entité {@link Local}.
 */
@Stateless
public class LocalRepository {

    @PersistenceContext(unitName = "locauxPU")
    private EntityManager em;

    /**
     * Retourne tous les locaux enregistrés.
     *
     * @return liste des locaux
     */
    public List<Local> findAll() {
        return em.createQuery("SELECT l FROM Local l ORDER BY l.code", Local.class)
                .getResultList();
    }

    /**
     * Recherche un local par identifiant.
     *
     * @param id identifiant du local
     * @return local éventuellement trouvé
     */
    public Optional<Local> findById(Long id) {
        return Optional.ofNullable(em.find(Local.class, id));
    }

    /**
     * Recherche les locaux disponibles avec une capacité minimale.
     *
     * @param capaciteMin capacité minimale requise
     * @return liste des locaux disponibles
     */
    public List<Local> findDisponibles(int capaciteMin) {
        TypedQuery<Local> query = em.createQuery(
                "SELECT l FROM Local l WHERE l.disponibilite = :dispo AND l.capacite >= :cap ORDER BY l.capacite",
                Local.class);
        query.setParameter("dispo", DisponibiliteLocal.DISPONIBLE);
        query.setParameter("cap", capaciteMin);
        return query.getResultList();
    }

    /**
     * Recherche les locaux par type.
     *
     * @param type type de local
     * @return liste filtrée
     */
    public List<Local> findByType(TypeLocal type) {
        TypedQuery<Local> query = em.createQuery(
                "SELECT l FROM Local l WHERE l.type = :type ORDER BY l.code", Local.class);
        query.setParameter("type", type);
        return query.getResultList();
    }

    /**
     * Persiste un nouveau local.
     *
     * @param local local à créer
     * @return local persisté
     */
    public Local create(Local local) {
        em.persist(local);
        return local;
    }

    /**
     * Met à jour un local existant.
     *
     * @param local local à fusionner
     * @return local mis à jour
     */
    public Local update(Local local) {
        return em.merge(local);
    }

    /**
     * Supprime un local par identifiant.
     *
     * @param id identifiant du local
     * @return true si supprimé
     */
    public boolean delete(Long id) {
        Local local = em.find(Local.class, id);
        if (local == null) {
            return false;
        }
        em.remove(local);
        return true;
    }

    /**
     * Vérifie l'unicité du code hors identifiant donné.
     *
     * @param code code à vérifier
     * @param id   identifiant à exclure
     * @return true si le code existe déjà
     */
    public boolean existsByCode(String code, Long id) {
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(l) FROM Local l WHERE l.code = :code AND (:id IS NULL OR l.id <> :id)",
                Long.class);
        query.setParameter("code", code);
        query.setParameter("id", id);
        return query.getSingleResult() > 0;
    }
}
