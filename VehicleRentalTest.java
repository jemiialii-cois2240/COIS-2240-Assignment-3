import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;


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
    
    @Test
    public void testRentAndReturnVehicle() {
        // Arrange
        Vehicle vehicle = new Car("Toyota", "Corolla", 2019, 5);
        vehicle.setLicensePlate("AAA111");   // valid plate so it doesn't throw

        Customer customer = new Customer(1, "George");

        RentalSystem rentalSystem = RentalSystem.getInstance();

        // Vehicle should start as AVAILABLE
        assertEquals(Vehicle.VehicleStatus.Available, vehicle.getStatus());

        // ---- First rent: should SUCCEED ----
        boolean firstRent = rentalSystem.rentVehicle(
                vehicle,
                customer,
                LocalDate.now(),
                100.0
        );
        assertTrue(firstRent, "First rent should succeed");
        assertEquals(Vehicle.VehicleStatus.Rented, vehicle.getStatus());

        // ---- Second rent: should FAIL ----
        boolean secondRent = rentalSystem.rentVehicle(
                vehicle,
                customer,
                LocalDate.now(),
                100.0
        );
        assertFalse(secondRent, "Second rent of same vehicle should fail");
        assertEquals(Vehicle.VehicleStatus.Rented, vehicle.getStatus());

        // ---- First return: should SUCCEED ----
        boolean firstReturn = rentalSystem.returnVehicle(
                vehicle,
                customer,
                LocalDate.now(),
                0.0
        );
        assertTrue(firstReturn, "First return should succeed");
        assertEquals(Vehicle.VehicleStatus.Available, vehicle.getStatus());

        // ---- Second return: should FAIL ----
        boolean secondReturn = rentalSystem.returnVehicle(
                vehicle,
                customer,
                LocalDate.now(),
                0.0
        );
        assertFalse(secondReturn, "Second return of same vehicle should fail");
        assertEquals(Vehicle.VehicleStatus.Available, vehicle.getStatus());
    }

    @Test
    public void testSingletonRentalSystem() throws Exception {
        // Get the declared (no-arg) constructor of RentalSystem
        Constructor<RentalSystem> constructor =
                RentalSystem.class.getDeclaredConstructor();

        // Check that the constructor is PRIVATE (Singleton requirement)
        int modifiers = constructor.getModifiers();
        assertTrue(Modifier.isPrivate(modifiers),
                "RentalSystem constructor should be private");

        // Use getInstance() to obtain instances
        RentalSystem instance1 = RentalSystem.getInstance();
        RentalSystem instance2 = RentalSystem.getInstance();

        // getInstance() must return a non-null instance
        assertNotNull(instance1, "getInstance() should not return null");

        // And both calls must return the same instance (Singleton)
        assertSame(instance1, instance2,
                "getInstance() should always return the same instance");
    }

    
}
