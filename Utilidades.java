import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

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
    
    public static byte[] decryptWithPublicKey(String filePath, PublicKey publicKey) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, publicKey);
        
        byte[] encryptedData = Files.readAllBytes(Paths.get(filePath));
        return cipher.doFinal(encryptedData);
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

    public static boolean validatePublicKey(PublicKey key) {
        // Implementación de ejemplo: validar la especificación de la clave y cualquier
        // otra propiedad necesaria
        return key.getAlgorithm().equals("DH") && key.getEncoded().length > 0;
    }

    public static void enviarArchivo(String filePath) throws IOException {
        Conexion.dataOutputStream.writeUTF(command.getUploadFile());
        Conexion.dataOutputStream.flush();

        File file = new File(filePath);
        long fileSize = file.length();
        String fileName = file.getName();

        System.out.println("Enviando archivo: " + fileName + "de tamaño: " + fileSize + " bytes");

        // Enviar el nombre del archivo y su tamaño
        Conexion.dataOutputStream.writeUTF(fileName);
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
}
