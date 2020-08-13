package com.fh.payday.views2.dashboard;

import android.arch.lifecycle.ViewModelProviders;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.fh.payday.BaseActivity;
import com.fh.payday.BuildConfig;
import com.fh.payday.R;
import com.fh.payday.datasource.models.Card;
import com.fh.payday.datasource.models.CustomerSummary;
import com.fh.payday.datasource.models.Item;
import com.fh.payday.datasource.models.Loan;
import com.fh.payday.datasource.models.login.User;
import com.fh.payday.datasource.remote.NetworkState2;
import com.fh.payday.preferences.AppPreferences;
import com.fh.payday.preferences.UserPreferences;
import com.fh.payday.utilities.AccessMatrix;
import com.fh.payday.utilities.AccessMatrixKt;
import com.fh.payday.utilities.Action;
import com.fh.payday.utilities.ConstantsKt;
import com.fh.payday.utilities.DateTime;
import com.fh.payday.utilities.DrawableUtil;
import com.fh.payday.utilities.GlideApp;
import com.fh.payday.utilities.ListUtilsKt;
import com.fh.payday.utilities.NumberFormatterKt;
import com.fh.payday.utilities.OnItemClickListener;
import com.fh.payday.utilities.UrlUtilsKt;
import com.fh.payday.viewmodels.LoanViewModel;
import com.fh.payday.viewmodels.MainViewModel;
import com.fh.payday.views.adapter.dashboard.DrawerAdapter;
import com.fh.payday.views2.campaign.CampaignActivity;
import com.fh.payday.views2.help.HelpActivity;
import com.fh.payday.views2.kyc.KycActivity;
import com.fh.payday.views2.loan.apply.ApplyLoanActivity;
import com.fh.payday.views2.loan.apply.LoanDetailActivity;
import com.fh.payday.views2.loan.apply.TermsAndConditionsDialog;
import com.fh.payday.views2.loancalculator.LoanCalculatorActivity;
import com.fh.payday.views2.locator.LocatorActivity;
import com.fh.payday.views2.message.ContactUsActivity;
import com.fh.payday.views2.notification.NotificationActivity;
import com.fh.payday.views2.offer.OfferActivity;
import com.fh.payday.views2.servicerequest.ServiceRequestActivity;
import com.fh.payday.views2.shared.custom.EligibilityDialogFragment;
import com.fh.payday.views2.shared.custom.EmiratesExpiredFragment;
import com.fh.payday.views2.shared.fragments.TermsConditionsDialogFragment;
import com.fh.payday.views2.shared.utils.ViewUtilsKt;
import com.fh.payday.views2.transactionhistory.paydaycard.PaydayCardActivity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import kotlin.Unit;

import static com.fh.payday.utilities.UrlUtilsKt.LOAN_TC_URL;

