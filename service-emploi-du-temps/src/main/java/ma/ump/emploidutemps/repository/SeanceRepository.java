package ma.ump.emploidutemps.repository;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import ma.ump.emploidutemps.entity.JourSemaine;
import ma.ump.emploidutemps.entity.Seance;
import ma.ump.emploidutemps.util.HeureUtil;

import java.util.List;
import java.util.Optional;

/**
 * Couche d'accès aux données pour l'entité {@link Seance}.
 */
@Stateless
public class SeanceRepository {

    @PersistenceContext(unitName = "emploiPU")
    private EntityManager em;

    /**
     * Retourne toutes les séances planifiées.
     *
     * @return liste des séances
     */
    public List<Seance> findAll() {
        return em.createQuery("SELECT s FROM Seance s ORDER BY s.jour, s.heureDebut", Seance.class)
                .getResultList();
    }

    /**
     * Recherche une séance par identifiant.
     *
     * @param id identifiant de la séance
     * @return séance éventuellement trouvée
     */
    public Optional<Seance> findById(Long id) {
        return Optional.ofNullable(em.find(Seance.class, id));
    }

    /**
     * Recherche les séances pour un jour donné.
     *
     * @param jour jour de la semaine
     * @return liste filtrée
     */
    public List<Seance> findByJour(JourSemaine jour) {
        TypedQuery<Seance> query = em.createQuery(
                "SELECT s FROM Seance s WHERE s.jour = :jour ORDER BY s.heureDebut", Seance.class);
        query.setParameter("jour", jour);
        return query.getResultList();
    }

    /**
     * Persiste une nouvelle séance.
     *
     * @param seance séance à créer
     * @return séance persistée
     */
    public Seance create(Seance seance) {
        em.persist(seance);
        return seance;
    }

    /**
     * Supprime une séance.
     *
     * @param seance séance à supprimer
     */
    public void delete(Seance seance) {
        Seance managed = em.contains(seance) ? seance : em.merge(seance);
        em.remove(managed);
    }

    /**
     * Vérifie l'existence d'un conflit horaire pour une salle et un jour donnés.
     *
     * @param localId     identifiant du local
     * @param jour        jour de la semaine
     * @param heureDebut  heure de début de la nouvelle séance
     * @param heureFin    heure de fin de la nouvelle séance
     * @param excludeId   identifiant de séance à exclure (null en création)
     * @return true si un conflit existe
     */
    public boolean existeConflit(Long localId, JourSemaine jour, String heureDebut, String heureFin, Long excludeId) {
        TypedQuery<Seance> query = em.createQuery(
                "SELECT s FROM Seance s WHERE s.localId = :localId AND s.jour = :jour "
                        + "AND (:excludeId IS NULL OR s.id <> :excludeId)",
                Seance.class);
        query.setParameter("localId", localId);
        query.setParameter("jour", jour);
        query.setParameter("excludeId", excludeId);

        List<Seance> seances = query.getResultList();
        for (Seance existante : seances) {
            if (HeureUtil.seChevauchent(heureDebut, heureFin, existante.getHeureDebut(), existante.getHeureFin())) {
                return true;
            }
        }
        return false;
    }
}
