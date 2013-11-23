package com.Prisekin.OnlineRadio;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.view.Menu;
import android.widget.Toast;
import android.media.MediaPlayer;
import android.view.View;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.ProgressBar;
import android.app.Service;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
//import android.content.Context;
import android.content.pm.ActivityInfo;
import android.widget.Button;
import android.content.Intent;
import android.widget.RelativeLayout;
import android.content.SharedPreferences;
import android.media.MediaRecorder;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;

public class OnlineRadio extends Activity implements MediaPlayer.OnPreparedListener, 
AdapterView.OnItemClickListener{
	MediaPlayer player; String[] radio_names; String[] radio_urls; ListView radio_list;
	TextView current_station; ProgressBar progress_bar;
	Button start_stop,record_button;
	WakeLock wake_lock;
	RelativeLayout main_layout;
	static final String WAIT="Wait",START="Start",STOP="Stop";
	SharedPreferences settings;
    byte[] buffer=new byte[512*1024];	InputStream radio_input_stream;
    File work_dir,out_file;
    FileOutputStream fos;
    static final int INIT_CONNECTION=0,WRITE_STREAM=1;
    static boolean write_status=false;
    String current_station_url;
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
        record_button=(Button)findViewById(R.id.record_button);
        radio_names=getResources().getStringArray(R.array.radio_names);
        radio_urls=getResources().getStringArray(R.array.radio_urls);
        current_station.setText(radio_names[0]);	//station number
        player=new MediaPlayer();
        
        current_station_url=radio_urls[0];
        work_dir=new File(Environment.getExternalStorageDirectory(),"OnlineRadio");
        if(!work_dir.exists()){work_dir.mkdir();}


        try{ player.setDataSource(radio_urls[0]);	//station number
        player.setOnPreparedListener(this);
        player.prepareAsync();
        }
        catch(Exception e){
        	AlertDialog.Builder build=new AlertDialog.Builder(this);
        	build.setTitle("Prepare status:");
        	build.setMessage("Problem: "+e.toString());
        	build.show();
        	}
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,radio_names);
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
    	write_status=false;
    	try{radio_input_stream.close(); fos.close();}catch(Exception e){}
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
	public void StartStopRecording(View v){
		write_status=!write_status; 
		if(write_status){ 
	        int file_counter=0;
	        while(new File(work_dir,"radio"+file_counter+".mp3").exists()){++file_counter;}
	        out_file=new File(work_dir,"radio"+file_counter+".mp3");
	        try{out_file.createNewFile();
	        fos=new FileOutputStream(out_file);
	        new Backgr().execute(INIT_CONNECTION);
	        } catch(Exception e){android.util.Log.e("File: ",e.toString());}
			record_button.setText("StopRec"); new Backgr().execute(WRITE_STREAM);}
		else{record_button.setText("Record");}
	}
	public void Setup(View v){
    	startActivity(new Intent(this,Setup.class));
    }
    @Override
    public void onItemClick(AdapterView<?> a,View v,int position,long l){
    	player.stop();
    	player.reset();
    	progress_bar.setVisibility(View.VISIBLE);
    	try{
    	current_station_url=radio_urls[position];
    	player.setDataSource(current_station_url);}
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
    class Backgr extends AsyncTask<Integer,Void,Void>{
    	@Override
    	public Void doInBackground(Integer... args){
    		if(args[0]==INIT_CONNECTION){
    		try{
    		URL url=new URL(current_station_url); 
            URLConnection url_conn=url.openConnection();
            radio_input_stream=new BufferedInputStream(url_conn.getInputStream());
            android.util.Log.e("Open","All OK");
        } catch(Exception e){android.util.Log.e("File: ",e.toString());}
    	}
    	if(args[0]==WRITE_STREAM){
    		while(write_status){
    		try{int length=radio_input_stream.available();
    		radio_input_stream.read(buffer,0,length-1);
    		fos.write(buffer,0,length-1); 
    		//android.util.Log.e("Write","All OK");
    		}
    		catch(Exception e){android.util.Log.e("Write",e.toString());}}
    		try{radio_input_stream.close(); fos.close();}catch(Exception e){}
    		}
    		try{ fos.flush(); }catch(Exception e){android.util.Log.e("Write",e.toString());}
			return null;
    		
    	}
    }
}
