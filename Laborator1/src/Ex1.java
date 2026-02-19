import static java.lang.Character.isLowerCase;
import static java.lang.Character.isUpperCase;

public class Ex1 {
    public static String CustomStringSorting(String str)
    {
        String lowerCh = "";
        String upperCh = "";
        for (int i = 0; i < str.length(); i++) {
            if (isLowerCase(str.charAt(i)))
                lowerCh += str.charAt(i);
            else if (isUpperCase(str.charAt(i)))
                upperCh += str.charAt(i);
        }

        str = lowerCh + upperCh;

        return str;
    }
}
