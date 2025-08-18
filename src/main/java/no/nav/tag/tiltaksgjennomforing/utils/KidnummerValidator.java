package no.nav.tag.tiltaksgjennomforing.utils;

/**
 * Hentet fra NoCommons - https://github.com/bekkopen/NoCommons
 */
public class KidnummerValidator {
    private static final int[] BASE_MOD11_WEIGHTS = new int[]{2, 3, 4, 5, 6, 7};

    public static boolean isValid(String kidnummer) {
        try {
            validateSyntax(kidnummer);
            validateChecksum(kidnummer);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static void validateSyntax(String kidnummer) {
        validateAllDigits(kidnummer.replace("-", ""));
        validateLengthInRange(kidnummer, 4, 25);
    }

    private static void validateChecksum(String kidnummer) {
        int kMod10 = calculateMod10CheckSum(getMod10Weights(kidnummer), kidnummer);
        int checksumDigit = kidnummer.charAt(kidnummer.length() - 1) - '0';
        if (kMod10 == checksumDigit) {
            return;
        }
        String kMod11 = calculateMod11CheckSumAllowDash(getMod11Weights(kidnummer), kidnummer);
        if ("-".equals(kMod11) || Integer.parseInt(kMod11) == checksumDigit) {
            return;
        }
        throw new IllegalArgumentException("Ugyldig kontrollsiffer: " + kidnummer);
    }

    private static void validateLengthInRange(String kidnummer, int i, int j) {
        if (kidnummer == null || kidnummer.length() < i || kidnummer.length() > j) {
            throw new IllegalArgumentException("Ugyldig format. Et KID-nummer skal være mellom 3(+1) og 25 siffer");
        }
    }

    private static int[] getMod10Weights(String k) {
        int[] weights = new int[k.length() - 1];
        for (int i = 0; i < weights.length; i++) {
            if ((i % 2) == 0) {
                weights[i] = 2;
            } else {
                weights[i] = 1;
            }
        }
        return weights;
    }

    private static int calculateMod10CheckSum(int[] weights, String number) {
        int c = calculateChecksum(weights, number, true) % 10;
        return c == 0 ? 0 : 10 - c;
    }

    private static int calculateChecksum(int[] weights, String number, boolean tverrsum) {
        int checkSum = 0;
        for (int i = 0; i < weights.length; i++) {
            int product = weights[i] * (number.charAt(weights.length - 1 - i) - '0');
            if (tverrsum) {
                checkSum += (product > 9 ? product - 9 : product);
            } else {
                checkSum += product;
            }
        }
        return checkSum;
    }

    private static String calculateMod11CheckSumAllowDash(int[] weights, String number) {
        int c = calculateChecksum(weights, number, false) % 11;
        if (c == 1) {
            return "-";
        }
        return String.valueOf((c == 0 ? 0 : 11 - c));
    }

    private static int[] getMod11Weights(String k) {
        int[] weights = new int[k.length() - 1];
        for (int i = 0; i < weights.length; i++) {
            int j = i % BASE_MOD11_WEIGHTS.length;
            weights[i] = BASE_MOD11_WEIGHTS[j];
        }
        return weights;
    }

    private static void validateAllDigits(String numberString) {
        if (numberString == null || numberString.length() <= 0) {
            throw new IllegalArgumentException("Et KID-nummer kan kun bestå av tall: " + numberString);
        }
        for (int i = 0; i < numberString.length(); i++) {
            if (!Character.isDigit(numberString.charAt(i))) {
                throw new IllegalArgumentException("Et KID-nummer kan kun bestå av tall: " + numberString);
            }
        }
    }
}
