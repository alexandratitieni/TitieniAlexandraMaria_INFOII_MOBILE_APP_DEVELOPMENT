public class Ex4 {
    public static int ValleyCounter(String path) {
        int level = 0, valleys = 0;
        for (int i = 0; i < path.length(); i++) {
            if (path.charAt(i) == 'U') level++;
            else level--;

            if (level == 0 && path.charAt(i) == 'U')
                valleys++;
        }
        return valleys;
    }
}
