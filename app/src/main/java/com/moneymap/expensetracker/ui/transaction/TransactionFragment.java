package com.moneymap.expensetracker.ui.transaction;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import android.animation.ValueAnimator;
import android.animation.AnimatorSet;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.view.animation.Animation;
import java.util.Calendar;

import com.moneymap.expensetracker.R;
import com.moneymap.expensetracker.transaction.TransactionViewModel;

public class TransactionFragment extends Fragment {

    private RadioGroup radioGroupIncomeExpense;
    private AutoCompleteTextView actvCategories;
    private EditText Amount;
    private TextView SelectedDate, Add;
    private TextView incomeTotal, expenseTotal;
    private DatePickerDialog datePickerDialog;
    private TransactionViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_transaction, container, false);

        radioGroupIncomeExpense = view.findViewById(R.id.radio_group_income_expense);
        actvCategories = view.findViewById(R.id.categories);
        Amount = view.findViewById(R.id.amount);
        SelectedDate = view.findViewById(R.id.selected_date);
        Add = view.findViewById(R.id.add_transaction);
        incomeTotal = view.findViewById(R.id.income_total);
        expenseTotal = view.findViewById(R.id.expense_total);

        viewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);

        ArrayAdapter<CharSequence> adapterCategories = ArrayAdapter.createFromResource(getContext(),
                R.array.categories_array, android.R.layout.simple_dropdown_item_1line);
        actvCategories.setAdapter(adapterCategories);

        Calendar calendar = Calendar.getInstance();
        datePickerDialog = new DatePickerDialog(getContext(), (dpView, year, month, dayOfMonth) -> {
            String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
            SelectedDate.setText(selectedDate);
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH));

        SelectedDate.setOnClickListener(v -> datePickerDialog.show());

        Add.setOnClickListener(v -> addDataToDatabase());

        viewModel.getTotalIncome().observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double totalIncome) {
                updateTextViewWithAnimation(incomeTotal, totalIncome);
            }
        });

        viewModel.getTotalExpense().observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double totalExpense) {
                updateTextViewWithAnimation(expenseTotal, totalExpense);
            }
        });

        return view;
    }

    private void addDataToDatabase() {
        int selectedId = radioGroupIncomeExpense.getCheckedRadioButtonId();

        if (selectedId == -1) {
            Toast.makeText(getContext(), "Please select income or expense", Toast.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedRadioButton = getView().findViewById(selectedId);
        if (selectedRadioButton == null) {
            Toast.makeText(getContext(), "Error! retrieving transaction type", Toast.LENGTH_SHORT).show();
            return;
        }
        String type = selectedRadioButton.getText().toString();

        String category = actvCategories.getText().toString();
        String amountStr = Amount.getText().toString();
        String date = SelectedDate.getText().toString();

        if (TextUtils.isEmpty(category) || TextUtils.isEmpty(amountStr) || TextUtils.isEmpty(date)) {
            Toast.makeText(getContext(), "Please fill all fields!", Toast.LENGTH_SHORT).show();
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(amountStr);
        } catch (NumberFormatException e) {
            Toast.makeText(getContext(), "Please enter a valid amount!", Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.addTransaction(type, category, amount, date);
        clearFields();
    }

    private void clearFields() {
        radioGroupIncomeExpense.clearCheck();
        actvCategories.setText("");
        Amount.setText("");
        SelectedDate.setText("");
    }

    @SuppressLint("DefaultLocale")
    private void updateTextViewWithAnimation(TextView textView, double newValue) {
        @SuppressLint("DefaultLocale") final String newValueStr = String.format("%.2f", newValue);

        ValueAnimator valueAnimator = ObjectAnimator.ofFloat(0, (float) newValue);
        valueAnimator.setDuration(500);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.addUpdateListener(animation -> {
            float animatedValue = (float) animation.getAnimatedValue();
            textView.setText(String.format("%.2f", animatedValue));
        });

        ScaleAnimation scaleUp = new ScaleAnimation(
                1f, 1.1f,
                1f, 1.1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scaleUp.setDuration(250);
        scaleUp.setFillAfter(true);

        ScaleAnimation scaleDown = new ScaleAnimation(
                1.1f, 1f,
                1.1f, 1f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        scaleDown.setDuration(250);
        scaleDown.setStartOffset(250);
        scaleDown.setFillAfter(true);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playSequentially(valueAnimator);
        animatorSet.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                textView.startAnimation(scaleUp);
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                textView.startAnimation(scaleDown);
            }

            @Override
            public void onAnimationCancel(Animator animation) {}

            @Override
            public void onAnimationRepeat(Animator animation) {}
        });

        animatorSet.start();
    }
}