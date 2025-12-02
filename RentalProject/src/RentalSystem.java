import java.util.List;
import java.time.LocalDate;
import java.util.ArrayList;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;


public class RentalSystem {
	
	//Singleton instance
	private static RentalSystem instance;
	
	//Existing fields
    private List<Vehicle> vehicles = new ArrayList<>();
    private List<Customer> customers = new ArrayList<>();
    private RentalHistory rentalHistory = new RentalHistory();
    
    //File names:
    private static final String VEHICLES_FILE = "vehicles.txt";
    private static final String CUSTOMERS_FILE = "customers.txt";
    private static final String RECORDS_FILE = "rental_records.txt";
    
 // Singleton constructor
    private RentalSystem() {
    	  loadData();  
    }

    public static RentalSystem getInstance() {
        if (instance == null) {
            instance = new RentalSystem();
        }
        return instance;
    }
    


    public boolean addVehicle(Vehicle vehicle) {
        // Check for duplicate license plate
        if (findVehicleByPlate(vehicle.getLicensePlate()) != null) {
            System.out.println("A vehicle with this license plate already exists.");
            return false;
        }

        vehicles.add(vehicle);
        saveVehicle(vehicle);
        return true;
    }

    public boolean addCustomer(Customer customer) {
        // Check for duplicate customer ID
        if (findCustomerById(customer.getCustomerId()) != null) {
            System.out.println("A customer with this ID already exists.");
            return false;
        }

        customers.add(customer);
        saveCustomer(customer);
        return true;
    }

    public boolean rentVehicle(Vehicle vehicle, Customer customer, LocalDate date, double amount) {
        if (vehicle.getStatus() == Vehicle.VehicleStatus.Available) {   // adjust enum name if needed
            vehicle.setStatus(Vehicle.VehicleStatus.Rented);
            rentalHistory.addRecord(new RentalRecord(vehicle, customer, date, amount, "RENT"));
            System.out.println("Vehicle rented to " + customer.getCustomerName());
            return true;
        } else {
            System.out.println("Vehicle is not available for renting.");
            return false;
        }
    }

    public boolean returnVehicle(Vehicle vehicle, Customer customer, LocalDate date, double extraFees) {
        if (vehicle.getStatus() == Vehicle.VehicleStatus.Rented) {
            vehicle.setStatus(Vehicle.VehicleStatus.Available);
            rentalHistory.addRecord(new RentalRecord(vehicle, customer, date, extraFees, "RETURN"));
            System.out.println("Vehicle returned by " + customer.getCustomerName());
            return true;
        } else {
            System.out.println("Vehicle is not rented.");
            return false;
        }
    }

    public void displayVehicles(Vehicle.VehicleStatus status) {
        // Display appropriate title based on status
        if (status == null) {
            System.out.println("\n=== All Vehicles ===");
        } else {
            System.out.println("\n=== " + status + " Vehicles ===");
        }
        
        // Header with proper column widths
        System.out.printf("|%-16s | %-12s | %-12s | %-12s | %-6s | %-18s |%n", 
            " Type", "Plate", "Make", "Model", "Year", "Status");
        System.out.println("|--------------------------------------------------------------------------------------------|");
    	  
        boolean found = false;
        for (Vehicle vehicle : vehicles) {
            if (status == null || vehicle.getStatus() == status) {
                found = true;
                String vehicleType;
                if (vehicle instanceof Car) {
                    vehicleType = "Car";
                } else if (vehicle instanceof Minibus) {
                    vehicleType = "Minibus";
                } else if (vehicle instanceof PickupTruck) {
                    vehicleType = "Pickup Truck";
                } else {
                    vehicleType = "Unknown";
                }
                System.out.printf("| %-15s | %-12s | %-12s | %-12s | %-6d | %-18s |%n", 
                    vehicleType, vehicle.getLicensePlate(), vehicle.getMake(), vehicle.getModel(), vehicle.getYear(), vehicle.getStatus().toString());
            }
        }
        if (!found) {
            if (status == null) {
                System.out.println("  No Vehicles found.");
            } else {
                System.out.println("  No vehicles with Status: " + status);
            }
        }
        System.out.println();
    }

    public void displayAllCustomers() {
        for (Customer c : customers) {
            System.out.println("  " + c.toString());
        }
    }
    
    public void displayRentalHistory() {
        if (rentalHistory.getRentalHistory().isEmpty()) {
            System.out.println("  No rental history found.");
        } else {
            // Header with proper column widths
            System.out.printf("|%-10s | %-12s | %-20s | %-12s | %-12s |%n", 
                " Type", "Plate", "Customer", "Date", "Amount");
            System.out.println("|-------------------------------------------------------------------------------|");
            
            for (RentalRecord record : rentalHistory.getRentalHistory()) {                
                System.out.printf("| %-9s | %-12s | %-20s | %-12s | $%-11.2f |%n", 
                    record.getRecordType(), 
                    record.getVehicle().getLicensePlate(),
                    record.getCustomer().getCustomerName(),
                    record.getRecordDate().toString(),
                    record.getTotalAmount()
                );
            }
            System.out.println();
        }
    }
    
