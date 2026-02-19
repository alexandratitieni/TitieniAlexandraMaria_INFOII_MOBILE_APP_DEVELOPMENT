public class Ex3 {
    //converts hexadecimal(string) to decimal
    public static String NumericConverter(String hexNr) {
        int decimalNr = 0, p = 1;
        char c;

        for (int i = hexNr.length() - 1; i >= 0; i--) {
            c = hexNr.charAt(i);
            if (c >= '0' && c <= '9')
                decimalNr += (c - '0') * p;
            else
                if (c >= 'A' && c <= 'F')
                    decimalNr += (c - 'A' + 10) * p;
            p *= 16;
        }
        return String.valueOf(decimalNr);
    }
}
