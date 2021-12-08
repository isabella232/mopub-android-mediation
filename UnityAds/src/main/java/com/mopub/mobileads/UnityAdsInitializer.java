package com.mopub.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.mopub.common.MoPub;
import com.mopub.common.logging.MoPubLog;
import com.mopub.common.privacy.ConsentStatus;
import com.mopub.common.privacy.PersonalInfoManager;
import com.unity3d.ads.IUnityAdsInitializationListener;
import com.unity3d.ads.UnityAds;
import com.unity3d.ads.metadata.MediationMetaData;
import com.unity3d.ads.metadata.MetaData;

import java.util.Map;

import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.CUSTOM;
import static com.mopub.mobileads.UnityAdsAdapterConfiguration.ADAPTER_VERSION;
import static com.mopub.mobileads.UnityAdsAdapterConfiguration.UnityAdsConstants.GAME_ID_KEY;
import static com.mopub.mobileads.UnityAdsAdapterConfiguration.UnityAdsConstants.LOG_GAME_ID_MISSING;
import static com.mopub.mobileads.UnityAdsAdapterConfiguration.UnityAdsConstants.LOG_INIT_FAILED_WITH_ERROR;
import static com.mopub.mobileads.UnityAdsAdapterConfiguration.UnityAdsConstants.LOG_INIT_SUCCESS;

public class UnityAdsInitializer {
    private static final String ADAPTER_NAME = UnityAdsInitializer.class.getSimpleName();
    private static UnityAdsInitializer mInstance;

    public static UnityAdsInitializer getInstance() {
        if (mInstance == null) {
            mInstance = new UnityAdsInitializer();
        }
        return mInstance;
    }

    // Returns false if initialized and true if initialization needs to take place
    public boolean checkOrInitUnityAds(@NonNull Activity launcherActivity, @NonNull Map<String, String> serverExtras) {
        if (UnityAds.isInitialized()) {
            MoPubLog.log(CUSTOM, ADAPTER_NAME, LOG_INIT_FAILED_WITH_ERROR.getMessage());
            return false;
        }

        // Return true since we have to initialize successfully before an ad can be loaded
        final String gameId = serverExtras.get(GAME_ID_KEY.getMessage());
        if (TextUtils.isEmpty(gameId)) {
            MoPubLog.log(CUSTOM, ADAPTER_NAME, LOG_GAME_ID_MISSING.getMessage());
            return true;
        }

        initializeUnityAds(launcherActivity, gameId, new IUnityAdsInitializationListener() {
            @Override
            public void onInitializationComplete() {
                MoPubLog.log(CUSTOM, ADAPTER_NAME, LOG_INIT_SUCCESS.getMessage());
            }

            @Override
            public void onInitializationFailed(UnityAds.UnityAdsInitializationError unityAdsInitializationError, String s) {
                MoPubLog.log(CUSTOM, ADAPTER_NAME, LOG_INIT_FAILED_WITH_ERROR.getMessage() + s);
            }
        });

        // Return true since initialization is not complete yet
        return true;
    }

    public void initializeUnityAds(final Context context, String gameId, IUnityAdsInitializationListener initializationListener) {
        UnityAds.setDebugMode(MoPubLog.getLogLevel() == MoPubLog.LogLevel.DEBUG);
        initGdpr(context);
        initMediationMetadata(context);
        UnityAds.initialize(context, gameId, false, initializationListener);
    }

    private void initGdpr(Context context) {
        // Pass the user consent from the MoPub SDK to Unity Ads as per GDPR
        PersonalInfoManager personalInfoManager = MoPub.getPersonalInformationManager();

        final boolean canCollectPersonalInfo = MoPub.canCollectPersonalInformation();
        final boolean shouldAllowLegitimateInterest = MoPub.shouldAllowLegitimateInterest();

        if (personalInfoManager != null && personalInfoManager.gdprApplies() == Boolean.TRUE) {
            final MetaData gdprMetaData = new MetaData(context);

            if (shouldAllowLegitimateInterest) {
                if (personalInfoManager.getPersonalInfoConsentStatus() == ConsentStatus.EXPLICIT_NO
                        || personalInfoManager.getPersonalInfoConsentStatus() == ConsentStatus.DNT) {
                    gdprMetaData.set("gdpr.consent", false);
                } else {
                    gdprMetaData.set("gdpr.consent", true);
                }
            } else {
                gdprMetaData.set("gdpr.consent", canCollectPersonalInfo);
            }
            gdprMetaData.commit();
        }
    }

    private void initMediationMetadata(Context context) {
        final MediationMetaData mediationMetaData = new MediationMetaData(context);
        mediationMetaData.setName("MoPub");
        mediationMetaData.setVersion(MoPub.SDK_VERSION);
        mediationMetaData.set("adapter_version", ADAPTER_VERSION);
        mediationMetaData.commit();
    }
}
