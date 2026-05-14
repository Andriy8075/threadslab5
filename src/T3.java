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
            double[][] ms = Data.useRandomInput
                    ? Data.fillMatrixRandom(Data.N, new Random(303))
                    : data.manualMS;

            double[] f3 = (double[]) data.receive(4, 3, "F3");
            double[] x3 = (double[]) data.receive(1, 3, "X3");
            double[][] ma3 = (double[][]) data.receive(1, 3, "MA3");
            double[][] ma4 = (double[][]) data.receive(1, 3, "MA4");

            data.send(3, 4, "MA4", ma4);
            data.send(3, 4, "MS4", Data.matrixColumns(ms, 4));
            data.send(3, 1, "MS1", Data.matrixColumns(ms, 1));
            data.send(3, 1, "MS2", Data.matrixColumns(ms, 2));

            double m3 = Data.min(x3);
            data.send(3, 1, "m3", m3);

            double minX = (Double) data.receive(1, 3, "m");
            double[] p3 = Data.multiplyVectorByMatrixRows(x3, ma3);
            double[] p4 = (double[]) data.receive(4, 3, "P4");
            data.send(3, 1, "P34", Data.sumVectors(p3, p4));

            double[] y = (double[]) data.receive(1, 3, "Y");
            data.send(3, 4, "Y", y);

            double[] z3 = Data.computeZPart(y, Data.matrixColumns(ms, 3), f3, minX);
            double[] z4 = (double[]) data.receive(4, 3, "Z4");

            data.send(3, 1, "Z3", z3);
            data.send(3, 1, "Z4", z4);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
