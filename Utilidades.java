import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

public class Utilidades {

    private static Commands command = new Commands();

    public static byte[] hashBytes(byte[] data) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(data);
    }

    public static byte[] hashFile(String filePath) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] fileBytes = Files.readAllBytes(Paths.get(filePath));
        return digest.digest(fileBytes);
    }

    public static PublicKey getPublicKeyFromFile(String filePath) throws Exception {
        byte[] publicKeyBytes = Files.readAllBytes(Paths.get(filePath));
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicKeyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePublic(keySpec);
    }

    public static boolean validatePublicKey(PublicKey key) {
        return key.getAlgorithm().equals("DH") && key.getEncoded().length > 0;
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public static void enviarArchivo(String filePath) throws IOException {
        Conexion.dataOutputStream.writeUTF(command.getUploadFile());
        Conexion.dataOutputStream.flush();

        File file = new File(filePath);
        long fileSize = file.length();
        String fileName = file.getName();

        System.out.println("Enviando archivo: " + fileName + " de tamaño: " + fileSize + " bytes");

        // Enviar el nombre del archivo y su tamaño
        Conexion.dataOutputStream.writeUTF(fileName);
        Conexion.dataOutputStream.flush();
        Conexion.dataOutputStream.writeLong(fileSize);
        Conexion.dataOutputStream.flush();

        FileInputStream fileInputStream = new FileInputStream(filePath);

        byte[] buffer = new byte[1024];
        int bytesRead;
        long totalBytesRead = 0;

        while (totalBytesRead < fileSize && (bytesRead = fileInputStream.read(buffer)) != -1) {
            try {
                Conexion.dataOutputStream.write(buffer, 0, bytesRead);
                Conexion.dataOutputStream.flush();
                totalBytesRead += bytesRead;

                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        fileInputStream.close();

        System.out.println("Archivo enviado al servidor");
    }

    protected static void recibirArchivo() {
        try {
            Conexion.dataOutputStream.writeUTF(command.getUploadFile());

            String fileName = Conexion.dataInputStream.readUTF();
            long fileSize = Conexion.dataInputStream.readLong();
            String saveFilePath = "recibido_" + fileName;

            System.out.println("Recibiendo archivo: " + fileName + " de tamaño: " + fileSize + " bytes");

            try (FileOutputStream fileOutputStream = new FileOutputStream(saveFilePath);
                    BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)) {

                byte[] buffer = new byte[1024];
                int bytesRead;
                long totalBytesRead = 0;

                while (totalBytesRead < fileSize && (bytesRead = Conexion.dataInputStream.read(buffer)) != -1) {
                    bufferedOutputStream.write(buffer, 0, bytesRead);
                    totalBytesRead += bytesRead;
                }

                bufferedOutputStream.flush(); // Asegurar que todataOutputStream los datos han sido escritos

                if (totalBytesRead == fileSize) {
                    System.out.println("Archivo recibido correctamente y guardado como " + saveFilePath);

                } else {
                    System.out.println("Error: El tamaño del archivo recibido (" + totalBytesRead
                            + " bytes) no coincide con el tamaño esperado (" + fileSize + " bytes).");
                }

                fileOutputStream.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
