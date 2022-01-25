package com.mopub.mobileads;

import com.mopub.common.logging.MoPubLog;

import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds.UnityAdsShowError;
import com.unity3d.ads.UnityAds.UnityAdsShowCompletionState;

import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.CUSTOM;
import static com.mopub.mobileads.UnityAdsAdapterConfiguration.UnityAdsConstants.LOG_FINISH_STATE;
import static com.mopub.mobileads.UnityAdsAdapterConfiguration.UnityAdsConstants.LOG_INTERSTITIAL_SHOW_COMPLETED;
import com.mopub.mobileads.UnityAdsAdapterConfiguration.AdEvent;

public class UnityInterstitial extends UnityVideoAd {

    private static final String ADAPTER_NAME = UnityInterstitial.class.getSimpleName();

    @Override
    protected IUnityAdsShowListener getIUnityAdsShowListener() {
        return new IUnityAdsShowListener() {
            @Override
            public void onUnityAdsShowStart(String placementId) {
                UnityInterstitial.super.onUnityAdsShowStart(placementId);
            }

            @Override
            public void onUnityAdsShowClick(String placementId) {
                UnityInterstitial.super.onUnityAdsShowClick(placementId);
            }

            @Override
            public void onUnityAdsShowComplete(String placementId, UnityAdsShowCompletionState state) {
                MoPubLog.log(CUSTOM, ADAPTER_NAME, LOG_FINISH_STATE.getMessage() + state);
                MoPubLog.log(CUSTOM, ADAPTER_NAME, LOG_INTERSTITIAL_SHOW_COMPLETED.getMessage() + placementId);
                UnityEventAdapter.sendAdPlaybackEvent(mInteractionListener, AdEvent.DISMISS);
            }

            @Override
            public void onUnityAdsShowFailure(String placementId, UnityAdsShowError error, String message) {
                UnityInterstitial.super.onUnityAdsShowFailure(placementId, error, message);
            }
        };
    }

    @Override
    protected String getDefaultPlacementId() {
        return "video";
    }
}
