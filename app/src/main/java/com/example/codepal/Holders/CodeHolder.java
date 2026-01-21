package com.example.codepal.Holders;

import android.annotation.SuppressLint;
import android.view.View;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.codepal.R;

public class CodeHolder extends RecyclerView.ViewHolder {
    public WebView webView;
    @SuppressLint("SetJavaScriptEnabled")
    public CodeHolder(@NonNull View itemView) {
        super(itemView);
        webView = itemView.findViewById(R.id.codeView);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setAllowUniversalAccessFromFileURLs(true);
    }
}
