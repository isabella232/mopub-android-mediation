package com.mopub.mobileads;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.mopub.common.DataKeys;
import com.mopub.common.logging.MoPubLog;
import com.unity3d.ads.IUnityAdsLoadListener;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.UnityAdsLoadOptions;
import com.unity3d.ads.UnityAdsShowOptions;
import com.unity3d.ads.metadata.MediationMetaData;

import java.util.UUID;

import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.CLICKED;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.CUSTOM;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.LOAD_ATTEMPTED;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.LOAD_FAILED;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.LOAD_SUCCESS;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.SHOW_ATTEMPTED;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.SHOW_FAILED;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.SHOW_SUCCESS;
import static com.mopub.mobileads.UnityAdsAdapterConfiguration.UnityAdsConstants.LOG_LOAD_FAILED;
import static com.mopub.mobileads.UnityAdsAdapterConfiguration.UnityAdsConstants.LOG_LOAD_SUCCESS;
import static com.mopub.mobileads.UnityAdsAdapterConfiguration.UnityAdsConstants.LOG_PLACEMENT_ID_MISSING;
import static com.mopub.mobileads.UnityAdsAdapterConfiguration.UnityAdsConstants.LOG_SHOW_ACTIVITY_NULL;
import static com.mopub.mobileads.UnityAdsAdapterConfiguration.UnityAdsConstants.LOG_SHOW_AD_FAILURE;

public abstract class UnityVideoAd extends UnityBaseAd {
    private static final String ADAPTER_NAME = UnityVideoAd.class.getSimpleName();
    private String mObjectId;

    protected int impressionOrdinal;
    protected int missedImpressionOrdinal;

    protected abstract IUnityAdsShowListener getIUnityAdsShowListener();

    @Override
    protected void onInvalidate() {
    }

    @Override
    protected void load(@NonNull Context context, @NonNull AdData adData) {
        super.load(context, adData);
        if (failIfPlacementIdNull()) {
            return;
        }

        MoPubLog.log(getAdNetworkId(), LOAD_ATTEMPTED, ADAPTER_NAME);
        final UnityAdsLoadOptions loadOptions = new UnityAdsLoadOptions();

        final String markup = adData.getExtras().get(DataKeys.ADM_KEY);
        if (markup != null) {
            loadOptions.setAdMarkup(markup);
            mObjectId = UUID.randomUUID().toString();
            loadOptions.setObjectId(mObjectId);
        }
        UnityAds.load(mPlacementId, loadOptions, mUnityLoadListener);
    }

    /**
     * IUnityAdsLoadListener instance. Contains ad load success and fail logic.
     */
    private IUnityAdsLoadListener mUnityLoadListener = new IUnityAdsLoadListener() {
        @Override
        public void onUnityAdsAdLoaded(String placementId) {
            mPlacementId = placementId;
            MoPubLog.log(CUSTOM, ADAPTER_NAME, LOG_LOAD_SUCCESS.getMessage() + placementId);
            MoPubLog.log(LOAD_SUCCESS, ADAPTER_NAME);

            if (mLoadListener != null) {
                mLoadListener.onAdLoaded();
            }
        }

        @Override
        public void onUnityAdsFailedToLoad(String placementId, UnityAds.UnityAdsLoadError error, String message) {
            mPlacementId = placementId;
            MoPubLog.log(CUSTOM, ADAPTER_NAME, LOG_LOAD_FAILED.getMessage() + placementId);
            MoPubLog.log(LOAD_FAILED, ADAPTER_NAME, MoPubErrorCode.NETWORK_NO_FILL.getIntCode(), MoPubErrorCode.NETWORK_NO_FILL);

            if (mLoadListener != null) {
                mLoadListener.onAdLoadFailed(MoPubErrorCode.NETWORK_NO_FILL);
            }
        }
    };

    @Override
    public void show() {
        MoPubLog.log(SHOW_ATTEMPTED, ADAPTER_NAME);
        MediationMetaData mediationMetaData = new MediationMetaData(mActivity);

        if (mActivity == null || mPlacementId == null) {
            if (mActivity == null) {
                MoPubLog.log(CUSTOM, ADAPTER_NAME, LOG_SHOW_ACTIVITY_NULL.getMessage());
            }
            if (!TextUtils.isEmpty(mPlacementId)) {
                MoPubLog.log(CUSTOM, ADAPTER_NAME, LOG_PLACEMENT_ID_MISSING.getMessage());
            }
            MoPubLog.log(SHOW_FAILED, ADAPTER_NAME, MoPubErrorCode.VIDEO_PLAYBACK_ERROR.getIntCode(), MoPubErrorCode.VIDEO_PLAYBACK_ERROR);

            mediationMetaData.setMissedImpressionOrdinal(++missedImpressionOrdinal);
            mediationMetaData.commit();

            if (mInteractionListener != null) {
                mInteractionListener.onAdFailed(MoPubErrorCode.VIDEO_PLAYBACK_ERROR);
            }
            return;
        }

        mediationMetaData.setOrdinal(++impressionOrdinal);
        mediationMetaData.commit();

        final UnityAdsShowOptions showOptions = new UnityAdsShowOptions();
        if (mObjectId != null) {
            showOptions.setObjectId(mObjectId);
        }

        UnityAds.show(mActivity, mPlacementId, showOptions, getIUnityAdsShowListener());
    }

    protected void onUnityAdsShowStart(String placementId) {
        MoPubLog.log(SHOW_SUCCESS, ADAPTER_NAME);

        if (mInteractionListener != null) {
            mInteractionListener.onAdShown();
            mInteractionListener.onAdImpression();
        }
    }

    protected void onUnityAdsShowClick(String placementId) {
        MoPubLog.log(CLICKED, ADAPTER_NAME);

        if (mInteractionListener != null) {
            mInteractionListener.onAdClicked();
        }
    }

    protected void onUnityAdsShowFailure(String placementId, UnityAds.UnityAdsShowError error, String message) {
        MoPubLog.log(CUSTOM, ADAPTER_NAME, String.format(LOG_SHOW_AD_FAILURE.getMessage(), placementId, error, message));

        MoPubLog.log(SHOW_FAILED, ADAPTER_NAME,
                MoPubErrorCode.AD_SHOW_ERROR.getIntCode(),
                MoPubErrorCode.AD_SHOW_ERROR);
        if (mInteractionListener != null) {
            mInteractionListener.onAdFailed(MoPubErrorCode.AD_SHOW_ERROR);
        }
    }
}
