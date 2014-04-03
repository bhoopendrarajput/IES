package com.ies;

import java.io.File;
import java.io.FileInputStream;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	File parent = new File(Environment.getExternalStorageDirectory(),"IES");

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		TextView tv = (TextView) findViewById(R.id.tv);
		StringBuffer msg=new StringBuffer( "File does not exist" ); 
		if (parent.exists()) {
			File files[] = parent.listFiles();
			if (files!=null  && files.length!=0) {
				File file = files[0];
				byte [] buff = new byte[99999];
				try{
				FileInputStream fis = new FileInputStream(file);
				int size=0;
				msg.delete(0, msg.length());
				while((size=fis.read(buff))>-1){
					msg.append(new String(buff,0,size));
				}
				}catch(Exception e){
					e.printStackTrace();
					msg.delete(0, msg.length()).append(e.getMessage());
				}
			}
		}else {
			parent.mkdirs();
			Toast.makeText(this, parent.getAbsolutePath(), 1).show();
		}
		tv.setText(msg.toString());
	}
}
