package com.fh.payday.views2.auth;

import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fh.payday.BaseActivity;
import com.fh.payday.R;
import com.fh.payday.biometricauth.BiometricPromptCompat;
import com.fh.payday.datasource.models.login.User;
import com.fh.payday.datasource.remote.ApiClient;
import com.fh.payday.datasource.remote.NetworkState2;
import com.fh.payday.datasource.remote.customer.CustomerService;
import com.fh.payday.preferences.AppPreferences;
import com.fh.payday.preferences.BiometricPreferences;
import com.fh.payday.preferences.LocalePreferences;
import com.fh.payday.preferences.UserPreferences;
import com.fh.payday.utilities.ConstantsKt;
import com.fh.payday.utilities.OnItemClickListener;
import com.fh.payday.utilities.TextViewUtilsKt;
import com.fh.payday.viewmodels.LoginViewModel;
import com.fh.payday.views.adapter.SelectLanguageAdapter;
import com.fh.payday.views2.auth.forgotcredentials.ForgotCredentialsMainActivity;
import com.fh.payday.views2.dashboard.MainActivity;
import com.fh.payday.views2.registration.RegisterActivity;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import org.jetbrains.annotations.NotNull;

import static com.fh.payday.utilities.ConstantsKt.CONNECTION_ERROR;
import static com.fh.payday.utilities.TextInputUtilsKt.clearErrorMessage;
import static com.fh.payday.utilities.TextInputUtilsKt.setErrorMessage;

public class LoginActivity extends BaseActivity implements OnItemClickListener, View.OnClickListener {

    private BottomSheetDialog bottomSheet;
    private TextInputEditText etUsername, etPassword;
    private TextInputLayout tilUsername, tilPassword;
    private LoginViewModel viewModel;
    private Button buttonLogin;
    private ProgressBar progressBar;
    private TextView tvWelcome;
    private TextView tvCachedName;
    private TextView tvRegister;
    private LinearLayout llLogos;
    private BiometricPromptCompat dialog;


