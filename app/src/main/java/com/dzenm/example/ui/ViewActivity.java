package com.dzenm.example.ui;

import android.os.Bundle;
import android.view.View;

import androidx.databinding.DataBindingUtil;

import com.dzenm.example.R;
import com.dzenm.example.databinding.ActivityViewBinding;
import com.dzenm.helper.base.AbsBaseActivity;
import com.dzenm.helper.toast.ToastHelper;

public class ViewActivity extends AbsBaseActivity {

    private ActivityViewBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_view);
        setToolbarWithImmersiveStatusBar(binding.toolbar, R.color.colorPrimary);

        binding.btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.editLayout.verify(false)) {
                    ToastHelper.show("校验成功");
                } else {
                    ToastHelper.show("校验失败");
                }
            }
        });
    }
}
