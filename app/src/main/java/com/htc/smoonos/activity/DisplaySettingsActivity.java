package com.htc.smoonos.activity;

import android.os.Bundle;
import android.view.LayoutInflater;

import com.htc.smoonos.R;
import com.htc.smoonos.databinding.ActivityDisplaySettingsBinding;

public class DisplaySettingsActivity extends BaseActivity {

    ActivityDisplaySettingsBinding displaySettingsBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        displaySettingsBinding = ActivityDisplaySettingsBinding.inflate(LayoutInflater.from(this));
        setContentView(displaySettingsBinding.getRoot());
        initView();
        initData();
    }

    private void initView(){

    }

    private void initData(){

    }

}