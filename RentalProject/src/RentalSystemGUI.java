import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.time.LocalDate;

public class RentalSystemGUI extends Application {

    private final RentalSystem rentalSystem = RentalSystem.getInstance();

    private ListView<String> vehiclesView;
    private ListView<String> historyView;

    public static void main(String[] args) {
        launch(args);   // start JavaFX app
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Vehicle Rental System - GUI");

        vehiclesView = new ListView<>();
        historyView = new ListView<>();

        TabPane tabPane = new TabPane();
        tabPane.getTabs().addAll(
                createVehiclesTab(),
                createCustomersTab(),
                createRentReturnTab(),
                createHistoryTab()
        );

        BorderPane root = new BorderPane();
        root.setCenter(tabPane);

        Scene scene = new Scene(root, 900, 600);
        primaryStage.setScene(scene);
        primaryStage.show();

        refreshVehicleList();
        refreshHistoryList();
    }

    // ---------- TAB 1: Vehicles ----------

    private Tab createVehiclesTab() {
        Tab tab = new Tab("Vehicles");
        tab.setClosable(false);

        TextField plateField = new TextField();
        TextField makeField = new TextField();
        TextField modelField = new TextField();
        TextField yearField = new TextField();

        ComboBox<String> typeBox = new ComboBox<>();
        typeBox.getItems().addAll("Car", "Minibus", "Pickup Truck");
        typeBox.getSelectionModel().selectFirst();

        TextField seatsField = new TextField();
        TextField cargoField = new TextField();
        CheckBox accessibleCheck = new CheckBox("Accessible (Minibus)");
        CheckBox trailerCheck = new CheckBox("Has trailer (Pickup)");

        Button addButton = new Button("Add Vehicle");

        addButton.setOnAction(e -> {
            try {
                String plate = plateField.getText().trim();
                String make = makeField.getText().trim();
                String model = modelField.getText().trim();
                int year = Integer.parseInt(yearField.getText().trim());

                if (plate.isEmpty() || make.isEmpty() || model.isEmpty()) {
                    showError("Plate, make, model, and year are required.");
                    return;
                }

                Vehicle vehicle;
                String type = typeBox.getValue();

                if ("Car".equals(type)) {
                    int seats = Integer.parseInt(seatsField.getText().trim());
                    vehicle = new Car(make, model, year, seats);
                } else if ("Minibus".equals(type)) {
                    boolean accessible = accessibleCheck.isSelected();
                    vehicle = new Minibus(make, model, year, accessible);
                } else { // Pickup Truck
                    double cargoSize = Double.parseDouble(cargoField.getText().trim());
                    boolean hasTrailer = trailerCheck.isSelected();
                    vehicle = new PickupTruck(make, model, year, cargoSize, hasTrailer);
                }

                // validate and set license plate (uses your isValidPlate logic)
                vehicle.setLicensePlate(plate);

                boolean added = rentalSystem.addVehicle(vehicle);
                if (added) {
                    showInfo("Vehicle added successfully.");
                    plateField.clear();
                    makeField.clear();
                    modelField.clear();
                    yearField.clear();
                    seatsField.clear();
                    cargoField.clear();
                    accessibleCheck.setSelected(false);
                    trailerCheck.setSelected(false);
                    refreshVehicleList();
                } else {
                    showError("A vehicle with that license plate already exists.");
                }
            } catch (NumberFormatException ex) {
                showError("Year / numeric fields must be valid numbers.");
            } catch (IllegalArgumentException ex) {
                showError(ex.getMessage());
            } catch (Exception ex) {
                showError("Unexpected error: " + ex.getMessage());
            }
        });

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);

