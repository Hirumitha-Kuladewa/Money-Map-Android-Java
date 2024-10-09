package com.moneymap.expensetracker.ui.summary;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import java.util.ArrayList;
import java.util.List;

import com.moneymap.expensetracker.R;
import com.moneymap.expensetracker.transaction.Transaction;
import com.moneymap.expensetracker.transaction.TransactionViewModel;
import com.moneymap.expensetracker.utils.PDFUtils;

public class ViewSummaryFragment extends Fragment {

    private EditText editTextDay, editTextMonth, editTextYear;
    private TextView buttonSearch, buttonSaveToPdf;
    private TableLayout tableLayoutResults;
    private BarChart barChart;
    private TransactionViewModel viewModel;

    private static final int CREATE_FILE_REQUEST_CODE = 1;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_view_summary, container, false);

        editTextDay = root.findViewById(R.id.editTextDay);
        editTextMonth = root.findViewById(R.id.editTextMonth);
        editTextYear = root.findViewById(R.id.editTextYear);
        buttonSearch = root.findViewById(R.id.buttonSearch);
        buttonSaveToPdf = root.findViewById(R.id.buttonSaveToPdf);
        tableLayoutResults = root.findViewById(R.id.tableLayoutResults);
        barChart = root.findViewById(R.id.barChart);

        viewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);

        populateTable(new ArrayList<>());

        buttonSearch.setOnClickListener(v -> performSearch());
        buttonSaveToPdf.setOnClickListener(v -> saveToPDF());

        return root;
    }

    private List<Transaction> filterTransactions(String day, String month, String year, List<Transaction> transactions) {
        List<Transaction> filteredTransactions = new ArrayList<>();

        if (transactions != null) {
            for (Transaction transaction : transactions) {
                String[] dateParts = transaction.getDate().split("/");
                String transDay = dateParts.length > 0 ? dateParts[0] : "";
                String transMonth = dateParts.length > 1 ? dateParts[1] : "";
                String transYear = dateParts.length > 2 ? dateParts[2] : "";

                if ((day.isEmpty() || day.equals(transDay)) &&
                        (month.isEmpty() || month.equals(transMonth)) &&
                        (year.isEmpty() || year.equals(transYear))) {
                    filteredTransactions.add(transaction);
                }
            }
        }
        return filteredTransactions;
    }


    @SuppressLint("SetTextI18n")
    private void performSearch() {
        String day = editTextDay.getText().toString().trim();
        String month = editTextMonth.getText().toString().trim();
        String year = editTextYear.getText().toString().trim();

        viewModel.getTransactions().observe(getViewLifecycleOwner(), transactions -> {
            List<Transaction> filteredTransactions = filterTransactions(day, month, year, transactions);

            tableLayoutResults.removeAllViews();
            if (filteredTransactions.isEmpty()) {
                TableRow noDataRow = new TableRow(getContext());
                TextView noDataView = new TextView(getContext());
                noDataView.setText("No transactions found!");
                noDataView.setTextSize(16);
                noDataRow.addView(noDataView);
                tableLayoutResults.addView(noDataRow);
            } else {
                populateTable(filteredTransactions);
            }

            float totalIncome = 0f;
            float totalExpenses = 0f;

            for (Transaction transaction : filteredTransactions) {
                if (transaction.getType().equals("Income")) {
                    totalIncome += transaction.getAmount();
                } else if (transaction.getType().equals("Expense")) {
                    totalExpenses += transaction.getAmount();
                }
            }

            populateBarChart(totalIncome, totalExpenses);
            displaySummary(totalIncome, totalExpenses);

            clearInputFields();
        });
    }

    private void clearInputFields() {
        editTextDay.setText("");
        editTextMonth.setText("");
        editTextYear.setText("");
    }

    @SuppressLint("DefaultLocale")
    private void populateTable(List<Transaction> transactions) {
        tableLayoutResults.removeAllViews();

        TableRow headerRow = new TableRow(getContext());
        headerRow.setBackgroundColor(Color.parseColor("#FF383838"));

        TextView typeHeader = createHeaderTextView(getString(R.string.type));
        TextView categoryHeader = createHeaderTextView(getString(R.string.category));
        TextView amountHeader = createHeaderTextView(getString(R.string.amount));
        TextView dateHeader = createHeaderTextView(getString(R.string.date));

        headerRow.addView(typeHeader);
        headerRow.addView(categoryHeader);
        headerRow.addView(amountHeader);
        headerRow.addView(dateHeader);

        tableLayoutResults.addView(headerRow);

        int rowCount = transactions.isEmpty() ? 5 : Math.min(transactions.size(), 5);
        for (int i = 0; i < rowCount; i++) {
            TableRow row = new TableRow(getContext());

            row.setBackgroundColor(Color.parseColor("#FF383838"));

            TextView typeView = createContentTextView();
            TextView categoryView = createContentTextView();
            TextView amountView = createContentTextView();
            TextView dateView = createContentTextView();

            if (i < transactions.size()) {
                Transaction transaction = transactions.get(i);
                typeView.setText(transaction.getType());
                categoryView.setText(transaction.getCategory());
                amountView.setText(String.format("%.2f", transaction.getAmount()));
                dateView.setText(transaction.getDate());
            } else {
                typeView.setText("");
                categoryView.setText("");
                amountView.setText("");
                dateView.setText("");
            }

            row.addView(typeView);
            row.addView(categoryView);
            row.addView(amountView);
            row.addView(dateView);

            tableLayoutResults.addView(row);
        }
    }

    private TextView createContentTextView() {
        TextView textView = new TextView(getContext());
        textView.setTextSize(14);
        textView.setPadding(16, 8, 16, 8);
        textView.setTextColor(Color.parseColor("#FFFFFFFF"));
        textView.setLayoutParams(createLayoutParams());
        return textView;
    }

    private TextView createHeaderTextView(String text) {
        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setLayoutParams(createLayoutParams());
        textView.setPadding(16, 8, 16, 8);
        textView.setTextSize(16);
        textView.setBackgroundColor(Color.parseColor("#00574B"));
        textView.setTextColor(Color.parseColor("#E4EFE7"));
        textView.setTypeface(null, Typeface.BOLD);
        return textView;
    }

    private TableRow.LayoutParams createLayoutParams() {
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(2, 2, 2, 2);
        return params;
    }

    private void populateBarChart(float totalIncome, float totalExpenses) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        List<Integer> colors = new ArrayList<>();

        entries.add(new BarEntry(0, totalIncome));
        labels.add("Total Income");
        colors.add(Color.parseColor("#00845A"));

        entries.add(new BarEntry(1, totalExpenses));
        labels.add("Total Expenses");
        colors.add(Color.parseColor("#C5141A"));

        float netIncome = totalIncome - totalExpenses;
        entries.add(new BarEntry(2, netIncome));
        labels.add("Net Income");
        colors.add(netIncome >= 0 ? Color.parseColor("#25A18E") : Color.parseColor("#C5141A"));

        BarDataSet dataSet = new BarDataSet(entries, "Income & Expenses");
        dataSet.setColors(colors);

        BarData barData = new BarData(dataSet);

        barChart.setData(barData);
        barChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(labels));
        barChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        barChart.getDescription().setEnabled(false);

        barChart.invalidate();
    }

    @SuppressLint("DefaultLocale")
    private void displaySummary(float totalIncome, float totalExpenses) {
        RelativeLayout summaryLayout = getView().findViewById(R.id.view_summary_total);
        TextView summaryTextView = summaryLayout.findViewById(R.id.summaryTextView);

        String totalIncomeText = String.format("Total Income: %.2f\n", totalIncome);
        String totalExpensesText = String.format("Total Expenses: %.2f\n", totalExpenses);
        String netIncomeText = String.format("Net Income: %.2f", totalIncome - totalExpenses);

        SpannableString spannableString = new SpannableString(totalIncomeText + totalExpensesText + netIncomeText);

        int incomeColor = Color.parseColor("#00845A");
        int expenseColor = Color.parseColor("#C5141A");
        int netIncomePositiveColor = Color.parseColor("#25A18E");
        int netIncomeNegativeColor = Color.parseColor("#C5141A");

        applyStyle(spannableString, totalIncomeText, incomeColor);
        applyStyle(spannableString, totalExpensesText, expenseColor);
        applyStyle(spannableString, netIncomeText,
                (totalIncome - totalExpenses >= 0) ? netIncomePositiveColor : netIncomeNegativeColor);

        summaryTextView.setText(spannableString);
    }

    private void applyStyle(SpannableString spannableString, String text, int color) {
        int startIndex = spannableString.toString().indexOf(text);
        int endIndex = startIndex + text.length();

        spannableString.setSpan(new StyleSpan(Typeface.BOLD), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        spannableString.setSpan(new ForegroundColorSpan(color), startIndex, endIndex, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private void saveToPDF() {
        String day = editTextDay.getText().toString().trim();
        String month = editTextMonth.getText().toString().trim();
        String year = editTextYear.getText().toString().trim();

        String dateSummary = !day.isEmpty() ? "Day: " + day : (!month.isEmpty() ? "Month: " + month : "Year: " + year);

        viewModel.getTransactions().observe(getViewLifecycleOwner(), transactions -> {
            List<Transaction> filteredTransactions = filterTransactions(day, month, year, transactions);

            float totalIncome = 0f;
            float totalExpenses = 0f;

            for (Transaction transaction : filteredTransactions) {
                if (transaction.getType().equals("Income")) {
                    totalIncome += transaction.getAmount();
                } else if (transaction.getType().equals("Expense")) {
                    totalExpenses += transaction.getAmount();
                }
            }

            float netIncome = totalIncome - totalExpenses;

            Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("application/pdf");
            intent.putExtra(Intent.EXTRA_TITLE, getString(R.string.transaction_summary_pdf));
            startActivityForResult(intent, CREATE_FILE_REQUEST_CODE);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CREATE_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null) {
            Uri uri = data.getData();

            if (uri != null) {
                String day = editTextDay.getText().toString().trim();
                String month = editTextMonth.getText().toString().trim();
                String year = editTextYear.getText().toString().trim();

                String dateSummary = !day.isEmpty() ? "Day: " + day : (!month.isEmpty() ? "Month: " + month : "Year: " + year);

                viewModel.getTransactions().observe(getViewLifecycleOwner(), transactions -> {
                    List<Transaction> filteredTransactions = new ArrayList<>();

                    if (transactions != null) {
                        for (Transaction transaction : transactions) {
                            String[] dateParts = transaction.getDate().split("/");
                            String transDay = dateParts.length > 0 ? dateParts[0] : "";
                            String transMonth = dateParts.length > 1 ? dateParts[1] : "";
                            String transYear = dateParts.length > 2 ? dateParts[2] : "";

                            if ((day.isEmpty() || day.equals(transDay)) &&
                                    (month.isEmpty() || month.equals(transMonth)) &&
                                    (year.isEmpty() || year.equals(transYear))) {
                                filteredTransactions.add(transaction);
                            }
                        }

                        float totalIncome = 0f;
                        float totalExpenses = 0f;

                        for (Transaction transaction : filteredTransactions) {
                            if (transaction.getType().equals("Income")) {
                                totalIncome += transaction.getAmount();
                            } else if (transaction.getType().equals("Expense")) {
                                totalExpenses += transaction.getAmount();
                            }
                        }

                        float netIncome = totalIncome - totalExpenses;

                        PDFUtils.createPDF(getContext(), uri, dateSummary, totalIncome, totalExpenses, netIncome, filteredTransactions, barChart);
                    } else {
                        Toast.makeText(getContext(), "No transactions available!", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        tableLayoutResults.removeAllViews();
    }
}