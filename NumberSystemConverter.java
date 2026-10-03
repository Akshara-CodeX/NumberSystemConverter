import java.math.BigDecimal;
import java.util.Scanner;

/**
 * NumberSystemConverter
 * Menu driven program to convert between the four number systems:
 *   Decimal (base 10), Binary (base 2), Octal (base 8), Hexadecimal (base 16)
 *
 * Features
 *   - Works with NEGATIVE numbers   (e.g. -45  ->  -101101)
 *   - Works with FRACTIONAL numbers (e.g. 10.101 binary -> 2.625 decimal)
 *   - Binary logic for negative numbers: 1's complement and 2's complement
 *
 * The conversions are done manually (without Integer.toBinaryString() etc.)
 * using these core functions:
 *   - decimalToBase()       : whole decimal number -> any base  (repeated division)
 *   - baseToDecimal()       : whole number of any base -> decimal (place values)
 *   - fractionToDecimal()   : digits after the point -> decimal fraction
 *   - fractionFromDecimal() : decimal fraction -> digits after the point (repeated multiplication)
 *   - convert()             : joins them together (sign + whole part + fraction part)
 */
class NumberSystemConverter {

    // Digits used by all number systems up to base 16
    static final String DIGITS = "0123456789ABCDEF";

    // Maximum number of digits shown after the point in the answer
    static final int MAX_FRACTION_DIGITS = 10;

    // Set to true when a fraction had more digits than we show (answer is cut)
    static boolean truncated = false;

    // ===============================================================
    //  PART 1 : CORE CONVERSION FUNCTIONS
    // ===============================================================

    // ---------------------------------------------------------------
    // Whole decimal number -> any base (2, 8, 10 or 16)
    // Method: divide by the base again and again, collect the remainders
    // and read them in REVERSE order.
    // Example: 45 to binary -> remainders 1,0,1,1,0,1 -> 101101
    // ---------------------------------------------------------------
    static String decimalToBase(long n, int base) {
        if (n == 0) return "0";                      // special case
        String result = "";
        while (n > 0) {
            int remainder = (int) (n % base);        // next digit (from the right)
            result = DIGITS.charAt(remainder) + result;   // put it in front
            n /= base;
        }
        return result;
    }

    // ---------------------------------------------------------------
    // Whole number of any base -> decimal
    // Method: go from left to right; for every digit
    //         value = value * base + digit
    // Example: 2D (hex) -> 2*16 + 13 = 45
    // ---------------------------------------------------------------
    static long baseToDecimal(String s, int base) {
        long value = 0;
        for (int i = 0; i < s.length(); i++) {
            int digit = DIGITS.indexOf(Character.toUpperCase(s.charAt(i)));
            value = value * base + digit;
        }
        return value;
    }

    // ---------------------------------------------------------------
    // Fraction digits of any base -> decimal fraction (exact value)
    // Method: the 1st digit is worth digit/base, the 2nd digit/base^2 ...
    // Example: .101 (binary) -> 1/2 + 0/4 + 1/8 = 0.625
    // ---------------------------------------------------------------
    static BigDecimal fractionToDecimal(String digits, int base) {
        BigDecimal value = BigDecimal.ZERO;
        BigDecimal place = BigDecimal.ONE;
        BigDecimal b = BigDecimal.valueOf(base);
        for (int i = 0; i < digits.length(); i++) {
            int digit = DIGITS.indexOf(Character.toUpperCase(digits.charAt(i)));
            place = place.divide(b);                              // 1/base, 1/base^2 ...
            value = value.add(place.multiply(BigDecimal.valueOf(digit)));
        }
        return value;
    }

