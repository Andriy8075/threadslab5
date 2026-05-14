import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

class Data {
    /** Розмір векторів і квадратних матриць. Для 4 задач N має ділитись на 4. */
    public static int N = 4;
    /** true — автоматична генерація даних; false — ручне введення з консолі. */
    public static boolean useRandomInput = true;

    // Буфери для ручного режиму. Дані читаються в Lab5 до запуску потоків.
    public double[] manualX;
    public double[] manualF;
    public double[][] manualMA;
    public double[][] manualMS;

    /** Поштові скриньки каналів: ключ "from->to", значення — черга повідомлень. */
    private final Map<String, List<Message>> mailboxes = new HashMap<>();

    public Data() {
        // Дозволені з'єднання за структурною схемою: T1-T2, T1-T3, T2-T4, T3-T4.
        addChannel(1, 2);
        addChannel(1, 3);
        addChannel(2, 4);
        addChannel(3, 4);
    }

    /** Створює двонапрямний канал між задачами a та b. */
    private void addChannel(int a, int b) {
        mailboxes.put(key(a, b), new ArrayList<>());
        mailboxes.put(key(b, a), new ArrayList<>());
    }

    /**
     * Передача повідомлення між сусідніми задачами.
     * payload копіюється, щоб задачі не працювали з одним спільним масивом.
     */
    public synchronized void send(int from, int to, String tag, Object payload) {
        List<Message> mailbox = mailboxes.get(key(from, to));
        if (mailbox == null) {
            throw new IllegalArgumentException("Direct transfer T" + from + " -> T" + to + " is not allowed");
        }
        mailbox.add(new Message(tag, copyPayload(payload)));
        notifyAll();
    }

