public class Main {
    public static void main(String[] args) {
        try {
            Conexion.connect();
            Conexion.setupKeys();
            LoginScreen.initialize(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
