package com.moneymap.expensetracker.ui.home;

import static java.lang.String.format;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.moneymap.expensetracker.ui.settings.SettingsActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.widget.ViewPager2;
import java.util.ArrayList;
import java.util.List;

import com.moneymap.expensetracker.R;
import com.moneymap.expensetracker.imageadapter.ImageCarouselAdapter;
import com.moneymap.expensetracker.transaction.Transaction;
import com.moneymap.expensetracker.transaction.TransactionViewModel;

public class HomeFragment extends Fragment {

    private TableLayout tableLayout;
    private BarChart barChart;
    private ImageView profileImageView;
    private ViewPager2 viewPager;
    private Handler handler;
    private Runnable runnable;
    private int currentPage = 0;
    private SQLiteDatabase database;
    private TransactionViewModel viewModel;

    private static final String DATABASE_NAME = "user_db";
    private static final String TABLE_NAME = "user_profile";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_PROFILE_PICTURE = "profile_picture";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        tableLayout = view.findViewById(R.id.table_layout);
        barChart = view.findViewById(R.id.bar_graph);
        profileImageView = view.findViewById(R.id.home_profile);
        viewPager = view.findViewById(R.id.carousel_viewpager);

        try {
            database = getContext().openOrCreateDatabase(DATABASE_NAME, Context.MODE_PRIVATE, null);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Failed to open database", Toast.LENGTH_SHORT).show();
            return view;
        }

        viewModel = new ViewModelProvider(requireActivity()).get(TransactionViewModel.class);

        viewModel.getTransactions().observe(getViewLifecycleOwner(), transactions -> {
            populateTable(transactions);
            updateBarChart(transactions);
        });

        setupImageCarousel();
        startAutoSlide();
        loadUserProfile();

        profileImageView.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void setupImageCarousel() {
        int[] images = {
                R.drawable.carousal_image1,
                R.drawable.carousal_image2,
                R.drawable.carousal_image3,
                R.drawable.carousal_image4,
                R.drawable.carousal_image5
        };

        ImageCarouselAdapter adapter = new ImageCarouselAdapter(getContext(), images);
        viewPager.setAdapter(adapter);
    }

