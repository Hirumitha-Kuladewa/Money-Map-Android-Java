package com.moneymap.expensetracker.ui.settings;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

import com.moneymap.expensetracker.R;

public class ContactUsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_us);
    }

    public void contactViaWhatsApp(View view) {
        String url = "https://wa.me/+94788421355";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    public void contactViaInstagram(View view) {
        String url = "https://www.instagram.com/mr_who593?igsh=MTZ0Ym13b3JtZmw5Mg==";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    public void contactViaFacebook(View view) {
        String url = "https://www.facebook.com/share/T4UC4Zrm2oKBoenm/?mibextid=qi2Omg";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }

    public void contactViaTelegram(View view) {
        String url = "https://t.me/mr_who593";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }
}