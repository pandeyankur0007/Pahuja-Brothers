package com.mobile.pahujabrothers;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.mobile.pahujabrothers.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements ErrorFragment.OnFragmentInteractionListener {

    private ActivityMainBinding activityMainBinding;
    private String loadUrl = "http://www.pahujabrothers.com";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        if (UtilHelper.isNetworkConnected(this)) {
            activityMainBinding.webView.loadUrl(loadUrl);
            activityMainBinding.webView.getSettings().setJavaScriptEnabled(true);
            activityMainBinding.webView.setWebViewClient(new WebViewClient() {
                @Override
                public void onPageStarted(WebView view, String url, Bitmap favicon) {
                    super.onPageStarted(view, url, favicon);
                    activityMainBinding.progressBar.setVisibility(View.GONE);
                }

                @Override
                public void onPageFinished(WebView view, String url) {
                    super.onPageFinished(view, url);
                    activityMainBinding.progressBar.setVisibility(View.GONE);
                }
            });
        } else {
            ErrorFragment errorFragment = new ErrorFragment();
            errorFragment.setCancelable(false);
            errorFragment.show(getSupportFragmentManager(), "error");
        }

        activityMainBinding.webView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    WebView webView = (WebView) v;

                    if (keyCode == KeyEvent.KEYCODE_BACK) {
                        if (webView.canGoBack()) {
                            webView.goBack();
                            return true;
                        }
                    }
                }

                return true;
            }
        });
    }

    @Override
    public void onOkButton() {
        finish();
    }
}
