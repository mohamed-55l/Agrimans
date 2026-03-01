package modules.dashboard.services;

import modules.equipement.services.EquipementService;
import modules.review.services.ReviewService;
import modules.user.services.UserService;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Service pour les statistiques du dashboard
 *
 * RÔLE: Agréger les données des différents modules
 */
public class DashboardService {

    private EquipementService equipementService = new EquipementService();
    private ReviewService reviewService = new ReviewService();
    private UserService userService = new UserService();

    /**
     * Statistiques pour l'ADMIN
     */
    public Map<String, Object> getAdminStats() {
        Map<String, Object> stats = new HashMap<>();

        try {
            stats.put("totalEquipements", equipementService.count());
            stats.put("equipementsDisponibles", equipementService.countDisponibles());
            stats.put("equipementsEnPanne", equipementService.countEnPanne());
            stats.put("equipementsEnMaintenance", equipementService.countEnMaintenance());
            stats.put("totalReviews", reviewService.count());
            stats.put("noteMoyenne", String.format("%.1f", reviewService.getAverageNote()));
            stats.put("totalAgriculteurs", userService.countAgriculteurs());

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stats;
    }

    /**
     * Statistiques pour un AGRICULTEUR
     */
    public Map<String, Object> getUserStats(int userId) {
        Map<String, Object> stats = new HashMap<>();

        try {
            stats.put("mesEquipements", equipementService.getByUserId(userId).size());
            stats.put("mesReviews", reviewService.getByUserId(userId).size());

            // Compter les pannes personnelles
            long pannes = equipementService.getByUserId(userId).stream()
                    .filter(e -> "Non disponible".equals(e.getDisponibilite()) || "En maintenance".equals(e.getDisponibilite()))
                    .count();
            stats.put("mesPannes", pannes);

            // Valeur totale du matériel
            double valeur = equipementService.getByUserId(userId).stream()
                    .mapToDouble(e -> e.getPrix())
                    .sum();
            stats.put("valeurTotale", String.format("%.2f", valeur));

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return stats;
    }
}