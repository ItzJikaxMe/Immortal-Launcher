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
        if(!"Play".equals(selected)) {
            content.addView(txt(selected,24,TEXT,true),margins(-1,-2,dp(10),dp(12),0,dp(10)));
            LinearLayout panel=card();
            panel.setGravity(Gravity.CENTER);
            panel.addView(txt("Your "+selected+" will appear here.",14,MUTED,false));
            content.addView(panel,new LinearLayout.LayoutParams(-1,dp(170)));
            return;
        }
        FrameLayout hero=new FrameLayout(this);
        hero.setBackground(shape(Color.rgb(20,19,27),22,Color.rgb(53,51,62)));
        EndermanArt art=new EndermanArt();
        hero.addView(art,new FrameLayout.LayoutParams(-1,-1));
        LinearLayout heroText=new LinearLayout(this);
        heroText.setOrientation(LinearLayout.VERTICAL);
        heroText.setGravity(Gravity.CENTER_VERTICAL);
        heroText.setPadding(dp(22),dp(15),dp(22),dp(15));
        heroText.addView(txt("Ready to play",27,TEXT,true));
        heroText.addView(txt("Minecraft Java  •  Vanilla / compatibility",14,MUTED,false),
            margins(-1,-2,0,dp(5),0,0));
        hero.addView(heroText,new FrameLayout.LayoutParams(-1,-1));
        content.addView(hero,new LinearLayout.LayoutParams(-1,dp(174)));

        LinearLayout actions=new LinearLayout(this);
        actions.setGravity(Gravity.CENTER_VERTICAL);
        actions.setLayoutDirection(View.LAYOUT_DIRECTION_LTR);
        TextView play=txt("▶  Play Game",14,BG,true);
        play.setGravity(Gravity.CENTER);
        play.setBackground(shape(Color.rgb(190,187,199),22,Color.TRANSPARENT));
        LinearLayout.LayoutParams pp=new LinearLayout.LayoutParams(dp(132),dp(42));
        actions.addView(play,pp);
        TextView version=txt("  Vanilla   ⌄  ",15,TEXT,true);
        version.setGravity(Gravity.CENTER);
        version.setBackground(shape(PANEL2,22,Color.rgb(50,47,59)));
        LinearLayout.LayoutParams vp=new LinearLayout.LayoutParams(0,dp(42),1);
        vp.leftMargin=dp(10);
        actions.addView(version,vp);
        LinearLayout.LayoutParams actp=new LinearLayout.LayoutParams(-1,-2);
        actp.topMargin=dp(12);
        content.addView(actions,actp);
        play.setOnClickListener(v->toast("Game launching will be connected after the runtime setup."));
        version.setOnClickListener(v->showVersionPicker(version));

        TextView status=txt("Searching... You can switch tabs while results load.",13,MUTED,false);
        content.addView(status,margins(-1,-2,0,dp(12),0,0));
    }

    private void showVersionPicker(TextView button) {
        final String[] versions={"Latest release","1.21.11","1.21.10","1.21.8","1.20.6","1.20.4","Download another version..."};
        new android.app.AlertDialog.Builder(this)
            .setTitle("Select Minecraft version")
            .setItems(versions,(dialog,which)->{
                if(which==versions.length-1) {
                    toast("Version downloads will be connected next.");
                } else {
                    String chosen=versions[which];
                    button.setText("  "+chosen+"  ⌄  ");
                    toast(chosen+" selected");
                }
            }).show();
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
            rect(canvas,black,cx-unit*.90f,top+unit*1.55f,cx-unit*.56f,h*.96f);
            rect(canvas,black,cx+unit*.56f,top+unit*1.55f,cx+unit*.90f,h*.96f);
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