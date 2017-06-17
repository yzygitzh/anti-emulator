package diff.strazzere.anti;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import diff.strazzere.anti.debugger.FindDebugger;
import diff.strazzere.anti.emulator.FindEmulator;
import diff.strazzere.anti.monkey.FindMonkey;
import diff.strazzere.anti.taint.FindTaint;
import diff.strazzere.anti.timing.FindSlowGraphic;
import diff.strazzere.anti.timing.GLTimingView;

public class MainActivity extends Activity {
    static final int REQUEST_CODE_READ_PHONE_STATE = 0;
    static TextView mTextView;
    static GLTimingView mTimingView;

    void detectedLog(String logStr) {
        log(logStr);
    }

    void unDetectedLog(String logStr) {
        log(logStr);
    }

    void detectSandbox() {
        final Handler textHandler = new Handler();
        new Thread() {
            @Override
            public void run() {
            super.run();
            final String threadName = Thread.currentThread().getName();
            final boolean taintTrackingDetected = isTaintTrackingDetected();
            final boolean monkeyDetected = isMonkeyDetected();
            final boolean debugged = isDebugged();
            final boolean QEmuEnvDetected = isQEmuEnvDetected();
            FindSlowGraphic.sampleFPS(mTimingView.getGLTimingRenderer());
            final double graphicLowestFPS = FindSlowGraphic.getLowestFPS();
            final double graphicHighestFPS = FindSlowGraphic.getHighestFPS();
            final boolean slowGraphicDetected = FindSlowGraphic.isSlowGraphic();

            textHandler.post(new Runnable() {
                public void run() {
                    mTextView.setText(
                        "threadName: " + threadName + "\n" +
                        "isTaintTrackingDetected: " + taintTrackingDetected + "\n" +
                        "isMonkeyDetected: " + monkeyDetected + "\n" +
                        "isDebugged: " + debugged + "\n" +
                        "isQEmuEnvDetected: " + QEmuEnvDetected + "\n" +
                        "graphicLowestFPS: " + graphicLowestFPS + "\n" +
                        "graphicHighestFPS: " + graphicHighestFPS + "\n" +
                        "slowGraphicDetected: " + slowGraphicDetected + "\n"
                    );
                }
            });
            }
        }.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = (TextView)findViewById(R.id.display_text_view);

        LinearLayout container = (LinearLayout) findViewById(R.id.main_container);
        mTimingView = new GLTimingView(this);
        container.addView(mTimingView);


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE_READ_PHONE_STATE);
        } else {
            detectSandbox();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_READ_PHONE_STATE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    detectSandbox();
                }
                return;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean isQEmuEnvDetected() {
        log("Checking for QEmu env...");
        log("hasKnownDeviceId : " + FindEmulator.hasKnownDeviceId(getApplicationContext()));
        log("hasKnownPhoneNumber : " + FindEmulator.hasKnownPhoneNumber(getApplicationContext()));
        log("isOperatorNameAndroid : " + FindEmulator.isOperatorNameAndroid(getApplicationContext()));
        log("hasKnownImsi : " + FindEmulator.hasKnownImsi(getApplicationContext()));
        log("hasEmulatorBuild : " + FindEmulator.hasEmulatorBuild(getApplicationContext()));
        log("hasPipes : " + FindEmulator.hasPipes());
        log("hasQEmuDriver : " + FindEmulator.hasQEmuDrivers());
        log("hasQEmuFiles : " + FindEmulator.hasQEmuFiles());
        log("hasGenyFiles : " + FindEmulator.hasGenyFiles());
        log("hasEmulatorAdb :" + FindEmulator.hasEmulatorAdb());
        log("hitsQemuBreakpoint : " + FindEmulator.checkQemuBreakpoint());
        if (FindEmulator.hasKnownDeviceId(getApplicationContext()) ||
            FindEmulator.hasKnownImsi(getApplicationContext()) ||
            FindEmulator.hasEmulatorBuild(getApplicationContext()) ||
            FindEmulator.hasKnownPhoneNumber(getApplicationContext()) ||
            FindEmulator.hasPipes() ||
            FindEmulator.hasQEmuDrivers() ||
            FindEmulator.hasEmulatorAdb() ||
            FindEmulator.hasQEmuFiles() ||
            FindEmulator.hasGenyFiles()) {
            detectedLog("QEmu environment detected.");
            return true;
        } else {
            unDetectedLog("QEmu environment not detected.");
            return false;
        }
    }

    public boolean isTaintTrackingDetected() {
        log("Checking for Taint tracking...");
        log("hasAppAnalysisPackage : " + FindTaint.hasAppAnalysisPackage(getApplicationContext()));
        log("hasTaintClass : " + FindTaint.hasTaintClass());
        log("hasTaintMemberVariables : " + FindTaint.hasTaintMemberVariables());
        if (FindTaint.hasAppAnalysisPackage(getApplicationContext()) ||
            FindTaint.hasTaintClass() ||
            FindTaint.hasTaintMemberVariables()) {
            detectedLog("Taint tracking was detected.");
            return true;
        } else {
            unDetectedLog("Taint tracking was not detected.");
            return false;
        }
    }

    public boolean isMonkeyDetected() {
        log("Checking for Monkey user...");
        log("isUserAMonkey : " + FindMonkey.isUserAMonkey());

        if (FindMonkey.isUserAMonkey()) {
            detectedLog("Monkey user was detected.");
            return true;
        } else {
            unDetectedLog("Monkey user was not detected.");
            return false;
        }
    }

    public boolean isDebugged() {
        log("Checking for debuggers...");

        boolean tracer = false;
        try {
            tracer = FindDebugger.hasTracerPid();
        } catch (Exception exception) {
            exception.printStackTrace();
        }

        if (FindDebugger.isBeingDebugged() || tracer) {
            detectedLog("Debugger was detected");
            return true;
        } else {
            unDetectedLog("No debugger was detected.");
            return false;
        }
    }

    public void log(String msg) {
        Log.v("AntiEmulator", msg);
    }
}
