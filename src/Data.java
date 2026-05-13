import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

class Data {
    public static int N = 4;
    public static boolean useRandomInput = true;

    public double[] manualX;
    public double[] manualF;
    public double[][] manualMA;
    public double[][] manualMS;

    private final Map<String, List<Message>> mailboxes = new HashMap<>();

    public Data() {
        addChannel(1, 2);
        addChannel(1, 3);
        addChannel(2, 4);
        addChannel(3, 4);
    }

    private void addChannel(int a, int b) {
        mailboxes.put(key(a, b), new ArrayList<>());
        mailboxes.put(key(b, a), new ArrayList<>());
    }

    public synchronized void send(int from, int to, String tag, Object payload) {
        List<Message> mailbox = mailboxes.get(key(from, to));
        if (mailbox == null) {
            throw new IllegalArgumentException("Direct transfer T" + from + " -> T" + to + " is not allowed");
        }
        mailbox.add(new Message(tag, copyPayload(payload)));
        notifyAll();
        System.out.println("T" + from + " sent " + tag + " to T" + to);
    }

    public synchronized Object receive(int from, int to, String tag) throws InterruptedException {
        List<Message> mailbox = mailboxes.get(key(from, to));
        if (mailbox == null) {
            throw new IllegalArgumentException("Direct transfer T" + from + " -> T" + to + " is not allowed");
        }
        while (true) {
            for (int i = 0; i < mailbox.size(); i++) {
                Message message = mailbox.get(i);
                if (message.tag.equals(tag)) {
                    mailbox.remove(i);
                    System.out.println("T" + to + " received " + tag + " from T" + from);
                    return copyPayload(message.payload);
                }
            }
            wait();
        }
    }

    public static int from(int part) {
        return (part - 1) * N / 4;
    }

    public static int to(int part) {
        return part * N / 4;
    }

    public static double[] vectorPart(double[] vector, int part) {
        int from = from(part);
        int to = to(part);
        return Arrays.copyOfRange(vector, from, to);
    }

    public static double[][] matrixRows(double[][] matrix, int part) {
        int from = from(part);
        int to = to(part);
        double[][] rows = new double[to - from][];
        for (int i = from; i < to; i++) {
            rows[i - from] = Arrays.copyOf(matrix[i], matrix[i].length);
        }
        return rows;
    }

    public static double min(double[] vector) {
        double min = vector[0];
        for (double value : vector) {
            if (value < min) min = value;
        }
        return min;
    }

    public static double[] computeZPart(double[] x, double[][] ma, double[][] ms, double[] fPart, double minX, int part) {
        int from = from(part);
        int to = to(part);
        double[] zPart = new double[to - from];
        for (int j = from; j < to; j++) {
            double product = 0.0;
            for (int k = 0; k < N; k++) {
                double maMs = 0.0;
                for (int l = 0; l < N; l++) {
                    maMs += ma[k][l] * ms[l][j];
                }
                product += x[k] * maMs;
            }
            zPart[j - from] = product + minX * fPart[j - from];
        }
        return zPart;
    }

    public static double[] fillVector(int n, double value) {
        double[] vector = new double[n];
        Arrays.fill(vector, value);
        return vector;
    }

    public static double[][] fillMatrix(int n, double value) {
        double[][] matrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            Arrays.fill(matrix[i], value);
        }
        return matrix;
    }

    public static double[] fillVectorRandom(int n, Random rnd) {
        double[] vector = new double[n];
        for (int i = 0; i < n; i++) {
            vector[i] = 1 + rnd.nextInt(9);
        }
        return vector;
    }

    public static double[][] fillMatrixRandom(int n, Random rnd) {
        double[][] matrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                matrix[i][j] = 1 + rnd.nextInt(9);
            }
        }
        return matrix;
    }

    public void allocateInputStorage() {
        manualX = new double[N];
        manualF = new double[N];
        manualMA = new double[N][N];
        manualMS = new double[N][N];
    }

    public void readAllManual(Scanner scanner) {
        Locale.setDefault(Locale.US);
        System.out.println("--- Manual input (decimal point . or ,) ---");
        readVector(scanner, manualX, "X");
        readMatrix(scanner, manualMA, "MA");
        readMatrix(scanner, manualMS, "MS");
        readVector(scanner, manualF, "F");
        System.out.println("--- End of manual input ---");
    }

    public static void readVector(Scanner scanner, double[] vector, String name) {
        System.out.println("Vector " + name + " (" + vector.length + " numbers, space-separated):");
        parseDoublesLine(scanner.nextLine(), vector.length, vector, 0);
    }

    public static void readMatrix(Scanner scanner, double[][] matrix, String name) {
        int n = matrix.length;
        System.out.println("Matrix " + name + " (" + n + "x" + n + "), " + n + " rows:");
        for (int i = 0; i < n; i++) {
            System.out.print("  row " + i + ": ");
            parseDoublesLine(scanner.nextLine(), n, matrix[i], 0);
        }
    }

    public static String formatVector(double[] vector) {
        if (vector.length <= 10) {
            return Arrays.toString(vector);
        }
        return "[" + vector[0] + ", " + vector[1] + ", ... length=" + vector.length + "]";
    }

    private static void parseDoublesLine(String line, int expectedCount, double[] destination, int offset) {
        String[] parts = line.trim().split("\\s+");
        if (parts.length < expectedCount) {
            throw new IllegalArgumentException("Expected " + expectedCount + " numbers, got " + parts.length);
        }
        for (int i = 0; i < expectedCount; i++) {
            destination[offset + i] = Double.parseDouble(parts[i].replace(',', '.'));
        }
    }

    private static String key(int from, int to) {
        return from + "->" + to;
    }

    private static Object copyPayload(Object payload) {
        if (payload instanceof double[] vector) {
            return Arrays.copyOf(vector, vector.length);
        }
        if (payload instanceof double[][] matrix) {
            double[][] copy = new double[matrix.length][];
            for (int i = 0; i < matrix.length; i++) {
                copy[i] = Arrays.copyOf(matrix[i], matrix[i].length);
            }
            return copy;
        }
        if (payload instanceof Double value) {
            return value;
        }
        throw new IllegalArgumentException("Unsupported message payload: " + payload.getClass().getName());
    }

    private static class Message {
        private final String tag;
        private final Object payload;

        private Message(String tag, Object payload) {
            this.tag = tag;
            this.payload = payload;
        }
    }
}
