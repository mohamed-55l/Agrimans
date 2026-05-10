package modules.user.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.Label;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import core.database.DBConnection;

public class VerifyController {

    @FXML
    private TextField txtEmail;

    @FXML
    private TextField txtCode;

    @FXML
    private Label lblMsg;

    @FXML
    private void verify() {

        try {

            Connection conn = DBConnection.getConnection();

            String sql =
                    "SELECT * FROM user WHERE email=? AND verification_code=?";

            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, txtEmail.getText());
            ps.setString(2, txtCode.getText());

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                PreparedStatement update =
                        conn.prepareStatement(
                                "UPDATE user SET is_verified=true WHERE email=?"
                        );

                update.setString(1, txtEmail.getText());
                update.executeUpdate();

                lblMsg.setText("Compte vérifié");

            } else {
                lblMsg.setText("Code incorrect");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
