package com.virtualbeehive.expensekeeper;

import android.app.*;
import android.os.*;
import android.content.*;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.content.res.ColorStateList;
import java.text.*;
import java.util.*;
import org.json.*;

public class MainActivity extends Activity {
    final int HONEY = Color.rgb(246, 184, 52);
    final int HONEY_LIGHT = Color.rgb(255, 212, 96);
    final int BG = Color.rgb(12, 10, 7);
    final int CARD = Color.rgb(29, 25, 19);
    final int CARD2 = Color.rgb(38, 32, 23);
    final int FIELD = Color.rgb(20, 18, 14);
    final int BORDER = Color.rgb(74, 60, 38);
    final int MUTED = Color.rgb(188, 178, 160);
    final int WHITE = Color.rgb(250, 248, 242);

    LinearLayout body;
    SharedPreferences sp;
    String activeTab = "Dashboard";

    String[] categories = {
            "Advertising & Marketing", "AI Tools Subscriptions", "Software & Subscriptions",
            "Website / App Development", "Office Supplies", "Computer / Equipment",
            "Phone & Internet", "Travel", "Business Meals", "Vehicle / Mileage",
            "Professional Fees", "Bank / Payment Processing Fees", "Rent / Office / Coworking",
            "Insurance", "Postage & Shipping", "Training / Education",
            "Licenses / Permits / Filing Fees", "Contract Labor", "Payroll / Wages", "Taxes",
            "Repairs & Maintenance", "Other Business Expense"
    };

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        sp = getSharedPreferences("vb_expense_keeper", 0);
        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setStatusBarColor(BG);
            getWindow().setNavigationBarColor(BG);
        }
        showDashboard();
    }

    int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }
    int statusBar() {
        int id = getResources().getIdentifier("status_bar_height", "dimen", "android");
        return id > 0 ? getResources().getDimensionPixelSize(id) : dp(24);
    }

    GradientDrawable round(int color, int radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radius));
        return g;
    }
    GradientDrawable outlined(int color, int radius, int stroke) {
        GradientDrawable g = round(color, radius);
        g.setStroke(dp(1), stroke);
        return g;
    }

    TextView tv(String s, int size, int color, int style) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(size);
        t.setTextColor(color);
        t.setTypeface(Typeface.DEFAULT, style);
        t.setIncludeFontPadding(true);
        t.setLineSpacing(dp(2), 1.0f);
        return t;
    }

    void add(LinearLayout parent, View child, int w, int h, int l, int t, int r, int b) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(w, h);
        lp.setMargins(dp(l), dp(t), dp(r), dp(b));
        parent.addView(child, lp);
    }

    TextView primaryButton(String s) {
        TextView b = tv(s, 15, Color.rgb(17, 13, 7), Typeface.BOLD);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(14), dp(13), dp(14), dp(13));
        b.setBackground(round(HONEY, 16));
        return b;
    }

    TextView secondaryButton(String s) {
        TextView b = tv(s, 14, HONEY_LIGHT, Typeface.BOLD);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(12), dp(12), dp(12), dp(12));
        b.setBackground(outlined(Color.rgb(26, 22, 16), 14, BORDER));
        return b;
    }

    TextView chip(String s) {
        TextView c = tv(s, 11, HONEY_LIGHT, Typeface.BOLD);
        c.setGravity(Gravity.CENTER);
        c.setPadding(dp(10), dp(5), dp(10), dp(5));
        c.setBackground(outlined(Color.rgb(42, 33, 18), 30, Color.rgb(110, 84, 33)));
        return c;
    }

    EditText input(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setTextSize(16);
        e.setTextColor(WHITE);
        e.setHintTextColor(Color.rgb(150, 141, 125));
        e.setPadding(dp(14), 0, dp(14), 0);
        e.setMinHeight(dp(52));
        e.setSingleLine(true);
        e.setBackground(outlined(FIELD, 14, BORDER));
        if (Build.VERSION.SDK_INT >= 21) e.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));
        return e;
    }

    Spinner spinner(String[] items) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items) {
            @Override public View getView(int position, View convertView, ViewGroup parent) {
                TextView v = (TextView) super.getView(position, convertView, parent);
                v.setTextColor(WHITE); v.setTextSize(16); v.setPadding(dp(14),0,dp(14),0);
                return v;
            }
            @Override public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView v = (TextView) super.getDropDownView(position, convertView, parent);
                v.setTextColor(Color.rgb(20,16,10)); v.setTextSize(16); v.setPadding(dp(16),dp(12),dp(16),dp(12));
                return v;
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner s = new Spinner(this);
        s.setAdapter(adapter);
        s.setPadding(0, 0, dp(8), 0);
        s.setBackground(outlined(FIELD, 14, BORDER));
        return s;
    }

    void label(LinearLayout parent, String s) { add(parent, tv(s, 13, HONEY_LIGHT, Typeface.BOLD), -1, -2, 2, 12, 0, 5); }

    void base(String screen) {
        activeTab = screen;
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);
        root.setPadding(dp(14), statusBar() + dp(8), dp(14), dp(8));

        LinearLayout header = new LinearLayout(this);
        header.setGravity(Gravity.CENTER_VERTICAL);
        header.setPadding(dp(12), dp(10), dp(12), dp(10));
        header.setBackground(outlined(Color.rgb(20, 17, 13), 20, Color.rgb(54, 43, 28)));
        ImageView logo = new ImageView(this);
        logo.setImageResource(getResources().getIdentifier("vb_logo", "drawable", getPackageName()));
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        add(header, logo, dp(38), dp(38), 0, 0, 10, 0);
        LinearLayout titleBox = new LinearLayout(this);
        titleBox.setOrientation(LinearLayout.VERTICAL);
        titleBox.addView(tv("VB Expense Keeper", 21, HONEY, Typeface.BOLD));
        titleBox.addView(tv("Virtual Beehive Inc. private bookkeeping", 12, MUTED, Typeface.NORMAL));
        header.addView(titleBox, new LinearLayout.LayoutParams(0, -2, 1));
        add(root, header, -1, -2, 0, 0, 0, 12);

        TextView screenTitle = tv(screen, 30, WHITE, Typeface.BOLD);
        add(root, screenTitle, -1, -2, 2, 0, 0, 8);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(0, 0, 0, dp(12));
        scroll.addView(body, new ScrollView.LayoutParams(-1, -2));
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        add(root, bottomNav(), -1, dp(70), 0, 6, 0, 0);
        setContentView(root, new ViewGroup.LayoutParams(-1, -1));
    }

    LinearLayout bottomNav() {
        LinearLayout nav = new LinearLayout(this);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(6), dp(6), dp(6), dp(6));
        nav.setBackground(outlined(Color.rgb(21, 18, 13), 22, Color.rgb(58, 47, 31)));
        String[] screens = {"Dashboard", "Add Expense", "Records", "Profile"};
        String[] labels = {"Home", "Add", "Records", "Profile"};
        for (int i=0; i<screens.length; i++) {
            String scr = screens[i];
            TextView tab = tv(labels[i], 12, scr.equals(activeTab) ? Color.rgb(18,13,6) : MUTED, Typeface.BOLD);
            tab.setGravity(Gravity.CENTER);
            tab.setBackground(scr.equals(activeTab) ? round(HONEY, 16) : round(Color.TRANSPARENT, 16));
            tab.setOnClickListener(v -> {
                if (scr.equals("Dashboard")) showDashboard();
                else if (scr.equals("Add Expense")) showAdd();
                else if (scr.equals("Records")) showRecords();
                else showProfile();
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -1, 1);
            lp.setMargins(dp(3), 0, dp(3), 0);
            nav.addView(tab, lp);
        }
        return nav;
    }

    LinearLayout card(String title) {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(16), dp(15), dp(16), dp(15));
        c.setBackground(outlined(CARD, 20, Color.rgb(60, 48, 32)));
        c.addView(tv(title, 18, HONEY, Typeface.BOLD));
        add(body, c, -1, -2, 0, 6, 0, 10);
        return c;
    }

    JSONArray getRecords() {
        try { return new JSONArray(sp.getString("records", "[]")); }
        catch(Exception e) { return new JSONArray(); }
    }

    public void showDashboard() {
        base("Dashboard");
        JSONArray records = getRecords();

        LinearLayout hero = card("This Year");
        TextView big = tv(records.length() + " expense record" + (records.length()==1 ? "" : "s"), 24, WHITE, Typeface.BOLD);
        big.setPadding(0, dp(8), 0, dp(4)); hero.addView(big);
        hero.addView(tv("Stored locally on this phone. Google Drive/Sheets is the next connection step.", 14, MUTED, Typeface.NORMAL));

        LinearLayout quick = card("Quick Actions");
        TextView addExpense = primaryButton("+ Add New Expense");
        addExpense.setOnClickListener(v -> showAdd());
        add(quick, addExpense, -1, -2, 0, 12, 0, 8);
        TextView viewRecords = secondaryButton("View Records");
        viewRecords.setOnClickListener(v -> showRecords());
        add(quick, viewRecords, -1, -2, 0, 0, 0, 0);

        LinearLayout rules = card("Bookkeeping Rule");
        rules.addView(tv("No user can delete a recorded expense. If a mistake happens, create a correction note later so the audit trail stays clean.", 15, WHITE, Typeface.NORMAL));
        LinearLayout chips = new LinearLayout(this); chips.setOrientation(LinearLayout.HORIZONTAL);
        add(chips, chip("No Delete"), -2, -2, 0, 12, 8, 0);
        add(chips, chip("Reference #"), -2, -2, 0, 12, 8, 0);
        add(chips, chip("CPA Ready"), -2, -2, 0, 12, 0, 0);
        rules.addView(chips);
    }

    public void showAdd() {
        base("Add Expense");
        LinearLayout c = card("New Expense");
        c.addView(tv("Enter only the key details. Your profile, timestamp, and reference number are added automatically.", 14, MUTED, Typeface.NORMAL));

        label(c, "Category"); Spinner cat = spinner(categories); add(c, cat, -1, dp(52), 0, 0, 0, 2);
        label(c, "Frequency"); Spinner freq = spinner(new String[]{"One-time", "Monthly recurring", "Yearly recurring"}); add(c, freq, -1, dp(52), 0, 0, 0, 2);

        label(c, "Amount"); EditText amount = input("49.99"); add(c, amount, -1, -2, 0, 0, 0, 2);
        label(c, "Title"); EditText title = input("Expense title"); add(c, title, -1, -2, 0, 0, 0, 2);
        label(c, "Description / Business Purpose"); EditText desc = input("Why this was for business"); desc.setSingleLine(false); desc.setMinLines(2); add(c, desc, -1, dp(76), 0, 0, 0, 2);
        label(c, "Vendor / Store"); EditText vendor = input("Vendor name"); add(c, vendor, -1, -2, 0, 0, 0, 12);

        TextView attach = secondaryButton("Take Photo or Attach Receipt");
        attach.setOnClickListener(v -> { Intent i = new Intent(Intent.ACTION_GET_CONTENT); i.setType("image/*"); startActivityForResult(Intent.createChooser(i, "Select receipt"), 77); });
        add(c, attach, -1, -2, 0, 2, 0, 8);

        TextView scan = secondaryButton("Simulate AI Scan");
        scan.setOnClickListener(v -> { vendor.setText("Detected Vendor"); amount.setText("49.99"); title.setText("AI tool subscription"); desc.setText("Business AI productivity tool for Virtual Beehive Inc."); cat.setSelection(1); });
        add(c, scan, -1, -2, 0, 0, 0, 12);

        TextView save = primaryButton("Record Expense");
        save.setOnClickListener(v -> saveExpense(cat, freq, amount, title, desc, vendor));
        add(c, save, -1, -2, 0, 0, 0, 0);
    }

    void saveExpense(Spinner cat, Spinner freq, EditText amount, EditText title, EditText desc, EditText vendor) {
        if (amount.getText().toString().trim().isEmpty() || title.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please enter amount and title.", Toast.LENGTH_LONG).show(); return;
        }
        String year = new SimpleDateFormat("yyyy", Locale.US).format(new Date());
        String ref = "VB-" + year + "-" + String.format(Locale.US, "%06d", getRecords().length()+1);
        JSONObject o = new JSONObject();
        try {
            o.put("ref", ref);
            o.put("date", new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date()));
            o.put("by", sp.getString("name", "Daniel Pirooz"));
            o.put("position", sp.getString("position", "CEO / Founder"));
            o.put("category", cat.getSelectedItem().toString());
            o.put("frequency", freq.getSelectedItem().toString());
            o.put("amount", amount.getText().toString());
            o.put("title", title.getText().toString());
            o.put("description", desc.getText().toString());
            o.put("vendor", vendor.getText().toString());
        } catch(Exception ignored) {}
        JSONArray arr = getRecords(); arr.put(o);
        sp.edit().putString("records", arr.toString()).apply();
        new AlertDialog.Builder(this)
                .setTitle("Expense recorded")
                .setMessage("Reference Number:\n" + ref + "\n\nThis record is locked in the local audit list.")
                .setPositiveButton("Dashboard", (d,w) -> showDashboard())
                .setNegativeButton("Add Another", (d,w) -> showAdd())
                .show();
    }

    public void showRecords() {
        base("Records");
        JSONArray arr = getRecords();
        if (arr.length()==0) {
            LinearLayout e = card("No Records Yet");
            e.addView(tv("Add an expense and it will appear here with a locked Virtual Beehive reference number.", 15, WHITE, Typeface.NORMAL));
            return;
        }
        for (int i=arr.length()-1; i>=0; i--) {
            try {
                JSONObject o = arr.getJSONObject(i);
                LinearLayout c = card(o.optString("ref"));
                TextView main = tv("$" + o.optString("amount") + "  •  " + o.optString("title"), 18, WHITE, Typeface.BOLD);
                main.setPadding(0, dp(8), 0, dp(6)); c.addView(main);
                c.addView(tv(o.optString("category") + "\n" + o.optString("frequency") + "\n" + o.optString("date") + "\nEntered by: " + o.optString("by"), 14, MUTED, Typeface.NORMAL));
            } catch(Exception ignored) {}
        }
    }

    public void showProfile() {
        base("Profile");
        LinearLayout c = card("User Profile");
        TextView avatar = tv("+ Add Profile Picture", 16, HONEY_LIGHT, Typeface.BOLD);
        avatar.setGravity(Gravity.CENTER); avatar.setPadding(0, dp(22), 0, dp(22));
        avatar.setBackground(outlined(Color.rgb(22, 18, 14), 24, HONEY));
        avatar.setOnClickListener(v -> { Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI); startActivityForResult(i, 55); });
        add(c, avatar, -1, -2, 0, 10, 0, 12);
        label(c, "Full Name"); EditText name = input("Full name"); name.setText(sp.getString("name", "Daniel Pirooz")); add(c, name, -1, -2, 0, 0, 0, 2);
        label(c, "Position"); EditText pos = input("Position"); pos.setText(sp.getString("position", "CEO / Founder")); add(c, pos, -1, -2, 0, 0, 0, 2);
        label(c, "Email"); EditText email = input("Email"); email.setText(sp.getString("email", "danny@virtualbeehiveinc.com")); add(c, email, -1, -2, 0, 0, 0, 12);
        TextView save = primaryButton("Save Profile");
        save.setOnClickListener(v -> { sp.edit().putString("name", name.getText().toString()).putString("position", pos.getText().toString()).putString("email", email.getText().toString()).apply(); Toast.makeText(this,"Profile saved",Toast.LENGTH_LONG).show(); showDashboard(); });
        add(c, save, -1, -2, 0, 0, 0, 0);
    }
}
