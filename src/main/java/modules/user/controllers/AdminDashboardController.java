package modules.user.controllers;

import modules.user.models.User;
import modules.user.utils.DBConnection; // تأكد من المسار الصحيح للـ DBConnection
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
    @FXML private StackPane contentPane; // متاح الآن في الـ FXML
    @FXML private VBox usersView;        // متاح الآن في الـ FXML

    @FXML
    public void initialize() {
        initTableColumns();
        initUpdateButton();
        initDeleteButton();
        loadUsers();
    }

    private void initTableColumns() {
        colId.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getId()));
        colName.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getFullName()));
        colEmail.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmail()));
        colPhone.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPhone()));
        colRole.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRole()));
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
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void openUpdateView(User user) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/user/update-user.fxml"));
            Parent view = loader.load();

            // الحصول على الكنترولر وتمرير البيانات
            UpdateUserController controller = loader.getController();
            controller.setUser(user);

            // التبديل داخل الـ StackPane (لن يكون NULL الآن)
            contentPane.getChildren().setAll(view);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Erreur: Vérifiez le fichier update-user.fxml");
        }
    }

    @FXML
    private void handleSearch() {
        // ... (كود البحث اللي عندك مريغل)
    }

    @FXML
    private void resetSearch() {
        searchField.clear();
        loadUsers();
    }

    private void initUpdateButton() {
        colUpdate.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("✏ Update");
            { btn.setOnAction(event -> openUpdateView(getTableView().getItems().get(getIndex()))); }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    private void initDeleteButton() {
        colDelete.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("🗑 Delete");
            { btn.setOnAction(event -> {
                User user = getTableView().getItems().get(getIndex());
                deleteUser(user);
            }); }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    private void deleteUser(User user) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM user WHERE id=?")) {
            ps.setInt(1, user.getId());
            ps.executeUpdate();
            loadUsers();
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void returnToList() {
        contentPane.getChildren().setAll(usersView);
    }
    @FXML
    private void handleLogout(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Déconnexion");
        alert.setHeaderText(null);
        alert.setContentText("Êtes-vous sûr de vouloir vous déconnecter ?");

        if (alert.showAndWait().get() == ButtonType.OK) {

            modules.user.utils.SessionManager.clearSession();

            switchScene("/fxml/user/login-view.fxml", (Node) event.getSource());
        }
    }
}