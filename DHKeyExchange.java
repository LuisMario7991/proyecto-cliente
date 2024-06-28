import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.*;
import java.util.Arrays;

import javax.crypto.*;
import javax.crypto.spec.*;

public class DHKeyExchange {

    @SuppressWarnings("unused")
    public static class DHParams implements Serializable {
        private static final long serialVersionUID = 1L;
        private BigInteger p;
        private BigInteger g;
        private int l;

        public DHParams(BigInteger p, BigInteger g, int l) {
            this.p = p;
            this.g = g;
            this.l = l;
        }

        public void saveToFile(String fileName, DHParams params) {
            try (FileOutputStream fileOut = new FileOutputStream(
                    fileName);
                    ObjectOutputStream out = new ObjectOutputStream(fileOut)) {
                out.writeObject(params);
                System.out.println("El objeto ha sido guardado en " + fileName);
            } catch (IOException i) {
                i.printStackTrace();
            }
        }

        public ServerDH readFromFile(String fileName) {
            ServerDH params = null;
            try (FileInputStream fileIn = new FileInputStream(fileName);
                    ObjectInputStream in = new ObjectInputStream(fileIn)) {

                params = (ServerDH) in.readObject();
                System.out.println("El objeto ha sido leído: " + params);
            } catch (IOException i) {
                i.printStackTrace();
            } catch (ClassNotFoundException c) {
                System.out.println("Clase Persona no encontrada");
                c.printStackTrace();
            }
            return params;
        }
    }

    public static class ServerDH implements Serializable {
        private final DHParameterSpec dhSpec;
        private final KeyPair keyPair;
        private final PublicKey publicKey;
        private final PrivateKey privateKey;

        public ServerDH() throws Exception {
            System.out.println("Generando parámetros Diffie-Hellman");

            // Generar parámetros Diffie-Hellman
            AlgorithmParameterGenerator paramGen = AlgorithmParameterGenerator.getInstance("DH");
            paramGen.init(2048);
            AlgorithmParameters params = paramGen.generateParameters();
            this.dhSpec = params.getParameterSpec(DHParameterSpec.class);

            // Generar par de claves Diffie-Hellman
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance("DH");
            keyPairGen.initialize(dhSpec);
            this.keyPair = keyPairGen.generateKeyPair();

            // Obtener clave pública y privada
            this.publicKey = keyPair.getPublic();
            this.privateKey = keyPair.getPrivate();
        }

        public byte[] exchangeKeys(Socket clientSocket) throws Exception {
            System.out.println("Compartiendo parámetros Diffie-Hellman");

            ObjectInputStream objectInputStream = new ObjectInputStream(clientSocket.getInputStream());
            // ObjectOutputStream objectOutputStream = new
            // ObjectOutputStream(clientSocket.getOutputStream());

            // Enviar parámetros Diffie-Hellman a Alice
            // objectOutputStream.writeObject(this.dhSpec.getP());
            // objectOutputStream.flush();
            // objectOutputStream.writeObject(this.dhSpec.getG());
            // objectOutputStream.flush();
            // objectOutputStream.writeInt(this.dhSpec.getL());
            // objectOutputStream.flush();
            // System.out.println("Parámetros Diffie-Hellman enviados a Alice.");

            // Enviar clave pública a Alice
            // objectOutputStream.writeObject(this.publicKey);
            // objectOutputStream.flush();
            // System.out.println("Clave pública de Bob enviada a Alice.");

            // Recibir clave pública de Alice
            PublicKey alicePublicKey = (PublicKey) objectInputStream.readObject();
            if (!Utilidades.validatePublicKey(alicePublicKey)) {
                throw new IllegalArgumentException("Clave pública recibida es inválida");
            }
            System.out.println("Clave pública de Alice recibida y validada.");

            // Generar la clave compartida
            KeyAgreement keyAgree = KeyAgreement.getInstance("DH");
            keyAgree.init(this.privateKey);
            keyAgree.doPhase(alicePublicKey, true);
            byte[] sharedSecret = keyAgree.generateSecret();

            // Calcular hash SHA-256 de la clave compartida
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] sharedSecretHash = sha256.digest(sharedSecret);
            byte[] first16Bytes = Arrays.copyOf(sharedSecretHash, 16);
            System.out.println("Clave compartida hash (Bob): " + Utilidades.bytesToHex(sharedSecretHash));

            // Guarda el hash en un archivo TXT
            String fileName = "hasht.txt";
            Files.write(Paths.get(fileName), first16Bytes, StandardOpenOption.CREATE);
            // Files.writeString(Paths.get(fileName), bytesToHex(first16Bytes),
            // StandardOpenOption.CREATE);

            return first16Bytes;
        }

        public PublicKey getPublicKey() {
            return publicKey;
        }

        public DHParameterSpec getDhSpec() {
            return dhSpec;
        }

    }
}
