package com.excellence.iptv.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.excellence.iptv.R;

/**
 * AboutFragment
 *
 * @author ggz
 * @date 2018/4/3
 */

public class AboutFragment extends Fragment {
    private static final String TAG = "AboutFragment";

    private View mView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.about_fragment, container, false);

        return mView;
    }
}
