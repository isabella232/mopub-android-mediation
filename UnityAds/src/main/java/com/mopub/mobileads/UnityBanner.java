package com.mopub.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mopub.common.logging.MoPubLog;

import com.unity3d.services.banners.BannerErrorInfo;
import com.unity3d.services.banners.BannerView;
import com.unity3d.services.banners.UnityBannerSize;

import static com.mopub.common.DataKeys.ADUNIT_FORMAT;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.CLICKED;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.CUSTOM;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.LOAD_ATTEMPTED;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.LOAD_SUCCESS;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.SHOW_ATTEMPTED;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.SHOW_SUCCESS;
import static com.mopub.common.logging.MoPubLog.AdapterLogEvent.WILL_LEAVE_APPLICATION;
import static com.mopub.mobileads.UnityAdsAdapterConfiguration.UnityAdsConstants.LOG_BANNER_LOAD_FAILED;
import static com.mopub.mobileads.UnityAdsAdapterConfiguration.UnityAdsConstants.LOG_BANNER_UNSUPPORTED;

public class UnityBanner extends UnityBaseAd implements BannerView.IListener {

    private static final String ADAPTER_NAME = UnityBanner.class.getSimpleName();
    private BannerView mBannerView;
    private final UnityBannerSize bannerSizeL = new UnityBannerSize(728, 90);
    private final UnityBannerSize bannerSizeM = new UnityBannerSize(468, 60);
    private final UnityBannerSize bannerSizeS = new UnityBannerSize(320, 50);

    @Override
    @Nullable
    public View getAdView() {
        return mBannerView;
    }

    @Override
    protected void onInvalidate() {
        cleanBanner();
    }

    @Override
    protected void load(@NonNull final Context context, @NonNull final AdData adData) {
        super.load(context, adData);
        if (failIfPlacementIdNull()) {
            return;
        }

        String adUnitFormat = adData.getExtras().get(ADUNIT_FORMAT);
        if (!TextUtils.isEmpty(adUnitFormat)) {
            adUnitFormat = adUnitFormat.toLowerCase();
        }

        final boolean isMediumRectangleFormat = "medium_rectangle".equals(adUnitFormat);

        if (isMediumRectangleFormat) {
            MoPubLog.log(getAdNetworkId(), CUSTOM, ADAPTER_NAME, LOG_BANNER_UNSUPPORTED.getMessage());

            if (mLoadListener != null) {
                mLoadListener.onAdLoadFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
            }
            return;
        }

        final UnityBannerSize bannerSize = getUnityAdsBannerSize(adData);

        cleanBanner();

        MoPubLog.log(getAdNetworkId(), LOAD_ATTEMPTED, ADAPTER_NAME);

        mBannerView = new BannerView((Activity) context, mPlacementId, bannerSize);
        mBannerView.setListener(this);
        mBannerView.load();
    }

    @Override
    public void onBannerLoaded(BannerView bannerView) {
        MoPubLog.log(getAdNetworkId(), LOAD_SUCCESS, ADAPTER_NAME);
        MoPubLog.log(getAdNetworkId(), SHOW_ATTEMPTED, ADAPTER_NAME);
        MoPubLog.log(getAdNetworkId(), SHOW_SUCCESS, ADAPTER_NAME);

        if (mLoadListener != null) {
            mLoadListener.onAdLoaded();
            mBannerView = bannerView;
        }

        if (mInteractionListener != null) {
            mInteractionListener.onAdImpression();
        }
    }

    @Override
    public void onBannerClick(BannerView bannerView) {
        MoPubLog.log(getAdNetworkId(), CLICKED, ADAPTER_NAME);

        if (mInteractionListener != null) {
            mInteractionListener.onAdClicked();
        }
    }

    @Override
    public void onBannerFailedToLoad(BannerView bannerView, BannerErrorInfo errorInfo) {
        MoPubLog.log(getAdNetworkId(), CUSTOM, ADAPTER_NAME, String.format(LOG_BANNER_LOAD_FAILED.getMessage(),
                getAdNetworkId(), errorInfo.errorMessage));

        if (mLoadListener != null) {
            mLoadListener.onAdLoadFailed(MoPubErrorCode.NETWORK_NO_FILL);
        }
    }

    @Override
    public void onBannerLeftApplication(BannerView bannerView) {
        MoPubLog.log(getAdNetworkId(), WILL_LEAVE_APPLICATION, ADAPTER_NAME);
    }

    // Returns the size of the Unity banner the ad will fit in to
    private UnityBannerSize getUnityAdsBannerSize(@NonNull final AdData adData) {
        final int adWidth = adData.getAdWidth() != null ? adData.getAdWidth() : 0;
        final int adHeight = adData.getAdHeight() != null ? adData.getAdHeight() : 0;

        if (adWidth >= bannerSizeL.getWidth() && adHeight >= bannerSizeL.getHeight()) {
            return bannerSizeL;
        } else if (adWidth >= bannerSizeM.getWidth() && adHeight >= bannerSizeM.getHeight()) {
            return bannerSizeM;
        } else {
            return bannerSizeS;
        }
    }

    private void cleanBanner() {
        if (mBannerView != null) {
            mBannerView.destroy();
            mBannerView = null;
        }
    }

    @Override
    protected String getDefaultPlacementId() {
        return "banner";
    }
}