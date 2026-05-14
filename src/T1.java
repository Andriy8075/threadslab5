import java.util.Random;

class T1 extends Thread {
    private final Data data;

    public T1(Data data) {
        super("T1");
        this.data = data;
    }

    @Override
    public void run() {
        try {
            // Крок 1. Введення MA
            double[][] ma = Data.useRandomInput
                    ? Data.fillMatrixRandom(Data.N, new Random(101))
                    : data.manualMA;

            // Крок 2. Прийняти X1, X3 та F1 від T2
            double[] x1 = (double[]) data.receive(2, 1, "X1");
            double[] x3 = (double[]) data.receive(2, 1, "X3");
            double[] f1 = (double[]) data.receive(2, 1, "F1");

            // Крок 3. Передати X3, MA3 та MA4 в T3
            data.send(1, 3, "X3", x3);
            data.send(1, 3, "MA3", Data.matrixRows(ma, 3));
            data.send(1, 3, "MA4", Data.matrixRows(ma, 4));

            // Крок 4. Прийняти MS1 та MS2 від T3
            double[][] ms1 = (double[][]) data.receive(3, 1, "MS1");
            double[][] ms2 = (double[][]) data.receive(3, 1, "MS2");

            // Крок 5. Передати MA2 та MS2 в T2
            data.send(1, 2, "MA2", Data.matrixRows(ma, 2));
            data.send(1, 2, "MS2", ms2);

            // Крок 6. Обчислення m1 = min(X1)
            double m1 = Data.min(x1);

            // Крок 7. Прийняти m3 від T3
            double m3 = (Double) data.receive(3, 1, "m3");

            // Крок 8. Передати m1 та m3 до T2
            data.send(1, 2, "m1", m1);
            data.send(1, 2, "m3", m3);

            // Крок 9. Прийняти m від T2
            double minX = (Double) data.receive(2, 1, "m");

            // Крок 10. Передати m до T3
            data.send(1, 3, "m", minX);

            // Крок 11. Обчислення Z1
            double[] p1 = Data.multiplyVectorByMatrixRows(x1, Data.matrixRows(ma, 1));
            double[] p2 = (double[]) data.receive(2, 1, "P2");
            double[] p34 = (double[]) data.receive(3, 1, "P34");
            double[] y = Data.sumVectors(p1, p2, p34);

            data.send(1, 2, "Y", y);
            data.send(1, 3, "Y", y);

            double[] z1 = Data.computeZPart(y, ms1, f1, minX);

            // Крок 12. Прийняти Z2 від T2
            double[] z2 = (double[]) data.receive(2, 1, "Z2");

            // Крок 13. Прийняти Z3 та Z4 від T3
            double[] z3 = (double[]) data.receive(3, 1, "Z3");
            double[] z4 = (double[]) data.receive(3, 1, "Z4");

            // Крок 14. Виведення Z
            double[] z = new double[Data.N];
            copyPart(z, z1, 1);
            copyPart(z, z2, 2);
            copyPart(z, z3, 3);
            copyPart(z, z4, 4);

            System.out.println("T1: Z = X*(MA*MS) + min(X)*F = " + Data.formatVector(z));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }

    private static void copyPart(double[] destination, double[] part, int partNumber) {
        System.arraycopy(part, 0, destination, Data.from(partNumber), part.length);
    }
}
