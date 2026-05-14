import java.util.Random;

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

            // Крок 3. Передати X1, X3 та F1 в T1
            data.send(2, 1, "X1", Data.vectorPart(x, 1));
            data.send(2, 1, "X3", Data.vectorPart(x, 3));
            data.send(2, 1, "F1", f1);

            // Крок 4. Передати X4 в T4
            data.send(2, 4, "X4", Data.vectorPart(x, 4));

            // Крок 5. Прийняти MA2 та MS2 від T1
            double[][] ma2 = (double[][]) data.receive(1, 2, "MA2");
            double[][] ms2 = (double[][]) data.receive(1, 2, "MS2");

            // Крок 6. Обчислення m2
            double m2 = Data.min(Data.vectorPart(x, 2));

            // Крок 7. Прийняти m4 від T4
            double m4 = (Double) data.receive(4, 2, "m4");

            // Крок 8. Прийняти m1 і m3 від T1
            double m1 = (Double) data.receive(1, 2, "m1");
            double m3 = (Double) data.receive(1, 2, "m3");

            // Крок 9. Обчислення m
            double minX = Math.min(Math.min(m1, m2), Math.min(m3, m4));

            // Крок 10. Передати m до T1
            data.send(2, 1, "m", minX);

            // Крок 11. Передати m до T4
            data.send(2, 4, "m", minX);

            // Крок 12. Обчислення Z2
            double[] p2 = Data.multiplyVectorByMatrixRows(Data.vectorPart(x, 2), ma2);
            data.send(2, 1, "P2", p2);

            double[] y = (double[]) data.receive(1, 2, "Y");
            double[] z2 = Data.computeZPart(y, ms2, f2, minX);

            // Крок 13. Передати Z2 до T1
            data.send(2, 1, "Z2", z2);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
