package ma.ump.emploidutemps.util;

/**
 * Utilitaire de manipulation et comparaison des horaires au format HH:mm.
 */
public final class HeureUtil {

    private HeureUtil() {
    }

    /**
     * Convertit une heure HH:mm en minutes depuis minuit.
     *
     * @param heure heure au format HH:mm
     * @return nombre de minutes
     * @throws IllegalArgumentException si le format est invalide
     */
    public static int enMinutes(String heure) {
        if (heure == null || !heure.matches("^([01]\\d|2[0-3]):[0-5]\\d$")) {
            throw new IllegalArgumentException("Format d'heure invalide (attendu HH:mm) : " + heure);
        }
        String[] parties = heure.split(":");
        int h = Integer.parseInt(parties[0]);
        int m = Integer.parseInt(parties[1]);
        return h * 60 + m;
    }

    /**
     * Vérifie que l'heure de début est strictement antérieure à l'heure de fin.
     *
     * @param heureDebut heure de début
     * @param heureFin   heure de fin
     * @return true si l'intervalle est valide
     */
    public static boolean intervalleValide(String heureDebut, String heureFin) {
        return enMinutes(heureDebut) < enMinutes(heureFin);
    }

    /**
     * Détermine si deux intervalles horaires se chevauchent.
     *
     * @param debut1 début intervalle 1
     * @param fin1   fin intervalle 1
     * @param debut2 début intervalle 2
     * @param fin2   fin intervalle 2
     * @return true en cas de chevauchement
     */
    public static boolean seChevauchent(String debut1, String fin1, String debut2, String fin2) {
        int d1 = enMinutes(debut1);
        int f1 = enMinutes(fin1);
        int d2 = enMinutes(debut2);
        int f2 = enMinutes(fin2);
        return d1 < f2 && d2 < f1;
    }
}
