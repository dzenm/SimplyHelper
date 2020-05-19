package com.dzenm.example.ui;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;

import com.dzenm.example.R;
import com.dzenm.example.databinding.ActivityMainBinding;
import com.dzenm.helper.base.AbsBaseActivity;
import com.dzenm.helper.base.FragmentHelper;

/**
 * @author dinzhenyan
 * @date 2019-06-27 10:32
 */
public class MainActivity extends AbsBaseActivity {

    private ActivityMainBinding binding;

    FragmentHelper fragmentHelper;
    HomeFragment f1;
    PersonalFragment f2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        f1 = new HomeFragment();
        f2 = new PersonalFragment();

        fragmentHelper = new FragmentHelper(this, R.id.frame_layout);
        fragmentHelper.show(f1);
    }

    public void onClick(View view) {
        if (view.getId() == R.id.first) {
            fragmentHelper.show(f1);
        } else if (view.getId() == R.id.second) {
            fragmentHelper.show(f2);
        }
    }
}
