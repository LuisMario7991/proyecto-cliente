import javax.crypto.*;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.file.Files;

public class AESGCMDecryptor {

      public static void decryptFile(File inputFile, File keyFile, File outputFile) throws Exception {
        // Leer la clave desde el archivo
        byte[] keyBytes = Files.readAllBytes(keyFile.toPath());
        SecretKeySpec secretKey = new SecretKeySpec(keyBytes, "AES");

        // Configurar el cifrador AES GCM
        Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
        byte[] iv = new byte[12]; // IV debe tener exactamente 12 bytes para AES GCM
        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, iv);
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmParameterSpec);

         // Leer el archivo cifrado y descifrarlo
         try (FileInputStream fis = new FileInputStream(inputFile);
         FileOutputStream fos = new FileOutputStream(outputFile);
         CipherInputStream cis = new CipherInputStream(fis, cipher)) {

        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = cis.read(buffer)) != -1) {
            fos.write(buffer, 0, bytesRead);
        }
    }
        } 
    }

