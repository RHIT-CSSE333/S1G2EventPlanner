package eventplanner.services;

import java.security.SecureRandom;

public class HelperService {

    public static String generateRandomIdOfLength50() {
        final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_.";
        final SecureRandom RANDOM = new SecureRandom();

        StringBuilder id = new StringBuilder();
        for (int i = 0; i < 50; i++) {
            id.append(CHARACTERS.charAt(RANDOM.nextInt(CHARACTERS.length())));
        }

        return id.toString();
    }

}
