package com.mopub.mobileads;

import com.mopub.common.MoPubReward;
import com.mopub.common.logging.MoPubLog;
import com.unity3d.ads.IUnityAdsShowListener;
import com.unity3d.ads.UnityAds.UnityAdsShowError;
import com.unity3d.ads.UnityAds.UnityAdsShowCompletionState;

import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.CUSTOM;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.SHOULD_REWARD;
import static com.mopub.mobileads.UnityAdsAdapterConfiguration.UnityAdsConstants.LOG_FINISH_STATE;
import static com.mopub.mobileads.UnityAdsAdapterConfiguration.UnityAdsConstants.LOG_REWARDED_SHOW_COMPLETED;
import static com.mopub.mobileads.UnityAdsAdapterConfiguration.UnityAdsConstants.LOG_SKIP;

public class UnityRewardedVideo extends UnityVideoAd {
    private static final String ADAPTER_NAME = UnityRewardedVideo.class.getSimpleName();

    @Override
    protected IUnityAdsShowListener getIUnityAdsShowListener() {
        return new IUnityAdsShowListener() {
            @Override
            public void onUnityAdsShowStart(String placementId) {
                UnityRewardedVideo.super.onUnityAdsShowStart(placementId);
            }

            @Override
            public void onUnityAdsShowClick(String placementId) {
                UnityRewardedVideo.super.onUnityAdsShowClick(placementId);
            }

            @Override
            public void onUnityAdsShowComplete(String placementId, UnityAdsShowCompletionState state) {
                MoPubLog.log(CUSTOM, ADAPTER_NAME, LOG_FINISH_STATE.getMessage() + state);

                switch (state) {
                    case COMPLETED:
                        MoPubLog.log(SHOULD_REWARD, ADAPTER_NAME, MoPubReward.NO_REWARD_AMOUNT, MoPubReward.NO_REWARD_LABEL);
                        if (mInteractionListener != null) {
                            mInteractionListener.onAdComplete(MoPubReward.success(MoPubReward.NO_REWARD_LABEL,
                                    MoPubReward.DEFAULT_REWARD_AMOUNT));
                            MoPubLog.log(CUSTOM, ADAPTER_NAME, LOG_REWARDED_SHOW_COMPLETED.getMessage() +
                                    placementId);
                        }
                        break;
                    case SKIPPED:
                        MoPubLog.log(CUSTOM, ADAPTER_NAME, LOG_SKIP.getMessage());
                        break;
                    default:
                        break;
                }

                if (mInteractionListener != null) {
                    mInteractionListener.onAdDismissed();
                }
            }

            @Override
            public void onUnityAdsShowFailure(String placementId, UnityAdsShowError error, String message) {
                UnityRewardedVideo.super.onUnityAdsShowFailure(placementId, error, message);
            }
        };
    }

    @Override
    protected String getDefaultPlacementId() {
        return "rewardedVideo";
    }
}
