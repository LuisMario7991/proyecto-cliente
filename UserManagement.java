import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
// import org.slf4j.Logger;
// import org.slf4j.LoggerFactory;

import org.mindrot.jbcrypt.BCrypt;

public class UserManagement {

    private static Connection connect() throws SQLException {
        // final Logger logger = LoggerFactory.getLogger(servidor.class);
        String url = "jdbc:mysql://chef-server.mysql.database.azure.com:3306/chef?useSSL=true";
        return DriverManager.getConnection(url, "chefadmin", "ch3f4dm1n!");
    }

    protected static String authenticateUser(String userEmail, String userPassword) {
        try {
            System.out.println("Autenticando usuario");
            try (Connection connection = connect();
                    PreparedStatement stmt = connection
                            .prepareStatement(
                                    "SELECT Contrasena, TipoUsuario FROM Usuarios WHERE Correo = ?")) {
                stmt.setString(1, userEmail);
                ResultSet rs = stmt.executeQuery();

                if (!rs.next()) {
                    return "NOT_FOUND";
                }

                String storedPassword = rs.getString("Contrasena");
                String userType = rs.getString("TipoUsuario");

                if (!BCrypt.checkpw(userPassword, storedPassword)) {
                    return "INVALID";
                }

                connection.close();

                return userType;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return "SQL_ERROR";
        }
    }
}
