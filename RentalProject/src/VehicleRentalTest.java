import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class VehicleRentalTest {

    @Test
    public void testLicensePlate() {
        // use Car as a concrete Vehicle type
        Vehicle vehicle = new Car("Toyota", "Corolla", 2019, 5);

        // ---- valid plates ----
        assertDoesNotThrow(() -> vehicle.setLicensePlate("AAA100"));
        assertEquals("AAA100", vehicle.getLicensePlate());

        assertDoesNotThrow(() -> vehicle.setLicensePlate("ABC567"));
        assertEquals("ABC567", vehicle.getLicensePlate());

        assertDoesNotThrow(() -> vehicle.setLicensePlate("ZZZ999"));
        assertEquals("ZZZ999", vehicle.getLicensePlate());

        // ---- invalid plates ----
        assertThrows(IllegalArgumentException.class,
                () -> vehicle.setLicensePlate(""));       // empty

        assertThrows(IllegalArgumentException.class,
                () -> vehicle.setLicensePlate(null));     // null

        assertThrows(IllegalArgumentException.class,
                () -> vehicle.setLicensePlate("AAA1000"));// too many digits

        assertThrows(IllegalArgumentException.class,
                () -> vehicle.setLicensePlate("ZZZ99"));  // too few digits

        // extra assertion using true/false as required
        assertTrue(vehicle.getLicensePlate().matches("[A-Z]{3}[0-9]{3}"));
    }
}
