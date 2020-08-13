package com.fh.payday.utilities;

import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;

import com.fh.payday.views.fragments.AlertDialogFragment;
import com.fh.payday.views2.loan.apply.LoanDialogFragment;

public class AlertDialog {
    public static void showDialog(FragmentManager fragmentManager, String message, int icon) {
        DialogFragment newFragment = AlertDialogFragment.newInstance(message, icon);

        newFragment.show(fragmentManager, "dialog");
    }

    public static void showDialog(FragmentManager fragmentManager,String title, String amount) {
        DialogFragment newFragment = LoanDialogFragment.newInstance(title, amount);
        newFragment.show(fragmentManager, "dialog");

    }
}
