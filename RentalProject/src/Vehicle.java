public abstract class Vehicle {
    private String licensePlate;
    private String make;
    private String model;
    private int year;
    private VehicleStatus status;

    public enum VehicleStatus { Available, Held, Rented, UnderMaintenance, OutOfService }

    public Vehicle(String make, String model, int year) {
        this.make = capitalize(make);
        this.model = capitalize(model);
        this.year = year;
        this.status = VehicleStatus.Available;   // keep whatever you already had
    }

    public Vehicle() {
        this(null, null, 0);
    }

    public void setLicensePlate(String plate) {
    	    if (!isValidPlate(plate)) {
    	        throw new IllegalArgumentException(
    	                "Invalid license plate. Expected format: three letters followed by three digits (e.g., ABC123).");
    	    }

    	    // normalize to upper-case before storing
    	    this.licensePlate = plate.trim().toUpperCase();
    	        
    	
    }

    	private boolean isValidPlate(String plate) {
    	    if (plate == null) {
    	        return false;
    	    }

    	    plate = plate.trim();
    	    if (plate.isEmpty()) {
    	        return false;
    	    }

    	    // Accept 3 letters + 3 digits, case-insensitive
    	    String upper = plate.toUpperCase();
    	    return upper.matches("[A-Z]{3}[0-9]{3}");
    	}
    	
    	
    public void setStatus(VehicleStatus status) {
    	this.status = status;
    }

    public String getLicensePlate() { return licensePlate; }

    public String getMake() { return make; }

    public String getModel() { return model;}

    public int getYear() { return year; }

    public VehicleStatus getStatus() { return status; }

    public String getInfo() {
        return "| " + licensePlate + " | " + make + " | " + model + " | " + year + " | " + status + " |";
    }
    
    private String capitalize(String input) {
        if (input == null) {
            return "";
        }
        String trimmed = input.trim();
        if (trimmed.isEmpty()) {
            return "";
        }

        String lower = trimmed.toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

}
