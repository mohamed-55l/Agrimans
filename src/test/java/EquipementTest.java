import models.Equipement;
import services.EquipementService;
import org.junit.jupiter.api.Test;

public class EquipementTest {

    @Test
    public void testCreate() {

        EquipementService es =
                new EquipementService();

        Equipement e =
                new Equipement(
                        0,
                        "Test Machine",
                        "Test",
                        100,
                        "Disponible"
                );

        try {
            es.create(e);
            System.out.println("Test OK");
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }
}
