package com.carrozzeriadsp;

import android.app.Activity;
import android.media.audiofx.Equalizer;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Virtualizer;
import android.os.Bundle;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.*;
import android.graphics.drawable.GradientDrawable;
import java.util.Locale;

public class MainActivity extends Activity {
    private Equalizer eq;
    private BassBoost bass;
    private Virtualizer virtualizer;
    private TextView status;
    private LinearLayout bandList;
    private TextView bassValue, wideValue;
    private SeekBar bassBar, wideBar;
    private boolean effectsReady = false;
    private final int BG = Color.rgb(8,13,22), PANEL = Color.rgb(17,27,42), CYAN = Color.rgb(35,185,255), MUTED = Color.rgb(145,164,185);

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        buildUi(); initEffects();
    }
    private GradientDrawable shape(int color, int radius) { GradientDrawable d = new GradientDrawable(); d.setColor(color); d.setCornerRadius(radius); return d; }
    private TextView text(String s, int size, int color, boolean bold) { TextView t = new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(color); if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD); return t; }
    private void buildUi() {
        ScrollView scroll = new ScrollView(this); scroll.setFillViewport(true); scroll.setBackgroundColor(BG);
        LinearLayout root = new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(24,18,24,18); scroll.addView(root);
        LinearLayout header = new LinearLayout(this); header.setGravity(Gravity.CENTER_VERTICAL); header.setOrientation(LinearLayout.HORIZONTAL);
        TextView brand = text("CARROZZERIA", 25, Color.WHITE, true); header.addView(brand);
        TextView sub = text("  DSP  /  0.1 PROTOTYPE", 12, CYAN, true); header.addView(sub);
        root.addView(header);
        status = text("AUDIO ENGINE: INITIALIZING…", 12, MUTED, true); status.setPadding(0,8,0,14); root.addView(status);
        LinearLayout row = new LinearLayout(this); row.setOrientation(LinearLayout.HORIZONTAL); root.addView(row, new LinearLayout.LayoutParams(-1,-2));
        LinearLayout eqPanel = panel(); row.addView(eqPanel, new LinearLayout.LayoutParams(0,-2, 2.2f));
        LinearLayout side = panel(); LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(0,-2,1); sp.leftMargin=14; row.addView(side,sp);
        TextView eqTitle = text("EQUALIZER",16,Color.WHITE,true); eqPanel.addView(eqTitle);
        TextView eqInfo = text("Device-supported frequency bands",11,MUTED,false); eqInfo.setPadding(0,4,0,12); eqPanel.addView(eqInfo);
        bandList = new LinearLayout(this); bandList.setOrientation(LinearLayout.VERTICAL); eqPanel.addView(bandList);
        TextView bassTitle = text("BASS ENHANCER",15,Color.WHITE,true); side.addView(bassTitle);
        bassValue = text("Strength  0%",12,CYAN,true); bassValue.setPadding(0,16,0,2); side.addView(bassValue);
        bassBar = new SeekBar(this); bassBar.setMax(100); side.addView(bassBar); bassBar.setOnSeekBarChangeListener(new SimpleSeek(v -> { bassValue.setText("Strength  "+v+"%"); if(bass!=null) try { bass.setStrength((short)(v*10)); } catch(Exception ignored){} }));
        TextView wideTitle = text("STEREO WIDTH",15,Color.WHITE,true); wideTitle.setPadding(0,22,0,0); side.addView(wideTitle);
        wideValue = text("Width  0%",12,CYAN,true); wideValue.setPadding(0,10,0,2); side.addView(wideValue);
        wideBar = new SeekBar(this); wideBar.setMax(100); side.addView(wideBar); wideBar.setOnSeekBarChangeListener(new SimpleSeek(v -> { wideValue.setText("Width  "+v+"%"); if(virtualizer!=null) try { virtualizer.setStrength((short)(v*10)); } catch(Exception ignored){} }));
        TextView presets = text("QUICK SOUND PROFILES",15,Color.WHITE,true); presets.setPadding(0,18,0,10); root.addView(presets);
        LinearLayout buttons = new LinearLayout(this); buttons.setOrientation(LinearLayout.HORIZONTAL); root.addView(buttons);
        addPreset(buttons,"FLAT",new int[]{0,0,0,0,0}); addPreset(buttons,"DEEP BASS",new int[]{4,3,0,-1,1}); addPreset(buttons,"SOFT",new int[]{2,1,0,1,2}); addPreset(buttons,"VOCAL",new int[]{-2,0,3,3,1});
        TextView note = text("Note: global output effects depend on the head unit firmware. Factory DSP / radio / Bluetooth paths may bypass Android AudioEffect.",11,MUTED,false); note.setPadding(0,16,0,0); root.addView(note);
        setContentView(scroll);
    }
    private LinearLayout panel(){ LinearLayout l=new LinearLayout(this); l.setOrientation(LinearLayout.VERTICAL); l.setPadding(16,16,16,16); l.setBackground(shape(PANEL,18)); return l; }
    private void addPreset(LinearLayout row,String name,int[] values){ Button b=new Button(this); b.setText(name); b.setTextSize(11); b.setTextColor(Color.WHITE); b.setAllCaps(false); b.setBackground(shape(Color.rgb(25,55,82),14)); LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,58,1); p.setMargins(3,0,3,0); row.addView(b,p); b.setOnClickListener(v->{ if(eq!=null && eq.getNumberOfBands()>=5){ short n=eq.getNumberOfBands(); for(short i=0;i<n;i++){ int idx=Math.min(4,Math.round(i*4f/Math.max(1,n-1))); setBand(i,values[idx]); } } }); }
    private void setBand(short band,int db){ if(eq==null)return; try { short[] r=eq.getBandLevelRange(); int level=Math.max(r[0],Math.min(r[1],db*100)); eq.setBandLevel(band,(short)level); } catch(Exception ignored){} }
    private void initEffects(){
        try {
            // Session 0 requests an output-mix effect. Device firmware may reject it or limit it to selected streams.
            eq = new Equalizer(0,0); eq.setEnabled(true); effectsReady=true;
            short count=eq.getNumberOfBands(); short[] range=eq.getBandLevelRange();
            for(short i=0;i<count;i++){ final short band=i; int center=eq.getCenterFreq(i)/1000; String label=center>=1000?String.format(Locale.US,"%.1f kHz",center/1000f):center+" Hz";
                LinearLayout line=new LinearLayout(this); line.setGravity(Gravity.CENTER_VERTICAL); line.setPadding(0,2,0,2);
                TextView name=text(label,12,MUTED,true); line.addView(name,new LinearLayout.LayoutParams(78,-2));
                SeekBar bar=new SeekBar(this); bar.setMax(range[1]-range[0]); bar.setProgress(eq.getBandLevel(i)-range[0]); line.addView(bar,new LinearLayout.LayoutParams(0,42,1));
                TextView val=text("0.0",11,CYAN,true); val.setGravity(Gravity.RIGHT|Gravity.CENTER_VERTICAL); line.addView(val,new LinearLayout.LayoutParams(44,-2));
                bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener(){ public void onStartTrackingTouch(SeekBar s){} public void onStopTrackingTouch(SeekBar s){} public void onProgressChanged(SeekBar s,int p,boolean user){ short db=(short)(p+range[0]); val.setText(String.format(Locale.US,"%.1f",db/100f)); if(user)setBand(band,db/100); }});
                bandList.addView(line);
            }
        } catch(Exception e){ status.setText("AUDIO ENGINE: GLOBAL EQ NOT AVAILABLE ON THIS FIRMWARE"); status.setTextColor(0xFFFFB36B); }
        try { bass=new BassBoost(0,0); bass.setEnabled(true); } catch(Exception ignored){ bass=null; }
        try { virtualizer=new Virtualizer(0,0); virtualizer.setEnabled(true); } catch(Exception ignored){ virtualizer=null; }
        if(effectsReady){ status.setText("AUDIO ENGINE: OUTPUT EQ CONNECTED · "+eq.getNumberOfBands()+" HARDWARE BANDS"); status.setTextColor(0xFF65DDAA); }
    }
    @Override protected void onDestroy(){ try{if(eq!=null)eq.release();}catch(Exception ignored){} try{if(bass!=null)bass.release();}catch(Exception ignored){} try{if(virtualizer!=null)virtualizer.release();}catch(Exception ignored){} super.onDestroy(); }
    private interface Change { void changed(int v); }
    private static class SimpleSeek implements SeekBar.OnSeekBarChangeListener { Change c; SimpleSeek(Change c){this.c=c;} public void onProgressChanged(SeekBar s,int v,boolean u){if(u)c.changed(v);} public void onStartTrackingTouch(SeekBar s){} public void onStopTrackingTouch(SeekBar s){} }
}
