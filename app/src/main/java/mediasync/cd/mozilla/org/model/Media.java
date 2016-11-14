package mediasync.cd.mozilla.org.model;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by arcturus on 13/11/2016.
 */

public class Media extends RealmObject {

    @PrimaryKey
    private String mId;
    private String mData;
    private Integer mSize;
    @Index
    private String mDisplayName;
    private String mTitle;
    private String mDateAdded;
    private String mDatedModified;
    private String mDescription;
    private Double mLatitude;
    private Double mLongitude;
    private String mBucketId;
    private String mBucketDisplayName;
    private Integer mWidth;
    private Integer mHeight;

    public String getId() {
        return mId;
    }

    public Media setId(String mId) {
        this.mId = mId;
        return this;
    }

    public String getData() {
        return mData;
    }

    public Media setData(String mData) {
        this.mData = mData;
        return this;
    }

    public Integer getSize() {
        return mSize;
    }

    public Media setSize(Integer mSize) {
        this.mSize = mSize;
        return this;
    }

    public String getDisplayName() {
        return mDisplayName;
    }

    public Media setDisplayName(String mDisplayName) {
        this.mDisplayName = mDisplayName;
        return this;
    }

    public String getTitle() {
        return mTitle;
    }

    public Media setTitle(String mTitle) {
        this.mTitle = mTitle;
        return this;
    }

    public String getDateAdded() {
        return mDateAdded;
    }

    public Media setDateAdded(String mDateAdded) {
        this.mDateAdded = mDateAdded;
        return this;
    }

    public String getDatedModified() {
        return mDatedModified;
    }

    public Media setDatedModified(String mDatedModified) {
        this.mDatedModified = mDatedModified;
        return this;
    }

    public String getDescription() {
        return mDescription;
    }

    public Media setDescription(String mDescription) {
        this.mDescription = mDescription;
        return this;
    }

    public Double getLatitude() {
        return mLatitude;
    }

    public Media setLatitude(Double mLatitude) {
        this.mLatitude = mLatitude;
        return this;
    }

    public Double getLongitude() {
        return mLongitude;
    }

    public Media setLongitude(Double mLongitude) {
        this.mLongitude = mLongitude;
        return this;
    }

    public String getBucketId() {
        return mBucketId;
    }

    public Media setBucketId(String mBucketId) {
        this.mBucketId = mBucketId;
        return this;
    }

    public String getBucketDisplayName() {
        return mBucketDisplayName;
    }

    public Media setBucketDisplayName(String mBucketDisplayName) {
        this.mBucketDisplayName = mBucketDisplayName;
        return this;
    }

    public Integer getWidth() {
        return mWidth;
    }

    public Media setWidth(Integer mWidth) {
        this.mWidth = mWidth;
        return this;
    }

    public Integer getHeight() {
        return mHeight;
    }

    public Media setHeight(Integer mHeight) {
        this.mHeight = mHeight;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Media) {
            return mId == ((Media) o).mId;
        }
        return false;
    }

}
