package com.mopub.mobileads;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;

import com.mopub.common.BaseAdapterConfiguration;
import com.mopub.common.OnNetworkInitializationFinishedListener;
import com.mopub.common.Preconditions;
import com.mopub.common.logging.MoPubLog;
import com.mopub.mobileads.unityads.BuildConfig;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.UnityAds;

import java.util.Map;

import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.CUSTOM;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.CUSTOM_WITH_THROWABLE;

public class UnityAdsAdapterConfiguration extends BaseAdapterConfiguration {

    public static final String ADAPTER_VERSION = com.mopub.mobileads.unityads.BuildConfig.VERSION_NAME;
    public static final String MOPUB_NETWORK_NAME = BuildConfig.NETWORK_NAME;

    /**
     * An enumeration of ad events that can occur during ad playback or playback attempt.
     *
     */
    public enum AdEvent {
        SHOW,
        SHOW_FAILED,
        CLICK,
        IMPRESSION,
        DISMISS,
        COMPLETE
    }

    public enum UnityAdsConstants {
        GAME_ID_KEY("gameId"),
        ZONE_ID_KEY("zoneId"),
        PLACEMENT_ID_KEY("placementId"),
        LOG_INIT_SUCCESS("Unity Ads successfully initialized."),
        LOG_ALREADY_INITIALIZED("Unity Ads already initialized. Not attempting to reinitialize."),
        LOG_CONFIGURATION_NULL("Unity Ads initialization failed. Configuration is null.  Note that initialization on the first app launch is a no-op. It will attempt again on first ad request."),
        LOG_GAME_ID_MISSING("Unity Ads initialization failed. Parameter gameId is missing or entered incorrectly in the Unity Ads network configuration."),
        LOG_PLACEMENT_ID_MISSING("Parameter placementId is missing or entered incorrectly in the Unity Ads network configuration."),
        LOG_INIT_FAILED_WITH_ERROR("Unity Ads initialization failed with error: "),
        LOG_INIT_EXCEPTION("Initializing Unity Ads has encountered an exception."),
        LOG_LOAD_SUCCESS("Unity ad successfully loaded for placement: "),
        LOG_LOAD_FAILED("Unity ad failed to load for placement: "),
        LOG_BANNER_UNSUPPORTED("Unity Ads does not support medium rectangle ads."),
        LOG_BANNER_LOAD_FAILED("Unity Ads failed to load banner for placement {0} with error {1}"),

        LOG_SHOW_ACTIVITY_NULL("Failed to show Unity ad as the activity calling it is null."),
        LOG_SHOW_AD_FAILURE("Unity Ads encountered a video playback error for placement {0}, caused by: [{1}] {2}"),
        LOG_INTERSTITIAL_SHOW_COMPLETED("Unity interstitial video completed for placement: "),
        LOG_REWARDED_SHOW_COMPLETED("Unity rewarded video completed for placement "),
        LOG_FINISH_STATE("Unity Ad finished with finish state = "),
        LOG_SKIP("Unity ad was skipped, no reward will be given.");

        private String message;

        UnityAdsConstants(final String message) {
            this.message = message;
        }

        public String getMessage() {
            return message;
        }

        public MoPubErrorCode getInitCode() {
            switch (this) {
                case LOG_ALREADY_INITIALIZED:
                case LOG_CONFIGURATION_NULL:
                case LOG_GAME_ID_MISSING:
                case LOG_INIT_FAILED_WITH_ERROR:
                    return MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR;
                case LOG_INIT_SUCCESS:
                    return MoPubErrorCode.ADAPTER_INITIALIZATION_SUCCESS;
                default:
                    return MoPubErrorCode.UNSPECIFIED;
            }
        }

        @Override
        public String toString() {
            return this.getMessage();
        }
    }

    private static final String ADAPTER_NAME = UnityAdsAdapterConfiguration.class.getSimpleName();

    @NonNull
    @Override
    public String getAdapterVersion() {
        return ADAPTER_VERSION;
    }

    @Nullable
    @Override
    public String getBiddingToken(@NonNull Context context) {
        Preconditions.checkNotNull(context);
        return UnityAds.getToken();
    }

    @NonNull
    @Override
    public String getMoPubNetworkName() {
        return MOPUB_NETWORK_NAME;
    }

    @NonNull
    @Override
    public String getNetworkSdkVersion() {
        final String sdkVersion = UnityAds.getVersion();

        if (!TextUtils.isEmpty(sdkVersion)) {
            return sdkVersion;
        }

        final String adapterVersion = getAdapterVersion();
        return adapterVersion.substring(0, adapterVersion.lastIndexOf('.'));
    }

    @Override
    public void initializeNetwork(@NonNull final Context context, @Nullable final Map<String, String> configuration, @NonNull final OnNetworkInitializationFinishedListener listener) {
        Preconditions.checkNotNull(context);
        Preconditions.checkNotNull(listener);

        synchronized (UnityAdsAdapterConfiguration.class) {
            try {
                if (UnityAds.isInitialized()) {
                    sendInitializationResult(listener, UnityAdsConstants.LOG_ALREADY_INITIALIZED);
                    return;
                }

                if (configuration == null) {
                    sendInitializationResult(listener, UnityAdsConstants.LOG_CONFIGURATION_NULL);
                    return;
                }

                final String gameId = configuration.get(UnityAdsConstants.GAME_ID_KEY.getMessage());
                if (gameId == null || gameId.isEmpty()) {
                    sendInitializationResult(listener, UnityAdsConstants.LOG_GAME_ID_MISSING);
                    return;
                }

                UnityAdsInitializer.getInstance().initializeUnityAds(context, gameId, new IUnityAdsInitializationListener() {
                    @Override
                    public void onInitializationComplete() {
                        sendInitializationResult(listener, UnityAdsConstants.LOG_INIT_SUCCESS);
                    }

                    @Override
                    public void onInitializationFailed(UnityAds.UnityAdsInitializationError unityAdsInitializationError, String errorMessage) {
                        sendInitializationResult(listener, UnityAdsConstants.LOG_INIT_FAILED_WITH_ERROR, errorMessage);
                    }
                });
            } catch (Exception e) {
                MoPubLog.log(CUSTOM_WITH_THROWABLE, UnityAdsConstants.LOG_INIT_EXCEPTION, e);
                listener.onNetworkInitializationFinished(UnityAdsAdapterConfiguration.class, MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
            }
        }
    }

    private void sendInitializationResult(@NonNull final OnNetworkInitializationFinishedListener listener, final UnityAdsConstants result) {
        sendInitializationResult(listener, result, "");
    }

    private void sendInitializationResult(@NonNull final OnNetworkInitializationFinishedListener listener, final UnityAdsConstants result, final String additionalErrorMessage) {
        String message = additionalErrorMessage.equals("") ? result.getMessage() : result.getMessage() + " " + additionalErrorMessage;
        MoPubLog.log(CUSTOM, ADAPTER_NAME, message);
        listener.onNetworkInitializationFinished(UnityAdsAdapterConfiguration.class, result.getInitCode());
    }
}
