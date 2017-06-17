package diff.strazzere.anti.timing;

/**
 * Created by yzy on 6/17/17.
 */

public class FindSlowGraphic {
    private static double mLowestFPS;
    private static double mHighestFPS;

    public static void sampleFPS(GLTimingRenderer renderer) {
        mLowestFPS = 1000.0;
        mHighestFPS = 0.0;
        try {
            for (int i = 0; i < 20; i += 1) {
                if (i >= 10) {
                    double sampleFPS = renderer.mFPS;
                    mLowestFPS = mLowestFPS > sampleFPS ? sampleFPS : mLowestFPS;
                    mHighestFPS = mHighestFPS < sampleFPS ? sampleFPS : mHighestFPS;
                }
                Thread.sleep(50);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static boolean isSlowGraphic() {
        return mHighestFPS - mLowestFPS > 10.0;
    }

    public static double getLowestFPS() { return mLowestFPS; }
    public static double getHighestFPS() { return mHighestFPS; }
}
