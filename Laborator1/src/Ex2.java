public class Ex2 {

    private static int SumDiv(int n) {
        int sum = 1;
        for (int d = 2; d * d <= n; d++)
            if (n % d == 0)
                sum += d + n / d;
        return sum;
    }

    public static boolean FriendlyNumbers(int num1, int num2)
    {
        if (SumDiv(num1) == num2 || SumDiv(num2) == num1)
            return true;
        return false;
    }
}
