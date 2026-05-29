package com.virtualbeehive.expensekeeper;

import android.Manifest;
import android.app.*;
import android.os.*;
import android.content.*;
import android.content.pm.PackageManager;
import android.provider.MediaStore;
import android.net.Uri;
import android.view.*;
import android.widget.*;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.content.res.ColorStateList;
import androidx.core.content.FileProvider;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.api.Scope;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.*;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.*;
import java.net.*;
import java.text.*;
import java.util.*;
import java.util.regex.*;
import org.json.*;

public class MainActivity extends Activity {
    final int HONEY = Color.rgb(246, 184, 52), HONEY_LIGHT = Color.rgb(255, 212, 96);
    final int BG = Color.rgb(12, 10, 7), CARD = Color.rgb(29, 25, 19), FIELD = Color.rgb(20, 18, 14), BORDER = Color.rgb(74, 60, 38);
    final int MUTED = Color.rgb(188, 178, 160), WHITE = Color.rgb(250, 248, 242);
    final String WEB_CLIENT_ID = "418570478857-n8088u5v7nsa45rffu2cpbnlg8o89rmp.apps.googleusercontent.com";
    final String DRIVE_FOLDER_ID = "1nQHsHI4KKbZAuMMvbE4z53E2O23fOigr";
    final String SCOPE_DRIVE = "https://www.googleapis.com/auth/drive.file";
    final String SCOPE_SHEETS = "https://www.googleapis.com/auth/spreadsheets";

    LinearLayout body;
    SharedPreferences sp;
    String activeTab = "Dashboard";
    Uri pendingCameraUri;
    Uri currentReceiptUri;
    GoogleSignInClient signInClient;

    String[] categories = {"Advertising & Marketing", "AI Tools Subscriptions", "Software & Subscriptions", "Website / App Development", "Office Supplies", "Computer / Equipment", "Phone & Internet", "Travel", "Business Meals", "Vehicle / Mileage", "Professional Fees", "Bank / Payment Processing Fees", "Rent / Office / Coworking", "Insurance", "Postage & Shipping", "Training / Education", "Licenses / Permits / Filing Fees", "Contract Labor", "Payroll / Wages", "Taxes", "Repairs & Maintenance", "Other Business Expense"};

