package com.immortal.launcher;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.SeekBar;
import android.widget.EditText;
import android.widget.Button;
import android.content.SharedPreferences;

public class MainActivity extends Activity {
    private static final int BG=Color.rgb(12,12,16), PANEL=Color.rgb(27,26,33),
        PANEL2=Color.rgb(35,33,42), TEXT=Color.rgb(239,237,245),
        MUTED=Color.rgb(157,154,170), ACCENT=Color.rgb(151,111,235);
    private LinearLayout content, navColumn;
    private String selected="Play";
    private String selectedMinecraftVersion="1.21.11";
    private String selectedLoader="Vanilla";
    private TextView versionButton, downloadStatus;
    private SharedPreferences prefs;
    private java.util.ArrayList<String> catalogIds = new java.util.ArrayList<>();
    private java.util.ArrayList<String> catalogLabels = new java.util.ArrayList<>();

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(0);
        setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        prefs=getSharedPreferences("immortal_settings",MODE_PRIVATE);
        selectedMinecraftVersion=prefs.getString("selected_version","1.21.11");
        selectedLoader=prefs.getString("selected_loader","Vanilla");
        setContentView(build());
    }

    private View build() {
        LinearLayout root=new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        root.setBackgroundColor(BG);
        root.setPadding(dp(14),dp(5),dp(14),dp(8));
        root.addView(header(),new LinearLayout.LayoutParams(-1,dp(58)));

        LinearLayout body=new LinearLayout(this);
        body.setOrientation(LinearLayout.HORIZONTAL);
        body.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        LinearLayout.LayoutParams bp=new LinearLayout.LayoutParams(-1,0,1);
        bp.topMargin=dp(5);
        root.addView(body,bp);

        navColumn=new LinearLayout(this);
        navColumn.setOrientation(LinearLayout.VERTICAL);
        navColumn.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        navColumn.setPadding(0,dp(6),dp(12),dp(6));
        body.addView(navColumn,new LinearLayout.LayoutParams(dp(145),-1));
        addNav("▶","Play"); addNav("▦","Mods"); addNav("▰","Resource Pack");
        addNav("☼","Shaders"); addNav("▱","Versions"); addNav("☷","Settings"); addNav("♟","Skins");
        addNav("▧","Modpacks"); addNav("◉","Accounts"); addNav("▤","Graphics"); addNav("≡","Logs");

        View divider=new View(this);
        divider.setBackgroundColor(Color.rgb(49,47,57));
        body.addView(divider,new LinearLayout.LayoutParams(dp(1),-1));

        ScrollView scroll=new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        LinearLayout.LayoutParams sp=new LinearLayout.LayoutParams(0,-1,1);
        sp.leftMargin=dp(14);
        body.addView(scroll,sp);
        content=new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        content.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        content.setPadding(dp(2),dp(5),dp(2),dp(4));
        scroll.addView(content);
        showPage();
        return root;
    }

    private View header() {
        LinearLayout h=new LinearLayout(this);
        h.setGravity(Gravity.CENTER_VERTICAL);
        h.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);

        // Use the standalone I mark inside the launcher; keep the full app icon separate.
        LogoView logo=new LogoView();
        logo.setContentDescription("Immortal I logo");
        LinearLayout.LayoutParams logoParams=new LinearLayout.LayoutParams(dp(38),dp(38));
        logoParams.gravity=Gravity.CENTER_VERTICAL;
        h.addView(logo,logoParams);

        TextView brand=txt("Immortal Launcher",20,TEXT,true);
        LinearLayout.LayoutParams brandp=new LinearLayout.LayoutParams(-2,-2);
        brandp.leftMargin=dp(9);
        h.addView(brand,brandp);

        TextView version=txt("0.1.0",12,MUTED,true);
        LinearLayout.LayoutParams verp=new LinearLayout.LayoutParams(-2,-2);
        verp.leftMargin=dp(9);
        h.addView(version,verp);
        View spacer=new View(this);
        h.addView(spacer,new LinearLayout.LayoutParams(0,1,1));
        return h;
    }

    private void addNav(String icon,String name) {
        LinearLayout item=new LinearLayout(this);
        item.setGravity(Gravity.CENTER_VERTICAL);
        item.setPadding(dp(10),0,dp(7),0);
        item.setBackground(shape(name.equals(selected)?PANEL2:BG,18,
            name.equals(selected)?Color.rgb(55,51,65):Color.TRANSPARENT));
        TextView symbol=txt(icon,17,name.equals(selected)?TEXT:MUTED,true);
        item.addView(symbol,new LinearLayout.LayoutParams(dp(24),-2));
        TextView label=txt(name,13,name.equals(selected)?TEXT:MUTED,true);
        label.setPadding(dp(5),0,0,0);
        item.addView(label);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(42));
        p.bottomMargin=dp(6);
        navColumn.addView(item,p);
        item.setOnClickListener(v->open(name));
    }

    private void showPage() {
        content.removeAllViews();
        if("Settings".equals(selected)) { showSettingsPage(); return; }
        if("Versions".equals(selected)) { showInstalledVersionsPage(); return; }
        if("Add Version".equals(selected)) { showAddVersionPage(); return; }
        if("Mods".equals(selected) || "Resource Pack".equals(selected) || "Shaders".equals(selected)
                || "Modpacks".equals(selected) || "Skins".equals(selected)) { showFileLibraryPage(); return; }
        if("Accounts".equals(selected)) { showAccountsPage(); return; }
        if("Graphics".equals(selected)) { showGraphicsPage(); return; }
        if("Logs".equals(selected)) { showLogsPage(); return; }
        if(!"Play".equals(selected)) {
            content.addView(txt(selected,24,TEXT,true),margins(-1,-2,dp(10),dp(12),0,dp(10)));
            LinearLayout panel=card(); panel.setGravity(Gravity.CENTER);
            panel.addView(txt("Your "+selected+" will appear here.",14,MUTED,false));
            content.addView(panel,new LinearLayout.LayoutParams(-1,dp(170)));
            return;
        }
        FrameLayout hero=new FrameLayout(this);
        hero.setBackground(shape(Color.rgb(20,19,27),22,Color.rgb(53,51,62)));
        EndermanArt art=new EndermanArt(); hero.addView(art,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout heroText=new LinearLayout(this); heroText.setOrientation(LinearLayout.VERTICAL);
        heroText.setGravity(Gravity.CENTER_VERTICAL); heroText.setPadding(dp(22),dp(15),dp(22),dp(15));
        heroText.addView(txt("Ready to play",27,TEXT,true));
        heroText.addView(txt("Minecraft Java  •  "+selectedMinecraftVersion+" / "+selectedLoader,14,MUTED,false),
            margins(-1,-2,0,dp(5),0,0));
        hero.addView(heroText,new FrameLayout.LayoutParams(-1,-1));
        content.addView(hero,new LinearLayout.LayoutParams(-1,dp(174)));

        LinearLayout actions=new LinearLayout(this);
        actions.setGravity(Gravity.CENTER_VERTICAL); actions.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        TextView play=txt("▶  Play Game",14,BG,true); play.setGravity(Gravity.CENTER);
        play.setBackground(shape(Color.rgb(190,187,199),22,Color.TRANSPARENT));
        LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(0,dp(42),1);
        actions.addView(play,pp);
        TextView add=txt("+ Add Version",12,TEXT,true); add.setGravity(Gravity.CENTER);
        add.setBackground(shape(PANEL2,18,Color.rgb(55,51,65)));
        LinearLayout.LayoutParams ap=new LinearLayout.LayoutParams(dp(112),dp(38));
        ap.leftMargin=dp(9); actions.addView(add,ap);
        content.addView(actions,margins(-1,-2,0,dp(12),0,0));
        play.setOnClickListener(v->{
            if(!isVersionInstalled(selectedMinecraftVersion)) {
                toast("Install this version first from Add Version.");
                return;
            }
            toast("Version selected. Full Java runtime and game launch integration is the next required step.");
        });
        add.setOnClickListener(v->open("Add Version"));

        versionButton=txt("Installed version: "+selectedMinecraftVersion+"  ⌄",13,TEXT,true);
        versionButton.setGravity(Gravity.CENTER_VERTICAL); versionButton.setPadding(dp(13),0,dp(12),0);
        versionButton.setBackground(shape(PANEL2,18,Color.rgb(50,47,59)));
        content.addView(versionButton,margins(-1,dp(38),0,dp(9),0,0));        versionButton.setOnClickListener(v->showInstalledVersionPicker());

        downloadStatus=txt("Choose an installed version or add a new one.",12,MUTED,false);
        content.addView(downloadStatus,margins(-1,-2,0,dp(10),0,0));
    }    private void showAddVersionPage() {
        content.addView(txt("Add Version",23,TEXT,true),margins(-1,-2,dp(5),dp(6),0,dp(8)));
        content.addView(txt("Choose an edition to install",13,MUTED,false),margins(-1,-2,dp(5),0,0,dp(10)));
        addVersionAction("CREATE VANILLA INSTANCE","Vanilla");
        addVersionAction("INSTALL OPTIFINE","OptiFine");
        addVersionAction("CREATE FABRIC INSTANCE","Fabric");
        addVersionAction("CREATE QUILT INSTANCE","Quilt");
        addVersionAction("CREATE LEGACY FABRIC INSTANCE","Legacy Fabric");
        addVersionAction("CREATE FORGE INSTANCE","Forge");
        addVersionAction("CREATE NEOFORGE INSTANCE","NeoForge");
        addVersionAction("CREATE MODPACK INSTANCE","Modpack");
        TextView list=txt("Browse official Minecraft versions  →",13,ACCENT,true);
        list.setPadding(dp(8),dp(10),dp(8),dp(10)); content.addView(list);
        list.setOnClickListener(v->loadVersionCatalog());
        downloadStatus=txt("Select a loader, then choose a Minecraft version.",12,MUTED,false);
        content.addView(downloadStatus,margins(-1,-2,dp(8),dp(4),0,0));
    }

    private void addVersionAction(String label,String loader) {
        TextView button=txt(label,12,TEXT,true); button.setGravity(Gravity.CENTER);
        button.setBackground(shape(PANEL2,12,Color.rgb(65,61,75)));
        LinearLayout.LayoutParams p=margins(-1,dp(38),dp(8),dp(4),dp(8),dp(4));
        content.addView(button,p);
        button.setOnClickListener(v->{selectedLoader=loader;loadVersionCatalog();});
    }

    private void showInstalledVersionPicker() {
        java.util.ArrayList<String> installed=getInstalledVersions();
        if(installed.isEmpty()) { toast("No installed versions yet. Tap Add Version."); return; }
        new android.app.AlertDialog.Builder(this).setTitle("Choose installed version")
            .setItems(installed.toArray(new String[0]),(d,which)->{
                selectedMinecraftVersion=installed.get(which);
                prefs.edit().putString("selected_version",selectedMinecraftVersion).apply();
                setContentView(build());
            }).setNegativeButton("Cancel",null).show();
    }

    private void showInstalledVersionsPage() {
        content.addView(txt("Installed Versions",23,TEXT,true),margins(-1,-2,dp(5),dp(6),0,dp(8)));
        TextView add=txt("+ Add Version",13,BG,true); add.setGravity(Gravity.CENTER);
        add.setBackground(shape(Color.rgb(190,187,199),16,Color.TRANSPARENT));
        content.addView(add,margins(dp(130),dp(36),0,0,0,dp(10)));
        add.setOnClickListener(v->open("Add Version"));
        java.util.ArrayList<String> installed=getInstalledVersions();
        if(installed.isEmpty()) {
            LinearLayout empty=card(); empty.setGravity(Gravity.CENTER);
            empty.addView(txt("No versions installed yet.",14,MUTED,false));
            content.addView(empty,new LinearLayout.LayoutParams(-1,dp(100))); return;
        }
        for(String id:installed) {
            LinearLayout row=card(); row.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout.LayoutParams rp=margins(-1,dp(52),0,dp(4),0,dp(4));
            TextView name=txt(id+"   •   "+(id.equals(selectedMinecraftVersion)?"Selected":"Minecraft Java"),14,TEXT,true);
            row.addView(name,new LinearLayout.LayoutParams(0,-2,1));
            TextView use=txt("SELECT",11,ACCENT,true); row.addView(use);
            content.addView(row,rp);
            row.setOnClickListener(v->{
                selectedMinecraftVersion=id; prefs.edit().putString("selected_version",id).apply();
                setContentView(build()); toast(id+" selected");
            });
            row.setOnLongClickListener(v->{
                new android.app.AlertDialog.Builder(this).setTitle("Remove "+id+"?")
                    .setMessage("Delete this installed version's files?")
                    .setNegativeButton("Cancel",null).setPositiveButton("Delete",(d,w)->{
                        java.io.File dir=new java.io.File(getExternalFilesDir(null),"minecraft/versions/"+id);
                        deleteTree(dir); showPage();
                    }).show(); return true;
            });
        }
    }

    private java.util.ArrayList<String> getInstalledVersions() {
        java.util.ArrayList<String> out=new java.util.ArrayList<>();
        java.io.File dir=new java.io.File(getExternalFilesDir(null),"minecraft/versions");
        java.io.File[] files=dir.listFiles();
        if(files!=null) for(java.io.File f:files)
            if(f.isDirectory() && new java.io.File(f,f.getName()+".json").exists()) {
                java.io.File jar=new java.io.File(f,f.getName()+".jar");
                java.io.File profile=new java.io.File(f,f.getName()+".json");
                boolean inherited=false;
                try { inherited=new String(java.nio.file.Files.readAllBytes(profile.toPath()),"UTF-8").contains("inheritsFrom"); }
                catch(Exception ignored) {}
                if(jar.exists() || inherited) out.add(f.getName());
            }
        java.util.Collections.sort(out); return out;
    }

    private boolean isVersionInstalled(String id) {
        java.io.File dir=new java.io.File(getExternalFilesDir(null),"minecraft/versions/"+id);
        java.io.File profile=new java.io.File(dir,id+".json");
        if(!profile.exists()) return false;
        if(new java.io.File(dir,id+".jar").exists()) return true;
        try { return new String(java.nio.file.Files.readAllBytes(profile.toPath()),"UTF-8").contains("inheritsFrom"); }
        catch(Exception e) { return false; }
    }

    private void deleteTree(java.io.File file) {
        if(file==null || !file.exists()) return;
        java.io.File[] children=file.listFiles();
        if(children!=null) for(java.io.File child:children) deleteTree(child);
        file.delete();
    }

    private void showSettingsPage() {
        content.addView(txt("Launcher Settings",23,TEXT,true),margins(-1,-2,dp(5),dp(6),0,dp(10)));
        content.addView(txt("Memory Allocation",15,TEXT,true),margins(-1,-2,dp(5),dp(4),0,dp(2)));
        int max=Math.max(1024,(int)(Runtime.getRuntime().maxMemory()/1048576L));
        int memory=prefs.getInt("ram_mb",Math.min(2048,Math.max(512,max/2)));
        TextView ramValue=txt(memory+" MB",13,ACCENT,true);
        content.addView(ramValue,margins(-1,-2,dp(5),0,0,0));
        SeekBar ram=new SeekBar(this); ram.setMax(8192-512); ram.setProgress(Math.max(0,Math.min(8192-512,memory-512)));
        content.addView(ram,margins(-1,dp(34),0,0,0,dp(8)));
        ram.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){
            public void onProgressChanged(SeekBar b,int value,boolean user){int mb=512+value;ramValue.setText(mb+" MB");}
            public void onStartTrackingTouch(SeekBar b){}
            public void onStopTrackingTouch(SeekBar b){prefs.edit().putInt("ram_mb",512+b.getProgress()).apply();}
        });
        TextView runtime=txt("Java Runtime  •  "+prefs.getString("java_runtime","Auto / not selected")+"  ⌄",13,TEXT,true);
        runtime.setPadding(dp(10),dp(10),dp(10),dp(10)); runtime.setBackground(shape(PANEL2,12,Color.rgb(55,51,65)));
        content.addView(runtime,margins(-1,dp(42),0,dp(3),0,dp(3))); runtime.setOnClickListener(v->chooseJavaRuntime(runtime));
        settingsText("JVM Arguments", "-Xmx is managed by the RAM slider; add other JVM flags here", "jvm_args");
        settingsText("Game Resolution", "Default / device resolution", "resolution");
        settingsSwitch("Enable VSync","vsync",true);
        settingsSwitch("Show FPS","show_fps",false);
        settingsSwitch("Fullscreen","fullscreen",true);
        settingsSwitch("Use custom controls","custom_controls",false);
        settingsSwitch("Keep launcher logs","logs",true);
        TextView note=txt("Java runtimes are selected from runtimes installed inside the app. Runtime download/installation and applying JVM settings to the game process are not connected yet.",11,MUTED,false);
        content.addView(note,margins(-1,-2,dp(5),dp(12),dp(5),dp(8)));
    }

    private void chooseJavaRuntime(TextView view) {
        java.io.File root=new java.io.File(getFilesDir(),"runtimes");
        java.util.ArrayList<String> names=new java.util.ArrayList<>();
        java.io.File[] dirs=root.listFiles();
        if(dirs!=null) for(java.io.File d:dirs) if(d.isDirectory()) names.add(d.getName());
        if(names.isEmpty()) {
            new android.app.AlertDialog.Builder(this).setTitle("Java Runtime")
                .setMessage("No Java runtimes are installed yet. Immortal needs an Android-compatible Java runtime before Minecraft can launch.")
                .setPositiveButton("OK",null).show(); return;
        }
        names.add(0,"Auto");
        new android.app.AlertDialog.Builder(this).setTitle("Choose Java Runtime")
            .setItems(names.toArray(new String[0]),(d,which)->{
                String value=names.get(which); prefs.edit().putString("java_runtime",value).apply();
                view.setText("Java Runtime  •  "+value+"  ⌄");
            }).setNegativeButton("Cancel",null).show();
    }
    private void settingsText(String title,String hint,String key) {
        content.addView(txt(title,14,TEXT,true),margins(-1,-2,dp(5),dp(7),0,dp(3)));
        EditText input=new EditText(this); input.setTextSize(12); input.setTextColor(TEXT);
        input.setHintTextColor(MUTED); input.setSingleLine(false); input.setHint(hint);
        input.setText(prefs.getString(key,""));
        input.setPadding(dp(10),dp(5),dp(10),dp(5)); input.setBackground(shape(PANEL2,12,Color.rgb(55,51,65)));
        content.addView(input,margins(-1,dp(42),0,0,0,dp(3)));
        input.setOnFocusChangeListener((v,has)->{if(!has)prefs.edit().putString(key,input.getText().toString()).apply();});
    }

    private void settingsSwitch(String title,String key,boolean def) {
        android.widget.Switch sw=new android.widget.Switch(this); sw.setText(title);
        sw.setTextColor(TEXT); sw.setTextSize(13); sw.setPadding(dp(5),0,dp(5),0);
        sw.setChecked(prefs.getBoolean(key,def));
        content.addView(sw,margins(-1,dp(42),0,dp(1),0,0));
        sw.setOnCheckedChangeListener((b,checked)->prefs.edit().putBoolean(key,checked).apply());
    }

    private void showFileLibraryPage() {
        String folder;
        String title=selected;
        if("Mods".equals(selected)) folder="mods";
        else if("Resource Pack".equals(selected)) folder="resourcepacks";
        else if("Shaders".equals(selected)) folder="shaderpacks";
        else if("Modpacks".equals(selected)) folder="modpacks";
        else folder="skins";
        java.io.File base=new java.io.File(getExternalFilesDir(null),"minecraft/versions/"+selectedMinecraftVersion+"/"+folder);
        if(!base.exists()) base.mkdirs();
        content.addView(txt(title,23,TEXT,true),margins(-1,-2,dp(5),dp(5),0,dp(4)));
        content.addView(txt("Files for "+selectedMinecraftVersion+" • stored in the selected instance",12,MUTED,false),
            margins(-1,-2,dp(5),0,0,dp(9)));
        if(!"skins".equals(folder)) {
            EditText searchBox=new EditText(this); searchBox.setSingleLine(true);
            searchBox.setTextColor(TEXT); searchBox.setHintTextColor(MUTED);
            searchBox.setHint("Search Modrinth…"); searchBox.setTextSize(13);
            searchBox.setBackground(shape(PANEL2,12,Color.rgb(55,51,65)));
            content.addView(searchBox,margins(-1,dp(40),0,0,0,dp(5)));
            TextView searchButton=txt("Search online",12,TEXT,true); searchButton.setGravity(Gravity.CENTER);
            searchButton.setBackground(shape(PANEL2,12,Color.rgb(65,61,75)));
            content.addView(searchButton,margins(dp(130),dp(34),0,0,0,dp(8)));
            searchButton.setOnClickListener(v->searchModrinth(folder,searchBox.getText().toString().trim()));
        }        TextView importButton=txt("+ Import "+(folder.equals("skins")?"PNG skin":"file"),13,BG,true);
        importButton.setGravity(Gravity.CENTER);
        importButton.setBackground(shape(Color.rgb(190,187,199),16,Color.TRANSPARENT));
        content.addView(importButton,margins(dp(155),dp(38),0,0,0,dp(10)));
        importButton.setOnClickListener(v->{
            android.content.Intent intent=new android.content.Intent(android.content.Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(android.content.Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");
            startActivityForResult(intent,folder.equals("mods")?101:folder.equals("resourcepacks")?102:folder.equals("shaderpacks")?103:folder.equals("modpacks")?104:105);
        });
        java.io.File[] files=base.listFiles();
        if(files==null || files.length==0) {
            LinearLayout empty=card(); empty.setGravity(Gravity.CENTER);
            empty.addView(txt("No files yet. Import a file to add it here.",13,MUTED,false));            content.addView(empty,new LinearLayout.LayoutParams(-1,dp(90)));
            return;
        }
        java.util.Arrays.sort(files,(a,b)->a.getName().compareToIgnoreCase(b.getName()));
        for(java.io.File file:files) {
            LinearLayout row=card(); row.setGravity(Gravity.CENTER_VERTICAL);
            TextView name=txt(file.getName(),13,TEXT,true);
            row.addView(name,new LinearLayout.LayoutParams(0,-2,1));
            TextView remove=txt("REMOVE",10,ACCENT,true); row.addView(remove);
            content.addView(row,margins(-1,dp(44),0,dp(3),0,dp(3)));
            remove.setOnClickListener(v->new android.app.AlertDialog.Builder(this)
                .setTitle("Remove "+file.getName()+"?")
                .setNegativeButton("Cancel",null).setPositiveButton("Remove",(d,w)->{file.delete();showPage();}).show());
        }
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,android.content.Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(resultCode!=RESULT_OK || data==null || data.getData()==null) return;
        String folder=requestCode==101?"mods":requestCode==102?"resourcepacks":requestCode==103?"shaderpacks":requestCode==104?"modpacks":"skins";
        String expected=requestCode==101?".jar":requestCode==105?".png":".zip";
        android.net.Uri uri=data.getData();
        new Thread(()->{
            try {
                String name="import-"+System.currentTimeMillis()+expected;
                try(android.database.Cursor cursor=getContentResolver().query(uri,null,null,null,null)) {
                    if(cursor!=null && cursor.moveToFirst()) {
                        int index=cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                        if(index>=0 && cursor.getString(index)!=null) name=cursor.getString(index).replaceAll("[^A-Za-z0-9._ -]","_");
                    }
                }
                if(!name.toLowerCase(java.util.Locale.ROOT).endsWith(expected))
                    throw new java.io.IOException("Choose a "+expected+" file for this section.");
                java.io.File dir=new java.io.File(getExternalFilesDir(null),"minecraft/versions/"+selectedMinecraftVersion+"/"+folder);
                if(!dir.exists()) dir.mkdirs();
                java.io.File target=new java.io.File(dir,name);
                try(java.io.InputStream in=getContentResolver().openInputStream(uri);
                    java.io.FileOutputStream out=new java.io.FileOutputStream(target)) {
                    if(in==null) throw new java.io.IOException("Cannot read selected file.");
                    byte[] buffer=new byte[16384]; int n;
                    while((n=in.read(buffer))!=-1) out.write(buffer,0,n);
                }
                final String importedName=name;
                runOnUiThread(()->{toast("Imported "+importedName);showPage();});
            } catch(Exception e) { runOnUiThread(()->toast("Import failed: "+e.getMessage())); }
        }).start();
    }

    private void showAccountsPage() {
        content.addView(txt("Accounts",23,TEXT,true),margins(-1,-2,dp(5),dp(5),0,dp(8)));
        content.addView(txt("Offline profile",14,TEXT,true),margins(-1,-2,dp(5),dp(4),0,dp(3)));
        EditText player=new EditText(this); player.setSingleLine(true); player.setTextColor(TEXT);
        player.setHintTextColor(MUTED); player.setHint("Player name");
        player.setText(prefs.getString("offline_player","Player"));
        player.setBackground(shape(PANEL2,12,Color.rgb(55,51,65)));
        content.addView(player,margins(-1,dp(44),0,0,0,dp(8)));
        TextView save=txt("Save offline profile",13,BG,true); save.setGravity(Gravity.CENTER);
        save.setBackground(shape(Color.rgb(190,187,199),14,Color.TRANSPARENT));
        content.addView(save,margins(dp(170),dp(38),0,0,0,dp(10)));
        save.setOnClickListener(v->{String name=player.getText().toString().trim();
            if(name.isEmpty() || name.length()>16){toast("Minecraft names must be 1–16 characters.");return;}
            prefs.edit().putString("offline_player",name).apply();toast("Offline profile saved.");
        });
        LinearLayout note=card();
        note.addView(txt("Microsoft account sign-in is not connected yet. Offline profiles do not provide access to online-mode servers.",12,MUTED,false));
        content.addView(note,new LinearLayout.LayoutParams(-1,-2));
    }

    private void showGraphicsPage() {
        content.addView(txt("Graphics & Device Diagnostics",22,TEXT,true),margins(-1,-2,dp(5),dp(5),0,dp(8)));
        android.app.ActivityManager am=(android.app.ActivityManager)getSystemService(ACTIVITY_SERVICE);
        android.content.pm.ConfigurationInfo info=am.getDeviceConfigurationInfo();
        String gl=info==null?"Unknown":info.getGlEsVersion();
        int req=info==null?0:info.reqGlEsVersion;
        long max=Runtime.getRuntime().maxMemory()/1048576L;
        addDiagnostic("OpenGL ES reported by Android",gl);
        addDiagnostic("OpenGL requirement code",String.format(java.util.Locale.ROOT,"0x%08X",req));
        addDiagnostic("App Java heap limit",max+" MB");
        addDiagnostic("Device architecture",System.getProperty("os.arch","Unknown"));
        addDiagnostic("Selected Minecraft version",selectedMinecraftVersion);
        addDiagnostic("Selected loader",selectedLoader);
        LinearLayout note=card();
        note.addView(txt("This page reports Android graphics capabilities. It is not a live Minecraft shader renderer; rendering Minecraft Java on Android still needs a compatible graphics translation layer and native libraries.",12,MUTED,false));
        content.addView(note,margins(-1,-2,0,dp(10),0,0));
    }

    private void addDiagnostic(String label,String value) {
        LinearLayout row=card(); row.setOrientation(LinearLayout.VERTICAL);
        row.addView(txt(label,11,MUTED,false));
        row.addView(txt(value,14,TEXT,true),margins(-1,-2,0,dp(3),0,0));
        content.addView(row,margins(-1,-2,0,dp(3),0,dp(3)));
    }

    private void showLogsPage() {
        content.addView(txt("Launcher Diagnostics",23,TEXT,true),margins(-1,-2,dp(5),dp(5),0,dp(8)));
        addDiagnostic("App version","0.1.0");
        addDiagnostic("Selected instance",selectedMinecraftVersion);
        addDiagnostic("Loader",selectedLoader);
        addDiagnostic("Storage directory",String.valueOf(getExternalFilesDir(null)));
        addDiagnostic("Java runtime",prefs.getString("java_runtime","Not installed / not selected"));
        LinearLayout note=card();
        note.addView(txt("Installer progress and errors are shown in the status message while downloading. Persistent log capture will be added with the game process/runtime integration.",12,MUTED,false));
        content.addView(note,margins(-1,-2,0,dp(8),0,0));
    }

    private void searchModrinth(String folder,String query) {
        if(query.isEmpty()){toast("Enter a search term first.");return;}
        String projectType=folder.equals("mods")?"mod":folder.equals("resourcepacks")?"resourcepack":folder.equals("shaderpacks")?"shader":"modpack";
        String game=prefs.getString("base_game_version",selectedMinecraftVersion);
        toast("Searching Modrinth…");
        new Thread(()->{
            try {
                ModrinthClient client=new ModrinthClient(this);
                org.json.JSONArray hits=client.search(projectType,query,game,selectedLoader);
                java.util.ArrayList<org.json.JSONObject> projects=new java.util.ArrayList<>();
                java.util.ArrayList<String> labels=new java.util.ArrayList<>();
                for(int i=0;i<hits.length();i++){
                    org.json.JSONObject hit=hits.getJSONObject(i);projects.add(hit);
                    labels.add(hit.optString("title",hit.optString("slug"))+"  •  "+hit.optString("description",""));
                }
                runOnUiThread(()->{
                    if(projects.isEmpty()){toast("No matching projects found for "+game+".");return;}
                    new android.app.AlertDialog.Builder(this).setTitle("Modrinth results • "+game)
                        .setItems(labels.toArray(new String[0]),(dialog,which)->downloadModrinthProject(client,projects.get(which),folder,game))
                        .setNegativeButton("Cancel",null).show();
                });
            }catch(Exception e){runOnUiThread(()->toast("Modrinth search failed: "+e.getMessage()));}
        }).start();
    }

    private void downloadModrinthProject(ModrinthClient client,org.json.JSONObject project,String folder,String game) {
        new Thread(()->{
            try{
                org.json.JSONObject version=client.versions(project.getString("project_id"),game,selectedLoader);
                String directory=selectedMinecraftVersion+"/"+folder;
                java.io.File file=client.downloadPrimary(version,directory);
                runOnUiThread(()->{toast("Downloaded "+file.getName());showPage();});
            }catch(Exception e){runOnUiThread(()->toast("Download failed: "+e.getMessage()));}
        }).start();
    }

    private void loadVersionCatalog() {
        downloadStatus.setText("Loading official Minecraft version list...");
        new Thread(() -> {
            try {
                String json=httpGet("https://piston-meta.mojang.com/mc/game/version_manifest_v2.json");
                org.json.JSONArray all=new org.json.JSONObject(json).getJSONArray("versions");
                java.util.ArrayList<String> ids=new java.util.ArrayList<>();
                java.util.ArrayList<String> labels=new java.util.ArrayList<>();
                for(int i=0;i<all.length();i++) {
                    org.json.JSONObject item=all.getJSONObject(i);
                    String id=item.getString("id");
                    String type=item.optString("type","");
                    if(type.equals("release") || type.equals("snapshot") || type.equals("old_beta") || type.equals("old_alpha")) {
                        ids.add(id);
                        labels.add(id+(type.equals("release")?"  •  Release":type.equals("old_beta")?"  •  Old Beta":"  •  Old Alpha"));
                    }
                }
                catalogIds=ids; catalogLabels=labels;                runOnUiThread(() -> {
                    String[] choices=labels.toArray(new String[0]);
                    new android.app.AlertDialog.Builder(this)
                        .setTitle("Minecraft Java versions")
                        .setItems(choices,(d,which)->{
                            selectedMinecraftVersion=ids.get(which);
                            prefs.edit().putString("selected_version",selectedMinecraftVersion).putString("selected_loader",selectedLoader).apply();
                            if(downloadStatus!=null) downloadStatus.setText(selectedMinecraftVersion+" selected • "+selectedLoader);
                            if("Play".equals(selected)) setContentView(build());
                            else showInstallConfirmation();
                        }).setNegativeButton("Cancel",null).show();
                    if(downloadStatus!=null) downloadStatus.setText(ids.size()+" official releases and legacy versions loaded.");
                });
            } catch(Exception e) {
                runOnUiThread(()->downloadStatus.setText("Could not load versions. Check your internet connection."));
            }
        }).start();
    }
    private void showInstallConfirmation() {
        new android.app.AlertDialog.Builder(this).setTitle("Install "+selectedMinecraftVersion+" • "+selectedLoader)
            .setMessage("Install the official game version files now?")
            .setNegativeButton("Cancel",null).setPositiveButton("Install",(d,w)->downloadBaseVersion()).show();
    }

    private void chooseLoaderForVersion() {
        final String[] loaders={"Vanilla","Fabric","Forge","NeoForge","Quilt"};
        new android.app.AlertDialog.Builder(this)
            .setTitle("Loader for Minecraft "+selectedMinecraftVersion)
            .setItems(loaders,(d,which)->{
                selectedLoader=loaders[which];
                versionButton.setText("  "+selectedMinecraftVersion+" • "+selectedLoader+"  ⌄  ");
                checkLoaderCompatibility();
            }).setNegativeButton("Cancel",null).show();
    }

    private void checkLoaderCompatibility() {
        if(selectedLoader.equals("Vanilla")) {
            downloadStatus.setText("Vanilla selected. Official game files are available.");
            return;
        }
        downloadStatus.setText("Checking "+selectedLoader+" compatibility...");
        new Thread(()->{
            try {
                String url;
                if(selectedLoader.equals("Fabric"))
                    url="https://meta.fabricmc.net/v2/versions/loader/"+java.net.URLEncoder.encode(selectedMinecraftVersion,"UTF-8");
                else if(selectedLoader.equals("Quilt"))
                    url="https://meta.quiltmc.org/v3/versions/loader/"+java.net.URLEncoder.encode(selectedMinecraftVersion,"UTF-8");
                else if(selectedLoader.equals("Forge"))
                    url="https://files.minecraftforge.net/net/minecraftforge/forge/maven-metadata.xml";
                else
                    url="https://maven.neoforged.net/releases/net/neoforged/neoforge/maven-metadata.xml";
                String data=httpGet(url);
                String msg;
                if(selectedLoader.equals("Fabric") || selectedLoader.equals("Quilt")) {
                    org.json.JSONArray arr=new org.json.JSONArray(data);
                    msg=arr.length()>0?selectedLoader+" supports Minecraft "+selectedMinecraftVersion+". "+arr.length()+" loader builds found."
                        :selectedLoader+" does not list support for "+selectedMinecraftVersion+".";
                } else {
                    msg="Official "+selectedLoader+" metadata received. Compatibility will be matched during installer integration.";
                }
                final String result=msg;
                runOnUiThread(()->downloadStatus.setText(result));
            } catch(Exception e) {
                runOnUiThread(()->downloadStatus.setText(selectedLoader+" compatibility could not be checked for this version."));
            }
        }).start();    }

    private void downloadBaseVersion() {
        final String game=selectedMinecraftVersion;
        final String loader=selectedLoader;
        if(downloadStatus!=null) downloadStatus.setText("Preparing "+game+" • "+loader+"…");
        new Thread(() -> {
            try {
                MinecraftInstaller installer=new MinecraftInstaller(this,
                    message -> runOnUiThread(() -> { if(downloadStatus!=null) downloadStatus.setText(message); }));
                installer.install(game);
                String installedId=game;
                if("Fabric".equals(loader) || "Quilt".equals(loader)) {
                    LoaderInstaller loaderInstaller=new LoaderInstaller(this,
                        message -> runOnUiThread(() -> { if(downloadStatus!=null) downloadStatus.setText(message); }));
                    installedId=loaderInstaller.install(game,loader);
                } else if(!"Vanilla".equals(loader)) {
                    throw new java.io.IOException(loader+" metadata is not enough to install this loader. Its official installer workflow is not implemented yet; base game files were downloaded, but no loader profile was created.");
                }
                final String finalId=installedId;
                runOnUiThread(() -> {
                    selectedMinecraftVersion=finalId;
                    prefs.edit().putString("selected_version",finalId).putString("selected_loader",loader).putString("base_game_version",game).apply();
                    if(downloadStatus!=null) downloadStatus.setText(finalId+" installed. Android Java runtime and graphics bridge are still required to launch.");
                    toast(finalId+" installed");
                    if("Versions".equals(selected)) showPage();
                });
            } catch(Exception e) {
                runOnUiThread(() -> {
                    String message="Install failed: "+e.getMessage();
                    if(downloadStatus!=null) downloadStatus.setText(message);
                    toast(message);
                });
            }
        }).start();
    }

    private String httpGet(String address) throws Exception {
        java.net.HttpURLConnection c=(java.net.HttpURLConnection)new java.net.URL(address).openConnection();
        c.setConnectTimeout(15000);c.setReadTimeout(30000);c.setRequestProperty("User-Agent","ImmortalLauncher/0.1.0");
        try(java.io.InputStream in=c.getInputStream();java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream()) {
            byte[] b=new byte[8192];int n;while((n=in.read(b))!=-1)out.write(b,0,n);
            return out.toString("UTF-8");
        } finally {c.disconnect();}
    }

    private void downloadFile(String address,java.io.File target) throws Exception {
        java.net.HttpURLConnection c=(java.net.HttpURLConnection)new java.net.URL(address).openConnection();
        c.setConnectTimeout(15000);c.setReadTimeout(60000);c.setRequestProperty("User-Agent","ImmortalLauncher/0.1.0");
        try(java.io.InputStream in=c.getInputStream();java.io.FileOutputStream out=new java.io.FileOutputStream(target)) {
            byte[] b=new byte[16384];int n;while((n=in.read(b))!=-1)out.write(b,0,n);
        } finally {c.disconnect();}
    }

    private void writeText(java.io.File file,String data) throws Exception {
        try(java.io.FileOutputStream out=new java.io.FileOutputStream(file)) {
            out.write(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        }
    }

    private void open(String name) {
        selected=name;
        setContentView(build());
    }
    private LinearLayout card() {
        LinearLayout c=new LinearLayout(this);
        c.setPadding(dp(16),dp(12),dp(16),dp(12));
        c.setBackground(shape(PANEL,18,Color.rgb(49,47,58)));
        return c;
    }
    private TextView txt(String s,int size,int color,boolean bold) {
        TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color);
        if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        return t;
    }
    private GradientDrawable shape(int color,int radius,int stroke) {
        GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(radius));
        if(stroke!=Color.TRANSPARENT)d.setStroke(dp(1),stroke); return d;
    }
    private LinearLayout.LayoutParams margins(int w,int h,int l,int t,int r,int b) {
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(w,h);
        p.setMargins(l,t,r,b); return p;
    }
    private int dp(int n){return (int)(n*getResources().getDisplayMetrics().density+.5f);}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}

    private class EndermanArt extends View {
        private final Paint paint=new Paint(Paint.ANTI_ALIAS_FLAG);
        EndermanArt(){super(MainActivity.this);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
        private void rect(Canvas c,int color,float l,float t,float r,float b){
            paint.setColor(color);paint.setStyle(Paint.Style.FILL);paint.setShader(null);
            c.drawRect(l,t,r,b,paint);
        }
        @Override protected void onDraw(Canvas canvas){
            super.onDraw(canvas);
            float w=getWidth(),h=getHeight();
            canvas.save();
            Path clip=new Path();
            clip.addRoundRect(0,0,w,h,dp(22),dp(22),Path.Direction.CW);
            canvas.clipPath(clip);
            paint.setShader(new android.graphics.LinearGradient(0,0,w,h,
                new int[]{Color.rgb(42,39,52),Color.rgb(22,21,29),Color.rgb(12,12,17)},
                null,android.graphics.Shader.TileMode.CLAMP));
            canvas.drawRect(0,0,w,h,paint);paint.setShader(null);
            // Distant blocky landscape
            int shadow=Color.rgb(17,16,23);
            rect(canvas,shadow,w*.52f,h*.68f,w*.70f,h);
            rect(canvas,shadow,w*.66f,h*.56f,w*.84f,h);
            rect(canvas,shadow,w*.79f,h*.70f,w,h);
            rect(canvas,Color.rgb(25,23,33),w*.58f,h*.61f,w*.72f,h*.76f);
            rect(canvas,Color.rgb(25,23,33),w*.75f,h*.48f,w*.90f,h*.68f);
            // Enderman silhouette on the right
            float cx=w*.82f, top=h*.16f, unit=h*.105f;
            int black=Color.rgb(7,7,11);
            rect(canvas,black,cx-unit*.72f,top,cx+unit*.72f,top+unit*1.38f);
            rect(canvas,Color.rgb(12,10,17),cx-unit*.58f,top+unit*1.48f,cx+unit*.58f,h*.78f);
            rect(canvas,black,cx-unit*.90f,top+unit*1.55f,cx-unit*.56f,h*.96f);            rect(canvas,black,cx+unit*.56f,top+unit*1.55f,cx+unit*.90f,h*.96f);
            rect(canvas,black,cx-unit*.48f,h*.72f,cx-unit*.16f,h*1.04f);
            rect(canvas,black,cx+unit*.16f,h*.72f,cx+unit*.48f,h*1.04f);
            // Subtle violet eyes
            paint.setColor(Color.rgb(174,112,255));paint.setStyle(Paint.Style.FILL);
            paint.setShadowLayer(dp(7),0,0,Color.rgb(137,70,255));
            canvas.drawRect(cx-unit*.48f,top+unit*.72f,cx-unit*.12f,top+unit*.84f,paint);
            canvas.drawRect(cx+unit*.12f,top+unit*.72f,cx+unit*.48f,top+unit*.84f,paint);
            paint.clearShadowLayer();
            // Soft dark overlay keeps the title readable
            paint.setShader(new android.graphics.LinearGradient(0,0,w*.78f,0,
                new int[]{Color.argb(0,0,0,0),Color.argb(20,0,0,0),Color.argb(100,0,0,0)},
                null,android.graphics.Shader.TileMode.CLAMP));
            canvas.drawRect(0,0,w,h,paint);paint.setShader(null);
            canvas.restore();
            paint.setStyle(Paint.Style.STROKE);paint.setStrokeWidth(dp(1));
            paint.setColor(Color.rgb(53,51,62));canvas.drawRoundRect(0,0,w,h,dp(22),dp(22),paint);
            paint.setStyle(Paint.Style.FILL);
        }
    }

    private class LogoView extends View {
        private final Paint p=new Paint(3);
        LogoView(){super(MainActivity.this);}
        @Override protected void onDraw(Canvas c){
            super.onDraw(c);
            float w=getWidth(),h=getHeight();
            p.setStyle(Paint.Style.FILL);
            p.setShader(new android.graphics.LinearGradient(0,0,w,h,
                new int[]{Color.rgb(205,202,218),Color.rgb(83,79,99),Color.rgb(174,169,190)},
                null,android.graphics.Shader.TileMode.CLAMP));
            Path i=new Path();
            i.moveTo(w*.22f,h*.12f);i.lineTo(w*.72f,h*.12f);i.lineTo(w*.62f,h*.28f);
            i.lineTo(w*.56f,h*.28f);i.lineTo(w*.56f,h*.72f);i.lineTo(w*.72f,h*.86f);
            i.lineTo(w*.22f,h*.86f);i.lineTo(w*.38f,h*.72f);i.lineTo(w*.38f,h*.28f);
            i.close(); c.drawPath(i,p); p.setShader(null);
            p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(3));
            p.setColor(ACCENT); c.drawOval(w*.08f,h*.31f,w*.91f,h*.69f,p);
            p.setStyle(Paint.Style.FILL);
        }
    }
}