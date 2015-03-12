package com.changheng.accountant.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.RelativeLayout;

import com.changheng.accountant.R;

public class AboutusFragment extends Fragment{
	private WebView webView;
	private RelativeLayout loading;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		View v = inflater.inflate(R.layout.aboutus_fragment, null);
		webView = (WebView) v.findViewById(R.id.web_view);
		loading = (RelativeLayout) v.findViewById(R.id.loading);
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new SampleWebViewClient());
		webView.loadUrl("file:///android_asset/other/about.html");
		return v;
	}
	private class SampleWebViewClient extends WebViewClient {
	@Override
	public boolean shouldOverrideUrlLoading(WebView view, String url) {
		if ( url.contains("file:///") == true){
            view.loadUrl(url);
            return true;
        }else{
            Intent in = new Intent (Intent.ACTION_VIEW , Uri.parse(url));
            startActivity(in);
            return true;
        }
	}
	@Override
	public void onPageFinished(WebView view, String url) {
			// TODO Auto-generated method stub
			loading.setVisibility(View.GONE);
			super.onPageFinished(view, url);
		}
}
}
