package dataServerApp;


import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import org.apache.log4j.Logger;

public class Cipher {
    private final Logger logger = Logger.getLogger(Cipher.class);

    private static final int ITERATIONS = 1000;
    private static final int KEY_LENGTH = 256;

    private static Cipher instance =null;
    private Cipher() {
    }

    private String decodeToString(byte[] input){
        Base64.Encoder encoder = Base64.getEncoder();
        logger.info("New generated salt is: "+ encoder.encodeToString(input));
        return encoder.encodeToString(input);
    }

    private String generateSalt(){
        SecureRandom random = new SecureRandom();
        byte bytes[] = new byte[16];
        random.nextBytes(bytes);
        return decodeToString(bytes);
    }
    public String[] getSaltAndPassSalt(String password){
        logger.info("Salt generate once");
        String salt = generateSalt();
        String passsalt = generatePassHash(salt, password);
        logger.info("Salt is: "+salt +"   " +passsalt);
        String[] output = new String[2];
        output[0] = passsalt;
        output[1] = salt;
        return output;
    }
    private String generatePassHash(String salt, String password){
        return decodeToString(hash(password,salt));
    }

    // Check if the password matches.
    public boolean isExpectedPassword(String passToCheck, String salt, String expectHash){
        byte[] pwdHash = hash(passToCheck, salt);
        Arrays.fill(passToCheck.toCharArray(), Character.MIN_VALUE);

        String decodeString = decodeToString(pwdHash);
        if(!decodeString.equals(expectHash)){
            return false;
        }
        return true;
    }

    private byte[] hash(String password, String salt){
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), ITERATIONS, KEY_LENGTH*2);
        Arrays.fill(password.toCharArray(),Character.MIN_VALUE);
        try{
            SecretKeyFactory skf = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
            return skf.generateSecret(spec).getEncoded();
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            spec.clearPassword();
        }
        return null;
    }
    public static Cipher getInstance(){
        if(instance == null){
            instance = new Cipher();
        }
        return instance;
    }
}