    private void startAutoSlide() {
        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                if (currentPage == viewPager.getAdapter().getItemCount()) {
                    currentPage = 0;
                }
                viewPager.setCurrentItem(currentPage++, true);
                handler.postDelayed(this, 5000);
            }
        };
        handler.postDelayed(runnable, 5000);
    }

    private void loadUserProfile() {
        Cursor cursor = null;
        try {
            cursor = database.rawQuery("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " = 1", null);
            if (cursor.moveToFirst()) {
                @SuppressLint("Range") String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                @SuppressLint("Range") String profilePicturePath = cursor.getString(cursor.getColumnIndex(COLUMN_PROFILE_PICTURE));

                if (profilePicturePath != null && !profilePicturePath.isEmpty()) {
                    Uri profileImageUri = Uri.parse(profilePicturePath);
                    Glide.with(this)
                            .load(profileImageUri)
                            .transform(new CircleCrop())
                            .error(R.drawable.profile_placeholder)
                            .into(profileImageView);
                } else {
                    profileImageView.setImageResource(R.drawable.profile_placeholder);
                }
            }
        } catch (Exception ignored) {
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @SuppressLint({"SetTextI18n", "DefaultLocale"})
    private void populateTable(List<Transaction> transactions) {
        tableLayout.removeAllViews();

        addHeaderRow();

        int maxRows = 5;
        int rowCount = Math.min(transactions.size(), maxRows);

        for (int i = 0; i < rowCount; i++) {
            addDataRow(transactions.get(i));
        }

        for (int i = rowCount; i < maxRows; i++) {
            addEmptyRow();
        }
    }

    private void addHeaderRow() {
        TableRow headerRow = new TableRow(getContext());
        headerRow.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        TextView typeHeader = createHeaderTextView(getString(R.string.type));
        TextView categoryHeader = createHeaderTextView(getString(R.string.category));
        TextView amountHeader = createHeaderTextView(getString(R.string.amount));
        TextView dateHeader = createHeaderTextView(getString(R.string.date));
        TextView actionsHeader = createHeaderTextView(getString(R.string.actions));

        headerRow.addView(typeHeader);
        headerRow.addView(categoryHeader);
        headerRow.addView(amountHeader);
        headerRow.addView(dateHeader);
        headerRow.addView(actionsHeader);

        tableLayout.addView(headerRow);
    }

    @SuppressLint("UseCompatLoadingForColorStateLists")
    private void addDataRow(Transaction transaction) {
        TableRow row = new TableRow(getContext());
        row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        TextView typeView = createDataTextView(transaction.getType());
        TextView categoryView = createDataTextView(transaction.getCategory());
        TextView amountView = createDataTextView(format(getString(R.string.ff), transaction.getAmount()));
        TextView dateView = createDataTextView(transaction.getDate());
        Button deleteButton = new Button(getContext());

        deleteButton.setBackgroundResource(R.drawable.delete_button_background);

        deleteButton.setTextColor(getResources().getColorStateList(R.color.button_text_color));

        deleteButton.setText(R.string.delete);
        TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        params.height = getResources().getDimensionPixelSize(R.dimen.button_height);
        deleteButton.setLayoutParams(params);

        deleteButton.setOnClickListener(v -> showDeleteConfirmationDialog(transaction.getId(), row));

        row.addView(typeView);
        row.addView(categoryView);
        row.addView(amountView);
        row.addView(dateView);
        row.addView(deleteButton);

        tableLayout.addView(row);
    }

    private void addEmptyRow() {
        TableRow row = new TableRow(getContext());
        row.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

        TextView typeView = createDataTextView("");
        TextView categoryView = createDataTextView("");
        TextView amountView = createDataTextView("");
        TextView dateView = createDataTextView("");
        TextView deleteView = createDataTextView("");

        row.addView(typeView);
        row.addView(categoryView);
        row.addView(amountView);
        row.addView(dateView);
        row.addView(deleteView);

        tableLayout.addView(row);
    }

    private TextView createHeaderTextView(String text) {
        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setLayoutParams(createLayoutParams());
        textView.setPadding(8, 8, 8, 8);
        textView.setTextSize(16);
        textView.setBackgroundColor(Color.DKGRAY);
        textView.setTextColor(Color.WHITE);
        return textView;
    }

    private TextView createDataTextView(String text) {
        TextView textView = new TextView(getContext());
        textView.setText(text);
        textView.setLayoutParams(createLayoutParams());
        textView.setPadding(8, 8, 8, 8);
        textView.setBackgroundColor(Color.LTGRAY);
        return textView;
    }

    private TableRow.LayoutParams createLayoutParams() {
        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.WRAP_CONTENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(1, 2, 1, 2);
        return params;
    }

    private void showDeleteConfirmationDialog(int transactionId, TableRow row) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_delete_confirmation, null);

        new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    viewModel.deleteTransaction(transactionId);
                    tableLayout.removeView(row);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void updateBarChart(List<Transaction> transactions) {
        List<BarEntry> entries = new ArrayList<>();
        float totalIncome = 0;
        float totalExpense = 0;

        for (Transaction transaction : transactions) {
            if (transaction.getType().equalsIgnoreCase("Income")) {
                totalIncome += transaction.getAmount();
            } else if (transaction.getType().equalsIgnoreCase("Expense")) {
                totalExpense += transaction.getAmount();
            }
        }

        float netIncome = totalIncome - totalExpense;

        entries.add(new BarEntry(0, totalIncome));
        entries.add(new BarEntry(1, totalExpense));
        entries.add(new BarEntry(2, netIncome));

        BarDataSet dataSet = new BarDataSet(entries, "");

        int incomeColor = ContextCompat.getColor(requireContext(), R.color.natural_green);
        int expenseColor = ContextCompat.getColor(requireContext(), R.color.shine_red);
        int netIncomeColor = (netIncome >= 0) ?
                ContextCompat.getColor(requireContext(), R.color.dark_green) :
                ContextCompat.getColor(requireContext(), R.color.shine_red);

        dataSet.setColors(incomeColor, expenseColor, netIncomeColor);
        dataSet.setValueTextSize(16f);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);
        barChart.getDescription().setEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(new String[]{"Income", "Expense", "Net Income"}));
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);

        barChart.invalidate();
    }
}