    // ---------------------------------------------------------------
    // Decimal fraction -> digits after the point in any base
    // Method: multiply the fraction by the base; the whole-number part is
    // the next digit; keep the rest and repeat.
    // Example: 0.625 to binary -> 0.625*2=1.25 (1), 0.25*2=0.5 (0), 0.5*2=1.0 (1) -> .101
    // ---------------------------------------------------------------
    static String fractionFromDecimal(BigDecimal fraction, int base) {
        if (fraction.signum() == 0) return "";                    // no fraction at all
        if (base == 10) {                                         // already decimal
            return fraction.stripTrailingZeros().toPlainString().substring(2);  // drop "0."
        }
        String result = "";
        BigDecimal b = BigDecimal.valueOf(base);
        for (int i = 0; i < MAX_FRACTION_DIGITS && fraction.signum() != 0; i++) {
            fraction = fraction.multiply(b);
            int digit = fraction.intValue();                      // whole part = next digit
            result += DIGITS.charAt(digit);
            fraction = fraction.subtract(BigDecimal.valueOf(digit));
        }
        if (fraction.signum() != 0) truncated = true;             // more digits were left
        return result;
    }

    // ---------------------------------------------------------------
    // Validation: is the text a legal number of the given base?
    // Allowed: optional sign (+ or -), digits, and at most one point.
    // Binary: 0-1   Octal: 0-7   Decimal: 0-9   Hexadecimal: 0-9, A-F
    // ---------------------------------------------------------------
    static boolean isValidNumber(String s, int base) {
        if (s.startsWith("-") || s.startsWith("+")) s = s.substring(1);   // skip the sign
        int points = 0, digitsFound = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '.') {
                points++;
                if (points > 1) return false;                     // only one point allowed
            } else {
                int digit = DIGITS.indexOf(Character.toUpperCase(c));
                if (digit < 0 || digit >= base) return false;     // not allowed in this base
                digitsFound++;
            }
        }
        return digitsFound > 0;                                   // at least one digit needed
    }

    /** Returns true if the text is a plain binary string such as 101101 (no sign/point). */
    static boolean isBinaryString(String s) {
        if (s.length() == 0) return false;
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != '0' && s.charAt(i) != '1') return false;
        }
        return true;
    }

    // ---------------------------------------------------------------
    // convert(): converts a signed / fractional number from one base to another
    // Steps: 1) remember the sign   2) split at the point
    //        3) convert the whole part   4) convert the fraction part
    //        5) join them: sign + whole + "." + fraction
    // Example: convert("-10.101", 2, 10)  ->  "-2.625"
    // ---------------------------------------------------------------
    static String convert(String s, int fromBase, int toBase) {
        truncated = false;
        boolean negative = s.startsWith("-");
        if (s.startsWith("-") || s.startsWith("+")) s = s.substring(1);

        String wholePart = s, fractionPart = "";
        int point = s.indexOf('.');
        if (point >= 0) {                                         // split at the point
            wholePart = s.substring(0, point);
            fractionPart = s.substring(point + 1);
        }
        if (wholePart.length() == 0) wholePart = "0";             // ".5" means "0.5"

        long whole = baseToDecimal(wholePart, fromBase);
        String wholeOut = decimalToBase(whole, toBase);

        BigDecimal fraction = fractionToDecimal(fractionPart, fromBase);
        String fractionOut = fractionFromDecimal(fraction, toBase);

        boolean isZero = (whole == 0 && fraction.signum() == 0);  // avoid printing "-0"
        String result = (negative && !isZero) ? "-" : "";
        result += wholeOut;
        if (fractionOut.length() > 0) result += "." + fractionOut;
        return result;
    }

    // ---------------------------------------------------------------
    // The 12 conversion functions (one for each of menu choices 1-12)
    // ---------------------------------------------------------------

    // ----- From Decimal -----
    static String decimalToBinary(String s)      { return convert(s, 10, 2); }
    static String decimalToOctal(String s)       { return convert(s, 10, 8); }
    static String decimalToHexadecimal(String s) { return convert(s, 10, 16); }

    // ----- From Binary -----
    static String binaryToDecimal(String s)      { return convert(s, 2, 10); }
    static String binaryToOctal(String s)        { return convert(s, 2, 8); }
    static String binaryToHexadecimal(String s)  { return convert(s, 2, 16); }

    // ----- From Octal -----
    static String octalToDecimal(String s)       { return convert(s, 8, 10); }
    static String octalToBinary(String s)        { return convert(s, 8, 2); }
    static String octalToHexadecimal(String s)   { return convert(s, 8, 16); }

    // ----- From Hexadecimal -----
    static String hexadecimalToDecimal(String s) { return convert(s, 16, 10); }
    static String hexadecimalToBinary(String s)  { return convert(s, 16, 2); }
    static String hexadecimalToOctal(String s)   { return convert(s, 16, 8); }

    // ===============================================================
    //  PART 2 : BINARY LOGIC FOR NEGATIVE NUMBERS (1's and 2's complement)
    // ===============================================================

    /** Adds zeros on the left until the text is 'length' characters long. */
    static String padLeft(String s, int length) {
        while (s.length() < length) s = "0" + s;
        return s;
    }

    // ---------------------------------------------------------------
    // 1's complement: flip every bit (0 -> 1 and 1 -> 0)
    // Example: 00101101 -> 11010010
    // ---------------------------------------------------------------
    static String onesComplement(String bits) {
        String result = "";
        for (int i = 0; i < bits.length(); i++) {
            result += (bits.charAt(i) == '0') ? '1' : '0';
        }
        return result;
    }

    // ---------------------------------------------------------------
    // Adds 1 to a binary string (same length; a carry out of the
    // leftmost bit is thrown away).  Example: 11010010 + 1 = 11010011
    // ---------------------------------------------------------------
    static String addOne(String bits) {
        char[] b = bits.toCharArray();
        int i = b.length - 1;
        while (i >= 0) {
            if (b[i] == '0') {          // 0 + 1 = 1, no carry -> finished
                b[i] = '1';
                break;
            }
            b[i] = '0';                 // 1 + 1 = 0, carry 1 moves to the left
            i--;
        }
        return new String(b);
    }

    // ---------------------------------------------------------------
    // 2's complement: 1's complement + 1
    // Example: 00101101 -> 11010010 -> 11010011
    // ---------------------------------------------------------------
    static String twosComplement(String bits) {
        return addOne(onesComplement(bits));
    }

    // ---------------------------------------------------------------
    // Decimal (can be negative) -> binary in 2's complement form
    // Positive: ordinary binary padded with zeros to the bit width.
    // Negative: write the positive value in binary, then take 2's complement.
    // Example (8 bits): -45 -> 00101101 -> 11010011
    // ---------------------------------------------------------------
    static String decimalToTwosComplement(long n, int bits) {
        if (n >= 0) {
            return padLeft(decimalToBase(n, 2), bits);
        }
        String magnitude = padLeft(decimalToBase(-n, 2), bits);   // binary of the positive value
        return twosComplement(magnitude);
    }

    // ---------------------------------------------------------------
    // Binary in 2's complement form -> decimal (can be negative)
    // If the leftmost bit is 0 the number is positive: normal conversion.
    // If it is 1 the number is negative: take 2's complement to get the
    // positive value, convert it and put a minus sign in front.
    // Example: 11010011 -> 00101101 = 45 -> -45
    // ---------------------------------------------------------------
    static long twosComplementToDecimal(String bits) {
        if (bits.charAt(0) == '0') {
            return baseToDecimal(bits, 2);
        }
        long magnitude = baseToDecimal(twosComplement(bits), 2);
        return -magnitude;
    }

    // ---------------------------------------------------------------
    // Menu display
    // ---------------------------------------------------------------
    static void showMenu() {
        System.out.println("\n========= NUMBER SYSTEM CONVERTER =========");
        System.out.println("  (negative and fractional numbers allowed)");
        System.out.println("  --- From Decimal ---");
        System.out.println("   1. Decimal     -> Binary");
        System.out.println("   2. Decimal     -> Octal");
        System.out.println("   3. Decimal     -> Hexadecimal");
        System.out.println("  --- From Binary ---");
        System.out.println("   4. Binary      -> Decimal");
        System.out.println("   5. Binary      -> Octal");
        System.out.println("   6. Binary      -> Hexadecimal");
        System.out.println("  --- From Octal ---");
        System.out.println("   7. Octal       -> Decimal");
        System.out.println("   8. Octal       -> Binary");
        System.out.println("   9. Octal       -> Hexadecimal");
        System.out.println("  --- From Hexadecimal ---");
        System.out.println("  10. Hexadecimal -> Decimal");
        System.out.println("  11. Hexadecimal -> Binary");
        System.out.println("  12. Hexadecimal -> Octal");
        System.out.println("  --- Binary logic for negative numbers ---");
        System.out.println("  13. Decimal -> Binary (2's complement)");
        System.out.println("  14. Binary (2's complement) -> Decimal");
        System.out.println("  15. Binary -> 1's and 2's complement");
        System.out.println("===========================================");
    }

    // ---------------------------------------------------------------
    // Handles menu choices 1-12 (normal conversions)
    // ---------------------------------------------------------------
    static void normalConversion(Scanner sc, int choice) {
        String[] fromName = {"Decimal", "Binary", "Octal", "Hexadecimal"};
        int[] fromBase = {10, 2, 8, 16};
        // longest whole part allowed (so the value fits safely in a long)
        int[] maxLength = {18, 62, 20, 15};

        // choices 1-3 -> Decimal, 4-6 -> Binary, 7-9 -> Octal, 10-12 -> Hexadecimal
        int group = (choice - 1) / 3;
        String name = fromName[group];
        int base = fromBase[group];

        System.out.print("Enter the " + name + " number (e.g. -45 or 10.5): ");
        String input = sc.next();

        if (!isValidNumber(input, base)) {
            System.out.println("Invalid " + name + " number! Allowed digits: "
                    + DIGITS.substring(0, base) + " (with optional - sign and one point)");
            return;
        }
        String whole = input.replace("-", "").replace("+", "");
        if (whole.indexOf('.') >= 0) whole = whole.substring(0, whole.indexOf('.'));
        if (whole.length() > maxLength[group] || input.length() > 40) {
            System.out.println("Number is too long! Maximum " + maxLength[group]
                    + " digits before the point for " + name + ".");
            return;
        }

        String result = "";
        String target = "";
        // call the function that matches the user's choice
        switch (choice) {
            case 1 -> {
                result = decimalToBinary(input);       target = "Binary";
            }
            case 2 -> {
                result = decimalToOctal(input);        target = "Octal";
            }
            case 3 -> {
                result = decimalToHexadecimal(input);  target = "Hexadecimal";
            }
            case 4 -> {
                result = binaryToDecimal(input);       target = "Decimal";
            }
            case 5 -> {
                result = binaryToOctal(input);         target = "Octal";
            }
            case 6 -> {
                result = binaryToHexadecimal(input);   target = "Hexadecimal";
            }
            case 7 -> {
                result = octalToDecimal(input);        target = "Decimal";
            }
            case 8 -> {
                result = octalToBinary(input);         target = "Binary";
            }
            case 9 -> {
                result = octalToHexadecimal(input);    target = "Hexadecimal";
            }
            case 10 -> {
                result = hexadecimalToDecimal(input);  target = "Decimal";
            }
            case 11 -> {
                result = hexadecimalToBinary(input);   target = "Binary";
            }
            case 12 -> {
                result = hexadecimalToOctal(input);    target = "Octal";
            }
        }
        System.out.println(">> " + name + " " + input.toUpperCase()
                + "  =  " + target + " " + result);
        if (truncated) {
            System.out.println("   (fraction cut after " + MAX_FRACTION_DIGITS + " digits)");
        }
    }

    // ---------------------------------------------------------------
    // Menu choice 13 : Decimal -> Binary in 2's complement form
    // ---------------------------------------------------------------
    static void decimalToTwosComplementMenu(Scanner sc) {
        System.out.print("Enter the number of bits (8, 16 or 32): ");
        String bitText = sc.next();
        if (!bitText.equals("8") && !bitText.equals("16") && !bitText.equals("32")) {
            System.out.println("Invalid bit size! Please enter 8, 16 or 32.");
            return;
        }
        int bits = Integer.parseInt(bitText);

        System.out.print("Enter a whole decimal number (positive or negative): ");
        String input = sc.next();
        if (!isValidNumber(input, 10) || input.indexOf('.') >= 0 || input.length() > 12) {
            System.out.println("Invalid number! Please enter a whole decimal number.");
            return;
        }
        long n = Long.parseLong(input.startsWith("+") ? input.substring(1) : input);

        long min = -(1L << (bits - 1));          // smallest value that fits
        long max = (1L << (bits - 1)) - 1;       // largest value that fits
        if (n < min || n > max) {
            System.out.println("Out of range! For " + bits + " bits enter a number from "
                    + min + " to " + max + ".");
            return;
        }

        if (n >= 0) {
            System.out.println(">> " + n + " in " + bits + "-bit binary = "
                    + decimalToTwosComplement(n, bits));
        } else {
            String magnitude = padLeft(decimalToBase(-n, 2), bits);
            System.out.println("   Step 1: binary of " + (-n) + "      = " + magnitude);
            System.out.println("   Step 2: 1's complement    = " + onesComplement(magnitude));
            System.out.println("   Step 3: add 1 (2's comp.) = " + twosComplement(magnitude));
            System.out.println(">> " + n + " in " + bits + "-bit 2's complement = "
                    + decimalToTwosComplement(n, bits));
        }
    }

    // ---------------------------------------------------------------
    // Menu choice 14 : Binary (2's complement) -> Decimal
    // ---------------------------------------------------------------
    static void twosComplementToDecimalMenu(Scanner sc) {
        System.out.print("Enter the binary number in 2's complement form (2 to 32 bits): ");
        String input = sc.next();
        if (!isBinaryString(input) || input.length() < 2 || input.length() > 32) {
            System.out.println("Invalid! Enter only 0s and 1s (2 to 32 bits).");
            return;
        }
        System.out.println(">> " + input + " (" + input.length() + " bits) = "
                + twosComplementToDecimal(input) + " in decimal");
    }

    // ---------------------------------------------------------------
    // Menu choice 15 : Binary -> 1's complement and 2's complement
    // ---------------------------------------------------------------
    static void complementMenu(Scanner sc) {
        System.out.print("Enter a binary number (up to 32 bits): ");
        String input = sc.next();
        if (!isBinaryString(input) || input.length() > 32) {
            System.out.println("Invalid! Enter only 0s and 1s (up to 32 bits).");
            return;
        }
        System.out.println(">> Binary         : " + input);
        System.out.println(">> 1's complement : " + onesComplement(input));
        System.out.println(">> 2's complement : " + twosComplement(input));
    }

    // ---------------------------------------------------------------
    // main - drives the whole menu loop
    // ---------------------------------------------------------------
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        char again;

        do {
            showMenu();
            System.out.print("Enter your choice (1-15): ");
            String choiceText = sc.next();

            // make sure the choice is a whole number between 1 and 15
            int choice = 0;
            if (choiceText.matches("[0-9]{1,2}")) {
                choice = Integer.parseInt(choiceText);
            }

            if (choice < 1 || choice > 15) {
                System.out.println("Invalid choice! Please select from 1 to 15.");
            } else if (choice <= 12) {
                normalConversion(sc, choice);
            } else if (choice == 13) {
                decimalToTwosComplementMenu(sc);
            } else if (choice == 14) {
                twosComplementToDecimalMenu(sc);
            } else {
                complementMenu(sc);
            }

            // ask whether the user wants to continue or exit
            System.out.print("\nDo you want to continue? (y = yes / n = exit): ");
            again = sc.next().charAt(0);

        } while (again == 'y' || again == 'Y');

        System.out.println("Thank you! Program ended.");
       
    }
}