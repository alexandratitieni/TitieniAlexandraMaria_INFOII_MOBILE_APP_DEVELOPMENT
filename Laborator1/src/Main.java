
void Ex1(Scanner scanner)
{
    System.out.println("String to sort: ");
    String str = scanner.nextLine();

    System.out.println("Sorted string: " + Ex1.CustomStringSorting(str));
}

void Ex2(Scanner scanner) {
    System.out.println("First number: ");
    int num1 = scanner.nextInt();
    System.out.println("Second number: ");
    int num2 = scanner.nextInt();

    if (Ex2.FriendlyNumbers(num1, num2))
        System.out.println("The numbers are friendly");
    else
        System.out.println("The numbers are not friendly");
}

void Ex3(Scanner scanner) {
    System.out.println("Hexadecimal number: ");
    String hexNr = scanner.nextLine();
   System.out.println(Ex3.NumericConverter(hexNr));
}

void  Ex4(Scanner scanner) {
    System.out.println("Path: ");
    String path = scanner.nextLine();

    System.out.println("Valleys: " + Ex4.ValleyCounter(path));
}

void main() {
    Scanner scanner = new Scanner(System.in);
    //Ex1(scanner);
    //Ex2(scanner)
    //Ex3(scanner);
    Ex4(scanner);
}
