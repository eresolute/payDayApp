package com.fh.payday.views2.moneytransfer;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.button.MaterialButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fh.payday.R;
import com.fh.payday.datasource.models.login.User;
import com.fh.payday.datasource.models.moneytransfer.Beneficiary;
import com.fh.payday.datasource.models.moneytransfer.LocalBeneficiary;
import com.fh.payday.datasource.models.moneytransfer.P2CBeneficiary;
import com.fh.payday.datasource.models.moneytransfer.PaydayBeneficiary;
import com.fh.payday.datasource.remote.NetworkState2;
import com.fh.payday.preferences.UserPreferences;
import com.fh.payday.utilities.ConstantsKt;
import com.fh.payday.utilities.ListUtilsKt;
import com.fh.payday.utilities.MobileNoValidator;
import com.fh.payday.utilities.OnLocalBeneficiaryClickListener;
import com.fh.payday.utilities.OnP2CBeneficiaryClickListener;
import com.fh.payday.utilities.PhoneUtils;
import com.fh.payday.utilities.TYPE;
import com.fh.payday.utilities.TextViewUtilsKt;
import com.fh.payday.viewmodels.beneficiaries.BeneficiaryViewModel;
import com.fh.payday.views.adapter.moneytransfer.BeneficiariesAdapter;
import com.fh.payday.views.adapter.moneytransfer.LocalBeneficiaryAdapter;
import com.fh.payday.views.adapter.moneytransfer.P2CBeneficiaryAdapter;
import com.fh.payday.views.fragments.BeneficiaryDialog;
import com.fh.payday.views2.moneytransfer.beneificaries.EditBeneficiaryActivity;
import com.fh.payday.views2.moneytransfer.beneificaries.creditCard.CreditCardBeneficiaryActivity;
import com.fh.payday.views2.moneytransfer.beneificaries.local.LocalBeneficiaryActivity;
import com.fh.payday.views2.moneytransfer.beneificaries.payday.toPayday.PaydayBeneficiaryActivity;
import com.fh.payday.views2.shared.custom.EligibilityDialogFragment;
import com.fh.payday.views2.shared.custom.PermissionsDialog;

import java.util.ArrayList;
import java.util.List;

import kotlin.Unit;

import static com.fh.payday.views2.moneytransfer.MoneyTransferType.CC;
import static com.fh.payday.views2.moneytransfer.MoneyTransferType.LOCAL;

public class BeneficiariesFragment extends Fragment implements BeneficiaryDialog.OnBeneficiaryClickListener {

    private RecyclerView recyclerView;
    private InternationalMoneyTransfer activity;
    private ConstraintLayout layout;
    List<Beneficiary> list = new ArrayList<>();
    List<LocalBeneficiary> localBeneficiaryList = new ArrayList<>();
    List<P2CBeneficiary> p2cBeneficiaryList = new ArrayList<>();
    TextView textView, tvError, tvPrefix;
    private String prefix;
    ProgressBar progressBar;
    Boolean isVisible = false;
    BeneficiaryViewModel beneficiaryViewModel;
    TextInputLayout textInputLayout;
    private EditText etMobile;
    private final static int CONTACT_PICKER_RESULT = 100;
    private final static int REQUEST_CODE = 101;
    private ConstraintLayout constraintLayout;
    private MaterialButton btnAddBeneficiary;


    private EditBeneficiaryActivity.OnBeneficiaryClick listener = new EditBeneficiaryActivity.OnBeneficiaryClick() {
        @Override
        public void onBeneficiaryClickListener(int position, Beneficiary beneficiary) {

            if (beneficiary.getEnabled()) {
                if (checkCardStatus()) {
                    activity.getViewModel().setSelectedP2PBeneficiary(beneficiary);
                    activity.onBeneficiarySelected();
                }
            } else {
                activity.showMessage(
                    getString(R.string.beneficiary_disabled),
                    R.drawable.ic_insufficient_fund_44,
                    R.color.colorError,
                    Dialog::dismiss
                );
            }

        }

        @Override
        public void onBeneficiaryClickListener(int position, Beneficiary beneficiary, boolean enabled) {
        }
    };

    OnLocalBeneficiaryClickListener localBeneficiaryClickListener = beneficiary -> {
        if (beneficiary.getEnabled()) {
            if (checkCardStatus()) {
                activity.getViewModel().setSelectedLocalBeneficiary(beneficiary);
                activity.onBeneficiarySelected();
            }
        } else {
            activity.showMessage(
                getString(R.string.beneficiary_disabled),
                R.drawable.ic_insufficient_fund_44,
                R.color.colorError,
                Dialog::dismiss
            );
        }
    };

