package com.changheng.accountant.ui;

import android.os.Bundle;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.changheng.accountant.R;
import com.handmark.pulltorefresh.library.PullToRefreshWebView;

public class KnowledgeDetailActivity extends BaseActivity{
	private PullToRefreshWebView refreshWebView;
	private WebView webView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.ui_knowledge_detail);
		refreshWebView = (PullToRefreshWebView) this.findViewById(R.id.webview);
		webView = refreshWebView.getRefreshableView();
		webView.getSettings().setJavaScriptEnabled(true);
		webView.setWebViewClient(new SampleWebViewClient());
		webView.loadUrl("file:///android_asset/html/4.html");
	}
	private static class SampleWebViewClient extends WebViewClient {
		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			view.loadUrl(url);
			return true;
		}
	}
}
