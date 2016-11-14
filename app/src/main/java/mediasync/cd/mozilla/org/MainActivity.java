package mediasync.cd.mozilla.org;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import mediasync.cd.mozilla.org.alarms.AlarmManager;
import mediasync.cd.mozilla.org.model.Config;
import mediasync.cd.mozilla.org.util.Logger;

public class MainActivity extends AppCompatActivity {

    private TextView mExternalCounter;
    private ProgressBar mProgress;
    private Button mSyncButton;
    private SyncService mService = null;
    private Logger mLogger;
    private Switch mSwitchEnabled;

    private final static int READ_MEDIA_PERM = 0;

    private ServiceConnection mConnector = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            SyncService.SyncServiceBinder binder = (SyncService.SyncServiceBinder) service;
            mService = binder.getService();
            fillInformation();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLogger = Logger.getLogger(this, this.getResources().getString(R.string.bugfender));

        inflate();
        checkPermissions();
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_MEDIA_PERM);
        } else {
            launchService();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch(requestCode) {
            case READ_MEDIA_PERM: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    launchService();
                } else {
                    onDestroy();
                }
            }
        }
    }

    private void inflate() {
        mExternalCounter = (TextView) this.findViewById(R.id.externalImagesLabel);
        mProgress = (ProgressBar) this.findViewById(R.id.progressBar);
        mSyncButton = (Button) this.findViewById(R.id.syncButton);
        mSwitchEnabled = (Switch) this.findViewById(R.id.syncEnabledSwitch);

        mSwitchEnabled.setChecked(Config.syncEnabled(this));

        mSyncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgress.setProgress(0);
                if (mService == null) {
                    mLogger.w("Service not running while trying to interact");
                    Toast.makeText(MainActivity.this, "Sync Service not running", Toast.LENGTH_LONG).show();
                    return;
                }
                int mediaCount = mService.getMediaCount();
                mLogger.d("Found " + mediaCount + " medias in the device");
                mProgress.setMax(mediaCount);

                mService.startSync(new SyncService.IMediaSyncListener() {
                    @Override
                    public void step() {
                        mProgress.incrementProgressBy(1);
                    }

                    @Override
                    public void onFinish(int total, int ok, int ko) {
                        Toast.makeText(MainActivity.this, "Finished uploading, " + ok + " ok", Toast.LENGTH_LONG).show();
                        mLogger.d("Process finished with " + ok + " ok and " + ko + " ko");
                    }
                });
            }
        });

        mSwitchEnabled.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AlarmManager.getInstance(MainActivity.this).setAlarm(isChecked);
            }
        });
    }

    private void updateProgress() {
        mProgress.incrementProgressBy(1);
    }

    private void launchService() {
        mLogger.d("Launching service");
        Intent intent = new Intent(this, SyncService.class);
        bindService(intent, mConnector, Context.BIND_AUTO_CREATE);
    }

    private void fillInformation() {
        if (mService == null) {
            mExternalCounter.setText("Cant access media");
            return;
        }

        int result = mService.getMediaCount();

        if (result == -1) {
            mExternalCounter.setText("Cant access media");
        } else {
            mExternalCounter.setText(String.valueOf(result));
        }
    }
}
