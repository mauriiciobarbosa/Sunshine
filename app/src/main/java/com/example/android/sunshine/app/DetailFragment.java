package com.example.android.sunshine.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by mauricio on 02/12/16.
 */
public class DetailFragment extends Fragment {

    protected TextView tvForecast;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        tvForecast = (TextView) rootView.findViewById(R.id.tvForecast);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        String forecast = getActivity().getIntent().getStringExtra(DetailActivity.PARAM_FORECAST);
        tvForecast.setText(forecast);
    }
}
