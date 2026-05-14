import java.util.Random;

/**
 * T2: вводить X, обчислює глобальний m = min(X) та свою частину Z2.
 * Має прямий обмін тільки з T1 і T4.
 */
class T2 extends Thread {
    private final Data data;

    public T2(Data data) {
        super("T2");
        this.data = data;
    }

    @Override
    public void run() {
        try {
            // Крок 1. Введення X
            double[] x = Data.useRandomInput
                    ? Data.fillVectorRandom(Data.N, new Random(202))
                    : data.manualX;

            // Крок 2. Прийняти F2 та F1 від T4
            double[] f1 = (double[]) data.receive(4, 2, "F1");
            double[] f2 = (double[]) data.receive(4, 2, "F2");

            // Крок 3. Передати X та F1 в T1
            data.send(2, 1, "X", x);
            data.send(2, 1, "F1", f1);

            // Крок 4. Передати X в T4
            data.send(2, 4, "X", x);

            // Крок 5. Прийняти MA2 та MS від T1
            double[][] ma2 = (double[][]) data.receive(1, 2, "MA2");
            double[][] ms = (double[][]) data.receive(1, 2, "MS");

            // Крок 6. Обчислення m2
            // m2 — мінімум другої чверті X.
            double m2 = Data.min(Data.vectorPart(x, 2));

            // Крок 7. Прийняти m4 від T4
            double m4 = (Double) data.receive(4, 2, "m4");

            // Крок 8. Прийняти m1 і m3 від T1
            double m1 = (Double) data.receive(1, 2, "m1");
            double m3 = (Double) data.receive(1, 2, "m3");

            // Крок 9. Обчислення m
            // Зведення локальних мінімумів m1..m4 у глобальний мінімум X.
            double minX = Math.min(Math.min(m1, m2), Math.min(m3, m4));

            // Крок 10. Передати m до T1
            data.send(2, 1, "m", minX);

            // Крок 11. Передати m до T4
            data.send(2, 4, "m", minX);

            // Крок 12. Обчислення Z2
            double[] z2 = Data.computeZPart(x, ma2, ms, f2, minX);

            // Крок 13. Передати Z2 до T1
            data.send(2, 1, "Z2", z2);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
