package com.Prisekin.OnlineRadio;

import android.app.Activity;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.view.View;

public class Setup extends Activity implements OnSeekBarChangeListener,OnItemSelectedListener{
	RelativeLayout setup_lay,frame_lay; TextView current_radio,radio1,radio2,radio3;
	Spinner setup_selector; SeekBar red_seek,green_seek,blue_seek;
	SharedPreferences settings; SharedPreferences.Editor edit;
	int setup_background,main_background,current_station_color,radio_list_background;
	static final int SETUP_COLOR=0xff5f5f5f,BACKGROUND_COLOR=0xff000000;
	static final int CURRENT_STATION_COLOR=0xff00cf00,RADIO_LIST_BACKGROUND=0xff003f7f;
	int setup_selection;	//selected in Spinner position of field to setup
@Override
public void onCreate(Bundle bund){
	super.onCreate(bund);
	setContentView(R.layout.setup);
	settings=getSharedPreferences("settings",MODE_PRIVATE);
	edit=settings.edit();
	setup_background=settings.getInt("setup_background",SETUP_COLOR);
	main_background=settings.getInt("main_background",BACKGROUND_COLOR);
	current_station_color=settings.getInt("current_station_color",CURRENT_STATION_COLOR);
	radio_list_background=settings.getInt("radio_list_background",RADIO_LIST_BACKGROUND);
	
	setup_selector=(Spinner)findViewById(R.id.setting_selector);
	setup_lay=(RelativeLayout)findViewById(R.id.setup);
	setup_lay.setBackgroundColor(setup_background);
	frame_lay=(RelativeLayout)findViewById(R.id.frame);
	frame_lay.setBackgroundColor(main_background);
	current_radio=(TextView)findViewById(R.id.current_radio);
	current_radio.setTextColor(current_station_color);
	radio1=(TextView)findViewById(R.id.radio1);
	radio1.setBackgroundColor(radio_list_background);
	radio2=(TextView)findViewById(R.id.radio2);
	radio2.setBackgroundColor(radio_list_background);
	radio3=(TextView)findViewById(R.id.radio3);
	radio3.setBackgroundColor(radio_list_background);
	red_seek=(SeekBar)findViewById(R.id.red_seek);
	red_seek.setBackgroundColor(0xff7f0000);
	red_seek.setMax(0xff);
	red_seek.setOnSeekBarChangeListener(this);
	green_seek=(SeekBar)findViewById(R.id.green_seek);
	green_seek.setBackgroundColor(0xff007f00);
	green_seek.setMax(0xff);
	green_seek.setOnSeekBarChangeListener(this);
	blue_seek=(SeekBar)findViewById(R.id.blue_seek);
	blue_seek.setBackgroundColor(0xff00007f);
	blue_seek.setMax(0xff);
	blue_seek.setOnSeekBarChangeListener(this);
	ArrayAdapter<CharSequence> setup_selector_adapter=ArrayAdapter.createFromResource(this,R.array.setup_options,android.R.layout.simple_spinner_item);
	setup_selector_adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
	setup_selector.setAdapter(setup_selector_adapter);
	setup_selector.setOnItemSelectedListener(this);
}
@Override
public void onPause(){
	edit.putInt("setup_background",setup_background);
	edit.putInt("main_background",main_background);
	edit.putInt("current_station_color",current_station_color);
	edit.putInt("radio_list_background",radio_list_background);
	edit.commit();
	super.onPause();
}
@Override
public void onItemSelected(AdapterView<?> a,View v,int pos,long l){
	setup_selection=pos; int color4seekbar=0;
	switch(pos){
	case 0: color4seekbar=settings.getInt("setup_background",SETUP_COLOR); break;
	case 1: color4seekbar=settings.getInt("main_background",BACKGROUND_COLOR); break;
	case 2: color4seekbar=settings.getInt("current_station_color",CURRENT_STATION_COLOR); break;
	case 3: color4seekbar=settings.getInt("radio_list_background",RADIO_LIST_BACKGROUND); break;
	}
	red_seek.setProgress((color4seekbar>>16)&0xff);
	green_seek.setProgress((color4seekbar>>8)&0xff);
	blue_seek.setProgress(color4seekbar&0xff);
}
@Override
public void onNothingSelected(AdapterView<?> a){}

@Override
public void onProgressChanged(SeekBar seek,int value,boolean b){
	int color=0xff000000|(red_seek.getProgress()<<16)|(green_seek.getProgress()<<8)|(blue_seek.getProgress());
	switch(setup_selection){
	case 0: setup_lay.setBackgroundColor(color); setup_background=color; break;
	case 1: frame_lay.setBackgroundColor(color); main_background=color; break;
	case 2: current_radio.setTextColor(color); current_station_color=color; break;
	case 3: radio1.setBackgroundColor(color); radio2.setBackgroundColor(color);
			radio3.setBackgroundColor(color); radio_list_background=color; break;
	}
}
@Override
public void onStartTrackingTouch(SeekBar seek){}
@Override
public void onStopTrackingTouch(SeekBar seek){}
}
