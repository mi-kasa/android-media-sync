package mediasync.cd.mozilla.org;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.text.TextUtils;

import java.io.FileDescriptor;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import mediasync.cd.mozilla.org.model.Media;
import mediasync.cd.mozilla.org.util.Logger;

public class SyncService extends Service {

    private IBinder mBinder = new SyncServiceBinder();
    private NetworkManager mNetManager;
    private List<Map<String, String>> okResponses = new ArrayList<>();
    private List<Map<String, String>> koResponses = new ArrayList<>();
    private Logger mLogger;
    private Realm mRealm;

    public final static String COMMAND_SYNC = "sync";

    @Override
    public void onCreate() {
        super.onCreate();
        mLogger = Logger.getLogger(this, this.getResources().getString(R.string.bugfender));
        mNetManager = NetworkManager.getInstance(getApplicationContext());
        Realm.init(this);
        mRealm = Realm.getDefaultInstance();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        String command = intent.getStringExtra("command");

        if (command.equals(COMMAND_SYNC)) {
            startSync();
        }

        return Service.START_NOT_STICKY;
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

    public int getMediaLeftCount() {
        MediaStore.Images.Media mediaStore = new MediaStore.Images.Media();
        Uri.Builder builder = new Uri.Builder();
        Uri uri = builder.scheme("content").authority("media").appendEncodedPath("external/images/media").build();
        grantUriPermission("mediasync.cd.mozilla.org", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        String selection = "_id NOT IN (" + TextUtils.join(",", getSyncedIds()) + ")";
        Cursor cursor = mediaStore.query(getContentResolver(), uri, null, selection, null);
        int result = -1;
        if (cursor != null && cursor.moveToFirst()) {
            result = cursor.getCount();
        }
        cursor.close();
        return result;
    }

    public int getNumberOfMediaSynced() {
        RealmQuery<Media> query = mRealm.where(Media.class);

        RealmResults<Media> results = query.findAll();
        return results.size();
    }

    private List<String> getSyncedIds() {
        RealmQuery<Media> query = mRealm.where(Media.class);

        RealmResults<Media> results = query.findAll();
        List<String> ids = new ArrayList<>();
        Iterator<Media> it = results.iterator();
        while(it.hasNext()) {
            ids.add(((Media)it.next()).getId());
        }

        return ids;
    }

    public void startSync() {
        // Silent sync that will call onDestroy after the process is finished
        mLogger.d("Starting silent sync");
        startSync(new IMediaSyncListener() {
            @Override
            public void step() {

            }

            @Override
            public void onFinish(int total, int ok, int ko) {
                mLogger.d("Silent sync finished, killing the service");
                SyncService.this.onDestroy();
            }
        });
    }

    public void startSync(IMediaSyncListener listener) {
        mLogger.d("Starting sync process");
        MediaStore.Images.Media mediaStore = new MediaStore.Images.Media();
        Uri.Builder builder = new Uri.Builder();
        Uri uri = builder.scheme("content").authority("media").appendEncodedPath("external/images/media").build();
        grantUriPermission("mediasync.cd.mozilla.org", uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        String selection = "_id NOT IN (" + TextUtils.join(",", getSyncedIds()) + ")";
        Cursor cursor = mediaStore.query(getContentResolver(), uri, null, selection, null);

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
                saveResult(data);
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

    private void saveResult(Map<String, String> data) {
        mRealm.beginTransaction();

        Media media = mRealm.createObject(Media.class, data.get("_id"));
        media.setData(data.get("_data"));
        media.setSize(Integer.parseInt(data.get("_size")));
        media.setDisplayName(data.get("_display_name"));
        media.setTitle(data.get("title"));
        media.setDateAdded(data.get("date_added"));
        media.setDatedModified(data.get("date_modified"));
        media.setDescription(data.get("description"));
        if (data.get("latitude") != null) {
            media.setLatitude(Double.parseDouble(data.get("latitude")));
        }
        if (data.get("longitude") != null) {
            media.setLongitude(Double.parseDouble(data.get("longitude")));
        }
        media.setBucketId(data.get("bucket_id"));
        media.setBucketDisplayName(data.get("bucket_display_name"));
        media.setWidth(Integer.parseInt(data.get("width")));
        media.setHeight(Integer.parseInt(data.get("height")));

        mRealm.commitTransaction();
    }

    public interface IMediaSyncListener {
        public void step();
        public void onFinish(int total, int ok, int ko);
    }
}
