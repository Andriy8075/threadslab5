import java.util.Random;

class T3 extends Thread {
    private final Data data;

    public T3(Data data) {
        super("T3");
        this.data = data;
    }

    @Override
    public void run() {
        try {
            // Крок 1. Введення MS
            double[][] ms = Data.useRandomInput
                    ? Data.fillMatrixRandom(Data.N, new Random(303))
                    : data.manualMS;

            // Крок 2. Прийняти F3 від T4
            double[] f3 = (double[]) data.receive(4, 3, "F3");

            // Крок 3. Прийняти X3, MA3 та MA4 від T1
            double[] x3 = (double[]) data.receive(1, 3, "X3");
            double[][] ma3 = (double[][]) data.receive(1, 3, "MA3");
            double[][] ma4 = (double[][]) data.receive(1, 3, "MA4");

            // Крок 4. Передати MA4 та MS4 в T4
            data.send(3, 4, "MA4", ma4);
            data.send(3, 4, "MS4", Data.matrixColumns(ms, 4));

            // Крок 5. Передати MS1 та MS2 в T1
            data.send(3, 1, "MS1", Data.matrixColumns(ms, 1));
            data.send(3, 1, "MS2", Data.matrixColumns(ms, 2));

            // Крок 6. Обчислення m3
            double m3 = Data.min(x3);

            // Крок 7. Передати m3 до T1
            data.send(3, 1, "m3", m3);

            // Крок 8. Отримати m від T1
            double minX = (Double) data.receive(1, 3, "m");

            // Крок 9. Обчислити Z3
            double[] p3 = Data.multiplyVectorByMatrixRows(x3, ma3);
            double[] p4 = (double[]) data.receive(4, 3, "P4");
            data.send(3, 1, "P34", Data.sumVectors(p3, p4));

            double[] y = (double[]) data.receive(1, 3, "Y");
            data.send(3, 4, "Y", y);

            double[] z3 = Data.computeZPart(y, Data.matrixColumns(ms, 3), f3, minX);

            // Крок 10. Отримати Z4 від T4
            double[] z4 = (double[]) data.receive(4, 3, "Z4");

            // Крок 11. Передати Z3 та Z4 до T1
            data.send(3, 1, "Z3", z3);
            data.send(3, 1, "Z4", z4);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