        int row = 0;
        form.add(new Label("Type:"), 0, row);
        form.add(typeBox, 1, row++);
        form.add(new Label("License plate:"), 0, row);
        form.add(plateField, 1, row++);
        form.add(new Label("Make:"), 0, row);
        form.add(makeField, 1, row++);
        form.add(new Label("Model:"), 0, row);
        form.add(modelField, 1, row++);
        form.add(new Label("Year:"), 0, row);
        form.add(yearField, 1, row++);
        form.add(new Label("Seats (Car):"), 0, row);
        form.add(seatsField, 1, row++);
        form.add(new Label("Cargo size (Pickup):"), 0, row);
        form.add(cargoField, 1, row++);
        form.add(accessibleCheck, 1, row++);
        form.add(trailerCheck, 1, row++);
        form.add(addButton, 1, row);

        VBox root = new VBox(10, form);
        root.setPadding(new Insets(10));

        tab.setContent(root);
        return tab;
    }

    // ---------- TAB 2: Customers ----------

    private Tab createCustomersTab() {
        Tab tab = new Tab("Customers");
        tab.setClosable(false);

        TextField idField = new TextField();
        TextField nameField = new TextField();
        Button addButton = new Button("Add Customer");

        addButton.setOnAction(e -> {
            try {
                int id = Integer.parseInt(idField.getText().trim());
                String name = nameField.getText().trim();

                if (name.isEmpty()) {
                    showError("Customer name is required.");
                    return;
                }

                Customer customer = new Customer(id, name);
                boolean added = rentalSystem.addCustomer(customer);
                if (added) {
                    showInfo("Customer added successfully.");
                    idField.clear();
                    nameField.clear();
                } else {
                    showError("A customer with that ID already exists.");
                }
            } catch (NumberFormatException ex) {
                showError("Customer ID must be a number.");
            } catch (Exception ex) {
                showError("Unexpected error: " + ex.getMessage());
            }
        });

        GridPane form = new GridPane();
        form.setHgap(10);
        form.setVgap(8);

        int row = 0;
        form.add(new Label("Customer ID:"), 0, row);
        form.add(idField, 1, row++);
        form.add(new Label("Name:"), 0, row);
        form.add(nameField, 1, row++);
        form.add(addButton, 1, row);

        VBox root = new VBox(10, form);
        root.setPadding(new Insets(10));

        tab.setContent(root);
        return tab;
    }

    // ---------- TAB 3: Rent & Return ----------

    private Tab createRentReturnTab() {
        Tab tab = new Tab("Rent / Return");
        tab.setClosable(false);

        // Rent controls
        TextField rentPlateField = new TextField();
        TextField rentCustomerIdField = new TextField();
        TextField rentAmountField = new TextField();
        Button rentButton = new Button("Rent Vehicle");

        rentButton.setOnAction(e -> {
            try {
                String plate = rentPlateField.getText().trim();
                int cid = Integer.parseInt(rentCustomerIdField.getText().trim());
                double amount = Double.parseDouble(rentAmountField.getText().trim());

                Vehicle vehicle = rentalSystem.findVehicleByPlate(plate);
                Customer customer = rentalSystem.findCustomerById(cid);

                if (vehicle == null || customer == null) {
                    showError("Vehicle or customer not found.");
                    return;
                }

                boolean ok = rentalSystem.rentVehicle(vehicle, customer, LocalDate.now(), amount);
                if (ok) {
                    showInfo("Vehicle rented successfully.");
                    refreshVehicleList();
                    refreshHistoryList();
                } else {
                    showError("Renting failed (vehicle might not be available).");
                }
            } catch (NumberFormatException ex) {
                showError("Customer ID and amount must be numbers.");
            } catch (Exception ex) {
                showError("Unexpected error: " + ex.getMessage());
            }
        });

        // Return controls
        TextField returnPlateField = new TextField();
        TextField returnCustomerIdField = new TextField();
        TextField returnFeesField = new TextField();
        Button returnButton = new Button("Return Vehicle");

        returnButton.setOnAction(e -> {
            try {
                String plate = returnPlateField.getText().trim();
                int cid = Integer.parseInt(returnCustomerIdField.getText().trim());
                double fees = Double.parseDouble(returnFeesField.getText().trim());

                Vehicle vehicle = rentalSystem.findVehicleByPlate(plate);
                Customer customer = rentalSystem.findCustomerById(cid);

                if (vehicle == null || customer == null) {
                    showError("Vehicle or customer not found.");
                    return;
                }

                boolean ok = rentalSystem.returnVehicle(vehicle, customer, LocalDate.now(), fees);
                if (ok) {
                    showInfo("Vehicle returned successfully.");
                    refreshVehicleList();
                    refreshHistoryList();
                } else {
                    showError("Returning failed (vehicle might not be rented).");
                }
            } catch (NumberFormatException ex) {
                showError("Customer ID and fees must be numbers.");
            } catch (Exception ex) {
                showError("Unexpected error: " + ex.getMessage());
            }
        });

        GridPane rentForm = new GridPane();
        rentForm.setHgap(10);
        rentForm.setVgap(8);

        int row = 0;
        rentForm.add(new Label("Rent - License plate:"), 0, row);
        rentForm.add(rentPlateField, 1, row++);
        rentForm.add(new Label("Rent - Customer ID:"), 0, row);
        rentForm.add(rentCustomerIdField, 1, row++);
        rentForm.add(new Label("Rent - Amount:"), 0, row);
        rentForm.add(rentAmountField, 1, row++);
        rentForm.add(rentButton, 1, row);

        GridPane returnForm = new GridPane();
        returnForm.setHgap(10);
        returnForm.setVgap(8);

        row = 0;
        returnForm.add(new Label("Return - License plate:"), 0, row);
        returnForm.add(returnPlateField, 1, row++);
        returnForm.add(new Label("Return - Customer ID:"), 0, row);
        returnForm.add(returnCustomerIdField, 1, row++);
        returnForm.add(new Label("Return - Additional fees:"), 0, row);
        returnForm.add(returnFeesField, 1, row++);
        returnForm.add(returnButton, 1, row);

        VBox root = new VBox(20, rentForm, new Separator(), returnForm);
        root.setPadding(new Insets(10));

        tab.setContent(root);
        return tab;
    }

    // ---------- TAB 4: Lists / History ----------

    private Tab createHistoryTab() {
        Tab tab = new Tab("Lists & History");
        tab.setClosable(false);

        vehiclesView.setPlaceholder(new Label("No vehicles yet."));
        historyView.setPlaceholder(new Label("No rental history yet."));

        Button refreshButton = new Button("Refresh");
        refreshButton.setOnAction(e -> {
            refreshVehicleList();
            refreshHistoryList();
        });

        VBox root = new VBox(10,
                new Label("Vehicles:"),
                vehiclesView,
                new Label("Rental history:"),
                historyView,
                refreshButton
        );
        root.setPadding(new Insets(10));

        tab.setContent(root);
        return tab;
    }

    // ---------- Helpers ----------

    private void refreshVehicleList() {
        if (vehiclesView == null) return;
        vehiclesView.getItems().clear();
        for (Vehicle v : rentalSystem.getVehicles()) {
            vehiclesView.getItems().add(formatVehicle(v));
        }
    }

    private void refreshHistoryList() {
        if (historyView == null) return;
        historyView.getItems().clear();
        for (RentalRecord r : rentalSystem.getRentalHistory().getRentalHistory()) {
            historyView.getItems().add(formatRecord(r));
        }
    }

    private String formatVehicle(Vehicle v) {
        return String.format("%s - %s %s (%d) [%s]",
                v.getLicensePlate(),
                v.getMake(),
                v.getModel(),
                v.getYear(),
                v.getStatus());
    }

    private String formatRecord(RentalRecord r) {
        return String.format("%s | %s rented %s on %s (%.2f)",
                r.getRecordType(),
                r.getCustomer().getCustomerName(),
                r.getVehicle().getLicensePlate(),
                r.getRecordDate(),
                r.getTotalAmount());
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