public class MainActivity extends BaseActivity implements OnItemClickListener, View.OnClickListener,
    SharedPreferences.OnSharedPreferenceChangeListener {

    private DrawerLayout drawer;
    private LinearLayout linearLayout;
    private static int REQUEST_NOTIFICATION = 1;

    private MainViewModel viewModel;
    private TextView tvCustomerName, tvTitle, tvTitle1, tvLoanAmount;
    private ImageView ivProfilePic;
    private User user;
    private ImageView ivNotification;
    private HashSet notificationReadList;

    private SharedPreferences preferences;
    public static boolean isActive = false;

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getExtras() != null) {
                if (user != null) {
                    viewModel.fetchCustomerSummary(user.getToken(), user.getSessionId(), user.getRefreshToken(), user.getCustomerId());
                }
            }
        }
    };

    private BroadcastReceiver readNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            markReadNotifications(intent);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isActive = true;

        if (savedInstanceState == null)
            setServicesFragment();

        String warning = getIntent().getStringExtra("warning");
        if (warning != null && !warning.isEmpty()) {
            onFailure(findViewById(R.id.root_view), warning);
        }

        preferences = getSharedPreferences(UserPreferences.PREFERENCE_NAME, Context.MODE_PRIVATE);
        preferences.registerOnSharedPreferenceChangeListener(this);

        LocalBroadcastManager.getInstance(this).registerReceiver(readNotificationReceiver,
                new IntentFilter(Action.ACTION_NOTIFICATION_READ_LIST));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (notificationReadList.isEmpty()) {
            if (user != null) {
                viewModel.fetchCustomerSummary(user.getToken(), user.getSessionId(), user.getRefreshToken(), user.getCustomerId());
            }
        }
    }

    @Override
    protected void onDestroy() {
        isActive = false;
        preferences.unregisterOnSharedPreferenceChangeListener(this);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(readNotificationReceiver);
        super.onDestroy();
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(Action.ACTION_OFFER));
    }

    @Override
    protected void onStop() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onStop();
    }

    private void setServicesFragment() {
        Bundle bundle = new Bundle();
        bundle.putInt("position", 0);

        CategoriesFragment fragment = new CategoriesFragment();
        fragment.setArguments(bundle);
        showFragment(fragment, R.id.container2);

        Bundle bundle1 = new Bundle();
        bundle1.putInt("position", 1);

        CategoriesFragment fragment1 = new CategoriesFragment();
        fragment1.setArguments(bundle1);
        showFragment(fragment1, R.id.container1);
    }

    public void showFragment(Fragment fragment, int container) {
        getSupportFragmentManager()
            .beginTransaction()
            .add(container, fragment)
            .commit();
    }

    @Override
    public int getLayout() {
        return R.layout.activity_main;
    }


    @Override
    public void init() {
        //AppPreferences.Companion.getInstance().newNotification(this);
        notificationReadList = new HashSet<Long>();
        ivNotification = findViewById(R.id.toolbar_notification);
        LayerDrawable layerDrawable = (LayerDrawable) ivNotification.getDrawable();
        DrawableUtil.Companion.setBadgeCount(this, layerDrawable, "0");
        viewModel = ViewModelProviders.of(this).get(MainViewModel.class);

        handleNotification();

        viewModel.setDrawerItems(getDrawerItems());

        user = UserPreferences.Companion.getInstance().getUser(this);

        TextView mToolbarTitle = findViewById(R.id.toolbar_title);
        mToolbarTitle.setText(R.string.welcome);

        tvCustomerName = findViewById(R.id.tvName);
        tvCustomerName.setText("...");
        linearLayout = findViewById(R.id.linear_layout);
        TextView tvLoanAmount = findViewById(R.id.loan_amount);

        findViewById(R.id.toolbar_help).setOnClickListener(v -> startHelpActivity("dashboardhelp"));

        drawer = findViewById(R.id.drawer);
        tvLoanAmount.setOnClickListener(this);

        initBottomBar();
        initCustomerInfo();

        findViewById(R.id.toolbar_menu).setOnClickListener(view -> {
            if (drawer.isDrawerOpen(linearLayout))
                drawer.closeDrawers();
            else
                drawer.openDrawer(linearLayout);
        });
        linearLayout.setOnClickListener(null);
        ImageView ivProfileEdit = findViewById(R.id.ivProfileEdit);
        ivProfileEdit.setOnClickListener(this);

        ImageView toolbar_back = findViewById(R.id.toolbar_back);
        findViewById(R.id.toolbar_notification).setOnClickListener(view -> {
            startActivityForResult(new Intent(this, NotificationActivity.class), REQUEST_NOTIFICATION);
        });
        toolbar_back.setOnClickListener(view -> {
            if (drawer.isDrawerOpen(linearLayout))
                drawer.closeDrawers();
            else
                drawer.openDrawer(linearLayout);
        });

        ivProfilePic = findViewById(R.id.ivProfilePic);
        ivProfilePic.setOnClickListener(this);
        findViewById(R.id.tvName).setOnClickListener(view -> {
            openKycActivity();
        });

        findViewById(R.id.ivProfileEdit).setOnClickListener(view -> {
            openKycActivity();
        });
        findViewById(R.id.iv_flag).setOnClickListener(view -> {
            openKycActivity();
        });
        TextView tvVersion = findViewById(R.id.tv_version);
        String versionName = String.format(getString(R.string.version_name), BuildConfig.VERSION_NAME);
        tvVersion.setText(versionName);

        List<Item> drawerItems = viewModel.getDrawerItems();
        if (drawerItems != null) {
            RecyclerView drawerRecyclerView = findViewById(R.id.drawerRecyclerView);
            drawerRecyclerView.setAdapter(new DrawerAdapter(this, drawerItems));
        }
    }

    private void handleNotification() {
        if (viewModel.hasLoanOffer(getIntent().getExtras())) {
            Bundle bundle = getIntent().getExtras();
            if (bundle != null) {
                Intent intent;
                if (OfferActivity.BANNER.equalsIgnoreCase(getIntent().getStringExtra("productType"))) {
                    intent = new Intent(this, OfferActivity.class);
                } else if (LoanViewModel.LOAN.equalsIgnoreCase(getIntent().getStringExtra("productType"))) {
                    intent = new Intent(this, CampaignActivity.class);
                } else if (LoanViewModel.TOPUP_LOAN.equalsIgnoreCase(getIntent().getStringExtra("productType"))) {
                    intent = new Intent(this, CampaignActivity.class);
                } else {
                    startActivityForResult(new Intent(this, NotificationActivity.class), REQUEST_NOTIFICATION);
                    return;
                }

                intent.putExtras(bundle);
                startActivity(intent);
            }
        }
    }

    /*@Override
    protected void onActivityResult(int requestCode, int resultCode,  Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_NOTIFICATION) {
            if (data == null) return;
            markReadNotifications(data);
        }
    }*/

    private void markReadNotifications(Intent data) {
        notificationReadList = (HashSet) data.getSerializableExtra("data");
        LayerDrawable layerDrawable = (LayerDrawable) ivNotification.getDrawable();
        DrawableUtil.Companion.setBadgeCount(this, layerDrawable, "0");
        addObserver();
        if (user != null) {
            List<String> notificationList = new ArrayList<String>(notificationReadList);
            viewModel.readNotifications(user.getToken(), user.getSessionId(), user.getRefreshToken(), user.getCustomerId(), notificationList);
        }
    }

    public MainViewModel getViewModel() {
        return viewModel;
    }

    private void initBottomBar() {
        TextView btmMenuHome = findViewById(R.id.btm_home);
        TextView btmMenuBranch = findViewById(R.id.btm_menu_branch);
        TextView btmSupport = findViewById(R.id.btm_menu_support);
        TextView btmLoan = findViewById(R.id.btm_menu_loan_calc);
        btmMenuHome.setOnClickListener(this);
        btmMenuBranch.setOnClickListener(this);
        btmSupport.setOnClickListener(this);
        btmLoan.setOnClickListener(this);
    }

    private void initCustomerInfo() {
        TextView tvCardName = findViewById(R.id.tv_card_name);
        TextView tvCardBalance = findViewById(R.id.tv_card_balance);
        TextView tvCardBalDate = findViewById(R.id.tv_card_balance_date);
        TextView tvLastSalary = findViewById(R.id.tv_last_salary);
        TextView tvLastSalaryDate = findViewById(R.id.tv_last_salary_date);
        tvTitle = findViewById(R.id.tv_card_balance_title);
        tvTitle1 = findViewById(R.id.tv_last_salary_title);

        tvLoanAmount = findViewById(R.id.loan_amount);
        //TextView tvESAmount = findViewById(R.id.early_salary_amount);

        ProgressBar progressBar = findViewById(R.id.progress_bar);

        viewModel.getNetworkState().observe(this, event -> {
            if (event == null) return;

            NetworkState2<CustomerSummary> summaryState = event.getContentIfNotHandled();

            if (summaryState == null) return;


            if (summaryState instanceof NetworkState2.Loading) {
                progressBar.setVisibility(View.VISIBLE);
                ViewUtilsKt.setVisibility(View.GONE, tvCardName,
                    tvCardBalance, tvCardBalDate, tvLastSalary, tvLastSalaryDate);
            } else if (summaryState instanceof NetworkState2.Success) {
                progressBar.setVisibility(View.GONE);
                ViewUtilsKt.setVisibility(View.VISIBLE, tvCardName,
                    tvCardBalance, tvCardBalDate, tvLastSalary, tvLastSalaryDate);

                CustomerSummary summary = ((NetworkState2.Success<CustomerSummary>) summaryState).getData();
                String warningMsg = ((NetworkState2.Success) summaryState).getMessage();

                if (warningMsg != null && !warningMsg.equals("null")) {
                    onFailure(findViewById(R.id.root_view), warningMsg);
                }
                //setCardPosition(summary);
                setCardInfo(summary, tvCardName, tvCardBalance, tvCardBalDate, tvLastSalary, tvLastSalaryDate);

                //TODO handle apply topUp loan after confirmation
                /*if (summary != null){
                    if (!ListUtilsKt.isEmptyList(summary.getCards()) && ListUtilsKt.isEmptyList(summary.getLoans()))
                        setLoanInfo(summary, tvLoanAmount);
                    else if (!ListUtilsKt.isEmptyList(summary.getCards()) && !ListUtilsKt.isEmptyList(summary.getLoans()))
                    {
                        handleApplyLoanTopUp(summary);
                    }
                }*/

                if (summary != null && ListUtilsKt.isEmptyList(summary.getCards())){
                    //handleApplyLoanTopUp(summary);
                    setLoanInfo(summary, tvLoanAmount);
                }

                else if (summary != null && !ListUtilsKt.isEmptyList(summary.getCards()))
                    setLoanInfo(summary, tvLoanAmount);


                //setEarlySalaryInfo(summary, tvEarlySalary);
                if (summary != null && !TextUtils.isEmpty(summary.getSelfie())) {
                    setSelfie(summary.getSelfie());
                }
            } else if (summaryState instanceof NetworkState2.Error) {
                progressBar.setVisibility(View.GONE);
                ViewUtilsKt.setVisibility(View.VISIBLE, tvCardName,
                    tvCardBalance, tvCardBalDate, tvLastSalary, tvLastSalaryDate);

                NetworkState2.Error<CustomerSummary> error = (NetworkState2.Error<CustomerSummary>) summaryState;
                if (error.isSessionExpired()) {
                    onSessionExpired(error.getMessage());
                    return;
                }

                try {
                    int code = Integer.parseInt(error.getMessage());
                    updateCredentials(code);

                } catch (NumberFormatException e) {
                    onFailure(findViewById(R.id.root_view), error.getMessage());
                }


            } else if (summaryState instanceof NetworkState2.Failure) {
                progressBar.setVisibility(View.GONE);
                ViewUtilsKt.setVisibility(View.VISIBLE, tvCardName,
                    tvCardBalance, tvCardBalDate, tvLastSalary, tvLastSalaryDate);
                onFailure(findViewById(R.id.root_view), ConstantsKt.CONNECTION_ERROR);
            } else {
                progressBar.setVisibility(View.GONE);
                ViewUtilsKt.setVisibility(View.VISIBLE, tvCardName,
                    tvCardBalance, tvCardBalDate, tvLastSalary, tvLastSalaryDate);
                onFailure(findViewById(R.id.root_view), ConstantsKt.CONNECTION_ERROR);
            }
        });
    }

    private void updateCredentials(int code) {
        switch (code) {

            case 399:
                DialogFragment fragment = new EmiratesExpiredFragment();
                fragment.setCancelable(false);
                fragment.show(getSupportFragmentManager(), "EmiratesExpire");
                break;
            case 203:
                new EligibilityDialogFragment.Builder(dialog -> {
                })
                    .setBtn1Text(getString(R.string.update))
                    .setBtn2Text(getString(R.string.cancel))
                    .setConfirmListener(dialog -> {
                        Intent intent = new Intent(this, KycActivity.class);
                        intent.putExtra("index", 5);
                        intent.putExtra("isExpired", true);
//                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        startActivity(intent);
                        finish();
                    })
                    .setCancelListener(dialog -> startLoginActivity())
                    .setTitle(getString(R.string.password_expired))
                    .build()
                    .show(getSupportFragmentManager(), "PasswordUpdate");
                break;


        }
    }


    private void setCardInfo(@Nullable CustomerSummary summary, TextView tvCardName,
                             TextView tvCardBalance, TextView tvCardBalDate,
                             TextView tvLastSalary, TextView tvLastSalaryDate) {
        if (summary == null) return;
        LayerDrawable layerDrawable = (LayerDrawable) ivNotification.getDrawable();
        DrawableUtil.Companion.setBadgeCount(this, layerDrawable, summary.getUnreadBellNotifications() == null ? "" : summary.getUnreadBellNotifications());
        if (ListUtilsKt.isEmptyList(summary.getCards())) {
            tvTitle.setText(getString(R.string.loan_amount_title));
            tvTitle1.setText(getString(R.string.outstanding_amount));
            if (ListUtilsKt.isEmptyList(summary.getLoans())) {
                return;
            }
            tvCardName.setText(summary.getUsername());
            tvCardBalance.setText(String.format(getString(R.string.amount_in_aed), NumberFormatterKt.getDecimalValue(summary.getLoans().get(0).getOriginalLoanAmount())));
            String outStandingAmount = summary.getLoans().get(0).getOutstandingAmount();
            if (outStandingAmount == null) return;
            tvLastSalary.setText(String.format(getString(R.string.amount_in_aed), NumberFormatterKt.getDecimalValue(outStandingAmount)));
            findViewById(R.id.cl_card_info).setOnClickListener(view -> startLoanActivity());

            return;
        }

        Card card = summary.getCards().get(0);

        if (card == null) return;

        AppPreferences.Companion.getInstance().cacheName(this, card.getCardName());

        if (viewModel != null && viewModel.getCustomerSummary() != null && viewModel.getCustomerSummary().getUsername() != null) {
            tvCustomerName.setText(viewModel.getCustomerSummary().getUsername());
        }
        tvTitle.setText(getString(R.string.card_balance));
        tvTitle1.setText(getString(R.string.last_salary_credited));
        tvCardName.setText(card.getCardName());
        tvCardBalance.setText(String.format(getString(R.string.amount_in_aed), NumberFormatterKt.getDecimalValue(card.getAvailableBalance())));
//        tvCardBalDate.setText(DateTime.Companion.parse(card.getLastStatementDate()));
        tvLastSalary.setText(String.format(getString(R.string.amount_in_aed),
            card.getLastSalaryCredit() != null ? NumberFormatterKt.getDecimalValue(card.getLastSalaryCredit()) : "0.00"));
        tvLastSalaryDate.setText(card.getLastSalaryCreditDate() != null
            ? DateTime.Companion.parse(card.getLastSalaryCreditDate()) : "");

        findViewById(R.id.cl_card_info).setOnClickListener(view -> startPaydayCardActivity());

    }

    private void setLoanInfo(@Nullable CustomerSummary summary, TextView tvLoanAmount) {
        if (summary == null || ListUtilsKt.isEmptyList(summary.getLoans())) {
            handleApplyLoan(tvLoanAmount, summary);
            return;
        }

        findViewById(R.id.loan_title).setVisibility(View.VISIBLE);
        ((TextView) findViewById(R.id.loan_title)).setText(getString(R.string.loan));

        Loan loan = summary.getLoans().get(0);
        String loanAmount = loan.getOutstandingAmount() != null ? loan.getOutstandingAmount() : "0.00";

        tvLoanAmount.setText(String.format(getString(R.string.amount_in_aed),
            NumberFormatterKt.getDecimalValue(loanAmount)));
        tvLoanAmount.setVisibility(View.VISIBLE);
        tvLoanAmount.setOnClickListener(this);
    }

    private void setEarlySalaryInfo(CustomerSummary summary, TextView tvEarlySalary) {
        tvEarlySalary.setVisibility(View.VISIBLE);
        tvEarlySalary.setText(getString(R.string.apply));

        tvEarlySalary.setOnClickListener(view -> handleApplySalary());
    }

    private void handleApplySalary() {
    }


    private void handleApplyLoan(TextView tvLoanAmount, CustomerSummary summary) {
        TextView loanTitle = findViewById(R.id.loan_title);
        loanTitle.setVisibility(View.INVISIBLE);
        loanTitle.setText(getString(R.string.loan));
        tvLoanAmount.setVisibility(View.INVISIBLE);
        /*tvLoanAmount.setText(getString(R.string.apply));
        tvLoanAmount.setOnClickListener(view -> {
            String status = ListUtilsKt.isNotEmptyList(summary.getCards()) ? summary.getCards().get(0).getCardStatus() : "";
            if (AccessMatrixKt.hasAccess(status, AccessMatrix.Companion.getAPPLY_LOAN())) {
                showTermsConditions(summary);
            } else {
                showCardStatusError(status);
            }

        });*/
    }

    private void handleApplyLoanTopUp(CustomerSummary summary) {
        TextView loanTitle = findViewById(R.id.loan_title);
        loanTitle.setVisibility(View.VISIBLE);
        loanTitle.setText(getString(R.string.apply_loan_top_up_title));
        tvLoanAmount.setVisibility(View.VISIBLE);
        tvLoanAmount.setText(getString(R.string.apply));

        if (ListUtilsKt.isEmptyList(summary.getCards()) && ListUtilsKt.isEmptyList(summary.getLoans())) {
            tvLoanAmount.setVisibility(View.GONE);
        } else  tvLoanAmount.setVisibility(View.VISIBLE);

        tvLoanAmount.setOnClickListener(view -> {
            String status = ListUtilsKt.isNotEmptyList(summary.getCards()) ? summary.getCards().get(0).getCardStatus() : "";
            if (ListUtilsKt.isEmptyList(summary.getCards()) || AccessMatrixKt.hasAccess(status, AccessMatrix.Companion.getAPPLY_TOPUP_LOAN())) {
                showTermsConditions(summary);
            } else {
                showCardStatusError(status);
            }

        });

        List<Loan> loans = summary.getLoans();
        if (ListUtilsKt.isEmptyList(loans)) return;

        String name = loans.get(0).getCustomerName();
        if (name != null && !name.trim().isEmpty()) {
            AppPreferences.Companion.getInstance().cacheName(this, name);
        } else {
            String username = summary.getUsername();
            AppPreferences.Companion.getInstance().cacheName(this, username != null ? username : "");
        }
    }

    private void setSelfie(String selfie) {
        ProgressBar progressBar = findViewById(R.id.progress_bar_profile);
        progressBar.setVisibility(View.VISIBLE);
        GlideApp.with(this)
            .load(Uri.parse(UrlUtilsKt.BASE_URL + selfie))
            .fitCenter()
            .listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    progressBar.setVisibility(View.GONE);
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    progressBar.setVisibility(View.GONE);
                    return false;
                }
            })
            .placeholder(R.drawable.ic_profile)
            .into(ivProfilePic);
    }

    private void setCardPosition(@Nullable CustomerSummary summary) {
        if (summary == null) return;
        CardView myView = findViewById(R.id.card_view);
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) myView.getLayoutParams();
        if (ListUtilsKt.isEmptyList(summary.getLoans())) params.verticalBias = 0.5f;
        else params.verticalBias = 0.2f;
        myView.setLayoutParams(params);
    }

    private List<Item> getDrawerItems() {
        List<Item> drawerItems = new ArrayList<>();
        Resources res = getResources();
        String[] arrays = res.getStringArray(R.array.drawer_items);

        TypedArray icons = res.obtainTypedArray(R.array.drawer_item_icons);

        for (int i = 0; i < arrays.length; i++) {
            drawerItems.add(new Item(arrays[i], icons.getResourceId(i, 0)));
        }

        icons.recycle();
        return drawerItems;
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(linearLayout)) {
            drawer.closeDrawers();
            return;
        }
        UserPreferences.Companion.getInstance().clearPreferences(this);
        super.onBackPressed();
    }

    @Override
    public void onItemClick(int index) {
        CustomerSummary summary = viewModel.getCustomerSummary();
        if (summary != null && ListUtilsKt.isEmptyList(summary.getCards()) && (index == 0 || index == 1)) {
            showWarning(getString(R.string.loan_customer_warning));
            return;
        }
        switch (index) {
            case 0:
                startPaydayCardActivity();
                break;
            /*case 1:
                if (viewModel.getCustomerSummary() == null) return;

                Intent intent = new Intent(this, ProductActivity.class);
                intent.putExtra("data", viewModel.getCustomerSummary());

                startActivity(intent);
                break;*/
            case 1:
                Intent intent1 = new Intent(this, ServiceRequestActivity.class);
                intent1.putExtra("summary", viewModel.getCustomerSummary());
                startActivity(intent1);
                break;
//            case 3: {
//                CustomerSummary summary = viewModel.getCustomerSummary();
//                if (summary != null) {
//                    Intent intent = new Intent(this, AccountSummaryActivity.class);
//                    intent.putExtra("summary", summary);
//                    startActivity(intent);
//                }
//                break;
//            }
            case 2:
                startActivity(new Intent(this, ContactUsActivity.class));
                break;
    /*        case 4:
                startActivity(new Intent(this, SettingsActivity.class));
                break;*/
            case 3: {
                logout();
                break;
            }
        }
        drawer.closeDrawers();
    }

    private void startPaydayCardActivity() {
        CustomerSummary summary = viewModel.getCustomerSummary();
        if (summary != null && ListUtilsKt.isNotEmptyList(summary.getCards())) {
            if (AccessMatrixKt.hasAccess(summary.getCards().get(0).getCardStatus(), AccessMatrix.Companion.getCARD_DETAILS())) {
                Intent intent = new Intent(this, PaydayCardActivity.class);
                intent.putExtra("summary", summary);
                startActivity(intent);
            } else {
                String status = summary.getCards().get(0).getCardStatus();
                showCardStatusError(status);
            }
        }
    }

    private void startLoanActivity() {
        CustomerSummary summary = viewModel.getCustomerSummary();
        Intent intent = new Intent(this, LoanDetailActivity.class);
        if (summary != null && ListUtilsKt.isNotEmptyList(summary.getLoans())) {
            intent.putExtra("summary", summary);
        }
        startActivity(intent);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {

            case R.id.btm_menu_branch:
                startActivity(new Intent(this, LocatorActivity.class));
                break;

            case R.id.toolbar_help:
                startActivity(new Intent(view.getContext(), HelpActivity.class));
                break;
            case R.id.btm_menu_support:
                startActivity(new Intent(view.getContext(), ContactUsActivity.class));
                break;
            case R.id.btm_menu_loan_calc:
                startActivity(new Intent(view.getContext(), LoanCalculatorActivity.class));
                break;
            case R.id.loan_amount:
                CustomerSummary summary = viewModel.getCustomerSummary();
                if (summary != null) {
                    String status = ListUtilsKt.isNotEmptyList(summary.getCards()) ? summary.getCards().get(0).getCardStatus() : "";
                    if (ListUtilsKt.isEmptyList(summary.getCards()) || AccessMatrixKt.hasAccess(status, AccessMatrix.Companion.getLOAN_DETAILS())) {
                        Intent intent = new Intent(view.getContext(), LoanDetailActivity.class);
                        if (ListUtilsKt.isNotEmptyList(summary.getLoans())) {
                            intent.putExtra("summary", summary);
                        }
                        startActivity(intent);
                    } else {
                        showCardStatusError(status);
                    }
                }
                break;

            case R.id.ivProfilePic:
                openKycActivity();
                break;
        }
    }

    private void openKycActivity(){
        Intent intent = new Intent(this, KycActivity.class);
        intent.putExtra("summary", viewModel.getCustomerSummary());
        startActivity(intent);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        User user = UserPreferences.Companion.getInstance().getUser(this);
        if (user == null) return;
        if (TextUtils.isEmpty(user.getProfilePic())) return;
        setSelfie(user.getProfilePic());
    }

    private void showTermsConditions(CustomerSummary summary) {
        TermsAndConditionsDialog dialog = new TermsAndConditionsDialog.Builder()
            .setTermsConditionsLink(true)
            .attachPositiveListener(() -> {
                if (summary != null) {
                    if (ListUtilsKt.isEmptyList(summary.getCards()) || (!ListUtilsKt.isEmptyList(summary.getCards()) && !ListUtilsKt.isEmptyList(summary.getLoans()))) {
                        Intent intent = new Intent(this, ApplyLoanActivity.class);
                        intent.putExtra("product_type", LoanViewModel.TOPUP_LOAN)
                            .putExtra("loan_number", summary.getLoans().get(0).getLoanNumber());
                        startActivity(intent);
                    } else
                        startActivity(new Intent(this, ApplyLoanActivity.class));

                }
                return Unit.INSTANCE;
            })
            .attachLinkListener(() -> {
                TermsConditionsDialogFragment termsAndConditionsDialog = TermsConditionsDialogFragment
                    .Companion.newInstance(LOAN_TC_URL, getString(R.string.close));
                termsAndConditionsDialog.show(getSupportFragmentManager(), "dialog");
                return Unit.INSTANCE;
            })
            .build();
        dialog.setCancelable(false);
        dialog.show(getSupportFragmentManager(), "dialog");

    }

    private void addObserver() {
        ProgressBar progressBar = findViewById(R.id.progress_bar);
        viewModel.getReadNotificationState().observe(this, event -> {
            if (event == null) return;

            NetworkState2<String> state = event.getContentIfNotHandled();

            if (state == null) return;

            if (state instanceof  NetworkState2.Loading){
                progressBar.setVisibility(View.VISIBLE);
                return;
            }
            progressBar.setVisibility(View.GONE);
            if (state instanceof NetworkState2.Success) {
                notificationReadList = new HashSet();
                initCustomerInfo();
                if (user != null) {
                    viewModel.fetchCustomerSummary(user.getToken(), user.getSessionId(), user.getRefreshToken(), user.getCustomerId());
                }
            } else if (state instanceof NetworkState2.Error) {
                onError(((NetworkState2.Error<String>) state).getMessage());
            } else if (state instanceof NetworkState2.Failure) {
                onFailure(findViewById(R.id.root_view), getString(R.string.request_error));
            } else {
                onFailure(findViewById(R.id.root_view), ConstantsKt.CONNECTION_ERROR);
            }
        });
    }

}
