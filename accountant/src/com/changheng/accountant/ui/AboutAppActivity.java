package com.changheng.accountant.ui;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import com.changheng.accountant.R;


public class AboutAppActivity extends BaseActivity{
	private TextView content;
//	private WebView webView;
//	private RelativeLayout loading;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ui_about);
		initView();
	}
	private void initView()
	{
		((TextView) findViewById(R.id.title)).setText("隐私协议");
		findViewById(R.id.btn_goback).setOnClickListener(new ReturnBtnClickListener(this));
		content = (TextView) findViewById(R.id.contentTv);
		content.setText(getFromAssets("privacy.txt").replaceAll("COMPANY_NAME", getResources().getString(R.string.company_name)));
//		webView = (WebView) findViewById(R.id.web_view);
//		loading = (RelativeLayout) findViewById(R.id.loading);
//		webView.getSettings().setJavaScriptEnabled(true);
//		webView.setWebViewClient(new SampleWebViewClient());
//		webView.loadUrl(getIntent().getStringExtra("url"));
	}
	private String getFromAssets(String fileName){ 
        try { 
             InputStreamReader inputReader = new InputStreamReader( getResources().getAssets().open(fileName) ); 
            BufferedReader bufReader = new BufferedReader(inputReader);
            String line="";
            String Result="";
            while((line = bufReader.readLine()) != null)
                Result += (line+"\n");
            return Result;
        } catch (Exception e) { 
            e.printStackTrace(); 
            return null;
        }
} 
//	private class SampleWebViewClient extends WebViewClient {
//		@Override
//		public boolean shouldOverrideUrlLoading(WebView view, String url) {
//			if ( url.contains("file:///") == true){
//	            view.loadUrl(url);
//	            return true;
//	        }else{
//	            Intent in = new Intent (Intent.ACTION_VIEW , Uri.parse(url));
//	            startActivity(in);
//	            return true;
//	        }
//		}
//		@Override
//		public void onPageFinished(WebView view, String url) {
//				// TODO Auto-generated method stub
//				loading.setVisibility(View.GONE);
//				super.onPageFinished(view, url);
//			}
//	}
}
