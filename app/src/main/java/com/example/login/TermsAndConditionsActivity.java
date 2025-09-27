package com.example.login;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class TermsAndConditionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terms);

        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Terms & Conditions");
        }

        // Setup content
        TextView termsContent = findViewById(R.id.terms_content);
        termsContent.setText(getTermsText());
    }

    private String getTermsText() {
        return "TERMS AND CONDITIONS OF USE\n\n" +
                "Last updated: " + java.text.DateFormat.getDateInstance().format(new java.util.Date()) + "\n\n" +

                "1. ACCEPTANCE OF TERMS\n" +
                "By accessing and using this mobile application, you accept and agree to be bound by the terms and provision of this agreement.\n\n" +

                "2. DESCRIPTION OF SERVICE\n" +
                "Our service provides users with access to a rich collection of resources, including various communications tools, forums, shopping services, search services, and personalized content.\n\n" +

                "3. USER ACCOUNTS\n" +
                "• You must be at least 13 years old to create an account\n" +
                "• You are responsible for maintaining the confidentiality of your account\n" +
                "• You are responsible for all activities that occur under your account\n" +
                "• You must provide accurate and complete information when creating your account\n\n" +

                "4. PRIVACY POLICY\n" +
                "Your privacy is important to us. Our Privacy Policy explains how we collect, use, and protect your information when you use our service.\n\n" +

                "5. USER CONDUCT\n" +
                "You agree not to use the service to:\n" +
                "• Upload, post, or transmit any unlawful, harmful, threatening, abusive, or objectionable content\n" +
                "• Impersonate any person or entity\n" +
                "• Interfere with or disrupt the service or servers\n" +
                "• Attempt to gain unauthorized access to any portion of the service\n\n" +

                "6. INTELLECTUAL PROPERTY\n" +
                "The service and its original content, features, and functionality are owned by us and are protected by international copyright, trademark, patent, trade secret, and other intellectual property laws.\n\n" +

                "7. TERMINATION\n" +
                "We may terminate or suspend your account and bar access to the service immediately, without prior notice or liability, under our sole discretion, for any reason whatsoever and without limitation.\n\n" +

                "8. DISCLAIMER\n" +
                "The information on this service is provided on an 'as is' basis. To the fullest extent permitted by law, we exclude all representations, warranties, and conditions relating to our service.\n\n" +

                "9. LIMITATION OF LIABILITY\n" +
                "In no event shall we be liable for any indirect, incidental, special, consequential, or punitive damages, including without limitation, loss of profits, data, use, goodwill, or other intangible losses.\n\n" +

                "10. GOVERNING LAW\n" +
                "These terms shall be interpreted and governed by the laws of the jurisdiction in which we operate, without regard to its conflict of law provisions.\n\n" +

                "11. CHANGES TO TERMS\n" +
                "We reserve the right to modify or replace these terms at any time. If a revision is material, we will provide at least 30 days notice prior to any new terms taking effect.\n\n" +

                "12. CONTACT INFORMATION\n" +
                "If you have any questions about these Terms and Conditions, please contact us at:\n" +
                "Email: support@yourapp.com\n" +
                "Phone: +1 (555) 123-4567\n\n" +

                "By using our service, you acknowledge that you have read and understood these terms and agree to be bound by them.";
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}