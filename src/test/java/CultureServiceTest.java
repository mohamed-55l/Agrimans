

import org.example.parcelle_culture.entities.Culture;
import org.example.parcelle_culture.entities.Parcelle;
import org.example.parcelle_culture.services.CultureService;
import org.example.parcelle_culture.services.ParcelleService;
import org.junit.jupiter.api.*;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CultureServiceTest {

    private CultureService service;
    private ParcelleService parcelleService;
    private Culture testCulture;
    private int parcelleId;

    @BeforeEach
    void setup() {

        service = new CultureService();
        parcelleService = new ParcelleService();

        // 1️⃣ Créer une parcelle pour éviter erreur FK
        Parcelle p = new Parcelle(
                "ParcelleJUnit",
                15.0,
                "TestVille",
                "Argileux",
                1
        );

        parcelleService.ajouterParcelle(p);

        // 2️⃣ Récupérer la dernière parcelle insérée
        List<Parcelle> parcelles = parcelleService.afficherParcelles();
        Parcelle lastParcelle = parcelles.get(parcelles.size() - 1);

        parcelleId = lastParcelle.getIdParcelle();

        // 3️⃣ Créer la culture avec ID valide
        testCulture = new Culture(
                "JUnitTest",
                "TestType",
                LocalDate.now(),
                LocalDate.now().plusDays(5),
                "En croissance",
                parcelleId
        );
    }

    @Test
    void testAjouterCulture() {

        service.ajouterCulture(testCulture);

        List<Culture> list = service.afficherCultures();

        boolean exists = list.stream()
                .anyMatch(c -> c.getNom().equals("JUnitTest"));

        assertTrue(exists);
    }

    @Test
    void testModifierCulture() {

        service.ajouterCulture(testCulture);

        List<Culture> list = service.afficherCultures();

        Culture c = list.stream()
                .filter(cul -> cul.getNom().equals("JUnitTest"))
                .findFirst()
                .orElse(null);

        assertNotNull(c);

        c.setNom("JUnitUpdated");
        service.modifierCulture(c);

        List<Culture> updatedList = service.afficherCultures();

        boolean updated = updatedList.stream()
                .anyMatch(cul -> cul.getNom().equals("JUnitUpdated"));

        assertTrue(updated);
    }

    @Test
    void testSupprimerCulture() {

        service.ajouterCulture(testCulture);

        List<Culture> list = service.afficherCultures();

        Culture c = list.stream()
                .filter(cul -> cul.getNom().equals("JUnitTest"))
                .findFirst()
                .orElse(null);

        assertNotNull(c);

        service.supprimerCulture(c.getIdCulture());

        List<Culture> afterDelete = service.afficherCultures();

        boolean exists = afterDelete.stream()
                .anyMatch(cul -> cul.getIdCulture() == c.getIdCulture());

        assertFalse(exists);
    }

    @Test
    void testAfficherCultures() {
        List<Culture> list = service.afficherCultures();
        assertNotNull(list);
    }
}
