package ma.ump.locaux.entity;

/**
 * Énumération des états de disponibilité d'un local universitaire.
 */
public enum DisponibiliteLocal {

    /** Local libre et réservable */
    DISPONIBLE,

    /** Local actuellement occupé */
    OCCUPE,

    /** Local en maintenance, non réservable */
    EN_MAINTENANCE
}