    @Override
    protected void onPause() {
        super.onPause();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (dialog == null) return;
            dialog.dismiss();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = ViewModelProviders.of(this).get(LoginViewModel.class);

        if (!isNetworkConnected()) {
            onFailure(findViewById(R.id.root_view), CONNECTION_ERROR);
        }
        etPassword.setTransformationMethod(new PasswordTransformationMethod());
        attachListeners();
        addObserver();
        checkAppUpdate();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null && intent.getExtras() != null) {
            getIntent().putExtras(intent.getExtras());
        }
    }

    @Override
    public int getLayout() {
        return R.layout.activity_login;
    }

    @Override
    public void init() {
        CustomerService.Companion.clearDisposable();

        String selectedLang = LocalePreferences.Companion.getInstance().getSelectedLang(this);
        ((TextView) findViewById(R.id.tvSelectedLang)).setText(selectedLang);

        llLogos = findViewById(R.id.ll_logos);
        tilUsername = findViewById(R.id.til_username);
        tilPassword = findViewById(R.id.til_password);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        buttonLogin = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progress_bar);
        tvRegister = findViewById(R.id.tv_register);
        tvWelcome = findViewById(R.id.tv_welcome);
        tvCachedName = findViewById(R.id.tv_cached_name);

        String cachedUserId = AppPreferences.Companion.getInstance().getCachedUserId(this);

        if (TextUtils.isEmpty(cachedUserId)) {
            handleNCView();
        } else {
            handleCachedView();
            etUsername.setText(cachedUserId);
        }

        TextView btmMenuCashOut = findViewById(R.id.btm_menu_cash_out);
        TextView btmMenuCurrConv = findViewById(R.id.btm_menu_currency_conv);
        TextView btmFag = findViewById(R.id.btm_menu_faq);
        TextView btmMenuHowTo = findViewById(R.id.btm_menu_how_to_reg);

        btmMenuCashOut.setOnClickListener(this);
        btmMenuCurrConv.setOnClickListener(this);
        btmFag.setOnClickListener(this);
        btmMenuHowTo.setOnClickListener(this);

        TextView textViewForgot = findViewById(R.id.tv_forgotPassword);
        TextViewUtilsKt.setTextWithUnderLine(textViewForgot, getString(R.string.forgot_user_id_password));
        textViewForgot.setOnClickListener(view -> {
            if (!isNetworkConnected()) {
                onFailure(findViewById(R.id.root_view), CONNECTION_ERROR);
            } else {
                startActivity(new Intent(this, ForgotCredentialsMainActivity.class));
            }
        });
    }

    private void handleNCView() {
        tvWelcome.setVisibility(View.GONE);
        tvCachedName.setVisibility(View.GONE);
        llLogos.setVisibility(View.VISIBLE);
        tilUsername.setVisibility(View.VISIBLE);
        findViewById(R.id.iv_fingerprint).setVisibility(View.GONE);

        TextViewUtilsKt.setTextWithUnderLine(tvRegister, getString(R.string.register));
        tvRegister.setOnClickListener(view -> {
            if (!isNetworkConnected()) {
                onFailure(findViewById(R.id.root_view), CONNECTION_ERROR);
            } else {
                startActivity(new Intent(this, RegisterActivity.class));
            }
        });
    }

    private void handleCachedView() {
        String cachedName = AppPreferences.Companion.getInstance().getCachedName(this);

        if (TextUtils.isEmpty(cachedName)) {
            handleNCView();
            return;
        }

        tvCachedName.setText(cachedName);
        llLogos.setVisibility(View.GONE);
        tilUsername.setVisibility(View.GONE);
        tvWelcome.setVisibility(View.VISIBLE);
        tvCachedName.setVisibility(View.VISIBLE);

        TextViewUtilsKt.setTextWithUnderLine(tvRegister, getString(R.string.sign_in_diff_account));
        tvRegister.setOnClickListener(view -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                BiometricPreferences.Companion.getInstance().clearPreferences(this);
            }
            AppPreferences.Companion.getInstance().clearPreferences(this);
            clearNotification();
            startIntroActivity();
           // startActivity(new Intent(this, IntroActivity.class));
           /* findViewById(R.id.iv_fingerprint).setVisibility(View.GONE);
            etUsername.setText("");
            etPassword.setText("");
            etUsername.requestFocus();
            handleNCView();*/
        });

        BiometricPreferences preferences = BiometricPreferences.Companion.getInstance();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && preferences.isBiometricAuthEnabled(this)) {
            View view = findViewById(R.id.iv_fingerprint);
            view.setVisibility(View.VISIBLE);
            view.setOnClickListener(v -> showBiometricAuth());
            showBiometricAuth();
        }
    }

    private void attachListeners() {
        etUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearErrorMessage(tilUsername);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {


            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                clearErrorMessage(tilPassword);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        findViewById(R.id.tvSelectedLang).setOnClickListener(view -> showBottomSheet());

        buttonLogin.setOnClickListener(view -> {
            ApiClient.Companion.clearInstance();
            String username;
            if (tilUsername.getVisibility() == View.GONE) {
                username = AppPreferences.Companion.getInstance().getCachedUserId(this);
            } else {
                username = etUsername.getText() != null ? etUsername.getText().toString() : "";
            }
            String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

            if (!validate(username, password)) {
                return;
            }

            if (!isNetworkConnected()) {
                onFailure(findViewById(R.id.root_view), getString(R.string.no_internet_connectivity));
                return;
            }

            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(task -> {
                        if (!task.isSuccessful()) {
                            return;
                        }

                        InstanceIdResult result = task.getResult();
                        if (result == null) return;

                        if (!TextUtils.isEmpty(result.getToken()))
                            viewModel.login(username, password, result.getToken());
                    });
            //hideKeyboard();

        });
    }

    @Override
    public void onItemClick(int index) {
        if (bottomSheet != null) bottomSheet.dismiss();
        switch (index) {
            case 0:
                setLocale("en");
                break;
            case 1:
                setLocale("ar");
                break;
            case 2:
                setLocale("hi");
                break;
            case 3:
                setLocale("ml");
                break;
            case 4:
                setLocale("ur");
                break;
            case 5:
                setLocale("bn");
                break;
            case 6:
                setLocale("ta");
                break;
            case 7:
                setLocale("tl");
                break;
            default:
                break;
        }
    }

    private void showBottomSheet() {
        String[] array = getResources().getStringArray(R.array.languages);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_select_language, findViewById(android.R.id.content), false);
        RecyclerView recyclerView = view.findViewById(R.id.recycler_view);

        recyclerView.setAdapter(new SelectLanguageAdapter(this, array));

        bottomSheet = new BottomSheetDialog(this);
        bottomSheet.setContentView(view);
        ((View) view.getParent()).setBackgroundColor(ContextCompat.getColor(this, R.color.transparent));
        bottomSheet.show();

        view.findViewById(R.id.cancel).setOnClickListener(v -> bottomSheet.dismiss());

        bottomSheet.setOnDismissListener(v -> bottomSheet = null);
        bottomSheet.setOnCancelListener(v -> bottomSheet = null);
    }

   /* @Override
    public void onClick(View view) {
        String locale = LocalePreferences.Companion.getInstance().getLocale(this);
        int id = view.getId();
        switch (id) {
            case R.id.btm_menu_cash_out:
                Intent mIntent = new Intent(this, BranchATMActivity.class);
                mIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                mIntent.putExtra("issue", "branch");
                startActivity(mIntent);
                break;

            case R.id.btm_menu_currency_conv:
                Intent exploreIntent;
                if ("en".equals(locale)) {
                    exploreIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(EXPLORE_URL_EN));
                } else if ("hi".equals(locale)) {
                    exploreIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(EXPLORE_URL_HI));
                } else {
                    exploreIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(EXPLORE_URL_EN));
                }
                startActivity(exploreIntent);
                break;

            case R.id.btm_menu_faq:
                Intent i = new Intent(this, FaqActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                break;

            case R.id.btm_menu_how_to_reg:
                Intent faqIntent;
                if ("en".equals(locale)) {
                    faqIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(FAQ_URL_EN));
                } else if ("hi".equals(locale)) {
                    faqIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(FAQ_URL_HI));
                } else {
                    faqIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(FAQ_URL_EN));
                }
                startActivity(faqIntent);
                break;

        }
    }*/

    // todo handle in viewmodel
    private boolean validate(String username, String password) {
        if (TextUtils.isEmpty(username) || TextUtils.getTrimmedLength(username) < 8) {
            setErrorMessage(tilUsername, getString(R.string.invalid_username));
            return false;
        } else if (TextUtils.isEmpty(password) || TextUtils.getTrimmedLength(password) < 8) {
            setErrorMessage(tilPassword, getString(R.string.invalid_password));
            return false;
        }

        return true;
    }

    private void addObserver() {
        viewModel.getLoginState().observe(this, event -> {
            if (event == null) return;

            NetworkState2<User> loginState = event.getContentIfNotHandled();

            if (loginState == null) return;

            if (loginState instanceof NetworkState2.Loading) {
                buttonLogin.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                return;
            }

            buttonLogin.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            hideProgress();

            if (loginState instanceof NetworkState2.Success) {
                User user = ((NetworkState2.Success<User>) loginState).getData();
                String warning = ((NetworkState2.Success<User>) loginState).getMessage();
                if (user != null && UserPreferences.Companion.getInstance().saveUser(this, user)) {
                    ConstantsKt.setToken(user.getToken());
                    AppPreferences.Companion.getInstance().cacheUserId(this, user.getUsername());

                    String customerName = user.getCustomerName();
                    if (customerName != null && !customerName.trim().isEmpty()) {
                        AppPreferences.Companion.getInstance().cacheName(this, customerName);
                    } else {
                        AppPreferences.Companion.getInstance().cacheName(this, user.getUsername());
                    }

                    startUserInteractionObserver(user, user.getSessionTimeout() / 2);

                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    if (warning != null && !warning.isEmpty()) {
                        intent.putExtra("warning", warning);
                    }
                    Bundle bundle = getIntent().getExtras();
                    if (bundle != null) {
                        intent.putExtras(bundle);
                    }
                    startActivity(intent);
                    finish();
                }
            } else if (loginState instanceof NetworkState2.Error) {
                NetworkState2.Error<?> error = (NetworkState2.Error<?>) loginState;
                onError(error.getMessage());
            } else if (loginState instanceof NetworkState2.Failure) {
                NetworkState2.Failure<?> failure = (NetworkState2.Failure<?>) loginState;
                onFailure(findViewById(R.id.root_view), failure.getThrowable());
            } else {
                onFailure(findViewById(R.id.root_view), getString(R.string.request_error));
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private BiometricPromptCompat.OnAuthenticateListener authListener = new BiometricPromptCompat.OnAuthenticateListener() {
        @Override
        public void onAuthSuccess() {
            try {
                String text1 = BiometricPreferences.Companion.getInstance().getEText1(LoginActivity.this);
                String text2 = BiometricPreferences.Companion.getInstance().getEText2(LoginActivity.this);

                if (!TextUtils.isEmpty(text1) && !TextUtils.isEmpty(text2)) {
                    FirebaseInstanceId.getInstance().getInstanceId()
                            .addOnCompleteListener(task -> {
                                if (!task.isSuccessful()) {
                                    return;
                                }

                                InstanceIdResult result = task.getResult();

                                if (result != null && !TextUtils.isEmpty(result.getToken()))
                                    viewModel.login(text1, text2, result.getToken());
                            });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onAuthFailure() {
        }

        @Override
        public void onAuthCancelled() {
        }

        @Override
        public void onAuthHelp(int helpCode, @NotNull CharSequence helpString) {
        }

        @Override
        public void onAuthError(int errorCode, @NotNull CharSequence errString) {
        }
    };

    private BiometricPromptCompat.Callback callback = new BiometricPromptCompat.Callback() {
        @Override
        public void onSDKNotSupported() {
            onFailure(findViewById(R.id.root_view), "SDK Not supported");
        }

        @Override
        public void onHardwareNotDetected() {
            onFailure(findViewById(R.id.root_view), "Hardware not detected");
        }

        @Override
        public void onBiometricAuthNotAvailable() {
            onFailure(findViewById(R.id.root_view), "No fingerprints available");
        }

        @Override
        public void onPermissionsNotGranted() {
            onFailure(findViewById(R.id.root_view), "Permission not granted");
        }

        @Override
        public void onError(@NotNull String error) {
            onFailure(findViewById(R.id.root_view), error);
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void showBiometricAuth() {
        if (BiometricPromptCompat.Companion.hasSDKSupport()
                && BiometricPromptCompat.Companion.hasHardwareSupport(this)
                && BiometricPromptCompat.Companion.isPermissionsGranted(this)) {

          dialog =   new BiometricPromptCompat.Builder(this)
                    .setTitle(getString(R.string.fingerprint_dialog_title2))
                    .setSubtitle(getString(R.string.fingerprint_dialog_subtitle))
                    .setDescription( getString(R.string.fingerprint_dialog_description))
                    .addOnAuthenticationListener(authListener)
                    .build();
            dialog.authenticate(callback);
        }
    }
/*
    private void startCampaignActivity() {
        Intent intent = new Intent(this, CampaignActivity.class);
        intent.putExtra("offerTitle", getIntent().getStringExtra("offerTitle"));
        intent.putExtra("offerBrief", getIntent().getStringExtra("offerBrief"));
        intent.putExtra("expiryDays", getIntent().getStringExtra("expiryDays"));
        intent.putExtra("notificationId", getIntent().getStringExtra("notificationId"));
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        getIntent().removeExtra("offerTitle");
        finish();

    }*/

}