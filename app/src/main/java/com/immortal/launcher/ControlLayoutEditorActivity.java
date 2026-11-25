package com.immortal.launcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;

/** Immortal's editable touch-layout designer. Layouts are saved independently of the game engine. */
public class ControlLayoutEditorActivity extends Activity {
    private final ArrayList<Control> controls = new ArrayList<>();
    private EditorView editor;
    private int selected = 0;
    private static final String PREFS = "immortal_controls";
    private static final String KEY = "layout_v1";
    private final int bg = Color.rgb(12,12,16), panel = Color.rgb(28,27,34);
    private final int accent = Color.rgb(151,111,235), text = Color.rgb(239,237,245);

    private static class Control {
        String label, kind;
        float x,y,w,h;
        Control(String label,String kind,float x,float y,float w,float h) {
            this.label=label;this.kind=kind;this.x=x;this.y=y;this.w=w;this.h=h;
        }
    }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(bg);
        getWindow().setNavigationBarColor(bg);
        load();
        if(controls.isEmpty()) preset();
        buildUi();
    }

    private int dp(float n) { return (int)(n*getResources().getDisplayMetrics().density+.5f); }
    private TextView label(String value,int size,boolean bold) {
        TextView v=new TextView(this);v.setText(value);v.setTextSize(size);v.setTextColor(text);
        if(bold)v.setTypeface(android.graphics.Typeface.DEFAULT,android.graphics.Typeface.BOLD);
        return v;
    }
    private Button action(String value,Runnable run) {
        Button b=new Button(this);b.setText(value);b.setTextSize(11);b.setAllCaps(false);
        b.setTextColor(text);b.setOnClickListener(v->run.run());return b;
    }

    private void buildUi() {
        LinearLayout root=new LinearLayout(this);root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(12),dp(5),dp(12),dp(7));root.setBackgroundColor(bg);
        LinearLayout top=new LinearLayout(this);top.setGravity(Gravity.CENTER_VERTICAL);
        TextView title=label("‹   Immortal Controls",19,true);
        top.addView(title,new LinearLayout.LayoutParams(0,dp(48),1));
        title.setOnClickListener(v->finish());
        top.addView(action("Add",this::addControl));
        top.addView(action("Presets",this::showPresets));
        top.addView(action("Reset",()->new AlertDialog.Builder(this).setTitle("Reset layout?")
            .setMessage("Restore the default Immortal touch controls?")
            .setNegativeButton("Cancel",null).setPositiveButton("Reset",(d,w)->{preset();editor.invalidate();}).show()));
        top.addView(action("Save",this::save));
        root.addView(top,new LinearLayout.LayoutParams(-1,dp(50)));
        editor=new EditorView();
        LinearLayout.LayoutParams ep=new LinearLayout.LayoutParams(-1,0,1);
        ep.topMargin=dp(5);ep.bottomMargin=dp(5);root.addView(editor,ep);
        LinearLayout bottom=new LinearLayout(this);bottom.setGravity(Gravity.CENTER_VERTICAL);
        TextView hint=label("Drag to move  •  Drag the small corner to resize  •  Select then Remove",10,false);
        bottom.addView(hint,new LinearLayout.LayoutParams(0,dp(34),1));
        bottom.addView(action("Remove selected",()->{
            if(selected>=0&&selected<controls.size()){controls.remove(selected);selected=Math.min(selected,controls.size()-1);editor.invalidate();}
        }));
        root.addView(bottom,new LinearLayout.LayoutParams(-1,dp(38)));
        setContentView(root);
    }

    private void preset() {
        controls.clear();
        controls.add(new Control("MOVE","joystick",.13f,.70f,.19f,.25f));
        controls.add(new Control("JUMP","button",.86f,.70f,.10f,.13f));
        controls.add(new Control("SNEAK","button",.75f,.84f,.09f,.10f));
        controls.add(new Control("ATTACK","button",.88f,.47f,.11f,.15f));
        controls.add(new Control("USE","button",.74f,.59f,.10f,.13f));
        controls.add(new Control("INVENTORY","button",.92f,.12f,.07f,.09f));
        controls.add(new Control("CHAT","button",.08f,.12f,.07f,.09f));
        controls.add(new Control("PAUSE","button",.50f,.08f,.08f,.09f));
        selected=0;
    }

    private void addControl() {
        final String[] names={"Jump","Sneak","Attack","Use","Inventory","Chat","Pause","Sprint","Drop","Swap hand","Joystick"};
        new AlertDialog.Builder(this).setTitle("Add control").setItems(names,(d,which)->{
            String name=names[which].toUpperCase();
            String kind="Joystick".equals(names[which])?"joystick":"button";
            controls.add(new Control(name,kind,.50f,.50f,.10f,.12f));
            selected=controls.size()-1;editor.invalidate();
        }).show();
    }

    private void showPresets() {
        final String[] names={"Default survival","Compact touch","Right-handed","Left-handed"};
        new AlertDialog.Builder(this).setTitle("Choose layout preset").setItems(names,(d,w)->{
            preset();
            if(w==1) for(Control c:controls){c.w*=.78f;c.h*=.78f;}
            if(w==2) { for(Control c:controls) if(c.label.equals("MOVE")) c.x=.13f; else if(c.x>.5f)c.x=.88f; }
            if(w==3) { for(Control c:controls) c.x=1f-c.x-c.w; }
            selected=0;editor.invalidate();
        }).show();
    }

    private void save() {
        try {
            JSONArray array=new JSONArray();
            for(Control c:controls) {
                JSONObject o=new JSONObject();o.put("label",c.label);o.put("kind",c.kind);
                o.put("x",c.x);o.put("y",c.y);o.put("w",c.w);o.put("h",c.h);array.put(o);
            }
            getSharedPreferences(PREFS,MODE_PRIVATE).edit().putString(KEY,array.toString()).apply();
            Toast.makeText(this,"Immortal control layout saved",Toast.LENGTH_SHORT).show();
        } catch(Exception e) { Toast.makeText(this,"Could not save layout",Toast.LENGTH_SHORT).show(); }
    }

    private void load() {
        try {
            String raw=getSharedPreferences(PREFS,MODE_PRIVATE).getString(KEY,"");
            JSONArray a=new JSONArray(raw);
            for(int i=0;i<a.length();i++) {
                JSONObject o=a.getJSONObject(i);
                controls.add(new Control(o.optString("label","BUTTON"),o.optString("kind","button"),
                    clamp((float)o.optDouble("x",.5)),clamp((float)o.optDouble("y",.5)),
                    clampSize((float)o.optDouble("w",.1)),clampSize((float)o.optDouble("h",.1))));
            }
        } catch(Exception ignored) { controls.clear(); }
    }
    private float clamp(float v){return Math.max(0,Math.min(.98f,v));}
    private float clampSize(float v){return Math.max(.045f,Math.min(.32f,v));}

    private class EditorView extends View {
        private final Paint p=new Paint(Paint.ANTI_ALIAS_FLAG);
        private float downX,downY,startX,startY,startW,startH;
        private boolean resizing=false,moving=false;
        EditorView(){super(ControlLayoutEditorActivity.this);setLayerType(View.LAYER_TYPE_SOFTWARE,null);}
        private RectF bounds(Control c) {
            float w=getWidth(),h=getHeight();
            return new RectF(c.x*w,c.y*h,(c.x+c.w)*w,(c.y+c.h)*h);
        }
        private void text(Canvas c,String s,float x,float y,float size,int color) {
            p.setColor(color);p.setTextSize(size);p.setTypeface(android.graphics.Typeface.DEFAULT_BOLD);
            p.setTextAlign(Paint.Align.CENTER);c.drawText(s,x,y,p);
        }
        @Override protected void onDraw(Canvas c) {
            super.onDraw(c);float w=getWidth(),h=getHeight();
            c.drawColor(Color.rgb(19,18,25));
            p.setColor(Color.rgb(36,34,44));p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(dp(1));
            for(int x=1;x<10;x++)c.drawLine(w*x/10f,0,w*x/10f,h,p);
            for(int y=1;y<7;y++)c.drawLine(0,h*y/7f,w,h*y/7f,p);
            p.setStyle(Paint.Style.FILL);
            // Subtle game-screen frame
            p.setColor(Color.rgb(24,23,31));c.drawRoundRect(dp(8),dp(8),w-dp(8),h-dp(8),dp(18),dp(18),p);
            text(c,"GAME SCREEN PREVIEW",w/2,dp(30),dp(10),Color.rgb(115,111,128));
            for(int i=0;i<controls.size();i++) {
                Control item=controls.get(i);RectF r=bounds(item);boolean active=i==selected;
                p.setColor(active?Color.argb(205,151,111,235):Color.argb(145,65,61,78));
                p.setStyle(Paint.Style.FILL);c.drawRoundRect(r,dp(13),dp(13),p);
                p.setColor(active?Color.WHITE:Color.rgb(210,207,220));p.setStyle(Paint.Style.STROKE);
                p.setStrokeWidth(active?dp(2):dp(1));c.drawRoundRect(r,dp(13),dp(13),p);
                p.setStyle(Paint.Style.FILL);
                if("joystick".equals(item.kind)) {
                    p.setColor(Color.argb(80,255,255,255));c.drawCircle(r.centerX(),r.centerY(),Math.min(r.width(),r.height())*.31f,p);
                    text(c,"MOVE",r.centerX(),r.centerY()+dp(4),dp(10),Color.WHITE);
                } else {
                    String t=item.label.length()>10?item.label.substring(0,9):item.label;
                    text(c,t,r.centerX(),r.centerY()+dp(4),Math.max(dp(8),Math.min(dp(12),r.width()*.15f)),Color.WHITE);
                }
                if(active) {
                    p.setColor(Color.WHITE);c.drawCircle(r.right,r.bottom,dp(7),p);
                    p.setColor(accent);c.drawCircle(r.right,r.bottom,dp(4),p);
                }
            }
        }
        @Override public boolean onTouchEvent(MotionEvent e) {
            float x=e.getX(),y=e.getY();
            if(e.getAction()==MotionEvent.ACTION_DOWN) {
                downX=x;downY=y;selected=-1;
                for(int i=controls.size()-1;i>=0;i--) {
                    RectF r=bounds(controls.get(i));
                    if(r.contains(x,y)){selected=i;resizing=(x>r.right-dp(18)&&y>r.bottom-dp(18));moving=!resizing;break;}
                }
                if(selected>=0) {
                    Control item=controls.get(selected);startX=item.x;startY=item.y;startW=item.w;startH=item.h;
                }
                invalidate();return true;
            }
            if(e.getAction()==MotionEvent.ACTION_MOVE&&selected>=0) {
                Control item=controls.get(selected);
                float dx=(x-downX)/Math.max(1,getWidth()),dy=(y-downY)/Math.max(1,getHeight());
                if(resizing){item.w=clampSize(startW+dx);item.h=clampSize(startH+dy);}
                else if(moving){item.x=Math.max(0,Math.min(1-item.w,startX+dx));item.y=Math.max(0,Math.min(1-item.h,startY+dy));}
                invalidate();return true;
            }
            if(e.getAction()==MotionEvent.ACTION_UP){moving=false;resizing=false;performClick();return true;}
            return true;
        }
        @Override public boolean performClick(){super.performClick();return true;}
    }
}
