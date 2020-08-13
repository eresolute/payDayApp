package com.fh.payday.views2.intro;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.support.annotation.Nullable;

import com.fh.payday.BaseActivity;
import com.fh.payday.R;
import com.fh.payday.preferences.AppPreferences;
import com.fh.payday.services.StickyService;
import com.fh.payday.utilities.NotificationUtilsKt;
import com.fh.payday.utilities.ServiceUtils;
import com.fh.payday.views2.auth.LoginActivity;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.security.ProviderInstaller;

public class SplashActivity extends BaseActivity {

    @Nullable
    private Handler handler;
    @Nullable
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            String message = "Your device is not compatible with this version of "
                + getString(R.string.app_name);
            AlertDialogFragment.OnConfirmListener confirmListener = dialog -> {
                dialog.dismiss();
                onBackPressed();
            };

            onError(message, false, getErrorIcon(), getTintColorError(),
                getAlertDismissListener(), getAlertCancelListener(), confirmListener);
            return;
        }*/
        if (!ServiceUtils.isGooglePlayServicesAvailable(this)) return;

        if (!NotificationUtilsKt.isAppInBackground(this))
            startService(new Intent(this, StickyService.class));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null && runnable != null)
            handler.removeCallbacks(runnable);
    }

    @Override
    public int getLayout() {
        return R.layout.activity_splash;
    }

    @Override
    public void init() {
        /*if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) return;*/
        if (!ServiceUtils.isGooglePlayServicesAvailable(getApplicationContext())) return;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            try {
                ProviderInstaller.installIfNeeded(this.getApplicationContext());
            } catch (GooglePlayServicesRepairableException e) {
                e.printStackTrace();
            } catch (GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            }
        }

        NotificationUtilsKt.createNotificationChannel(this);
        handler = new Handler();
        runnable = () -> {
            AppPreferences preferences = AppPreferences.Companion.getInstance();
            String cachedUserId = AppPreferences.Companion.getInstance().getCachedUserId(this);
            Intent intent;

            if (preferences.isFirstLaunch(SplashActivity.this) || TextUtils.isEmpty(cachedUserId)) {
                preferences.setFirstLaunch(SplashActivity.this);
                intent = new Intent(this, IntroActivity.class);
            } else {
                intent = new Intent(this, LoginActivity.class);
            }

            /*intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
              intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);*/
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        };
        handler.postDelayed(runnable, 1500);
    }
}
