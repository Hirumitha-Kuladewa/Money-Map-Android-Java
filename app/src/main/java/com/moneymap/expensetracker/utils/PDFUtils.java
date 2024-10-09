package com.moneymap.expensetracker.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.widget.Toast;
import com.github.mikephil.charting.charts.BarChart;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import com.moneymap.expensetracker.transaction.Transaction;

public class PDFUtils {
    @SuppressLint("DefaultLocale")
    public static void createPDF(Context context, Uri uri, String dateSummary, float totalIncome, float totalExpenses, float netIncome, List<Transaction> transactions, BarChart barChart) {
        try {
            PdfDocument pdfDocument = new PdfDocument();
            PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(800, 1200, 1).create();
            PdfDocument.Page page = pdfDocument.startPage(pageInfo);
            Canvas canvas = page.getCanvas();
            Paint paint = new Paint();

            paint.setTextSize(20);
            canvas.drawText("Summary of " + dateSummary, 50, 50, paint);

            paint.setTextSize(16);
            canvas.drawText(String.format("Total Income: %.2f", totalIncome), 50, 100, paint);
            canvas.drawText(String.format("Total Expenses: %.2f", totalExpenses), 50, 130, paint);
            canvas.drawText(String.format("Net Income: %.2f", netIncome), 50, 160, paint);

            paint.setTextSize(14);
            canvas.drawText("Type", 50, 200, paint);
            canvas.drawText("Category", 150, 200, paint);
            canvas.drawText("Amount", 250, 200, paint);
            canvas.drawText("Date", 350, 200, paint);

            int y = 230;
            for (Transaction transaction : transactions) {
                canvas.drawText(transaction.getType(), 50, y, paint);
                canvas.drawText(transaction.getCategory(), 150, y, paint);
                canvas.drawText(String.format("%.2f", transaction.getAmount()), 250, y, paint);
                canvas.drawText(transaction.getDate(), 350, y, paint);
                y += 30;
            }

            barChart.setDrawingCacheEnabled(true);
            Bitmap barChartBitmap = Bitmap.createBitmap(barChart.getDrawingCache());
            barChart.setDrawingCacheEnabled(false);

            canvas.drawBitmap(barChartBitmap, 50, y, paint);

            pdfDocument.finishPage(page);

            ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "w");
            FileOutputStream outputStream = new FileOutputStream(pfd.getFileDescriptor());
            pdfDocument.writeTo(outputStream);
            pdfDocument.close();
            outputStream.close();

            Toast.makeText(context, "PDF saved to " + uri.getPath(), Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}