    public Vehicle findVehicleByPlate(String plate) {
        for (Vehicle v : vehicles) {
            if (v.getLicensePlate().equalsIgnoreCase(plate)) {
                return v;
            }
        }
        return null;
    }
    
    public Customer findCustomerById(int id) {
        for (Customer c : customers)
            if (c.getCustomerId() == id)
                return c;
        return null;
    }
    
   
    
 // Append a vehicle to vehicles.txt
    public void saveVehicle(Vehicle vehicle) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(
                new FileWriter(VEHICLES_FILE, true)))) {

            // TODO: adjust to match your Vehicle fields if needed
            // Format: plate,type,make,model,year
            out.printf("%s,%s,%s,%s,%d%n",
                    vehicle.getLicensePlate(),
                    vehicle.getClass().getSimpleName(), // Car, Minibus, PickupTruck...
                    vehicle.getMake(),
                    vehicle.getModel(),
                    vehicle.getYear());
        } catch (IOException e) {
            System.out.println("Error saving vehicle: " + e.getMessage());
        }
    }

    // Append a customer to customers.txt
    private void saveCustomer(Customer customer) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(
                new FileWriter(CUSTOMERS_FILE, true)))) {

            // Format: id,name
            out.printf("%d,%s%n",
                    customer.getCustomerId(),
                    customer.getCustomerName());
        } catch (IOException e) {
            System.out.println("Error saving customer: " + e.getMessage());
        }
    }

    // Append a rental record to rental_records.txt
    private void saveRecord(RentalRecord record) {
        try (PrintWriter out = new PrintWriter(new BufferedWriter(
                new FileWriter(RECORDS_FILE, true)))) {

            // customerId,plate,recordType,date,amount
            out.printf("%d,%s,%s,%s,%.2f%n",
                    record.getCustomer().getCustomerId(),
                    record.getVehicle().getLicensePlate(),
                    record.getRecordType(),
                    record.getRecordDate().toString(),
                    record.getTotalAmount());
        } catch (IOException e) {
            System.out.println("Error saving rental record: " + e.getMessage());
        }
    }
    
    private void loadData() {
        loadVehiclesFromFile();
        loadCustomersFromFile();
        loadRecordsFromFile();
    }

    private void loadVehiclesFromFile() {
        File file = new File(VEHICLES_FILE);
        if (!file.exists()) {
            return; // nothing to load yet
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                // plate,type,make,model,year
                String[] parts = line.split(",");
                if (parts.length < 5) continue;

                String plate = parts[0];
                String type  = parts[1];
                String make  = parts[2];
                String model = parts[3];
                int year     = Integer.parseInt(parts[4]);

                Vehicle vehicle = null;

                switch (type) {
                    case "Car":
                        // We don't store seats in the file, so just use a reasonable default
                        int defaultSeats = 5;
                        vehicle = new Car(make, model, year, defaultSeats);
                        break;

                    case "Minibus":
                        // We don't store accessibility flag either, default to false
                        boolean defaultAccessible = false;
                        vehicle = new Minibus(make, model, year, defaultAccessible);
                        break;

                    case "PickupTruck":
                        // IMPORTANT: cargoSize must be > 0, or the constructor throws
                        double defaultCargoSize = 100.0;
                        boolean defaultHasTrailer = false;
                        vehicle = new PickupTruck(make, model, year, defaultCargoSize, defaultHasTrailer);
                        break;

                    default:
                        // Unknown type – skip this line
                        break;
                }

                // common logic: if we created something, set the plate and add to list
                if (vehicle != null) {
                    vehicle.setLicensePlate(plate);
                    vehicles.add(vehicle);
                }


                vehicle.setLicensePlate(plate);
                vehicles.add(vehicle);
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error loading vehicles: " + e.getMessage());
        }
    }

    private void loadCustomersFromFile() {
        File file = new File(CUSTOMERS_FILE);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                // id,name
                String[] parts = line.split(",");
                if (parts.length < 2) continue;

                int id = Integer.parseInt(parts[0]);
                String name = parts[1];

                Customer customer = new Customer(id, name);
                customers.add(customer);
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error loading customers: " + e.getMessage());
        }
    }

    private void loadRecordsFromFile() {
        File file = new File(RECORDS_FILE);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                // customerId,plate,recordType,date,amount
                String[] parts = line.split(",");
                if (parts.length < 5) continue;

                int customerId = Integer.parseInt(parts[0]);
                String plate   = parts[1];
                String type    = parts[2];
                LocalDate date = LocalDate.parse(parts[3]);
                double amount  = Double.parseDouble(parts[4]);

                Vehicle vehicle = findVehicleByPlate(plate);
                Customer customer = findCustomerById(customerId);

                // if something is missing, skip this record
                if (vehicle == null || customer == null) {
                    continue; //temporary comment to commit
                }

                RentalRecord record = new RentalRecord(vehicle, customer, date, amount, type);
                rentalHistory.addRecord(record);
            }
        } catch (IOException | NumberFormatException e) {
            System.out.println("Error loading rental records: " + e.getMessage());
        }
    }
    
    public java.util.List<Vehicle> getVehicles() {
        return vehicles;
    }

    public java.util.List<Customer> getCustomers() {
        return customers;
    }

    public RentalHistory getRentalHistory() {
        return rentalHistory;
    }

}