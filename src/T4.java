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
            double[] f = Data.useRandomInput
                    ? Data.fillVectorRandom(Data.N, new Random(404))
                    : data.manualF;

            data.send(4, 2, "F1", Data.vectorPart(f, 1));
            data.send(4, 2, "F2", Data.vectorPart(f, 2));
            data.send(4, 3, "F3", Data.vectorPart(f, 3));

            double[] x4 = (double[]) data.receive(2, 4, "X4");
            double[][] ma4 = (double[][]) data.receive(3, 4, "MA4");
            double[][] ms4 = (double[][]) data.receive(3, 4, "MS4");

            double m4 = Data.min(x4);
            data.send(4, 2, "m4", m4);

            double minX = (Double) data.receive(2, 4, "m");
            double[] p4 = Data.multiplyVectorByMatrixRows(x4, ma4);
            data.send(4, 3, "P4", p4);

            double[] y = (double[]) data.receive(3, 4, "Y");
            double[] z4 = Data.computeZPart(y, ms4, Data.vectorPart(f, 4), minX);
            data.send(4, 3, "Z4", z4);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
