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
            double[] x = Data.useRandomInput
                    ? Data.fillVectorRandom(Data.N, new Random(202))
                    : data.manualX;

            double[] f1 = (double[]) data.receive(4, 2, "F1");
            double[] f2 = (double[]) data.receive(4, 2, "F2");

            data.send(2, 1, "X1", Data.vectorPart(x, 1));
            data.send(2, 1, "X3", Data.vectorPart(x, 3));
            data.send(2, 1, "F1", f1);
            data.send(2, 4, "X4", Data.vectorPart(x, 4));

            double[][] ma2 = (double[][]) data.receive(1, 2, "MA2");
            double[][] ms2 = (double[][]) data.receive(1, 2, "MS2");

            double m2 = Data.min(Data.vectorPart(x, 2));
            double m4 = (Double) data.receive(4, 2, "m4");
            double m1 = (Double) data.receive(1, 2, "m1");
            double m3 = (Double) data.receive(1, 2, "m3");
            double minX = Math.min(Math.min(m1, m2), Math.min(m3, m4));

            data.send(2, 1, "m", minX);
            data.send(2, 4, "m", minX);

            double[] p2 = Data.multiplyVectorByMatrixRows(Data.vectorPart(x, 2), ma2);
            data.send(2, 1, "P2", p2);

            double[] y = (double[]) data.receive(1, 2, "Y");
            double[] z2 = Data.computeZPart(y, ms2, f2, minX);
            data.send(2, 1, "Z2", z2);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
