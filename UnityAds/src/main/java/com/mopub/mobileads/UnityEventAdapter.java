package com.mopub.mobileads;

import com.mopub.common.MoPubReward;
import com.mopub.common.logging.MoPubLog;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.CUSTOM;

public class UnityEventAdapter {

    private static final String ADAPTER_NAME = UnityEventAdapter.class.getSimpleName();

    UnityEventAdapter() {
    }

    /**
     * Sends a load success event to MoPub.
     * @param mLoadListener The LoadListener to forward load success to MoPub.
     */
    public static void sendAdLoadedEvent(AdLifecycleListener.LoadListener mLoadListener) {
        if (mLoadListener == null) {
            MoPubLog.log(CUSTOM, ADAPTER_NAME, "ERROR: Cannot forward load success to MoPub, load listener is null");
            return;
        }

        mLoadListener.onAdLoaded();
    }

    /**
     * Sends a load failure event to MoPub.
     * @param mLoadListener The LoadListener to forward load failure to MoPub.
     * @param errorCode The MoPubErrorCode of the load error.
     */
    public static void sendAdFailedToLoadEvent(AdLifecycleListener.LoadListener mLoadListener, MoPubErrorCode errorCode) {
        if (mLoadListener == null) {
            MoPubLog.log(CUSTOM, ADAPTER_NAME, "ERROR: Cannot forward load failure to MoPub, load listener is null");
            return;
        }

        mLoadListener.onAdLoadFailed(errorCode);
    }

    /**
     * Calls sendAdPlaybackEvent with a null MoPubErrorCode to forward an ad event to MoPub.
     *
     * @param mInteractionListener    The InteractionListener of the ad currently showing or being attempted to show.
     * @param adEvent                 The ad event to be forwarded.
     *
     */
    public static void sendAdPlaybackEvent(AdLifecycleListener.InteractionListener mInteractionListener, UnityAdsAdapterConfiguration.AdEvent adEvent) {
        sendAdPlaybackEvent(mInteractionListener, adEvent, null);
    }

    /**
     * Forwards an ad event to MoPub.
     *
     * @param mInteractionListener    The InteractionListener of the ad currently showing or being attempted to show.
     * @param adEvent                 The ad event to be forwarded.
     * @param errorCode               The MoPubErrorCode of the error in the case of an ad failing to show.
     */
    public static void sendAdPlaybackEvent(AdLifecycleListener.InteractionListener mInteractionListener, UnityAdsAdapterConfiguration.AdEvent adEvent, MoPubErrorCode errorCode) {
        if (mInteractionListener == null) {
            MoPubLog.log(CUSTOM, ADAPTER_NAME, "ERROR: Cannot forward ad event to MoPub, interaction listener is null");
            return;
        }

        switch (adEvent) {
            case SHOW:
                mInteractionListener.onAdShown();
                break;
            case SHOW_FAILED:
                mInteractionListener.onAdFailed(errorCode);
                break;
            case CLICK:
                mInteractionListener.onAdClicked();
                break;
            case IMPRESSION:
                mInteractionListener.onAdImpression();
                break;
            case DISMISS:
                mInteractionListener.onAdDismissed();
                break;
            case COMPLETE:
                mInteractionListener.onAdComplete(MoPubReward.success(MoPubReward.NO_REWARD_LABEL,
                        MoPubReward.DEFAULT_REWARD_AMOUNT));
                break;
            default:
                MoPubLog.log(CUSTOM, ADAPTER_NAME, "ERROR: Cannot report unknown event");
                break;
        }
    }
}
