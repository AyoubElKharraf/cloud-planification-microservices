package ma.ump.catalogue.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import ma.ump.catalogue.entity.Cours;

import java.util.List;
import java.util.Optional;

/**
 * Couche d'accès aux données pour l'entité {@link Cours}.
 */
@Stateless
public class CoursRepository {

    @PersistenceContext(unitName = "cataloguePU")
    private EntityManager em;

    /**
     * Retourne la liste de tous les cours.
     *
     * @return liste des cours
     */
    public List<Cours> findAll() {
        return em.createQuery("SELECT c FROM Cours c ORDER BY c.code", Cours.class)
                .getResultList();
    }

    /**
     * Recherche un cours par identifiant.
     *
     * @param id identifiant du cours
     * @return cours éventuellement trouvé
     */
    public Optional<Cours> findById(Long id) {
        return Optional.ofNullable(em.find(Cours.class, id));
    }

    /**
     * Recherche un cours par code unique.
     *
     * @param code code du cours
     * @return cours éventuellement trouvé
     */
    public Optional<Cours> findByCode(String code) {
        try {
            TypedQuery<Cours> query = em.createQuery(
                    "SELECT c FROM Cours c WHERE c.code = :code", Cours.class);
            query.setParameter("code", code);
            return Optional.of(query.getSingleResult());
        } catch (NoResultException ex) {
            return Optional.empty();
        }
    }

    /**
     * Persiste un nouveau cours.
     *
     * @param cours cours à créer
     * @return cours persisté
     */
    public Cours create(Cours cours) {
        em.persist(cours);
        return cours;
    }

    /**
     * Met à jour un cours existant.
     *
     * @param cours cours à mettre à jour
     * @return cours fusionné
     */
    public Cours update(Cours cours) {
        return em.merge(cours);
    }

    /**
     * Supprime un cours par identifiant.
     *
     * @param id identifiant du cours
     * @return true si supprimé, false si introuvable
     */
    public boolean delete(Long id) {
        Cours cours = em.find(Cours.class, id);
        if (cours == null) {
            return false;
        }
        em.remove(cours);
        return true;
    }

    /**
     * Vérifie l'existence d'un code distinct de l'identifiant fourni.
     *
     * @param code code à vérifier
     * @param id   identifiant à exclure (null pour création)
     * @return true si le code existe déjà
     */
    public boolean existsByCode(String code, Long id) {
        TypedQuery<Long> query = em.createQuery(
                "SELECT COUNT(c) FROM Cours c WHERE c.code = :code AND (:id IS NULL OR c.id <> :id)",
                Long.class);
        query.setParameter("code", code);
        query.setParameter("id", id);
        return query.getSingleResult() > 0;
    }
}