    EditText addAmount, addTitle, addDesc, addVendor, addDate;
    Spinner addCat;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        sp = getSharedPreferences("vb_expense_keeper", 0);
        if (Build.VERSION.SDK_INT >= 21) { getWindow().setStatusBarColor(BG); getWindow().setNavigationBarColor(BG); }
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(new Scope(SCOPE_DRIVE), new Scope(SCOPE_SHEETS))
                .requestIdToken(WEB_CLIENT_ID)
                .build();
        signInClient = GoogleSignIn.getClient(this, gso);
        showDashboard();
    }

    int dp(int v){return (int)(v*getResources().getDisplayMetrics().density+0.5f);}    
    int statusBar(){int id=getResources().getIdentifier("status_bar_height","dimen","android");return id>0?getResources().getDimensionPixelSize(id):dp(24);}    
    GradientDrawable round(int color,int radius){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(radius));return g;}
    GradientDrawable outlined(int color,int radius,int stroke){GradientDrawable g=round(color,radius);g.setStroke(dp(1),stroke);return g;}
    TextView tv(String s,int size,int color,int style){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setTypeface(Typeface.DEFAULT,style);t.setLineSpacing(dp(2),1);return t;}
    void add(LinearLayout p,View c,int w,int h,int l,int t,int r,int b){LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(w,h);lp.setMargins(dp(l),dp(t),dp(r),dp(b));p.addView(c,lp);}    
    TextView primaryButton(String s){TextView b=tv(s,15,Color.rgb(17,13,7),Typeface.BOLD);b.setGravity(Gravity.CENTER);b.setPadding(dp(14),dp(13),dp(14),dp(13));b.setBackground(round(HONEY,16));return b;}
    TextView secondaryButton(String s){TextView b=tv(s,14,HONEY_LIGHT,Typeface.BOLD);b.setGravity(Gravity.CENTER);b.setPadding(dp(12),dp(12),dp(12),dp(12));b.setBackground(outlined(Color.rgb(26,22,16),14,BORDER));return b;}
    EditText input(String hint){EditText e=new EditText(this);e.setHint(hint);e.setTextSize(16);e.setTextColor(WHITE);e.setHintTextColor(Color.rgb(150,141,125));e.setPadding(dp(14),0,dp(14),0);e.setMinHeight(dp(52));e.setSingleLine(true);e.setBackground(outlined(FIELD,14,BORDER));if(Build.VERSION.SDK_INT>=21)e.setBackgroundTintList(ColorStateList.valueOf(Color.TRANSPARENT));return e;}
    Spinner spinner(String[] items){ArrayAdapter<String> a=new ArrayAdapter<String>(this,android.R.layout.simple_spinner_item,items){@Override public View getView(int p,View v,ViewGroup par){TextView x=(TextView)super.getView(p,v,par);x.setTextColor(WHITE);x.setTextSize(16);x.setPadding(dp(14),0,dp(14),0);return x;}@Override public View getDropDownView(int p,View v,ViewGroup par){TextView x=(TextView)super.getDropDownView(p,v,par);x.setTextColor(Color.rgb(20,16,10));x.setTextSize(16);x.setPadding(dp(16),dp(12),dp(16),dp(12));return x;}};a.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);Spinner s=new Spinner(this);s.setAdapter(a);s.setBackground(outlined(FIELD,14,BORDER));return s;}
    void label(LinearLayout p,String s){add(p,tv(s,13,HONEY_LIGHT,Typeface.BOLD),-1,-2,2,12,0,5);}    

    void base(String screen){activeTab=screen;LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);root.setBackgroundColor(BG);root.setPadding(dp(14),statusBar()+dp(8),dp(14),dp(8));
        LinearLayout header=new LinearLayout(this);header.setGravity(Gravity.CENTER_VERTICAL);header.setPadding(dp(12),dp(10),dp(12),dp(10));header.setBackground(outlined(Color.rgb(20,17,13),20,Color.rgb(54,43,28)));
        ImageView logo=new ImageView(this);logo.setImageResource(getResources().getIdentifier("vb_logo","drawable",getPackageName()));logo.setScaleType(ImageView.ScaleType.FIT_CENTER);add(header,logo,dp(38),dp(38),0,0,10,0);
        LinearLayout titleBox=new LinearLayout(this);titleBox.setOrientation(LinearLayout.VERTICAL);titleBox.addView(tv("VB Expense Keeper",21,HONEY,Typeface.BOLD));titleBox.addView(tv("Virtual Beehive Inc. private bookkeeping",12,MUTED,Typeface.NORMAL));header.addView(titleBox,new LinearLayout.LayoutParams(0,-2,1));add(root,header,-1,-2,0,0,0,12);
        add(root,tv(screen,30,WHITE,Typeface.BOLD),-1,-2,2,0,0,8);
        ScrollView scroll=new ScrollView(this);scroll.setFillViewport(true);scroll.setBackgroundColor(BG);body=new LinearLayout(this);body.setOrientation(LinearLayout.VERTICAL);body.setPadding(0,0,0,dp(12));scroll.addView(body,new ScrollView.LayoutParams(-1,-2));root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));
        add(root,bottomNav(),-1,dp(70),0,6,0,0);setContentView(root,new ViewGroup.LayoutParams(-1,-1));}
    LinearLayout bottomNav(){LinearLayout nav=new LinearLayout(this);nav.setGravity(Gravity.CENTER);nav.setPadding(dp(6),dp(6),dp(6),dp(6));nav.setBackground(outlined(Color.rgb(21,18,13),22,Color.rgb(58,47,31)));String[] screens={"Dashboard","Add Expense","Records","Profile"};String[] labels={"Home","Add","Records","Profile"};for(int i=0;i<screens.length;i++){String scr=screens[i];TextView tab=tv(labels[i],12,scr.equals(activeTab)?Color.rgb(18,13,6):MUTED,Typeface.BOLD);tab.setGravity(Gravity.CENTER);tab.setBackground(scr.equals(activeTab)?round(HONEY,16):round(Color.TRANSPARENT,16));tab.setOnClickListener(v->{if(scr.equals("Dashboard"))showDashboard();else if(scr.equals("Add Expense"))showAdd();else if(scr.equals("Records"))showRecords();else showProfile();});LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(0,-1,1);lp.setMargins(dp(3),0,dp(3),0);nav.addView(tab,lp);}return nav;}
    LinearLayout card(String title){LinearLayout c=new LinearLayout(this);c.setOrientation(LinearLayout.VERTICAL);c.setPadding(dp(16),dp(15),dp(16),dp(15));c.setBackground(outlined(CARD,20,Color.rgb(60,48,32)));c.addView(tv(title,18,HONEY,Typeface.BOLD));add(body,c,-1,-2,0,6,0,10);return c;}
    JSONArray getRecords(){try{return new JSONArray(sp.getString("records","[]"));}catch(Exception e){return new JSONArray();}}

    public void showDashboard(){base("Dashboard");JSONArray records=getRecords();LinearLayout hero=card("This Year");hero.addView(tv(records.length()+" expense record"+(records.length()==1?"":"s"),24,WHITE,Typeface.BOLD));hero.addView(tv("Local records are saved on this phone. Google Sheets sync is now included in v4 after Google sign-in.",14,MUTED,Typeface.NORMAL));
        LinearLayout cloud=card("Google Drive / Sheets");String email=sp.getString("googleEmail","");cloud.addView(tv(email.isEmpty()?"Not connected":"Connected as: "+email,15,email.isEmpty()?HONEY_LIGHT:WHITE,Typeface.BOLD));TextView connect=secondaryButton(email.isEmpty()?"Connect Google Account":"Reconnect Google Account");connect.setOnClickListener(v->startActivityForResult(signInClient.getSignInIntent(),900));add(cloud,connect,-1,-2,0,12,0,0);
        LinearLayout quick=card("Quick Actions");TextView addExpense=primaryButton("+ Add New Expense");addExpense.setOnClickListener(v->showAdd());add(quick,addExpense,-1,-2,0,12,0,8);TextView view=secondaryButton("View Records");view.setOnClickListener(v->showRecords());add(quick,view,-1,-2,0,0,0,0);
        LinearLayout rules=card("Bookkeeping Rule");rules.addView(tv("No user can delete a recorded expense. If a mistake happens, create a correction note later so the audit trail stays clean.",15,WHITE,Typeface.NORMAL));}

    public void showAdd(){base("Add Expense");LinearLayout c=card("New Expense");c.addView(tv("Take or upload a receipt. The app will try to read date, amount, vendor and title. Review before saving.",14,MUTED,Typeface.NORMAL));
        label(c,"Expense Date");addDate=input("YYYY-MM-DD");addDate.setText(new SimpleDateFormat("yyyy-MM-dd",Locale.US).format(new Date()));add(c,addDate,-1,-2,0,0,0,2);
        label(c,"Category");addCat=spinner(categories);add(c,addCat,-1,dp(52),0,0,0,2);
        label(c,"Frequency");Spinner freq=spinner(new String[]{"One-time","Monthly recurring","Yearly recurring"});add(c,freq,-1,dp(52),0,0,0,2);
        label(c,"Amount");addAmount=input("49.99");add(c,addAmount,-1,-2,0,0,0,2);
        label(c,"Title");addTitle=input("Expense title");add(c,addTitle,-1,-2,0,0,0,2);
        label(c,"Description / Business Purpose");addDesc=input("Why this was for business");addDesc.setSingleLine(false);addDesc.setMinLines(2);add(c,addDesc,-1,dp(76),0,0,0,2);
        label(c,"Vendor / Store");addVendor=input("Vendor name");add(c,addVendor,-1,-2,0,0,0,12);
        TextView camera=secondaryButton("Take Receipt Photo");camera.setOnClickListener(v->takeReceiptPhoto());add(c,camera,-1,-2,0,2,0,8);
        TextView upload=secondaryButton("Upload Existing Receipt");upload.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_GET_CONTENT);i.setType("image/*");startActivityForResult(Intent.createChooser(i,"Select receipt"),77);});add(c,upload,-1,-2,0,0,0,8);
        TextView save=primaryButton("Record Expense + Sync Sheet");save.setOnClickListener(v->saveExpense(addCat,freq,addAmount,addTitle,addDesc,addVendor,addDate));add(c,save,-1,-2,0,6,0,0);
    }

    void takeReceiptPhoto(){try{if(Build.VERSION.SDK_INT>=23 && checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED){requestPermissions(new String[]{Manifest.permission.CAMERA},44);return;}File f=new File(getExternalCacheDir(),"receipt_"+System.currentTimeMillis()+".jpg");pendingCameraUri=FileProvider.getUriForFile(this,getPackageName()+".fileprovider",f);Intent i=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);i.putExtra(MediaStore.EXTRA_OUTPUT,pendingCameraUri);i.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION|Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivityForResult(i,78);}catch(Exception e){Toast.makeText(this,"Camera error: "+e.getMessage(),Toast.LENGTH_LONG).show();}}

    @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(result!=RESULT_OK)return;try{
            if(request==55 && data!=null){Uri u=data.getData();sp.edit().putString("profilePhoto",u.toString()).apply();Toast.makeText(this,"Profile picture saved",Toast.LENGTH_LONG).show();showProfile();}
            else if(request==77 && data!=null){currentReceiptUri=data.getData();scanReceipt(currentReceiptUri);}
            else if(request==78){currentReceiptUri=pendingCameraUri;scanReceipt(currentReceiptUri);}
            else if(request==900){GoogleSignInAccount acct=GoogleSignIn.getSignedInAccountFromIntent(data).getResult(Exception.class);sp.edit().putString("googleEmail",acct.getEmail()==null?"Connected":acct.getEmail()).apply();Toast.makeText(this,"Google connected",Toast.LENGTH_LONG).show();showDashboard();}
        }catch(Exception e){Toast.makeText(this,"Result error: "+e.getMessage(),Toast.LENGTH_LONG).show();}}

    void scanReceipt(Uri uri){Toast.makeText(this,"Reading receipt...",Toast.LENGTH_SHORT).show();try{InputImage image=InputImage.fromFilePath(this,uri);TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS).process(image).addOnSuccessListener(text->{String raw=text.getText();fillFromReceipt(raw);sp.edit().putString("lastOcr",raw).apply();Toast.makeText(this,"Receipt read. Please review fields.",Toast.LENGTH_LONG).show();}).addOnFailureListener(e->Toast.makeText(this,"Could not read receipt. Please enter manually.",Toast.LENGTH_LONG).show());}catch(Exception e){Toast.makeText(this,"Receipt scan error: "+e.getMessage(),Toast.LENGTH_LONG).show();}}
    void fillFromReceipt(String raw){String[] lines=raw.split("\\n");String vendor="";for(String l:lines){String x=l.trim();if(x.length()>2&&!x.matches(".*\\d{2,}.*")){vendor=x;break;}}if(!vendor.isEmpty())addVendor.setText(vendor);String amount=findAmount(raw);if(!amount.isEmpty())addAmount.setText(amount);String date=findDate(raw);if(!date.isEmpty())addDate.setText(date);if(addTitle.getText().toString().trim().isEmpty())addTitle.setText(vendor.isEmpty()?"Receipt expense":vendor+" receipt");if(addDesc.getText().toString().trim().isEmpty())addDesc.setText("Business expense recorded from receipt for Virtual Beehive Inc.");}
    String findAmount(String raw){Matcher m=Pattern.compile("(?i)(total|amount|balance)[^0-9$]{0,20}\\$?([0-9]+\\.[0-9]{2})").matcher(raw);String last="";while(m.find())last=m.group(2);if(!last.isEmpty())return last;m=Pattern.compile("\\$?([0-9]+\\.[0-9]{2})").matcher(raw);while(m.find())last=m.group(1);return last;}
    String findDate(String raw){Matcher m=Pattern.compile("(\\d{1,2})[/-](\\d{1,2})[/-](\\d{2,4})").matcher(raw);if(m.find()){int y=Integer.parseInt(m.group(3));if(y<100)y+=2000;return String.format(Locale.US,"%04d-%02d-%02d",y,Integer.parseInt(m.group(1)),Integer.parseInt(m.group(2)));}return "";}

    void saveExpense(Spinner cat,Spinner freq,EditText amount,EditText title,EditText desc,EditText vendor,EditText date){if(amount.getText().toString().trim().isEmpty()||title.getText().toString().trim().isEmpty()){Toast.makeText(this,"Please enter amount and title.",Toast.LENGTH_LONG).show();return;}String year=date.getText().toString().length()>=4?date.getText().toString().substring(0,4):new SimpleDateFormat("yyyy",Locale.US).format(new Date());String ref="VB-"+year+"-"+String.format(Locale.US,"%06d",getRecords().length()+1);JSONObject o=new JSONObject();try{o.put("ref",ref);o.put("entered",new SimpleDateFormat("yyyy-MM-dd HH:mm",Locale.US).format(new Date()));o.put("expenseDate",date.getText().toString());o.put("by",sp.getString("name","Daniel Pirooz"));o.put("position",sp.getString("position","CEO / Founder"));o.put("category",cat.getSelectedItem().toString());o.put("frequency",freq.getSelectedItem().toString());o.put("amount",amount.getText().toString());o.put("title",title.getText().toString());o.put("description",desc.getText().toString());o.put("vendor",vendor.getText().toString());o.put("receipt",currentReceiptUri==null?"":currentReceiptUri.toString());}catch(Exception ignored){}JSONArray arr=getRecords();arr.put(o);sp.edit().putString("records",arr.toString()).apply();syncRecord(o,year);new AlertDialog.Builder(this).setTitle("Expense recorded").setMessage("Reference Number:\n"+ref+"\n\nSaved locally. Google Sheets sync is attempted if Google is connected.").setPositiveButton("Dashboard",(d,w)->showDashboard()).setNegativeButton("Add Another",(d,w)->showAdd()).show();}

    void syncRecord(JSONObject o,String year){GoogleSignInAccount acct=GoogleSignIn.getLastSignedInAccount(this);if(acct==null){Toast.makeText(this,"Local saved. Connect Google on Dashboard to sync sheets.",Toast.LENGTH_LONG).show();return;}new Thread(()->{try{String token=GoogleAuthUtil.getToken(this,acct.getAccount(),"oauth2:"+SCOPE_DRIVE+" "+SCOPE_SHEETS);String sheetId=getOrCreateSheet(token,year);appendRow(token,sheetId,o);runOnUiThread(()->Toast.makeText(this,"Synced to VB expenses "+year,Toast.LENGTH_LONG).show());}catch(UserRecoverableAuthException e){runOnUiThread(()->startActivityForResult(e.getIntent(),901));}catch(Exception e){runOnUiThread(()->Toast.makeText(this,"Google sync failed: "+e.getMessage(),Toast.LENGTH_LONG).show());}}).start();}
    String getOrCreateSheet(String token,String year)throws Exception{String key="sheet_"+year;String id=sp.getString(key,"");if(!id.isEmpty())return id;JSONObject body=new JSONObject();body.put("name","VB expenses "+year);body.put("mimeType","application/vnd.google-apps.spreadsheet");body.put("parents",new JSONArray().put(DRIVE_FOLDER_ID));JSONObject res=new JSONObject(http("POST","https://www.googleapis.com/drive/v3/files",token,body.toString()));id=res.getString("id");sp.edit().putString(key,id).apply();JSONArray headers=new JSONArray().put(new JSONArray(Arrays.asList("Reference Number","Date Entered","Entered By","Position","Expense Date","Year","Expense Category","Frequency","Title","Description / Business Purpose","Vendor / Store","Amount","Receipt URI","Company","Status")));JSONObject h=new JSONObject().put("values",headers);http("PUT","https://sheets.googleapis.com/v4/spreadsheets/"+id+"/values/Sheet1!A1:O1?valueInputOption=RAW",token,h.toString());return id;}
    void appendRow(String token,String sheetId,JSONObject o)throws Exception{String year=o.optString("expenseDate").length()>=4?o.optString("expenseDate").substring(0,4):"";JSONArray row=new JSONArray().put(o.optString("ref")).put(o.optString("entered")).put(o.optString("by")).put(o.optString("position")).put(o.optString("expenseDate")).put(year).put(o.optString("category")).put(o.optString("frequency")).put(o.optString("title")).put(o.optString("description")).put(o.optString("vendor")).put(o.optString("amount")).put(o.optString("receipt")).put("Virtual Beehive Inc.").put("New");JSONObject body=new JSONObject().put("values",new JSONArray().put(row));http("POST","https://sheets.googleapis.com/v4/spreadsheets/"+sheetId+"/values/Sheet1!A:O:append?valueInputOption=USER_ENTERED",token,body.toString());}
    String http(String method,String url,String token,String json)throws Exception{HttpURLConnection c=(HttpURLConnection)new URL(url).openConnection();c.setRequestMethod(method);c.setRequestProperty("Authorization","Bearer "+token);c.setRequestProperty("Content-Type","application/json; charset=UTF-8");c.setDoInput(true);if(json!=null){c.setDoOutput(true);OutputStream os=c.getOutputStream();os.write(json.getBytes("UTF-8"));os.close();}int code=c.getResponseCode();InputStream is=code>=200&&code<300?c.getInputStream():c.getErrorStream();String s=readAll(is);if(code<200||code>=300)throw new Exception("HTTP "+code+" "+s);return s;}
    String readAll(InputStream is)throws Exception{BufferedReader br=new BufferedReader(new InputStreamReader(is));StringBuilder sb=new StringBuilder();String line;while((line=br.readLine())!=null)sb.append(line);return sb.toString();}

    public void showRecords(){base("Records");JSONArray arr=getRecords();if(arr.length()==0){LinearLayout e=card("No Records Yet");e.addView(tv("Add an expense and it will appear here with a locked reference number.",15,WHITE,Typeface.NORMAL));return;}for(int i=arr.length()-1;i>=0;i--){try{JSONObject o=arr.getJSONObject(i);LinearLayout c=card(o.optString("ref"));TextView main=tv("$"+o.optString("amount")+"  •  "+o.optString("title"),18,WHITE,Typeface.BOLD);main.setPadding(0,dp(8),0,dp(6));c.addView(main);c.addView(tv(o.optString("category")+"\n"+o.optString("frequency")+"\n"+o.optString("entered")+"\nEntered by: "+o.optString("by"),14,MUTED,Typeface.NORMAL));}catch(Exception ignored){}}}
    public void showProfile(){base("Profile");LinearLayout c=card("User Profile");ImageView avatarImg=null;String photo=sp.getString("profilePhoto","");if(!photo.isEmpty()){avatarImg=new ImageView(this);avatarImg.setImageURI(Uri.parse(photo));avatarImg.setScaleType(ImageView.ScaleType.CENTER_CROP);avatarImg.setBackground(outlined(Color.rgb(22,18,14),24,HONEY));avatarImg.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);startActivityForResult(i,55);});add(c,avatarImg,-1,dp(170),0,10,0,12);}TextView avatar=secondaryButton(photo.isEmpty()?"+ Add Profile Picture":"Change Profile Picture");avatar.setOnClickListener(v->{Intent i=new Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);startActivityForResult(i,55);});add(c,avatar,-1,-2,0,photo.isEmpty()?10:0,0,12);label(c,"Full Name");EditText name=input("Full name");name.setText(sp.getString("name","Daniel Pirooz"));add(c,name,-1,-2,0,0,0,2);label(c,"Position");EditText pos=input("Position");pos.setText(sp.getString("position","CEO / Founder"));add(c,pos,-1,-2,0,0,0,2);label(c,"Email");EditText email=input("Email");email.setText(sp.getString("email","danny@virtualbeehiveinc.com"));add(c,email,-1,-2,0,0,0,12);TextView save=primaryButton("Save Profile");save.setOnClickListener(v->{sp.edit().putString("name",name.getText().toString()).putString("position",pos.getText().toString()).putString("email",email.getText().toString()).apply();Toast.makeText(this,"Profile saved",Toast.LENGTH_LONG).show();showDashboard();});add(c,save,-1,-2,0,0,0,0);}
}
