package backend.Utils;

import java.util.Random;

public class DataGeneratorUtils {

    public static String generatePanNumber() {

        String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String numbers = "123456789";
        // String panFormt = "XXXPX1111X";
        StringBuilder pan = new StringBuilder();
        Random rnd = new Random();
        int i = 0;
        while (i < 3) {
            int index = (int) (rnd.nextFloat() * SALTCHARS.length());
            pan.append(SALTCHARS.charAt(index));
            i++;
        }
        pan.append("P");
        pan.append(SALTCHARS.charAt((int) (rnd.nextFloat() * SALTCHARS.length())));

        i = 0;
        while (i < 4) {
            int index = (int) (rnd.nextFloat() * numbers.length());
            pan.append(numbers.charAt(index));
            i++;
        }
        pan.append(SALTCHARS.charAt((int) (rnd.nextFloat() * SALTCHARS.length())));
        return pan.toString();
    }

    public static String generateMobileNumber() {
        Random rnd = new Random();
        // Indian mobile numbers typically start with 6, 7, 8, or 9
        int firstDigit = 6 + rnd.nextInt(4);
        StringBuilder mobile = new StringBuilder(String.valueOf(firstDigit));
        for (int i = 0; i < 9; i++) {
            mobile.append(rnd.nextInt(10));
        }
        return mobile.toString();
    }
}
