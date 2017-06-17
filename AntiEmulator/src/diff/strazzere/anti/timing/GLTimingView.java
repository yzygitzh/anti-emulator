package diff.strazzere.anti.timing;

import android.content.Context;
import android.opengl.GLSurfaceView;

/**
 * Created by yzy on 6/17/17.
 */

public class GLTimingView extends GLSurfaceView {
    private final GLTimingRenderer mRenderer;

    public GLTimingView(Context context){
        super(context);
        setEGLContextClientVersion(2);
        mRenderer = new GLTimingRenderer();
        setRenderer(mRenderer);
    }

    public GLTimingRenderer getGLTimingRenderer() {
        return mRenderer;
    }
}
