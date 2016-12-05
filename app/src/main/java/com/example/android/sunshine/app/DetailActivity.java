package com.example.android.sunshine.app;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;

public class DetailActivity extends AppCompatActivity {

    public static final String PARAM_FORECAST = "com.example.android.sunshine.app.DetailActivity.PARAM_FORECAST";

    private ShareActionProvider actionProvider;

    private String contentShare;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        contentShare = getIntent().getStringExtra(DetailActivity.PARAM_FORECAST);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.detail, menu);

        MenuItem item = menu.findItem(R.id.action_share);

        actionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);

        if (actionProvider != null) {
            actionProvider.setShareIntent(createShareIntent(contentShare));
        }

        return true;
    }

    private Intent createShareIntent(String contentShare) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, contentShare + " #SunshineApp");
        return intent;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
