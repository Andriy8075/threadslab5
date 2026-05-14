import java.util.Random;

class T4 extends Thread {
    private final Data data;

    public T4(Data data) {
        super("T4");
        this.data = data;
    }

    @Override
    public void run() {
        try {
            // Крок 1. Введення F
            double[] f = Data.useRandomInput
                    ? Data.fillVectorRandom(Data.N, new Random(404))
                    : data.manualF;

            // Крок 2. Передати F1 та F2 до T2
            data.send(4, 2, "F1", Data.vectorPart(f, 1));
            data.send(4, 2, "F2", Data.vectorPart(f, 2));

            // Крок 3. Передати F3 до T3
            data.send(4, 3, "F3", Data.vectorPart(f, 3));

            // Крок 4. Прийняти X4 від T2
            double[] x4 = (double[]) data.receive(2, 4, "X4");

            // Крок 5. Прийняти MA4 та MS4 від T3
            double[][] ma4 = (double[][]) data.receive(3, 4, "MA4");
            double[][] ms4 = (double[][]) data.receive(3, 4, "MS4");

            // Крок 6. Обчислення m4
            double m4 = Data.min(x4);

            // Крок 7. Передати m4 до T2
            data.send(4, 2, "m4", m4);

            // Крок 8. Отримати m від T2
            double minX = (Double) data.receive(2, 4, "m");

            // Крок 9. Обчислення Z4
            double[] p4 = Data.multiplyVectorByMatrixRows(x4, ma4);
            data.send(4, 3, "P4", p4);

            double[] y = (double[]) data.receive(3, 4, "Y");
            double[] z4 = Data.computeZPart(y, ms4, Data.vectorPart(f, 4), minX);

            // Крок 10. Передати Z4 до T3
            data.send(4, 3, "Z4", z4);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
