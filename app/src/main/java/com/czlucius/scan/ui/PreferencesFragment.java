/*
 * Code Scanner. An android app to scan and create codes(barcodes, QR codes, etc)
 * Copyright (C) 2021 Lucius Chee Zihan
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.czlucius.scan.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;

import com.czlucius.scan.R;
import com.czlucius.scan.Utils;
import com.czlucius.scan.callbacks.ManualResetPreferenceClickListener;
import com.czlucius.scan.misc.monetization.AdStrategy2;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class PreferencesFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {

        // Instantiate ads if on play flavour.
        View v = super.onCreateView(inflater, container, savedInstanceState);
        ViewGroup vg = (ViewGroup) v;
        if (vg != null) {
            AdStrategy2.getInstance(getContext())
                    .addAdViewTo(vg);
        }


        Preference oss_link = findPreference("open_source");
        if (oss_link != null) {
            oss_link.setOnPreferenceClickListener(preference -> {
                Intent urlIntent = new Intent(Intent.ACTION_VIEW);
                Uri url = Uri.parse(getString(R.string.oss_link));
                urlIntent.setData(url);
                boolean appAvailable = Utils.launchIntentCheckAvailable(urlIntent, requireContext());
                if (!appAvailable) {
                    Toast.makeText(getContext(), R.string.no_browsers, Toast.LENGTH_SHORT).show();
                }
                return true;
            });
        }

        Preference oss_licenses = findPreference("oss_licenses");
        if (oss_licenses != null) {
            setupOSSLicensesDialog(oss_licenses);
        }

        if (findPreference("watch_ads_prefbtn") != null) {
            findPreference("watch_ads_prefbtn").setOnPreferenceClickListener(new ManualResetPreferenceClickListener() {
                @Override
                public boolean onSingleClick(Preference p) {
                    AdStrategy2.getInstance(getContext())
                            .loadRewardedAdVideo(getActivity(), getView(), getResetCallback());
                    return true;
                }
            });
        }

        return v;
    }

    private void setupOSSLicensesDialog(Preference preferenceToBeClicked) {
        // WebView for displaying open-source licenses
        WebView webView = new WebView(requireContext());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return shouldOverrideUrlLoading(Uri.parse(url));
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Uri url = request.getUrl();
                return shouldOverrideUrlLoading(url);
            }

            private boolean shouldOverrideUrlLoading(Uri url) {
                Intent urlIntent = new Intent(Intent.ACTION_VIEW);
                urlIntent.setData(url);
                boolean appAvailable = Utils.launchIntentCheckAvailable(urlIntent, requireContext());
                if (!appAvailable) {
                    Toast.makeText(getContext(), R.string.no_browsers, Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
        webView.zoomBy(0.5F);

        MaterialAlertDialogBuilder ossDialog = new MaterialAlertDialogBuilder(requireContext());
        ossDialog.setView(webView);
        AlertDialog finalDialog = ossDialog.create();

        preferenceToBeClicked.setOnPreferenceClickListener(preference -> {
            webView.loadUrl("file:///android_asset/licenses.html"); // Load from app's asset folder
            finalDialog.show();
            return true;
        });
    }
}