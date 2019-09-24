package com.mobile.pahujabrothers;

import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.mobile.pahujabrothers.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements ErrorFragment.OnFragmentInteractionListener {

    private ActivityMainBinding activityMainBinding;
    private String loadUrl = "http://www.pahujabrothers.com";
    private ValueCallback<Uri> mUploadMessage;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;
    private final static int FILECHOOSER_RESULTCODE = 1;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        if (UtilHelper.isNetworkConnected(this)) {
            activityMainBinding.webView.loadUrl(loadUrl);

            activityMainBinding.webView.getSettings().setJavaScriptEnabled(true);
            activityMainBinding.webView.getSettings().setAllowFileAccess(true);
            activityMainBinding.webView.getSettings().setAllowContentAccess(true);
            activityMainBinding.webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(true);
            activityMainBinding.webView.getSettings().setSupportMultipleWindows(true);

            activityMainBinding.webView.setWebChromeClient(new WebChromeClient() {

                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
                    if (uploadMessage != null) {
                        uploadMessage.onReceiveValue(null);
                        uploadMessage = null;
                    }

                    uploadMessage = filePathCallback;

                    Intent intent = fileChooserParams.createIntent();
                    try {
                        startActivityForResult(intent, REQUEST_SELECT_FILE);
                    } catch (ActivityNotFoundException e) {
                        uploadMessage = null;
                        Toast.makeText(MainActivity.this, "Cannot Open File Chooser", Toast.LENGTH_LONG).show();
                        return false;
                    }
                    return true;
                }


                @Override
                public boolean onCreateWindow(WebView view, boolean dialog, boolean userGesture, android.os.Message resultMsg) {
                    WebView.HitTestResult result = view.getHitTestResult();
                    String data = result.getExtra();
                    Context context = view.getContext();
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(data));
                    context.startActivity(browserIntent);
                    return false;
                }
            });


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


                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    if (!UtilHelper.isNetworkConnected(MainActivity.this)) {
                        view.loadUrl("file:///android_asset/error.html");
                    } else {
                        if (url.endsWith(".pdf")) {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.parse(url), "application/pdf");
                            try {
                                view.getContext().startActivity(intent);
                            } catch (ActivityNotFoundException e) {
                                //user does not have a pdf viewer installed
                            }
                        } else {
                            view.loadUrl(url);
                            activityMainBinding.progressBar.setVisibility(View.VISIBLE);
                        }


                    }
                    return super.shouldOverrideUrlLoading(view, url);
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (requestCode == REQUEST_SELECT_FILE) {
                if (uploadMessage == null)
                    return;
                uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
                uploadMessage = null;
            }
        } else if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage)
                return;
            // Use MainActivity.RESULT_OK if you're implementing WebView inside Fragment
            // Use RESULT_OK only if you're implementing WebView inside an Activity
            Uri result = intent == null || resultCode != MainActivity.RESULT_OK ? null : intent.getData();
            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        } else
            Toast.makeText(MainActivity.this, "Failed to Upload Image", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onOkButton() {
        finish();
    }
}