    OnP2CBeneficiaryClickListener ccListener = beneficiary -> {
        if (beneficiary.getEnabled()) {
            if (checkCardStatus()) {
                activity.getViewModel().setSelectedP2CBeneficiary(beneficiary);
                activity.onBeneficiarySelected();
            }
        } else {
            activity.showMessage(
                getString(R.string.beneficiary_disabled),
                R.drawable.ic_insufficient_fund_44,
                R.color.colorError,
                Dialog::dismiss
            );
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (InternationalMoneyTransfer) context;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);

        if (activity != null) {
            activity.hideKeyboard();
        }

        if (isVisibleToUser) {

            if (activity != null)
                activity.getViewModel().setChecked(false);

            if (tvError != null && constraintLayout != null && etMobile != null) {
                constraintLayout.setBackground(ContextCompat.getDrawable(activity, R.drawable.bg_blue_rounded_border));
                tvError.setVisibility(View.INVISIBLE);
                etMobile.setText(null);
            }

            if (list.size() == 0) {
                isVisible = true;
                getBeneficiaries();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        tvError.setVisibility(View.INVISIBLE);
        if (isVisible) {
            getBeneficiaries();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_beneficiaries, container, false);
        init(view);

        beneficiaryViewModel = ViewModelProviders.of(this).get(BeneficiaryViewModel.class);
        activity.setupFocusOutside(view.findViewById(R.id.constraint_layout));
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));

        addPaydayBeneficiaryObserver();
        addInstantBeneficiaryObserver();
        addLocalBeneficiaryObserver();
        addP2CBeneficiaryObserver();
        return view;
    }

    private void getBeneficiaries() {
        if (activity == null) return;
        activity.hideNoInternetView();
        if (!activity.isNetworkConnected()) {
            hideRV();
            activity.showNoInternetView(() -> {
                getBeneficiaries();
                return Unit.INSTANCE;
            });
            return;
        }

        activity.findViewById(R.id.tv_reg_beneficiaries).setVisibility(View.VISIBLE);
        MoneyTransferType type = activity.getViewModel().getTransferType();
        User user = UserPreferences.Companion.getInstance().getUser(activity);
        if (user == null) return;

        switch (type) {
            case PAYDAY:
                if (constraintLayout != null && btnAddBeneficiary != null) {

                    tvPrefix.setText(getString(R.string.uea_plus_country_dash));
                    prefix = tvPrefix.getText().toString().replace("+971-", "0");
                    constraintLayout.setVisibility(View.VISIBLE);
                    btnAddBeneficiary.setVisibility(View.INVISIBLE);
                }
                activity.getViewModel().getP2PBeneficiaries(user.getToken(), user.getSessionId(), user.getRefreshToken(), user.getCustomerId());
                break;
            case LOCAL:
                if (constraintLayout != null && btnAddBeneficiary != null) {
                    constraintLayout.setVisibility(View.INVISIBLE);
                    btnAddBeneficiary.setVisibility(View.VISIBLE);
                    addListener(LOCAL);
                }
                activity.getViewModel().getLocalBeneficiaries(user.getToken(), user.getSessionId(), user.getRefreshToken(), user.getCustomerId());
                break;
            case CC:
                if (constraintLayout != null && btnAddBeneficiary != null) {
                    constraintLayout.setVisibility(View.INVISIBLE);
                    btnAddBeneficiary.setVisibility(View.VISIBLE);
                    addListener(CC);
                }
                activity.getViewModel().getP2CBeneficiaries(user.getToken(), user.getSessionId(), user.getRefreshToken(), user.getCustomerId());
                break;
        }
    }

    private void hideRV() {
        recyclerView.setVisibility(View.INVISIBLE);
        activity.findViewById(R.id.tv_reg_beneficiaries).setVisibility(View.INVISIBLE);
    }

    private void addListener(MoneyTransferType type) {
        switch (type) {
            case LOCAL:
                btnAddBeneficiary.setOnClickListener(v -> {
                    Intent i = new Intent(getActivity(), LocalBeneficiaryActivity.class);
                    i.putExtra("index", TYPE.ADD);
                    i.putExtra("label", LOCAL);
                    startActivity(i);
                });
                break;
            case INTERNATIONAL:
                break;
            case CC:
                btnAddBeneficiary.setOnClickListener(v -> {
                    Intent i = new Intent(getActivity(), CreditCardBeneficiaryActivity.class);
                    i.putExtra("index", TYPE.ADD);
                    i.putExtra("label", CC);
                    startActivity(i);
                });
                break;
        }
    }

