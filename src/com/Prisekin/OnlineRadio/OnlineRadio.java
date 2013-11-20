package com.Prisekin.OnlineRadio;

import android.os.Build;
import android.os.Bundle;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.app.AlertDialog;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.Toast;
import android.media.MediaPlayer;
import android.view.View;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.widget.LinearLayout;
import android.app.Service;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.widget.Button;
import android.content.Intent;
import android.widget.RelativeLayout;
import android.content.SharedPreferences;

public class OnlineRadio extends Activity implements MediaPlayer.OnPreparedListener, 
AdapterView.OnItemClickListener{
	MediaPlayer player; String[] radio_names; String[] radio_urls; ListView radio_list;
	TextView current_station; ProgressBar progress_bar;  Button start_stop;
	WakeLock wake_lock;
	RelativeLayout main_layout;
	static final String WAIT="Wait",START="Start",STOP="Stop";
	SharedPreferences settings;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTheme(android.R.style.Theme_Black_NoTitleBar);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.online_radio);
        PowerManager power_manager=(PowerManager)getSystemService(Service.POWER_SERVICE);
        wake_lock=power_manager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK|PowerManager.ON_AFTER_RELEASE,"OnlineRadio");
        progress_bar=(ProgressBar)findViewById(R.id.progress_bar);
        progress_bar.setVisibility(View.VISIBLE);

        main_layout=(RelativeLayout)findViewById(R.id.layout);
        current_station=(TextView)findViewById(R.id.current_station);
        radio_list=(ListView)findViewById(R.id.radio_list);
        
        
        start_stop=(Button)findViewById(R.id.start_stop_button);
        radio_names=getResources().getStringArray(R.array.radio_names);
        radio_urls=getResources().getStringArray(R.array.radio_urls);
        current_station.setText(radio_names[0]);	//station number
        player=new MediaPlayer();
        try{ player.setDataSource(radio_urls[0]);	//station number
        player.setOnPreparedListener(this);
        player.prepareAsync();}
        catch(Exception e){
        	AlertDialog.Builder build=new AlertDialog.Builder(this);
        	build.setTitle("Prepare status:");
        	build.setMessage("Problem: "+e.toString());
        	build.show();
        	}
        ArrayAdapter<String> adapter=new ArrayAdapter(this,android.R.layout.simple_list_item_1,radio_names);
        radio_list.setAdapter(adapter);
        radio_list.setOnItemClickListener(this);
    }
    @Override
    public void onStart(){
    	super.onStart();
        settings=getSharedPreferences("settings",MODE_PRIVATE);
        main_layout.setBackgroundColor(settings.getInt("main_background",0xff000000));
        current_station.setTextColor(settings.getInt("current_station_color",0xff00cf00));
        radio_list.setBackgroundColor(settings.getInt("radio_list_background",0xff003f7f));
    	wake_lock.acquire();
    }
    @Override
    public void onStop(){
    	wake_lock.release();
    	super.onStop();
    }
    @Override
    public void onDestroy(){
    	if(player.isPlaying()){player.stop();}
    	player.release();
    	super.onDestroy();
    }
    @Override
    public void onPrepared(MediaPlayer media_player){
    	media_player.start();
    	progress_bar.setVisibility(View.GONE);
    	start_stop.setText(STOP);
    }
    public void StartStopPlaying(View v){
    	if(start_stop.getText()==START){player.start(); start_stop.setText(STOP);}
    	else{
    	if(start_stop.getText()==STOP){player.pause(); start_stop.setText(START);}}
    }
	public void Setup(View v){
    	startActivity(new Intent(this,Setup.class));
    }
    @Override
    public void onItemClick(AdapterView<?> a,View v,int position,long l){
    	player.stop();
    	player.reset();
    	progress_bar.setVisibility(View.VISIBLE);
    	try{player.setDataSource(radio_urls[position]);}
    	catch(Exception e){Toast.makeText(this,e.toString(),Toast.LENGTH_LONG).show();}
    	start_stop.setText(WAIT);
    	player.prepareAsync();
    	current_station.setText(radio_names[position]);
    	//this.setTitle(radio_names[position]);
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.online_radio, menu);
        return true;
    }
}
