package com.fh.payday.datasource;

import android.content.Context;
import android.content.res.TypedArray;

import com.fh.payday.R;
import com.fh.payday.datasource.models.AtmLocator;
import com.fh.payday.datasource.models.CardDisputeOption;
import com.fh.payday.datasource.models.CardTransactionDetails;
import com.fh.payday.datasource.models.Country;
import com.fh.payday.datasource.models.CurrencyConv;
import com.fh.payday.datasource.models.Item;
import com.fh.payday.datasource.models.MonthlyStatement;
import com.fh.payday.datasource.models.Option;
import com.fh.payday.datasource.models.Plan;
import com.fh.payday.datasource.models.QuestionBank;
import com.fh.payday.datasource.models.RegisterStep;
import com.fh.payday.datasource.models.SMSBanking;
import com.fh.payday.datasource.models.TransactionHistoryDetail;
import com.fh.payday.datasource.models.message.MessageBody;
import com.fh.payday.datasource.models.message.MessageOption;
import com.fh.payday.datasource.models.moneytransfer.Beneficiary;
import com.fh.payday.datasource.models.moneytransfer.ui.EditBeneficiaryOptions;
import com.fh.payday.datasource.models.moneytransfer.ui.TransferSummary;
import com.fh.payday.datasource.models.shared.ListModel;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataGenerator {
    private static final double[] lat = {25.1201842, 25.1252667, 25.0875595};
    private static final double[] longitude = {55.1978097, 55.2037588, 55.1786495};

    private static final double[] latOffice = {24.3871984, 25.0757073, 25.3177395};
    private static final double[] longitudeOffice = {54.2784198, 54.9475464, 55.3705193};


    //Generating data for ATM LOCATION FRAGMENT
    public static List<AtmLocator> getAtmLocation(Context context) {
        List<AtmLocator> atmLocators = new ArrayList<>();
        atmLocators.add(new AtmLocator("Dubai - UAE", "Mall of the Emirates", 25.1201842, 55.1978097, R.drawable.ic_bank));
        atmLocators.add(new AtmLocator("Dubai - UAE", "Emirates Mall Branch", 25.1201842, 55.1978097, R.drawable.ic_withdraw_money));
        atmLocators.add(new AtmLocator("Dubai - UAE", "Dubai Metro Station", 25.1252667, 55.2037588, R.drawable.ic_withdraw_money));
        atmLocators.add(new AtmLocator("Dubai - UAE", "Al Barsha", 25.0875595, 55.1786495, R.drawable.ic_bank));
        return atmLocators;
    }

 /*   public static List<BranchLocator> getLocators(Context context) {
        List<BranchLocator> branchLocators = new ArrayList<>();
        branchLocators.add(new BranchLocator(R.drawable.ic_bank,"Head Office","Abu Dhabi - UAE", "Sat-Thurs(8am - 5pm)",
                24.4791319,54.3523223, new BranchLocatorDetails("Orjowan Tower Building, Zayed 1st Street, Khalidiya Area P.O.Box: 7878 Abu Dhabi, U.A.E.",
                "UAE Toll Free: 600511114", "Tel: +971 2 619 4000","Fax: +971 2 619 4099","Email: customerservice@fh.ae")));
        branchLocators.add(new BranchLocator(R.drawable.ic_bank,"Dubai Branch","Dubai - UAE", "Sat-Thurs(8am - 5pm)",
                25.1510075,55.2243277, new BranchLocatorDetails("Zayed Road, Al Quz Area, 3rd Interchange Towards Dubai P.O.Box 124100 Dubai, U.A.E.",
                "UAE Toll Free: 600511114", "Tel: +971 4 4075000","Fax: +971 4 3285622","Email: customerservice@fh.ae")));
        branchLocators.add(new BranchLocator(R.drawable.ic_bank,"Sharjah Branch","Sharjah - UAE", "Sat-Thurs(8am - 5pm)",
                25.2247164,55.1798503, new BranchLocatorDetails("Khan Corniche Street, P.O.Box 31004 Sharjah, U.A.E.",
                "UAE Toll Free: 600511114", "Tel: +971 6 5932333","Fax: +971 6 5302459","Email: customerservice@fh.ae")));
        branchLocators.add(new BranchLocator(R.drawable.ic_bank,"Mussafah Branch","Mussafah - UAE", "Sat-Thurs(8am - 5pm)",
                24.37393,54.517064, new BranchLocatorDetails("17th Street, Mussafah Industrial Area, P.O. Box 2878 Abu Dhabi, U.A.E.",
                "UAE Toll Free: 600511114", "Tel: +971 2 307 4444","Fax: +971 2 307 4455","Email: customerservice@fh.ae")));
        return  branchLocators;
    }*/

    //Generating data for FAQ FRAGMENT
    public static List<QuestionBank> getQuestions(Context context) {
        List<QuestionBank> questionsList = new ArrayList<>();
   /*     String[] questions = context.getResources().getStringArray(R.array.questions);
        String[] answers = context.getResources().getStringArray(R.array.answers);


        for (int i = 0; i < questions.length; i++) {
            questionsList.add(new QuestionBank(questions[i], answers[i]));
        }*/
        return questionsList;
    }

    //Generating data for Card statement transaction
    public static List<CardTransactionDetails> getTransactions(Context context) {

        List<CardTransactionDetails> cardTransactions = new ArrayList<>();
       /* List<String> cardMiniStatements = new ArrayList<>();
        String[] transactionType = context.getResources().getStringArray(R.array.transaction_detail);
        String[] transactionDate = context.getResources().getStringArray(R.array.transaction_date);
        String[] transactionAmount = context.getResources().getStringArray(R.array.transaction_amount);
        String[] transactionItemDetails = context.getResources().getStringArray(R.array.transaction_item_details);

        cardMiniStatements.addAll(Arrays.asList(transactionItemDetails));


        for (int i = 0; i < transactionType.length; i++) {
            cardTransactions.add(new CardTransactionDetails(transactionType[i], transactionDate[i], transactionAmount[i], cardMiniStatements));
        }*/
        return cardTransactions;
    }

    //Generating data for Card statement transaction
    public static List<CardDisputeOption> getTransactionsDisputeOptions(Context context) {

        List<CardDisputeOption> cardTransactions = new ArrayList<>();
       /* String[] transactionType = context.getResources().getStringArray(R.array.transaction_detail);
        String[] transactionDate = context.getResources().getStringArray(R.array.transaction_date);
        String[] transactionAmount = context.getResources().getStringArray(R.array.transaction_amount);
        String[] transactionItemDetails = context.getResources().getStringArray(R.array.transaction_item_details);


        for (int i = 0; i < transactionType.length; i++) {
            cardTransactions.add(new CardDisputeOption(transactionType[i], transactionDate[i], transactionAmount[i]));
        }*/
        return cardTransactions;
    }

    // Generating Data for Options
    public static List<Option> getProducts(Context context, boolean hasLoan) {
        List<Option> products = new ArrayList<>();
        List<String> productOption = new ArrayList<>();

        if (!hasLoan) {
            productOption.add(context.getString(R.string.apply_loan_top_up_title));
            //productOption.add(context.getString(R.string.payday_finance));
        } else {
            productOption.add(context.getString(R.string.loan));
            //productOption.add(context.getString(R.string.payday_finance));
        }
        TypedArray productIcons = context.getResources().obtainTypedArray(R.array.product_icons);
        for (int i = 0; i < productOption.size(); i++) {
            products.add(new Option(productOption.get(i), productIcons.getResourceId(i, 0), context.getString(R.string.apply)));
        }
        productIcons.recycle();
        return products;
    }

    // Generating Data for Options
    public static List<Option> getAccountOptions(Context context) {
        List<Option> products = new ArrayList<>();
        String[] productOption = context.getResources().getStringArray(R.array.account_option);
        TypedArray productIcons = context.getResources().obtainTypedArray(R.array.account_options);
        for (int i = 0; i < productOption.length; i++) {
            products.add(new Option(productOption[i], productIcons.getResourceId(i, 0), "Link"));
        }
        productIcons.recycle();
        return products;
    }

    //Generating Data for Apply Credit Card
    public static List<ListModel> getCCDetails(Context context) {
        List<ListModel> creditCardDetails = new ArrayList<>();
    /*    String[] keys = context.getResources().getStringArray(R.array.credit_card_key);
        String[] values = context.getResources().getStringArray(R.array.credit_card_value);
        for (int i = 0; i < keys.length; i++) {
            creditCardDetails.add(new ListModel(keys[i], values[i]));
        }*/
        return creditCardDetails;
    }

    // Generating transaction history Options
    public static List<Item> getTransactionHistory(Context context) {
        List<Item> itemList = new ArrayList<>();
        String[] transactionHistory = context.getResources().getStringArray(R.array.transaction_history);
        TypedArray transactionHistoryIcons = context.getResources().obtainTypedArray(R.array.transaction_history_icons);
        for (int i = 0; i < transactionHistory.length; i++) {
            itemList.add(new Item(transactionHistory[i], transactionHistoryIcons.getResourceId(i, 0)));
        }

        transactionHistoryIcons.recycle();
        return itemList;
    }

    public static List<Item> getTrackTransaction(Context context) {

        List<Item> itemList = new ArrayList<>();
        String[] trackTransaction = context.getResources().getStringArray(R.array.track_transaction);
        TypedArray trackTransactionIcons = context.getResources().obtainTypedArray(R.array.track_transaction_icons);
        for (int i = 0; i < trackTransaction.length; i++) {
            itemList.add(new Item(trackTransaction[i], trackTransactionIcons.getResourceId(i, 0)));
        }
        trackTransactionIcons.recycle();
        return itemList;
    }

    public static List<String> getIntlTransferTabs(Context context) {
        String[] tempList = context.getResources().getStringArray(R.array.intl_transfer_tabs);
        return new ArrayList<>(Arrays.asList(tempList));
    }

    public static List<String> getIntlTransactionTabs(Context context) {
        String[] tempList = context.getResources().getStringArray(R.array.intl_transaction_tabs);
        return new ArrayList<>(Arrays.asList(tempList));
    }

    //BottomSheet Transaction Options
    public static ArrayList<Item> getIntlTransactionOptions(Context context) {
        ArrayList<Item> itemList = new ArrayList<>();
        String[] transactionOptions = context.getResources().getStringArray(R.array.transaction_options);
        TypedArray transactionOptionIcons = context.getResources().obtainTypedArray(R.array.transaction_option_icons);
        for (int i = 0; i < transactionOptions.length; i++) {
            itemList.add(new Item(transactionOptions[i], transactionOptionIcons.getResourceId(i, 0)));
        }
        transactionOptionIcons.recycle();
        return itemList;
    }
    public static ArrayList<Item> getIntlTransactionOptions2(Context context) {
        ArrayList<Item> itemList = new ArrayList<>();
        String[] transactionOptions = context.getResources().getStringArray(R.array.transaction_options2);
        TypedArray transactionOptionIcons = context.getResources().obtainTypedArray(R.array.transaction_option_icons2);
        for (int i = 0; i < transactionOptions.length; i++) {
            itemList.add(new Item(transactionOptions[i], transactionOptionIcons.getResourceId(i, 0)));
        }
        transactionOptionIcons.recycle();
        return itemList;
    }
    // Generating transaction history  option data
    public static List<Item> getTransactionHistoryOption(Context context) {
        List<Item> itemList = new ArrayList<>();
        String[] transactionHistory = context.getResources().getStringArray(R.array.transaction_history_option);
        TypedArray transactionHistoryIcons = context.getResources().obtainTypedArray(R.array.transaction_history_option_icons);
        for (int i = 0; i < transactionHistory.length; i++) {
            itemList.add(new Item(transactionHistory[i], transactionHistoryIcons.getResourceId(i, 0)));
        }
        transactionHistoryIcons.recycle();
        return itemList;
    }

    // Generating transaction history data
    public static List<ListModel> getPaydayCardDetails(Context context) {
        List<ListModel> itemList = new ArrayList<>();
//        String[] keys = context.getResources().getStringArray(R.array.payday_card_key);
//        String[] values = context.getResources().getStringArray(R.array.payday_card_value);
//        for (int i = 0; i < keys.length; i++) {
//            itemList.add(new ListModel(keys[i], values[i]));
//        }
        return itemList;
    }

    // Generating transaction history data
    public static List<ListModel> getPaydayCardOverdraftDetails(Context context) {
        List<ListModel> itemList = new ArrayList<>();
//        String[] keys = context.getResources().getStringArray(R.array.payday_card_overdraft_key);
//        String[] values = context.getResources().getStringArray(R.array.payday_card_overdraft_value);
//        for (int i = 0; i < keys.length; i++) {
//            itemList.add(new ListModel(keys[i], values[i]));
//        }
        return itemList;
    }

    // Generating transaction history  option data
    public static List<TransactionHistoryDetail> getTransactionHistoryDetail(Context context) {
        List<TransactionHistoryDetail> transactionHistoryDetail = new ArrayList<>();
//        String[] transactionType = context.getResources().getStringArray(R.array.transaction_history_details_type);
//        String[] transactionDate = context.getResources().getStringArray(R.array.transaction_history_details_date);
//        String[] transactionAmount = context.getResources().getStringArray(R.array.transaction_history_details_amount);
//        for (int i = 0; i < transactionType.length; i++) {
//            transactionHistoryDetail.add(new TransactionHistoryDetail(transactionType[i], transactionDate[i], transactionAmount[i]));
//        }
        return transactionHistoryDetail;
    }

    //    Generating data fpr card email Subscription Month Statement
    public static List<Item> getSettingOptions(Context context) {
        List<Item> monthStatements = new ArrayList<>();
        String[] monthEmailStatement = context.getResources().getStringArray(R.array.setting_options);
        TypedArray beneficiaryIcons = context.getResources().obtainTypedArray(R.array.setting_icons);

        for (int i = 0; i < monthEmailStatement.length; i++) {
            monthStatements.add(new Item(monthEmailStatement[i], beneficiaryIcons.getResourceId(i, 0)));
        }

        beneficiaryIcons.recycle();

        return monthStatements;
    }


    //Generating data for CURRENCY CONVERTER FRAGMENT
    public static List<Country> getCountries() {
        List<Country> countries = new ArrayList<>();
//        countries.add(new Country(R.drawable.ic_pakistan, "Pakistan", "PKR", "PKR"));
//        countries.add(new Country(R.drawable.ic_united_arab_emirates, "United Arab Emirates", "AED", "AED"));
//        countries.add(new Country(R.drawable.ic_afghanistan, "Afghanistan", "AFN", "AFN"));
//        countries.add(new Country(R.drawable.ic_india, "India", "INR", "INR"));
//        countries.add(new Country(R.drawable.ic_china, "China", "CYN", "CNY"));
//        countries.add(new Country(R.drawable.ic_saudi_arabia, "Saudi Arabia", "SAR", "SAR"));
//        countries.add(new Country(R.drawable.ic_turkey, "Turkey", "TRY", "TRY"));
//        countries.add(new Country(R.drawable.ic_indonesia, "Indonesia", "IDR", "IDR"));
        return countries;
    }

    public static List<RegisterStep> getInstructions() {
        List<RegisterStep> registerSteps = new ArrayList<>();
        registerSteps.add(new RegisterStep("- Capture and Verify Emirates ID"));
        registerSteps.add(new RegisterStep("- Scan and Verify Payday Card"));
        registerSteps.add(new RegisterStep("- Verify Mobile Number"));
        registerSteps.add(new RegisterStep("- Create Login Details"));
        registerSteps.add(new RegisterStep("- Add Biometric Authentication ( Optional )"));
        return registerSteps;
    }

    public static List<Item> getAreaOfIssue() {
        List<Item> items = new ArrayList<>();
        items.add(new Item("Payday Card", 0));
        items.add(new Item("Account", 0));
        items.add(new Item("Profile", 0));
        items.add(new Item("Transactions", 0));
        items.add(new Item("Statement", 0));
        return items;
    }

    public static List<Item> getIssues() {
        List<Item> items = new ArrayList<>();
        items.add(new Item("Scan Payday Card", 0));
        items.add(new Item("Scan Emirates ID", 0));
        items.add(new Item("Confirm Card Details", 0));
        items.add(new Item("Enter Pin", 0));
        items.add(new Item("Verify Mobile Number", 0));
        items.add(new Item("Create Login Details", 0));
        return items;
    }

    public static CurrencyConv convertCurrency(int position) {
        switch (position) {
            case 0:
                return new CurrencyConv(null, null, 1, 26, "27-10-2018");
            case 1:
                return new CurrencyConv(null, null, 1, 8, "27-10-2018");
            case 2:
                return new CurrencyConv(null, null, 1, 45, "27-10-2018");
            case 3:
                return new CurrencyConv(null, null, 1, 4, "27-10-2018");
            case 4:
                return new CurrencyConv(null, null, 1, 6, "27-10-2018");
            case 5:
                return new CurrencyConv(null, null, 1, 12, "27-10-2018");
            case 6:
                return new CurrencyConv(null, null, 1, 98, "27-10-2018");
            case 7:
                return new CurrencyConv(null, null, 1, 126, "27-10-2018");
            default:
                return new CurrencyConv(null, null, 1, 26, "27-10-2018");
        }
    }

    public static List<EditBeneficiaryOptions> getEditBeneficiaryData(Context context, List<Beneficiary> paydayList, List<Beneficiary> localList,
                                                                      List<Beneficiary> internationalList, List<Beneficiary> loanList) {
        List<EditBeneficiaryOptions> beneficiaryOptions = new ArrayList<>();
        String[] editBeneficiaryOptions = context.getResources().getStringArray(R.array.edit_beneficiary_options);
        TypedArray beneficiaryIcons = context.getResources().obtainTypedArray(R.array.edit_beneficiary_icons);

        beneficiaryOptions.add(new EditBeneficiaryOptions(beneficiaryIcons.getResourceId(0, 0), editBeneficiaryOptions[0], paydayList));
        beneficiaryOptions.add(new EditBeneficiaryOptions(beneficiaryIcons.getResourceId(1, 0), editBeneficiaryOptions[1], localList));
        beneficiaryOptions.add(new EditBeneficiaryOptions(beneficiaryIcons.getResourceId(2, 0), editBeneficiaryOptions[2], internationalList));
        beneficiaryOptions.add(new EditBeneficiaryOptions(beneficiaryIcons.getResourceId(3, 0), editBeneficiaryOptions[3], loanList));

        beneficiaryIcons.recycle();

        return beneficiaryOptions;
    }


    public static List<Item> getBeneficiaryOptions(Context context) {
        List<Item> items = new ArrayList<>();
        String[] editBeneficiaryOptions = context.getResources().getStringArray(R.array.edit_beneficiary_options);
        TypedArray beneficiaryIcons = context.getResources().obtainTypedArray(R.array.edit_beneficiary_icons);

        for (int i = 0; i < editBeneficiaryOptions.length; i++) {
            items.add(new Item(
                    editBeneficiaryOptions[i],
                    beneficiaryIcons.getResourceId(i, 0)));
        }

        return items;
    }

    public static List<Item> getLocatorOptions(Context context) {
        List<Item> items = new ArrayList<>();
        String[] locatorOptions = context.getResources().getStringArray(R.array.locator_options);
        TypedArray locatorIcons = context.getResources().obtainTypedArray(R.array.locator_icons);

        for (int i = 0; i < locatorOptions.length; i++) {
            items.add(new Item(
                    locatorOptions[i],
                    locatorIcons.getResourceId(i, 0)));
        }

        return items;
    }

    @NotNull
    public static List<Item> getIntlRemittanceLocator(Context context) {
        List<Item> items = new ArrayList<>();
        String[] locatorOptions = context.getResources().getStringArray(R.array.intl_locator_options);
        TypedArray locatorIcons = context.getResources().obtainTypedArray(R.array.locator_icons);

        for (int i = 0; i < locatorOptions.length; i++) {
            items.add(new Item(
                    locatorOptions[i],
                    locatorIcons.getResourceId(i, 0)));
        }

        locatorIcons.recycle();

        return items;
    }

    public static List<Item> getContactUsOptions(Context context) {
        List<Item> items = new ArrayList<>();
        String[] locatorOptions = context.getResources().getStringArray(R.array.contact_us_options);
        TypedArray locatorIcons = context.getResources().obtainTypedArray(R.array.contact_us_icons);

        for (int i = 0; i < locatorOptions.length; i++) {
            items.add(new Item(
                    locatorOptions[i],
                    locatorIcons.getResourceId(i, 0)));
        }

        locatorIcons.recycle();

        return items;
    }

    public static List<SMSBanking> getSMSCodes(Context context) {
        List<SMSBanking> items = new ArrayList<>();
        String[] smsCodes = context.getResources().getStringArray(R.array.sms_codes);
        String[] description = context.getResources().getStringArray(R.array.sms_description);

        for (int i = 0; i < smsCodes.length; i++) {
            items.add(new SMSBanking(
                    smsCodes[i],
                    description[i]));
        }

        return items;
    }

    public static List<Plan> getCallingCards() {
        List<Plan> callingCards = new ArrayList<>();
        callingCards.add(new Plan(15, 0));
        callingCards.add(new Plan(30, 0));
        callingCards.add(new Plan(50, 0));
        callingCards.add(new Plan(75, 0));
        return callingCards;
    }

    public static List<ListModel> getLoanDetails(Context context) {
        List<ListModel> loanDetails = new ArrayList<>();
    /*    String[] loanKey = context.getResources().getStringArray(R.array.loan_title);
        String[] loanValue = context.getResources().getStringArray(R.array.loan_details);

        for (int i = 0; i < loanKey.length; i++) {
            loanDetails.add(new ListModel(loanKey[i], loanValue[i]));
        }*/
        return loanDetails;
    }

    public static List<TransferSummary> getIntlTransferSummary() {
        List<TransferSummary> intlTransferSummary = new ArrayList<>();
     /*   intlTransferSummary.add(new TransferSummary("Description", "No Description"));
        intlTransferSummary.add(new TransferSummary("Purpose of Payment", "Lorem Ipsum"));
        intlTransferSummary.add(new TransferSummary("Exchange Amount", "AED 0.00"));
        intlTransferSummary.add(new TransferSummary("Exchange Rate", "20.03"));
        intlTransferSummary.add(new TransferSummary("Remittance Currency", "INR"));
        intlTransferSummary.add(new TransferSummary("Remittance Country", "India"));
        intlTransferSummary.add(new TransferSummary("Mobile Number", "+91 9596979798"));*/
        return intlTransferSummary;
    }

    public static List<TransferSummary> getLocalTransferSummary() {
        List<TransferSummary> localTransferSummary = new ArrayList<>();
/*        localTransferSummary.add(new TransferSummary("Description", "No Description"));
        localTransferSummary.add(new TransferSummary("Currency", "AED"));
        localTransferSummary.add(new TransferSummary("Exchange Rate", "1.0"));
        localTransferSummary.add(new TransferSummary("Exchange Amount", "AED 1,500.00"));*/
        return localTransferSummary;
    }

    public static List<MonthlyStatement> getMonthlyStatements() {
        List<MonthlyStatement> statements = new ArrayList<>();
    /*    statements.add(new MonthlyStatement("January", 15000F, 5000F));
        statements.add(new MonthlyStatement("February", 2000F, 5000F));
        statements.add(new MonthlyStatement("March", 1000F, 5000F));
        statements.add(new MonthlyStatement("April", 1500F, 5000F));
        statements.add(new MonthlyStatement("May", 2200F, 5000F));
        statements.add(new MonthlyStatement("June", 1000F, 5000F));
        statements.add(new MonthlyStatement("July", 500F, 5000F));
        statements.add(new MonthlyStatement("August", 2000F, 5000F));
        statements.add(new MonthlyStatement("September", 1200F, 5000F));
        statements.add(new MonthlyStatement("October", 1300F, 5000F));
        statements.add(new MonthlyStatement("November", 2500F, 5000F));
        statements.add(new MonthlyStatement("December", 1000F, 5000F));*/
        return statements;
    }

    private static List<MessageBody> getMessageBody() {
        List<MessageBody> messageBodyList = new ArrayList<>();
        return messageBodyList;
    }

    public static List<MessageOption> getMessages() {
        List<MessageOption> messageOptions = new ArrayList<>();
        messageOptions.add(new MessageOption(R.drawable.ic_icon_inbox, "Inbox", getMessageBody()));
        messageOptions.add(new MessageOption(R.drawable.ic_outbox, "Sent", getMessageBody()));
        return messageOptions;
    }

    public static List<Item> getServiceRequestOptions() {
        List<Item> items = new ArrayList<>();
        items.add(new Item("Activate Card", R.drawable.ic_activate_card_blue));
        items.add(new Item("Card Transaction\nDispute", R.drawable.ic_card_transaction_dispute_blue));
        items.add(new Item("Reset PIN", R.drawable.ic_reset_change_pin));
        items.add(new Item("Block Card", R.drawable.ic_lock_card_blue));
        return items;
    }

    public static List<Item> getOffers() {
        List<Item> offerList = new ArrayList<>();
        /*offerList.add(new Item("Personal Loans", R.mipmap.intro1_bg));
        offerList.add(new Item("Credit Card", R.mipmap.banner));
        offerList.add(new Item("Discounts", R.mipmap.intro1_bg));
        offerList.add(new Item("Credit Card", R.mipmap.intro3_bg));
        offerList.add(new Item("Discounts", R.mipmap.intro1_bg));*/
        return offerList;
    }

    public static List<Option> getApplicationOptions() {
        List<Option> list = new ArrayList<>();
        list.add(new Option("Payday Finance", R.drawable.ic_loan_small, "Request"));
        list.add(new Option("Early Finance", R.drawable.ic_request_for_overdraft, "Request"));
        return list;

    }

    public static List<Item> getServices(Context context) {
        List<Item> itemList = new ArrayList<>();

        TypedArray beneficiaryIcons = context.getResources().obtainTypedArray(R.array.services_icons);
        String[] list = context.getResources().getStringArray(R.array.services);

        for (int i = 0; i < list.length; i++) {
            itemList.add(new Item(list[i], beneficiaryIcons.getResourceId(i, 0)));
        }

        beneficiaryIcons.recycle();

        return itemList;
    }

    public static List<Item> getFrequentServices(Context context) {
        List<Item> itemList = new ArrayList<>();

        TypedArray beneficiaryIcons = context.getResources().obtainTypedArray(R.array.freq_services_icons);
        String[] list = context.getResources().getStringArray(R.array.freq_services);

        for (int i = 0; i < list.length; i++) {
            itemList.add(new Item(list[i], beneficiaryIcons.getResourceId(i, 0)));
        }

        beneficiaryIcons.recycle();

        return itemList;
    }
}
