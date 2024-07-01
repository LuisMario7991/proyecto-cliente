public class Main {
    public static void main(String[] args) {
        Conexion.connect();
        Conexion.setupKeys();
        LoginScreen.initialize(args);
    }
}
