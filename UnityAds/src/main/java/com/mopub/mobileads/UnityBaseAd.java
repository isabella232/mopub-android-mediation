package com.mopub.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mopub.common.LifecycleListener;
import com.mopub.common.Preconditions;
import com.mopub.common.logging.MoPubLog;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.UnityAds;

import java.util.Map;

import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.CUSTOM;
import static com.mopub.mobileads.UnityAdsAdapterConfiguration.UnityAdsConstants.GAME_ID_KEY;
import static com.mopub.mobileads.UnityAdsAdapterConfiguration.UnityAdsConstants.LOG_GAME_ID_MISSING;
import static com.mopub.mobileads.UnityAdsAdapterConfiguration.UnityAdsConstants.LOG_INIT_FAILED_WITH_ERROR;
import static com.mopub.mobileads.UnityAdsAdapterConfiguration.UnityAdsConstants.LOG_INIT_SUCCESS;
import static com.mopub.mobileads.UnityAdsAdapterConfiguration.UnityAdsConstants.LOG_PLACEMENT_ID_MISSING;
import static com.mopub.mobileads.UnityAdsAdapterConfiguration.UnityAdsConstants.PLACEMENT_ID_KEY;
import static com.mopub.mobileads.UnityAdsAdapterConfiguration.UnityAdsConstants.ZONE_ID_KEY;

public abstract class UnityBaseAd extends BaseAd {
    private static final String ADAPTER_NAME = UnityBaseAd.class.getSimpleName();
    protected Activity mActivity;
    protected String mPlacementId = "";

    protected abstract String getDefaultPlacementId();

    @Nullable
    @Override
    protected LifecycleListener getLifecycleListener() {
        return null;
    }

    @NonNull
    @Override
    protected String getAdNetworkId() {
        return mPlacementId;
    }

    @Override
    protected boolean checkAndInitializeSdk(@NonNull Activity launcherActivity, @NonNull AdData adData) {
        Preconditions.checkNotNull(launcherActivity);
        Preconditions.checkNotNull(adData);

        mActivity = launcherActivity;
        return UnityAdsInitializer.getInstance().checkOrInitUnityAds(mActivity, adData.getExtras());
    }

    public String placementIdForServerExtras(Map<String, String> serverExtras) {
        String placementId = null;
        if (serverExtras.containsKey(PLACEMENT_ID_KEY.getMessage())) {
            placementId = serverExtras.get(PLACEMENT_ID_KEY.getMessage());
        } else if (serverExtras.containsKey(ZONE_ID_KEY.getMessage())) {
            placementId = serverExtras.get(ZONE_ID_KEY.getMessage());
        }
        return TextUtils.isEmpty(placementId) ? getDefaultPlacementId() : placementId;
    }

    @Override
    protected void load(@NonNull Context context, @NonNull AdData adData) {
        Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(adData);

        final Map<String, String> extras = adData.getExtras();

        UnityAdsInitializer.getInstance().checkOrInitUnityAds((Activity) context, extras);

        mPlacementId = placementIdForServerExtras(extras);
        setAutomaticImpressionAndClickTracking(false);
    }

    // Returns true if placementId is null and reports the load failure
    public boolean failIfPlacementIdNull() {
        if (mPlacementId == null) {
            MoPubLog.log(CUSTOM, ADAPTER_NAME, LOG_PLACEMENT_ID_MISSING.getMessage());
            if (mLoadListener != null) {
                mLoadListener.onAdLoadFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
            }
            return true;
        }
        return false;
    }
}
