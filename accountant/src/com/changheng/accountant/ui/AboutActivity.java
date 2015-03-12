package com.changheng.accountant.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.changheng.accountant.R;

public class AboutActivity extends BaseActivity implements OnClickListener{
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.ui_about_app);
		((TextView)findViewById(R.id.title)).setText("关	   于");
		findViewById(R.id.btn_goback).setOnClickListener(this);
		findViewById(R.id.website1).setOnClickListener(this);
		findViewById(R.id.website2).setOnClickListener(this);
		findViewById(R.id.telephone).setOnClickListener(this);
	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch(v.getId())
		{
		case R.id.btn_goback:
			this.finish();
			break;
		case R.id.website1:
			visitWebsite(Intent.ACTION_VIEW,"http://www.cyedu.org");
			break;
		case R.id.website2:
			visitWebsite(Intent.ACTION_VIEW,"http://www.examw.com");
			break;
		case R.id.telephone:
//			visitWebsite(Intent.ACTION_CALL,"tel:4000525585");
			break;
		}
	}
	private void visitWebsite(String action,String url)
	{
		 Uri uri = Uri.parse(url);          
	     Intent it = new Intent(action, uri);
	     startActivity(it);
	}
}
