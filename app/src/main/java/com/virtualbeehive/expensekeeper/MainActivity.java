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
import java.text.*;
import java.util.*;
import org.json.*;

public class MainActivity extends Activity {
    final int honey = Color.rgb(246, 184, 52);
    final int honey2 = Color.rgb(255, 205, 82);
    final int dark = Color.rgb(13, 11, 8);
    final int panel = Color.rgb(25, 22, 17);
    final int card = Color.rgb(34, 29, 22);
    final int line = Color.rgb(72, 60, 42);
    final int muted = Color.rgb(190, 182, 168);
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
            getWindow().setStatusBarColor(dark);
            getWindow().setNavigationBarColor(dark);
        }
        showDashboard();
    }

    int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }

    GradientDrawable bg(int color, int radius) {
        GradientDrawable g = new GradientDrawable();
        g.setColor(color);
        g.setCornerRadius(dp(radius));
        return g;
    }

    GradientDrawable strokeBg(int color, int radius, int strokeColor) {
        GradientDrawable g = bg(color, radius);
        g.setStroke(dp(1), strokeColor);
        return g;
    }

    TextView text(String s, int spSize, int color, int style) {
        TextView t = new TextView(this);
        t.setText(s);
        t.setTextSize(spSize);
        t.setTextColor(color);
        t.setTypeface(Typeface.DEFAULT, style);
        t.setLineSpacing(0, 1.08f);
        return t;
    }

    TextView pill(String s) {
        TextView t = text(s, 12, honey2, Typeface.BOLD);
        t.setPadding(dp(12), dp(7), dp(12), dp(7));
        t.setBackground(strokeBg(Color.rgb(42, 33, 18), 30, Color.rgb(107, 82, 31)));
        return t;
    }

    TextView button(String s) {
        TextView b = text(s, 15, Color.rgb(20,16,10), Typeface.BOLD);
        b.setGravity(Gravity.CENTER);
        b.setPadding(dp(14), dp(14), dp(14), dp(14));
        b.setBackground(bg(honey, 16));
        return b;
    }

    EditText field(String hint) {
        EditText e = new EditText(this);
        e.setHint(hint);
        e.setTextColor(Color.WHITE);
        e.setHintTextColor(Color.rgb(145, 137, 122));
        e.setTextSize(15);
        e.setPadding(dp(14), dp(10), dp(14), dp(10));
        e.setMinHeight(dp(54));
        e.setSingleLine(false);
        e.setBackground(strokeBg(Color.rgb(22, 19, 15), 14, line));
        return e;
    }

    void add(View parent, View child, int w, int h, int l, int t, int r, int b) {
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(w, h);
        lp.setMargins(dp(l), dp(t), dp(r), dp(b));
        ((ViewGroup)parent).addView(child, lp);
    }

    void base(String screen) {
        activeTab = screen;
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(dark);
        root.setPadding(dp(16), dp(10), dp(16), 0);

        LinearLayout headerCard = new LinearLayout(this);
        headerCard.setOrientation(LinearLayout.VERTICAL);
        headerCard.setPadding(dp(16), dp(14), dp(16), dp(14));
        headerCard.setBackground(strokeBg(panel, 20, Color.rgb(62, 51, 35)));

        LinearLayout top = new LinearLayout(this);
        top.setGravity(Gravity.CENTER_VERTICAL);
        ImageView logo = new ImageView(this);
        logo.setImageResource(getResources().getIdentifier("vb_logo", "drawable", getPackageName()));
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        add(top, logo, dp(54), dp(54), 0, 0, 12, 0);

        LinearLayout titles = new LinearLayout(this);
        titles.setOrientation(LinearLayout.VERTICAL);
        titles.addView(text("VB Expense Keeper", 24, honey, Typeface.BOLD));
        TextView sub = text("Private bookkeeping for Virtual Beehive Inc.", 13, muted, Typeface.NORMAL);
        titles.addView(sub);
        top.addView(titles, new LinearLayout.LayoutParams(0, -2, 1));
        headerCard.addView(top);

        LinearLayout chips = new LinearLayout(this);
        chips.setOrientation(LinearLayout.HORIZONTAL);
        add(chips, pill("No delete audit trail"), -2, -2, 0, 12, 8, 0);
        add(chips, pill("Local v1"), -2, -2, 0, 12, 0, 0);
        headerCard.addView(chips);
        add(root, headerCard, -1, -2, 0, 6, 0, 12);

        TextView screenTitle = text(screen, 28, Color.WHITE, Typeface.BOLD);
        add(root, screenTitle, -1, -2, 2, 0, 0, 8);

        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(false);
        body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(0, 0, 0, dp(12));
        scroll.addView(body);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, 0, 1));

        root.addView(bottomNav(), new LinearLayout.LayoutParams(-1, dp(76)));
        setContentView(root);
    }

    LinearLayout bottomNav() {
        LinearLayout nav = new LinearLayout(this);
        nav.setGravity(Gravity.CENTER);
        nav.setPadding(dp(4), dp(8), dp(4), dp(8));
        nav.setBackground(strokeBg(Color.rgb(19, 16, 12), 22, Color.rgb(55, 45, 31)));
        String[] names = {"Dashboard", "Add", "Records", "Profile"};
        String[] labels = {"⌂\nHome", "+\nAdd", "≡\nRecords", "●\nProfile"};
        for (int i=0; i<names.length; i++) {
            final String name = names[i];
            TextView tab = text(labels[i], 11, name.equals(activeTab) ? Color.rgb(20,16,10) : muted, Typeface.BOLD);
            tab.setGravity(Gravity.CENTER);
            tab.setPadding(dp(4), dp(7), dp(4), dp(7));
            tab.setBackground(name.equals(activeTab) ? bg(honey, 16) : bg(Color.TRANSPARENT, 16));
            tab.setOnClickListener(v -> {
                if (name.equals("Dashboard")) showDashboard();
                else if (name.equals("Add")) showAdd();
                else if (name.equals("Records")) showRecords();
                else showProfile();
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, -1, 1);
            lp.setMargins(dp(3), 0, dp(3), 0);
            nav.addView(tab, lp);
        }
        return nav;
    }

    LinearLayout cardView(String title) {
        LinearLayout c = new LinearLayout(this);
        c.setOrientation(LinearLayout.VERTICAL);
        c.setPadding(dp(18), dp(16), dp(18), dp(16));
        c.setBackground(strokeBg(card, 20, Color.rgb(59, 48, 34)));
        TextView h = text(title, 18, honey, Typeface.BOLD);
        c.addView(h);
        add(body, c, -1, -2, 0, 8, 0, 10);
        return c;
    }

    void smallCard(String title, String value) {
        LinearLayout c = cardView(title);
        TextView v = text(value, 17, Color.WHITE, Typeface.NORMAL);
        v.setPadding(0, dp(8), 0, 0);
        c.addView(v);
    }

    JSONArray getRecords() {
        try { return new JSONArray(sp.getString("records", "[]")); }
        catch(Exception e) { return new JSONArray(); }
    }

    public void showDashboard() {
        base("Dashboard");
        JSONArray records = getRecords();
        LinearLayout summary = cardView("Expense Summary");
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(10), 0, 0);
        TextView count = text(records.length()+"", 34, Color.WHITE, Typeface.BOLD);
        TextView desc = text(" recorded expense(s)\nstored on this phone", 14, muted, Typeface.NORMAL);
        row.addView(count);
        row.addView(desc);
        summary.addView(row);

        LinearLayout quick = cardView("Quick Actions");
        TextView addBtn = button("+ Record New Expense");
        addBtn.setOnClickListener(v -> showAdd());
        add(quick, addBtn, -1, -2, 0, 12, 0, 0);
        TextView viewBtn = text("View saved records  →", 15, honey2, Typeface.BOLD);
        viewBtn.setGravity(Gravity.CENTER);
        viewBtn.setPadding(0, dp(14), 0, dp(2));
        viewBtn.setOnClickListener(v -> showRecords());
        quick.addView(viewBtn);

        LinearLayout rules = cardView("Bookkeeping Rules");
        TextView copy = text("Expenses cannot be deleted from the record list. If a mistake happens, add a correction note later. Google Drive/Sheets connection will be added after APK testing.", 15, Color.WHITE, Typeface.NORMAL);
        copy.setPadding(0, dp(8), 0, 0);
        rules.addView(copy);
    }

    public void showProfile() {
        base("Profile");
        LinearLayout c = cardView("User Profile");
        TextView avatar = text("Add Profile Photo", 16, honey, Typeface.BOLD);
        avatar.setGravity(Gravity.CENTER);
        avatar.setPadding(0, dp(22), 0, dp(22));
        avatar.setBackground(strokeBg(Color.rgb(22, 18, 14), 24, honey));
        avatar.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(i, 55);
        });
        add(c, avatar, -1, -2, 0, 12, 0, 12);

        EditText name = field("Full name"); name.setText(sp.getString("name", "Daniel Pirooz"));
        EditText pos = field("Position"); pos.setText(sp.getString("position", "CEO / Founder"));
        EditText email = field("Email"); email.setText(sp.getString("email", "danny@virtualbeehiveinc.com"));
        add(c, name, -1, -2, 0, 4, 0, 10);
        add(c, pos, -1, -2, 0, 0, 0, 10);
        add(c, email, -1, -2, 0, 0, 0, 14);
        TextView save = button("Save Profile");
        save.setOnClickListener(v -> {
            sp.edit().putString("name", name.getText().toString()).putString("position", pos.getText().toString()).putString("email", email.getText().toString()).apply();
            Toast.makeText(this, "Profile saved", Toast.LENGTH_LONG).show();
            showDashboard();
        });
        c.addView(save);
    }

    public void showAdd() {
        base("Add Expense");
        LinearLayout c = cardView("New Expense");
        c.addView(text("Only a few fields are required. The profile and timestamp are added automatically.", 14, muted, Typeface.NORMAL));

        TextView label1 = text("Category", 13, honey, Typeface.BOLD);
        add(c, label1, -1, -2, 0, 14, 0, 4);
        Spinner cat = new Spinner(this);
        cat.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, categories));
        cat.setBackground(strokeBg(Color.rgb(22, 19, 15), 14, line));
        add(c, cat, -1, dp(54), 0, 0, 0, 10);

        TextView label2 = text("Frequency", 13, honey, Typeface.BOLD);
        add(c, label2, -1, -2, 0, 0, 0, 4);
        Spinner freq = new Spinner(this);
        freq.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"One-time", "Monthly recurring", "Yearly recurring"}));
        freq.setBackground(strokeBg(Color.rgb(22, 19, 15), 14, line));
        add(c, freq, -1, dp(54), 0, 0, 0, 10);

        EditText amount = field("Amount, e.g. 49.99");
        EditText title = field("Expense title");
        EditText desc = field("Description / business purpose");
        EditText vendor = field("Vendor / store");
        add(c, amount, -1, -2, 0, 0, 0, 10);
        add(c, title, -1, -2, 0, 0, 0, 10);
        add(c, desc, -1, dp(88), 0, 0, 0, 10);
        add(c, vendor, -1, -2, 0, 0, 0, 12);

        LinearLayout actions = new LinearLayout(this);
        actions.setOrientation(LinearLayout.HORIZONTAL);
        TextView receipt = button("Attach Receipt");
        receipt.setTextSize(13);
        receipt.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.setType("image/*");
            startActivityForResult(Intent.createChooser(i, "Select receipt"), 77);
        });
        TextView scan = button("AI Scan Demo");
        scan.setTextSize(13);
        scan.setOnClickListener(v -> {
            vendor.setText("Detected Vendor"); amount.setText("49.99"); title.setText("AI tool subscription");
            desc.setText("Business AI productivity tool for Virtual Beehive Inc."); cat.setSelection(1);
        });
        LinearLayout.LayoutParams half = new LinearLayout.LayoutParams(0, -2, 1);
        half.setMargins(0,0,dp(6),0); actions.addView(receipt, half);
        LinearLayout.LayoutParams half2 = new LinearLayout.LayoutParams(0, -2, 1);
        half2.setMargins(dp(6),0,0,0); actions.addView(scan, half2);
        add(c, actions, -1, -2, 0, 0, 0, 14);

        TextView save = button("Record Expense Securely");
        save.setOnClickListener(v -> {
            if (amount.getText().toString().trim().isEmpty() || title.getText().toString().trim().isEmpty()) {
                Toast.makeText(this, "Please enter amount and title.", Toast.LENGTH_LONG).show();
                return;
            }
            String year = new SimpleDateFormat("yyyy", Locale.US).format(new Date());
            String ref = "VB-" + year + "-" + String.format(Locale.US, "%06d", getRecords().length()+1);
            JSONObject o = new JSONObject();
            try {
                o.put("ref", ref); o.put("date", new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date()));
                o.put("by", sp.getString("name", "Daniel Pirooz")); o.put("position", sp.getString("position", "CEO / Founder"));
                o.put("category", cat.getSelectedItem().toString()); o.put("frequency", freq.getSelectedItem().toString());
                o.put("amount", amount.getText().toString()); o.put("title", title.getText().toString());
                o.put("description", desc.getText().toString()); o.put("vendor", vendor.getText().toString());
            } catch(Exception ignored) {}
            JSONArray arr = getRecords(); arr.put(o);
            sp.edit().putString("records", arr.toString()).apply();
            new AlertDialog.Builder(this)
                    .setTitle("Expense recorded")
                    .setMessage("Reference Number:\n" + ref + "\n\nThis record is locked in the local audit list.")
                    .setPositiveButton("View Dashboard", (d,w) -> showDashboard())
                    .setNegativeButton("Add Another", (d,w) -> showAdd())
                    .show();
        });
        c.addView(save);
    }

    public void showRecords() {
        base("Records");
        JSONArray arr = getRecords();
        if (arr.length() == 0) {
            LinearLayout empty = cardView("No Records Yet");
            empty.addView(text("Add your first Virtual Beehive Inc. expense. Saved expenses will appear here with locked reference numbers.", 15, Color.WHITE, Typeface.NORMAL));
            return;
        }
        for (int i = arr.length()-1; i >= 0; i--) {
            try {
                JSONObject o = arr.getJSONObject(i);
                LinearLayout c = cardView(o.optString("ref"));
                TextView amount = text("$" + o.optString("amount") + "  •  " + o.optString("title"), 18, Color.WHITE, Typeface.BOLD);
                amount.setPadding(0, dp(8), 0, dp(5)); c.addView(amount);
                c.addView(text(o.optString("category") + "\n" + o.optString("frequency") + "\n" + o.optString("date") + "\nEntered by: " + o.optString("by"), 14, muted, Typeface.NORMAL));
            } catch(Exception ignored) {}
        }
    }
}
