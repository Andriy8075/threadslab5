import java.util.Scanner;

public class Lab5 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        try {
            parseSizeAndMode(args, scanner);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            Data.N = 4;
            Data.useRandomInput = true;
        }

        if (Data.N < 4 || Data.N % 4 != 0) {
            System.err.println("N must be a positive multiple of 4. Using N=4.");
            Data.N = 4;
        }

        System.out.println("Lab5: N=" + Data.N + ", mode=" + (Data.useRandomInput ? "random" : "manual"));

        Data data = new Data();
        // У manual-режимі введення виконується до запуску потоків, щоб не змішувати Scanner між задачами.
        if (!Data.useRandomInput) {
            data.allocateInputStorage();
            data.readAllManual(scanner);
        }

        System.out.println("Limit cores in Windows Task Manager, then enter Start or press Enter to launch threads:");
        scanner.nextLine();
        System.out.println("Launching threads...");

        long startTime = System.nanoTime();

        // Створення задач згідно зі схемою: T1-MA, T2-X, T3-MS, T4-F.
        Thread t1 = new T1(data);
        Thread t2 = new T2(data);
        Thread t3 = new T3(data);
        Thread t4 = new T4(data);

        // Паралельний старт задач.
        t1.start();
        t2.start();
        t3.start();
        t4.start();

        try {
            // Головний потік чекає завершення всіх задач перед підрахунком часу.
            t1.join();
            t2.join();
            t3.join();
            t4.join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Interrupted");
        }

        long endTime = System.nanoTime();
        double elapsedSeconds = (endTime - startTime) / 1_000_000_000.0;
        System.out.println("Execution time: " + elapsedSeconds + " s (" + (endTime - startTime) + " ns)");
    }

    private static void parseSizeAndMode(String[] args, Scanner scanner) {
        if (args.length >= 1) {
            Data.N = Integer.parseInt(args[0].trim());
        } else {
            System.out.print("Enter N (size of vectors and matrices): ");
            Data.N = Integer.parseInt(scanner.nextLine().trim());
        }

        if (args.length >= 2) {
            Data.useRandomInput = parseModeFlag(args[1].trim());
        } else {
            System.out.print("Input data: random (r) or manual (m)? ");
            String line = scanner.nextLine().trim();
            Data.useRandomInput = line.isEmpty() || parseModeFlag(line);
        }
    }

    private static boolean parseModeFlag(String value) {
        if (value.isEmpty()) return true;
        String low = value.toLowerCase();
        if (low.equals("m") || low.equals("man") || low.equals("manual")) return false;
        if (low.equals("r") || low.equals("rand") || low.equals("random")) return true;
        return true;
    }
}
