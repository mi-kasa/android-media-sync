package mediasync.cd.mozilla.org;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.provider.MediaStore;

import java.io.FileDescriptor;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import mediasync.cd.mozilla.org.util.Logger;

public class SyncService extends Service {

    private IBinder mBinder = new SyncServiceBinder();
    private NetworkManager mNetManager;
    private List<Map<String, String>> okResponses = new ArrayList<>();
    private List<Map<String, String>> koResponses = new ArrayList<>();
    private Logger mLogger;

    @Override
    public void onCreate() {
        super.onCreate();
        mLogger = Logger.getLogger(this, this.getResources().getString(R.string.bugfender));
        mNetManager = NetworkManager.getInstance(getApplicationContext());
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public class SyncServiceBinder extends Binder {
        public SyncService getService() {
            return SyncService.this;
        }
    }

    // Service operations
    public int getMediaCount() {
        MediaStore.Images.Media mediaStore = new MediaStore.Images.Media();
        Uri.Builder builder = new Uri.Builder();
        Uri uri = builder.scheme("content").authority("media").appendEncodedPath("external/images/media").build();
        grantUriPermission("mediasync.cd.mozilla.org", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Cursor cursor = mediaStore.query(getContentResolver(), uri, null);
        int result = -1;
        if (cursor != null && cursor.moveToFirst()) {
            result = cursor.getCount();
        }
        cursor.close();
        return result;
    }

    public void startSync(IMediaSyncListener listener) {
        mLogger.d("Starting sync process");
        MediaStore.Images.Media mediaStore = new MediaStore.Images.Media();
        Uri.Builder builder = new Uri.Builder();
        Uri uri = builder.scheme("content").authority("media").appendEncodedPath("external/images/media").build();
        grantUriPermission("mediasync.cd.mozilla.org", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Cursor cursor = mediaStore.query(getContentResolver(), uri, null);

        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        if (cursor != null && cursor.moveToFirst()) {
            int columns = cursor.getColumnCount();
            do {
                Map<String, String> elem = new HashMap<String, String>();
                for(int i = 0; i < columns; i++) {
                    elem.put(cursor.getColumnName(i), cursor.getString(i));
                }
                data.add(elem);
            } while (cursor.moveToNext());
            cursor.close();
            uploadFiles(data, listener);
        }
    }

    private void uploadFiles(List<Map<String, String>> datas, IMediaSyncListener listener) {
        okResponses.clear();
        koResponses.clear();
        Queue<Map<String, String>> queue = new ArrayDeque<>(datas);
        Map<String, String> data = queue.remove();

        uploadFile(queue, listener);
    }

    private void uploadFinished(IMediaSyncListener listener) {
        int ok = okResponses.size();
        int ko = koResponses.size();
        System.out.println("Process finished");
        System.out.println("OK uploads: " + ok);
        System.out.println("Failed uploads: " + ko);

        listener.onFinish(ok + ko, ok, ko);
    }

    private void uploadFile(final Queue<Map<String, String>> restOfDatas, final IMediaSyncListener listener) {
        if (restOfDatas.size() == 0) {
            uploadFinished(listener);
            return;
        }

        Map<String, String> data = restOfDatas.poll();

        mNetManager.uploadFile(data, new NetworkManager.IUploadListener() {
            @Override
            public void onResponse(Map<String, String> data, String response) {
                data.put("response", response);
                okResponses.add(data);
                listener.step();
                uploadFile(restOfDatas, listener);
            }

            @Override
            public void onError(Map<String, String> data, String error) {
                data.put("error", error);
                koResponses.add(data);
                listener.step();
                uploadFile(restOfDatas, listener);
            }
        });
    }

    public interface IMediaSyncListener {
        public void step();
        public void onFinish(int total, int ok, int ko);
    }
}
