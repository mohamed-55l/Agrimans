package modules.user.controllers;

import modules.user.models.User;
import core.database.DBConnection;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class AdminDashboardController extends BaseController {

    @FXML private TableView<User> usersTable;

    @FXML private TableColumn<User, Integer> colId;
    @FXML private TableColumn<User, String> colName;
    @FXML private TableColumn<User, String> colEmail;
    @FXML private TableColumn<User, String> colPhone;
    @FXML private TableColumn<User, String> colRole;

    @FXML private TableColumn<User, Void> colUpdate;
    @FXML private TableColumn<User, Void> colDelete;

    @FXML private TextField searchField;

    @FXML private StackPane contentPane;
    @FXML private VBox usersView;

    @FXML
    public void initialize() {
        initTableColumns();
        initUpdateButton();
        initDeleteButton();
        loadUsers();
    }

    private void initTableColumns() {
        colId.setCellValueFactory(data ->
                new SimpleObjectProperty<>(data.getValue().getId()));
        colName.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getFullName()));
        colEmail.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getEmail()));
        colPhone.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getPhone()));
        colRole.setCellValueFactory(data ->
                new SimpleStringProperty(data.getValue().getRole()));
    }

    private void loadUsers() {

        usersTable.getItems().clear();

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM user");
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                User user = new User();

                user.setId(rs.getInt("id"));
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone"));
                user.setRole(rs.getString("role"));

                usersTable.getItems().add(user);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= RECHERCHE GLOBALE =================
    @FXML
    private void handleSearch() {

        usersTable.getItems().clear();

        String keyword = searchField.getText().trim();

        if (keyword.isEmpty()) {
            loadUsers();
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {

            String sql = """
                    SELECT * FROM user
                    WHERE CAST(id AS CHAR) LIKE ?
                       OR full_name LIKE ?
                       OR email LIKE ?
                       OR phone LIKE ?
                       OR role LIKE ?
                    """;

            PreparedStatement ps = conn.prepareStatement(sql);

            String pattern = "%" + keyword + "%";

            ps.setString(1, pattern);
            ps.setString(2, pattern);
            ps.setString(3, pattern);
            ps.setString(4, pattern);
            ps.setString(5, pattern);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                User user = new User();

                user.setId(rs.getInt("id"));
                user.setFullName(rs.getString("full_name"));
                user.setEmail(rs.getString("email"));
                user.setPhone(rs.getString("phone"));
                user.setRole(rs.getString("role"));

                usersTable.getItems().add(user);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void resetSearch() {
        searchField.clear();
        loadUsers();
    }

    // ================= DELETE =================
    private void deleteUser(User user) {

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps =
                     conn.prepareStatement("DELETE FROM user WHERE id=?")) {

            ps.setInt(1, user.getId());
            ps.executeUpdate();
            loadUsers();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= UPDATE BUTTON =================
    private void initUpdateButton() {

        colUpdate.setCellFactory(param -> new TableCell<>() {

            private final Button btn = new Button("✏ Update");

            {
                btn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    openUpdateView(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    private void initDeleteButton() {

        colDelete.setCellFactory(param -> new TableCell<>() {

            private final Button btn = new Button("🗑 Delete");

            {
                btn.setOnAction(event -> {
                    User user = getTableView().getItems().get(getIndex());
                    deleteUser(user);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    private void openUpdateView(User user) {
        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/user/update-user.fxml")
            );

            Parent view = loader.load();

            // 🔥 RÉCUPÉRER LE CONTROLLER
            UpdateUserController controller = loader.getController();

            // 🔥 PASSER L'OBJET USER
            controller.setUser(user);

            contentPane.getChildren().setAll(view);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= LOGOUT =================
    @FXML
    private void handleLogout(ActionEvent event) {

        try {

            Node source = (Node) event.getSource();

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/user/login-view.fxml"));

            Parent root = loader.load();

            Stage stage = (Stage) source.getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= RETURN TO USERS LIST =================
    public void returnToList() {
        contentPane.getChildren().clear();
        contentPane.getChildren().add(usersView);
        loadUsers();
    }

    public void refreshTable() {
        loadUsers();
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
