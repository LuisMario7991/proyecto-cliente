import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class LoginScreen extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Configurar los elementos de la interfaz de usuario
        Label userLabel = new Label("Usuario:");
        Label passwordLabel = new Label("Contraseña:");
        TextField userField = new TextField();
        TextField passwordField = new TextField();
        Button loginButton = new Button("Iniciar sesión");

        // Configurar el evento del botón de inicio de sesión
        loginButton.setOnAction(e -> {
            // Validar usuario y contraseña (simulado)
            String usuario = userField.getText();
            String contraseña = passwordField.getText();
            if (validarUsuario(usuario, contraseña)) {
                conexion.showMainInterface(primaryStage); // Llama a la interfaz principal después de la autenticación
            }
        });

        // Configurar el diseño del grid para organizar los elementos
        GridPane grid = new GridPane();
        grid.add(userLabel, 0, 0);
        grid.add(userField, 1, 0);
        grid.add(passwordLabel, 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(loginButton, 1, 2);

        // Configurar la escena principal y mostrar la ventana
        Scene scene = new Scene(grid, 300, 150);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Login");
        primaryStage.show();
    }

    public static void initialize(String[] args) {
        launch(args);
    }

    private boolean validarUsuario(String usuario, String contraseña) {
        // Implementa tu lógica de validación aquí
        return true; // Solo como ejemplo
    }
}
