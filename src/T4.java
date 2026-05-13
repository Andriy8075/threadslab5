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
            double[] x = (double[]) data.receive(2, 4, "X");
            data.receive(3, 4, "MA4");
            data.receive(3, 4, "MS4");
            double[][] ma = (double[][]) data.receive(3, 4, "MA");
            double[][] ms = (double[][]) data.receive(3, 4, "MS");

            double m4 = Data.min(x4);
            data.send(4, 2, "m4", m4);

            double minX = (Double) data.receive(2, 4, "m");
            double[] z4 = Data.computeZPart(x, ma, ms, Data.vectorPart(f, 4), minX, 4);
            data.send(4, 3, "Z4", z4);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        }
    }
}