    private void init(View view) {
        recyclerView = view.findViewById(R.id.recycler_view);
        textView = view.findViewById(R.id.text_view);
        progressBar = view.findViewById(R.id.progress_bar);
        etMobile = view.findViewById(R.id.et_mobile);
        tvError = view.findViewById(R.id.tv_error);
        constraintLayout = view.findViewById(R.id.constraintLayout);
        btnAddBeneficiary = view.findViewById(R.id.btn_add_beneficiary);
        tvPrefix = view.findViewById(R.id.tv_prefix);

        view.findViewById(R.id.img_contact).setOnClickListener(v -> doLaunchContactPicker());
        view.findViewById(R.id.img_confirm).setOnClickListener(v -> {
            if (activity != null && activity.getViewModel() != null && activity.getViewModel().getSelectedCard() != null) {
                if (ConstantsKt.CARD_STOP_LIST.equalsIgnoreCase(activity.getViewModel().getSelectedCard().getCardStatus())
                    || ConstantsKt.CARD_CANCELLED.equalsIgnoreCase(activity.getViewModel().getSelectedCard().getCardStatus())
                    || ConstantsKt.CARD_REPLACED.equalsIgnoreCase(activity.getViewModel().getSelectedCard().getCardStatus())) {
                    activity.showMessage(
                        getString(R.string.card_blocked),
                        R.drawable.ic_insufficient_fund_44,
                        R.color.colorError,
                        Dialog::dismiss
                    );
                    return;
                }
            }
            activity.hideKeyboard();
            getInstantBeneficiary();
        });

        recyclerView.setOnClickListener(v -> activity.hideKeyboard());
        textView.setOnClickListener(v -> activity.hideKeyboard());

        etMobile.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)) {
                if (activity != null && activity.getViewModel() != null && activity.getViewModel().getSelectedCard() != null) {
                    if (ConstantsKt.CARD_STOP_LIST.equalsIgnoreCase(activity.getViewModel().getSelectedCard().getCardStatus())
                        || ConstantsKt.CARD_CANCELLED.equalsIgnoreCase(activity.getViewModel().getSelectedCard().getCardStatus())
                        || ConstantsKt.CARD_REPLACED.equalsIgnoreCase(activity.getViewModel().getSelectedCard().getCardStatus())) {
                        activity.showMessage(
                            getString(R.string.card_blocked),
                            R.drawable.ic_insufficient_fund_44,
                            R.color.colorError,
                            Dialog::dismiss
                        );
                        return false;
                    }
                }
                getInstantBeneficiary();
            }
            activity.hideKeyboard();
            return false;
        });

        etMobile.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                constraintLayout.setBackground(ContextCompat.getDrawable(activity, R.drawable.bg_blue_rounded_border));
                tvError.setVisibility(View.INVISIBLE);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }

    private void getInstantBeneficiary() {
        String phone = prefix + etMobile.getText().toString();
        if (!TextUtils.isEmpty(phone)
            && phone.length() == 10
            && MobileNoValidator.Companion.validate(phone)) {

            User user = UserPreferences.Companion.getInstance().getUser(activity);
            if (user == null) return;

            if (beneficiaryExists(etMobile.getText().toString())) {
                activity.onBeneficiarySelected();
                return;
            }
            beneficiaryViewModel.getPaydayBeneficiary(user.getToken(), user.getSessionId(), user.getRefreshToken(),
                user.getCustomerId(), phone);
        } else {
            constraintLayout.setBackground(ContextCompat.getDrawable(activity, R.drawable.bg_border_error));
            tvError.setVisibility(View.VISIBLE);
        }
    }

    private boolean beneficiaryExists(String mobile) {

        if (list.isEmpty()) return false;

        for (Beneficiary b : list) {
            if (b.getMobileNumber().equals(mobile)) {
                activity.getViewModel().setSelectedP2PBeneficiary(b);
                return true;
            }
        }
        return false;
    }

    private void updateCredentials(int code) {
        if (code == 399) {
            activity.onError("Your Emirates Id is expired or about to expire");
        }
    }

    public void doLaunchContactPicker() {
        if (getContext() == null || getActivity() == null) return;
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED){
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
            startActivityForResult(intent, CONTACT_PICKER_RESULT);
        } else {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.READ_CONTACTS)) {
                new PermissionsDialog.Builder()
                    .setTitle(getString(R.string.contact_permission_title))
                    .setDescription(getString(R.string.contact_permission_description))
                    .setNegativeText(getString(R.string.app_settings))
                    .setPositiveText(getString(R.string.not_now))
                    .build()
                    .show(getContext());
            } else {
                requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, REQUEST_CODE);
            }
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CODE){
            if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_TYPE);
                startActivityForResult(intent, CONTACT_PICKER_RESULT);
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        InternationalMoneyTransfer activity = (InternationalMoneyTransfer) getActivity();
        if (activity == null) return;

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CONTACT_PICKER_RESULT) {


                Uri contactData = null;
                if (data != null) {
                    contactData = data.getData();
                }

                Cursor cursor = null;
                if (contactData != null) {
                    ContentResolver contentResolver = activity.getContentResolver();
                    cursor = contentResolver.query(contactData, null, null, null, null);
                }

                if (cursor == null) return;

                if (cursor.getCount() > 0) {

                    cursor.moveToFirst();

                    int hasPhone = Integer.parseInt(cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.Contacts.HAS_PHONE_NUMBER)));
                    if (hasPhone > 0) {
                        String number = cursor.getString(cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER));

                        TextViewUtilsKt.replaceZero(etMobile, PhoneUtils.Companion.extractNumber(number));
                    }
                } else {
                    activity.onFailure(activity.findViewById(R.id.root_view), getString(R.string.no_mobile_no));
                }
                cursor.moveToFirst();
                cursor.close();
            }
        }
    }

    private void addPaydayBeneficiaryObserver() {
        activity.getViewModel().getBeneficiaryState().observe(this, event -> {
            if (event == null) return;

            NetworkState2<List<Beneficiary>> state = event.getContentIfNotHandled();

            if (state == null) return;

            if (state instanceof NetworkState2.Loading) {
                textView.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                return;
            }

            progressBar.setVisibility(View.GONE);

            if (state instanceof NetworkState2.Success) {
                list = ((NetworkState2.Success<List<Beneficiary>>) state).getData();
                BeneficiariesAdapter beneficiariesAdapter = new BeneficiariesAdapter(list, listener, optionListener);
                recyclerView.setVisibility(View.VISIBLE);
                activity.findViewById(R.id.tv_reg_beneficiaries).setVisibility(View.VISIBLE);
                recyclerView.setAdapter(beneficiariesAdapter);

                if (ListUtilsKt.isEmptyList(list)) {
                    textView.setVisibility(View.VISIBLE);
                    beneficiariesAdapter.notifyDataSetChanged();
                    return;
                }

            } else if (state instanceof NetworkState2.Error) {

                NetworkState2.Error<?> error = (NetworkState2.Error<?>) state;
//                int code = Integer.parseInt(error.getErrorCode());
//                updateCredentials(code);
                if (error.isSessionExpired()) {
                    activity.onSessionExpired(error.getMessage());
                    return;
                }

                activity.handleErrorCode(
                    Integer.parseInt(error.getErrorCode()), error.getMessage()
                );
//                activity.onError(error.getMessage());

            } else if (state instanceof NetworkState2.Failure) {
                NetworkState2.Failure<?> error = (NetworkState2.Failure<?>) state;
                activity.onFailure(activity.findViewById(R.id.root_view), error.getThrowable());
            } else {
                activity.onFailure(activity.findViewById(R.id.root_view), ConstantsKt.CONNECTION_ERROR);
            }
        });
    }

    private void addInstantBeneficiaryObserver() {
        beneficiaryViewModel.getBeneficiary().observe(this, event -> {
            if (event == null) return;

            NetworkState2<PaydayBeneficiary> response = event.getContentIfNotHandled();

            if (response == null) return;

            if (response instanceof NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing));
                return;
            }

            activity.hideProgress();

            if (response instanceof NetworkState2.Success) {
                PaydayBeneficiary paydayBeneficiary = ((NetworkState2.Success<PaydayBeneficiary>) response).getData();
                if (paydayBeneficiary == null) return;

                recyclerView.setVisibility(View.VISIBLE);
                Beneficiary beneficiary = new Beneficiary(Integer.valueOf(paydayBeneficiary.getCIFId()),
                    paydayBeneficiary.getCardAccountNumber(),
                    paydayBeneficiary.getCustomerName(),
                    prefix + etMobile.getText().toString(),
                    true);

                DialogFragment dialog = BeneficiaryDialog.newInstance(beneficiary, this);
                dialog.show(getFragmentManager(), null);

            } else if (response instanceof NetworkState2.Error) {
                NetworkState2.Error<?> error = (NetworkState2.Error<?>) response;

                if (error.isSessionExpired()) {
                    activity.onSessionExpired(error.getMessage());
                    return;
                }

                if (Integer.parseInt(error.getErrorCode()) == 1) {
                    activity.onError(error.getMessage());
                    return;
                }

                activity.handleErrorCode(
                    Integer.parseInt(error.getErrorCode()), error.getMessage()
                );

            } else if (response instanceof NetworkState2.Failure) {
                NetworkState2.Failure<?> error = (NetworkState2.Failure<?>) response;
                activity.onFailure(activity.findViewById(R.id.root_view), error.getThrowable());
            } else {
                activity.onFailure(activity.findViewById(R.id.root_view), new Throwable(ConstantsKt.CONNECTION_ERROR));
            }
        });
    }

    private void addLocalBeneficiaryObserver() {
        activity.getViewModel().getLocalBeneficiaryState().observe(this, event -> {
            if (event == null) return;

            NetworkState2<List<LocalBeneficiary>> state = event.getContentIfNotHandled();

            if (state == null) return;

            if (state instanceof NetworkState2.Loading) {
                textView.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                return;
            }

            progressBar.setVisibility(View.GONE);

            if (state instanceof NetworkState2.Success) {
                localBeneficiaryList.clear();
                recyclerView.setVisibility(View.VISIBLE);
                localBeneficiaryList = ((NetworkState2.Success<List<LocalBeneficiary>>) state).getData();
                if (ListUtilsKt.isEmptyList(localBeneficiaryList)) {
                    textView.setVisibility(View.VISIBLE);
                    return;
                }
                LocalBeneficiaryAdapter beneficiariesAdapter = new LocalBeneficiaryAdapter(localBeneficiaryList,
                    localBeneficiaryClickListener, optionListener);
                recyclerView.setAdapter(beneficiariesAdapter);

            } else if (state instanceof NetworkState2.Error) {

                NetworkState2.Error<?> error = (NetworkState2.Error<?>) state;

                if (error.isSessionExpired()) {
                    activity.onSessionExpired(error.getMessage());
                    return;
                }

//                activity.onError(error.getMessage());
                activity.handleErrorCode(
                    Integer.parseInt(error.getErrorCode()), error.getMessage()
                );

            } else if (state instanceof NetworkState2.Failure) {
                NetworkState2.Failure<?> error = (NetworkState2.Failure<?>) state;
                activity.onFailure(activity.findViewById(R.id.root_view), error.getThrowable());
            } else {
                activity.onFailure(activity.findViewById(R.id.root_view), ConstantsKt.CONNECTION_ERROR);
            }
        });
    }

    private void addP2CBeneficiaryObserver() {
        activity.getViewModel().getP2cBeneficiaryState().observe(this, event -> {
            if (event == null) return;

            NetworkState2<List<P2CBeneficiary>> state = event.getContentIfNotHandled();

            if (state == null) return;

            if (state instanceof NetworkState2.Loading) {
                textView.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                return;
            }

            progressBar.setVisibility(View.GONE);

            if (state instanceof NetworkState2.Success) {
                p2cBeneficiaryList.clear();
                recyclerView.setVisibility(View.VISIBLE);
                p2cBeneficiaryList = ((NetworkState2.Success<List<P2CBeneficiary>>) state).getData();
                if (ListUtilsKt.isEmptyList(p2cBeneficiaryList)) {
                    textView.setVisibility(View.VISIBLE);
                    return;
                }
                P2CBeneficiaryAdapter beneficiariesAdapter = new P2CBeneficiaryAdapter(p2cBeneficiaryList, optionListener, ccListener);
                recyclerView.setAdapter(beneficiariesAdapter);

            } else if (state instanceof NetworkState2.Error) {

                NetworkState2.Error<?> error = (NetworkState2.Error<?>) state;

                if (error.isSessionExpired()) {
                    activity.onSessionExpired(error.getMessage());
                    return;
                }
//                activity.onError(error.getMessage());
                activity.handleErrorCode(
                    Integer.parseInt(error.getErrorCode()), error.getMessage()
                );

            } else if (state instanceof NetworkState2.Failure) {
                NetworkState2.Failure<?> error = (NetworkState2.Failure<?>) state;
                activity.onFailure(activity.findViewById(R.id.root_view), error.getThrowable());
            } else {
                activity.onFailure(activity.findViewById(R.id.root_view), ConstantsKt.CONNECTION_ERROR);
            }
        });
    }

    private EditBeneficiaryActivity.OnBeneficiaryOptionClick optionListener = (item, view) -> {
        PopupMenu menu = showPopUpMenu(item, view);
        menu.show();
        menu.setOnMenuItemClickListener(menuItem -> {
            switch (menuItem.getItemId()) {
                case R.id.edit:
                    beneficiaryViewModel.setOptionType(TYPE.EDIT);
                    editBeneficiary(item);
                    return true;
                case R.id.delete:
                    beneficiaryViewModel.setOptionType(TYPE.DELETE);
                    deleteBeneficiary(item);
                    return true;
                case R.id.enable:
                    beneficiaryViewModel.setOptionType(TYPE.ENABLE);
                    enableBeneficairy(item);
                    return true;
                default:
                    return false;
            }
        });
    };

    private void editBeneficiary(Object item) {
        if (item instanceof LocalBeneficiary) {
            beneficiaryViewModel.setSelectedOptionBeneficiary(item);
            Intent intent = new Intent(activity, LocalBeneficiaryActivity.class);
            intent.putExtra("beneficiary", (LocalBeneficiary) item);
            intent.putExtra("index", TYPE.EDIT);
            startActivity(intent);
        } else if (item instanceof Beneficiary) {
            beneficiaryViewModel.setSelectedOptionBeneficiary(item);
            Intent intent = new Intent(activity, PaydayBeneficiaryActivity.class);
            intent.putExtra("beneficiary", (Beneficiary) item);
            intent.putExtra("index", TYPE.EDIT);
            startActivity(intent);
        } else if (item instanceof P2CBeneficiary) {
            beneficiaryViewModel.setSelectedOptionBeneficiary(item);
            Intent intent = new Intent(activity, CreditCardBeneficiaryActivity.class);
            intent.putExtra("beneficiary", (P2CBeneficiary) item);
            intent.putExtra("index", TYPE.EDIT);
            startActivity(intent);
        }
    }

    private void deleteBeneficiary(Object item) {
        if (item instanceof LocalBeneficiary) {
            beneficiaryViewModel.setSelectedOptionBeneficiary(item);
            addOtpObserver(TYPE.DELETE, item);
            showMessage(getString(R.string.delete_warning), item);
        } else if (item instanceof Beneficiary) {
            beneficiaryViewModel.setSelectedOptionBeneficiary(item);
            addPaydayOtpObserver(TYPE.DELETE, item);
            showMessage(getString(R.string.delete_warning), item);
        } else if (item instanceof P2CBeneficiary) {
            beneficiaryViewModel.setSelectedOptionBeneficiary(item);
            addP2COtpObserver(TYPE.DELETE, item);
            showMessage(getString(R.string.delete_warning), item);
        }
    }


    private void enableBeneficairy(Object item) {
        if (item instanceof LocalBeneficiary) {
            beneficiaryViewModel.setSelectedOptionBeneficiary(item);
            addOtpObserver(TYPE.ENABLE, item);

            if (((LocalBeneficiary) item).getEnabled()) {
                showMessage(getString(R.string.disable_warning), item);
            } else {
                showMessage(getString(R.string.enable_warning), item);
            }
        } else if (item instanceof Beneficiary) {
            beneficiaryViewModel.setSelectedOptionBeneficiary(item);

            if (((Beneficiary) item).getEnabled()) {
                addPaydayOtpObserver(TYPE.ENABLE, item);
                showMessage(getString(R.string.disable_warning), item);
            } else {
                addPaydayOtpObserver(TYPE.ENABLE, item);
                showMessage(getString(R.string.enable_warning), item);
            }
        } else if (item instanceof P2CBeneficiary) {
            beneficiaryViewModel.setSelectedOptionBeneficiary(item);

            if (((P2CBeneficiary) item).getEnabled()) {
                addP2COtpObserver(TYPE.ENABLE, item);
                showMessage(getString(R.string.disable_warning), item);
            } else {
                addP2COtpObserver(TYPE.ENABLE, item);
                showMessage(getString(R.string.enable_warning), item);
            }
        }
    }

    private PopupMenu showPopUpMenu(Object beneficiary, View view) {
        PopupMenu menu = new PopupMenu(activity, view);
        menu.inflate(R.menu.menu_beneficiary);

        if (beneficiary instanceof LocalBeneficiary) {
            if (((LocalBeneficiary) beneficiary).getEnabled()) {
                menu.getMenu().getItem(2).setTitle(getString(R.string.disable));
            } else {
                menu.getMenu().getItem(2).setTitle(getString(R.string.enable));
            }
        } else if (beneficiary instanceof Beneficiary) {
            if (((Beneficiary) beneficiary).getEnabled()) {
                menu.getMenu().getItem(2).setTitle(getString(R.string.disable));
            } else {
                menu.getMenu().getItem(2).setTitle(getString(R.string.enable));
            }
        } else if (beneficiary instanceof P2CBeneficiary) {
            if (((P2CBeneficiary) beneficiary).getEnabled()) {
                menu.getMenu().getItem(2).setTitle(getString(R.string.disable));
            } else {
                menu.getMenu().getItem(2).setTitle(getString(R.string.enable));
            }
        }
        return menu;
    }


    private void showMessage(String string, Object item) {
        new EligibilityDialogFragment.Builder(dialog -> {

        }).setBtn1Text(getString(R.string.ok))
            .setBtn2Text(getString(R.string.cancel))
            .setConfirmListener(dialog -> {
                User user = UserPreferences.Companion.getInstance().getUser(activity);
                if (user == null) return;
                if (item instanceof LocalBeneficiary) {
                    beneficiaryViewModel.generateOtp(user.getToken(), user.getSessionId(), user.getRefreshToken(), user.getCustomerId());
                } else if (item instanceof Beneficiary) {
                    beneficiaryViewModel.getLocalOtp(user.getToken(), user.getSessionId(), user.getRefreshToken(), user.getCustomerId());
                } else if (item instanceof P2CBeneficiary) {
                    beneficiaryViewModel.getP2COtp(user.getToken(), user.getSessionId(), user.getRefreshToken(), user.getCustomerId());
                }

                dialog.dismiss();
            })
            .setCancelListener(DialogInterface::dismiss)
            .setTitle(string)
            .build()
            .show(getFragmentManager(), "PasswordUpdate");
    }

    private void addOtpObserver(TYPE type, Object beneficiary) {
        if (activity == null) return;

        beneficiaryViewModel.getOtpState().observe(this, event -> {
            if (event == null) return;

            NetworkState2<String> otpResponse = event.getContentIfNotHandled();

            if (otpResponse == null) return;

            if (otpResponse instanceof NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing));
                return;
            }

            activity.hideProgress();
            if (otpResponse instanceof NetworkState2.Success) {

                if (beneficiaryViewModel.getOptionType() == TYPE.DELETE) {
                    Intent intent = new Intent(activity, LocalBeneficiaryActivity.class);
                    intent.putExtra("index", TYPE.DELETE);
                    intent.putExtra("beneficiary", (LocalBeneficiary) beneficiaryViewModel.getSelectedOptionBeneficiary());
                    startActivity(intent);

                } else if (beneficiaryViewModel.getOptionType() == TYPE.ENABLE) {
                    Intent intent = new Intent(activity, LocalBeneficiaryActivity.class);
                    intent.putExtra("index", TYPE.ENABLE);
                    intent.putExtra("beneficiary", (LocalBeneficiary) beneficiaryViewModel.getSelectedOptionBeneficiary());
                    if (((LocalBeneficiary) beneficiaryViewModel.getSelectedOptionBeneficiary()).getEnabled()) {
                        intent.putExtra("enable", 0);
                    } else {
                        intent.putExtra("enable", 1);
                    }
                    startActivity(intent);
                }
            } else if (otpResponse instanceof NetworkState2.Error) {
                NetworkState2.Error<String> error = (NetworkState2.Error<String>) otpResponse;
                if (error.isSessionExpired()) {
                    activity.onSessionExpired(error.getMessage());
                    return;
                }
//                activity.onError(error.getMessage());
                activity.handleErrorCode(
                    Integer.parseInt(error.getErrorCode()), error.getMessage()
                );

            } else if (otpResponse instanceof NetworkState2.Failure) {

                NetworkState2.Failure<String> error = (NetworkState2.Failure<String>) otpResponse;
                activity.onFailure(activity.findViewById(R.id.card_view), error.getThrowable());

            } else {
                activity.onFailure(activity.findViewById(R.id.card_view), ConstantsKt.CONNECTION_ERROR);
            }
        });
    }

    private void addPaydayOtpObserver(TYPE type, Object beneficiary) {
        if (activity == null) return;

        beneficiaryViewModel.getLocalOtpState().observe(this, event -> {
            if (event == null) return;

            NetworkState2<String> otpResponse = event.getContentIfNotHandled();

            if (otpResponse == null) return;

            if (otpResponse instanceof NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing));
                return;
            }

            activity.hideProgress();
            if (otpResponse instanceof NetworkState2.Success) {
                if (beneficiaryViewModel.getOptionType() == TYPE.DELETE) {
                    Intent intent = new Intent(activity, PaydayBeneficiaryActivity.class);
                    intent.putExtra("index", TYPE.DELETE);
                    intent.putExtra("beneficiary", (Beneficiary) beneficiaryViewModel.getSelectedOptionBeneficiary());
                    startActivity(intent);
                } else if (beneficiaryViewModel.getOptionType() == TYPE.ENABLE) {
                    Intent intent = new Intent(activity, PaydayBeneficiaryActivity.class);
                    intent.putExtra("index", TYPE.ENABLE);
                    intent.putExtra("beneficiary", (Beneficiary) beneficiaryViewModel.getSelectedOptionBeneficiary());
                    if (((Beneficiary) beneficiaryViewModel.getSelectedOptionBeneficiary()).getEnabled()) {
                        intent.putExtra("enable", 0);
                    } else {
                        intent.putExtra("enable", 1);
                    }
                    startActivity(intent);
                }
            } else if (otpResponse instanceof NetworkState2.Error) {
                NetworkState2.Error<String> error = (NetworkState2.Error<String>) otpResponse;

                if (error.isSessionExpired()) {
                    activity.onSessionExpired(error.getMessage());
                    return;
                }
//                activity.onError(error.getMessage());
                activity.handleErrorCode(
                    Integer.parseInt(error.getErrorCode()), error.getMessage()
                );
            } else if (otpResponse instanceof NetworkState2.Failure) {
                NetworkState2.Failure<String> error = (NetworkState2.Failure<String>) otpResponse;
                activity.onFailure(activity.findViewById(R.id.card_view), error.getThrowable());

            } else {
                activity.onFailure(activity.findViewById(R.id.card_view), ConstantsKt.CONNECTION_ERROR);
            }
        });
    }

    private void addP2COtpObserver(TYPE type, Object beneficiary) {
        if (activity == null) return;

        beneficiaryViewModel.getP2cOtpState().observe(this, event -> {
            if (event == null) return;

            NetworkState2<String> otpResponse = event.getContentIfNotHandled();

            if (otpResponse == null) return;

            if (otpResponse instanceof NetworkState2.Loading) {
                activity.showProgress(getString(R.string.processing));
                return;
            }

            activity.hideProgress();
            if (otpResponse instanceof NetworkState2.Success) {
                if (beneficiaryViewModel.getOptionType() == TYPE.DELETE) {
                    Intent intent = new Intent(activity, CreditCardBeneficiaryActivity.class);
                    intent.putExtra("index", TYPE.DELETE);
                    intent.putExtra("beneficiary", (P2CBeneficiary) beneficiaryViewModel.getSelectedOptionBeneficiary());
                    startActivity(intent);
                } else if (beneficiaryViewModel.getOptionType() == TYPE.ENABLE) {
                    Intent intent = new Intent(activity, CreditCardBeneficiaryActivity.class);
                    intent.putExtra("index", TYPE.ENABLE);
                    intent.putExtra("beneficiary", (P2CBeneficiary) beneficiaryViewModel.getSelectedOptionBeneficiary());
                    if (((P2CBeneficiary) beneficiaryViewModel.getSelectedOptionBeneficiary()).getEnabled()) {
                        intent.putExtra("enable", 0);
                    } else {
                        intent.putExtra("enable", 1);
                    }
                    startActivity(intent);
                }
            } else if (otpResponse instanceof NetworkState2.Error) {
                NetworkState2.Error<String> error = (NetworkState2.Error<String>) otpResponse;

                if (error.isSessionExpired()) {
                    activity.onSessionExpired(error.getMessage());
                    return;
                }
//                activity.onError(error.getMessage());
                activity.handleErrorCode(
                    Integer.parseInt(error.getErrorCode()), error.getMessage()
                );
            } else if (otpResponse instanceof NetworkState2.Failure) {
                NetworkState2.Failure<String> error = (NetworkState2.Failure<String>) otpResponse;
                activity.onFailure(activity.findViewById(R.id.card_view), error.getThrowable());

            } else {
                activity.onFailure(activity.findViewById(R.id.card_view), ConstantsKt.CONNECTION_ERROR);
            }
        });
    }


    @Override
    public void onBeneficiaryClick(Beneficiary beneficiary, boolean isChecked) {

        if (isChecked) {
            activity.getViewModel().setChecked(true);
        }
        activity.getViewModel().setSelectedP2PBeneficiary(beneficiary);
        activity.onBeneficiarySelected();
    }

    public boolean checkCardStatus() {
        if (activity != null && activity.getViewModel() != null && activity.getViewModel().getSelectedCard() != null) {
            if (ConstantsKt.CARD_STOP_LIST.equalsIgnoreCase(activity.getViewModel().getSelectedCard().getCardStatus())
                || ConstantsKt.CARD_CANCELLED.equalsIgnoreCase(activity.getViewModel().getSelectedCard().getCardStatus())
                || ConstantsKt.CARD_REPLACED.equalsIgnoreCase(activity.getViewModel().getSelectedCard().getCardStatus())) {

                activity.showMessage(
                    getString(R.string.card_blocked),
                    R.drawable.ic_insufficient_fund_44,
                    R.color.colorError,
                    Dialog::dismiss
                );

                return false;
            }
            return true;
        }
        return false;
    }
}