    /**
     * Очікування повідомлення з потрібним тегом.
     * Якщо такого повідомлення ще немає, задача блокується через wait().
     */
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
                    return copyPayload(message.payload);
                }
            }
            wait();
        }
    }

    /** Початковий індекс part-ї чверті, нумерація частин з 1. */
    public static int from(int part) {
        return (part - 1) * N / 4;
    }

    /** Кінцевий індекс part-ї чверті, не включно. */
    public static int to(int part) {
        return part * N / 4;
    }

    /** Повертає part-ту частину вектора: X1, X2, X3 або X4. */
    public static double[] vectorPart(double[] vector, int part) {
        int from = from(part);
        int to = to(part);
        return Arrays.copyOfRange(vector, from, to);
    }

    /** Повертає part-ту групу рядків матриці: MA1, MA2, MA3 або MA4. */
    public static double[][] matrixRows(double[][] matrix, int part) {
        int from = from(part);
        int to = to(part);
        double[][] rows = new double[to - from][];
        for (int i = from; i < to; i++) {
            rows[i - from] = Arrays.copyOf(matrix[i], matrix[i].length);
        }
        return rows;
    }

    /** Повертає part-ту групу стовпців матриці. Залишено для можливих схем з розбиттям MS. */
    public static double[][] matrixColumns(double[][] matrix, int part) {
        int from = from(part);
        int to = to(part);
        double[][] columns = new double[N][to - from];
        for (int i = 0; i < N; i++) {
            for (int j = from; j < to; j++) {
                columns[i][j - from] = matrix[i][j];
            }
        }
        return columns;
    }

    /** Мінімальний елемент частини вектора X. */
    public static double min(double[] vector) {
        double min = vector[0];
        for (double value : vector) {
            if (value < min) min = value;
        }
        return min;
    }

    /** Частковий добуток Xi * MAi, результат має довжину N. */
    public static double[] multiplyVectorByMatrixRows(double[] xPart, double[][] maRows) {
        double[] result = new double[N];
        for (int j = 0; j < N; j++) {
            double sum = 0.0;
            for (int i = 0; i < xPart.length; i++) {
                sum += xPart[i] * maRows[i][j];
            }
            result[j] = sum;
        }
        return result;
    }

    /** Додавання двох векторів однакової довжини. */
    public static double[] sumVectors(double[] first, double[] second) {
        double[] result = new double[first.length];
        for (int i = 0; i < first.length; i++) {
            result[i] = first[i] + second[i];
        }
        return result;
    }

    /** Додавання трьох векторів однакової довжини. */
    public static double[] sumVectors(double[] first, double[] second, double[] third) {
        double[] result = new double[first.length];
        for (int i = 0; i < first.length; i++) {
            result[i] = first[i] + second[i] + third[i];
        }
        return result;
    }

    /**
     * Обчислення локальної частини Zi.
     * Для кожного рядка MAi обчислюється рядок (MAi * MS), після чого множиться на X.
     * Матриця MA*MS повністю не зберігається — проміжне значення maMs рахується в циклі.
     */
    public static double[] computeZPart(double[] x, double[][] maRows, double[][] ms, double[] fPart, double minX) {
        double[] zPart = new double[fPart.length];
        for (int i = 0; i < fPart.length; i++) {
            double product = 0.0;
            for (int j = 0; j < N; j++) {
                double maMs = 0.0;
                for (int k = 0; k < N; k++) {
                    maMs += maRows[i][k] * ms[k][j];
                }
                product += maMs * x[j];
            }
            zPart[i] = product + minX * fPart[i];
        }
        return zPart;
    }

    /** Вектор розміру n, усі елементи дорівнюють value. */
    public static double[] fillVector(int n, double value) {
        double[] vector = new double[n];
        Arrays.fill(vector, value);
        return vector;
    }

    /** Квадратна матриця n x n, усі елементи дорівнюють value. */
    public static double[][] fillMatrix(int n, double value) {
        double[][] matrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            Arrays.fill(matrix[i], value);
        }
        return matrix;
    }

    /** Випадковий вектор з цілими значеннями 1..9. */
    public static double[] fillVectorRandom(int n, Random rnd) {
        double[] vector = new double[n];
        for (int i = 0; i < n; i++) {
            vector[i] = 1 + rnd.nextInt(9);
        }
        return vector;
    }

    /** Випадкова квадратна матриця з цілими значеннями 1..9. */
    public static double[][] fillMatrixRandom(int n, Random rnd) {
        double[][] matrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                matrix[i][j] = 1 + rnd.nextInt(9);
            }
        }
        return matrix;
    }

    /** Виділяє пам'ять для ручного введення X, F, MA, MS. */
    public void allocateInputStorage() {
        manualX = new double[N];
        manualF = new double[N];
        manualMA = new double[N][N];
        manualMS = new double[N][N];
    }

    /** Зчитування всіх вхідних даних у ручному режимі до старту потоків. */
    public void readAllManual(Scanner scanner) {
        Locale.setDefault(Locale.US);
        System.out.println("--- Manual input (decimal point . or ,) ---");
        readVector(scanner, manualX, "X");
        readMatrix(scanner, manualMA, "MA");
        readMatrix(scanner, manualMS, "MS");
        readVector(scanner, manualF, "F");
        System.out.println("--- End of manual input ---");
    }

    /** Зчитування одного вектора з консолі. */
    public static void readVector(Scanner scanner, double[] vector, String name) {
        System.out.println("Vector " + name + " (" + vector.length + " numbers, space-separated):");
        parseDoublesLine(scanner.nextLine(), vector.length, vector, 0);
    }

    /** Зчитування квадратної матриці з консолі по рядках. */
    public static void readMatrix(Scanner scanner, double[][] matrix, String name) {
        int n = matrix.length;
        System.out.println("Matrix " + name + " (" + n + "x" + n + "), " + n + " rows:");
        for (int i = 0; i < n; i++) {
            System.out.print("  row " + i + ": ");
            parseDoublesLine(scanner.nextLine(), n, matrix[i], 0);
        }
    }

    /** Компактне виведення результату: повністю для малих N і скорочено для великих. */
    public static String formatVector(double[] vector) {
        if (vector.length <= 10) {
            return Arrays.toString(vector);
        }
        return "[" + vector[0] + ", " + vector[1] + ", ... length=" + vector.length + "]";
    }

    /** Розбір рядка з числами; підтримує як крапку, так і кому в дробових числах. */
    private static void parseDoublesLine(String line, int expectedCount, double[] destination, int offset) {
        String[] parts = line.trim().split("\\s+");
        if (parts.length < expectedCount) {
            throw new IllegalArgumentException("Expected " + expectedCount + " numbers, got " + parts.length);
        }
        for (int i = 0; i < expectedCount; i++) {
            destination[offset + i] = Double.parseDouble(parts[i].replace(',', '.'));
        }
    }

    /** Ключ каналу для поштової скриньки. */
    private static String key(int from, int to) {
        return from + "->" + to;
    }

    /** Глибоке копіювання payload перед передачею або поверненням з receive(). */
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

    /** Одне повідомлення в каналі: тег + копія даних. */
    private static class Message {
        private final String tag;
        private final Object payload;

        private Message(String tag, Object payload) {
            this.tag = tag;
            this.payload = payload;
        }
    }
}
