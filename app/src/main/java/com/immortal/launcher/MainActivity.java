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
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
    private static final int BG=Color.rgb(12,12,16), PANEL=Color.rgb(27,26,33),
        PANEL2=Color.rgb(35,33,42), TEXT=Color.rgb(239,237,245),
        MUTED=Color.rgb(157,154,170), ACCENT=Color.rgb(151,111,235);
    private LinearLayout content, navColumn;
    private String selected="Play";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);
        getWindow().getDecorView().setSystemUiVisibility(0);
        setRequestedOrientation(android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
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
        addNav("☼","Shaders"); addNav("▱","Installed"); addNav("☷","Settings"); addNav("♟","Skins");

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

        ImageView logo=new ImageView(this);
        logo.setImageResource(R.drawable.immortal_logo);
        logo.setScaleType(ImageView.ScaleType.FIT_CENTER);
        h.addView(logo,new LinearLayout.LayoutParams(dp(48),dp(48)));

        TextView brand=txt("IMMORTAL",20,TEXT,true);
        LinearLayout.LayoutParams brandp=new LinearLayout.LayoutParams(0,-2,1);
        brandp.leftMargin=dp(10);
        h.addView(brand,brandp);

        LinearLayout right=new LinearLayout(this);
        right.setGravity(Gravity.CENTER_VERTICAL);
        right.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);

        TextView version=txt("0.1.0",12,MUTED,true);
        LinearLayout.LayoutParams verp=new LinearLayout.LayoutParams(-2,-2);
        verp.rightMargin=dp(12);
        right.addView(version,verp);

        TextView account=txt("Player  ⌄",14,TEXT,true);
        account.setGravity(Gravity.CENTER);
        account.setPadding(dp(14),0,dp(14),0);
        account.setBackground(shape(PANEL,30,Color.rgb(48,45,56)));
        right.addView(account,new LinearLayout.LayoutParams(-2,dp(42)));
        account.setOnClickListener(v->toast("Account selection will be added."));

        TextView settings=txt("☷",21,MUTED,true);
        settings.setGravity(Gravity.CENTER);
        settings.setBackground(shape(PANEL2,50,Color.rgb(51,48,60)));
        LinearLayout.LayoutParams setp=new LinearLayout.LayoutParams(dp(44),dp(44));
        setp.leftMargin=dp(8);
        right.addView(settings,setp);
        settings.setOnClickListener(v->open("Settings"));

        h.addView(right,new LinearLayout.LayoutParams(-2,-1));
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
        if(!"Play".equals(selected)) {
            content.addView(txt(selected,24,TEXT,true),margins(-1,-2,dp(10),dp(12),0,dp(10)));
            LinearLayout panel=card();
            panel.setGravity(Gravity.CENTER);
            panel.addView(txt("Your "+selected+" will appear here.",14,MUTED,false));
            content.addView(panel,new LinearLayout.LayoutParams(-1,dp(170)));
            return;
        }
        LinearLayout hero=new LinearLayout(this);
        hero.setOrientation(LinearLayout.VERTICAL);
        hero.setGravity(Gravity.CENTER_VERTICAL);
        hero.setPadding(dp(22),dp(15),dp(22),dp(15));
        GradientDrawable banner=new GradientDrawable(GradientDrawable.Orientation.TL_BR,
            new int[]{Color.rgb(43,41,51),Color.rgb(24,23,30),Color.rgb(16,16,21)});
        banner.setCornerRadius(dp(22));
        banner.setStroke(dp(1),Color.rgb(53,51,62));
        hero.setBackground(banner);
        hero.addView(txt("Ready to play",27,TEXT,true));
        hero.addView(txt("Minecraft Java  •  Vanilla / compatibility",14,MUTED,false),
            margins(-1,-2,0,dp(5),0,0));
        content.addView(hero,new LinearLayout.LayoutParams(-1,0.0f==0?dp(174):dp(174)));

        LinearLayout actions=new LinearLayout(this);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        actions.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        TextView version=txt("  Vanilla   ⌄  ",15,TEXT,true);
        version.setGravity(Gravity.CENTER);
        version.setBackground(shape(PANEL2,24,Color.rgb(50,47,59)));
        LinearLayout.LayoutParams vp=new LinearLayout.LayoutParams(dp(155),dp(52));
        actions.addView(version,vp);
        TextView play=txt("▶    Play Minecraft",16,BG,true);
        play.setGravity(Gravity.CENTER);
        play.setBackground(shape(Color.rgb(190,187,199),24,Color.TRANSPARENT));
        LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(0,dp(52),1);
        pp.leftMargin=dp(10);
        actions.addView(play,pp);
        LinearLayout.LayoutParams actp=new LinearLayout.LayoutParams(-1,-2);
        actp.topMargin=dp(12);
        content.addView(actions,actp);
        play.setOnClickListener(v->toast("Game launching will be connected after the runtime setup."));
        version.setOnClickListener(v->toast("Minecraft version selector will be added."));

        TextView status=txt("Searching... You can switch tabs while results load.",13,MUTED,false);
        content.addView(status,margins(-1,-2,0,dp(12),0,0));
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