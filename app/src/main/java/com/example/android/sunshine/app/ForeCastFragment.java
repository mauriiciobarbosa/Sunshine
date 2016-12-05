package com.example.android.sunshine.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.example.android.sunshine.app.utils.SunshineUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by mauricio-MTM on 11/30/2016.
 */
public class ForeCastFragment extends Fragment implements AdapterView.OnItemClickListener {

    private static final String LOG_TAG = ForeCastFragment.class.getSimpleName();

    protected ArrayAdapter<String> arrayAdapter;
    protected ListView listView;

    public ForeCastFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        listView = (ListView) rootView.findViewById(R.id.listview_forecast);

        arrayAdapter = new ArrayAdapter<>(getActivity(), R.layout.list_item_forecast,
                R.id.list_item_forecast_textview, new ArrayList<String>());

        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(this);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.forecastfragment, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_refresh:
                updateWeather();
                return true;
            case R.id.action_show_location:
                String location = getPreferenceValue(getString(R.string.pref_location_key), getString(R.string.pref_location_default_value));
                Uri geoLocation = Uri.parse("geo:0,0:?q=" + location);
                showMap(geoLocation);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showMap(Uri geoLocation) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.e(LOG_TAG, "No activity to resolve");
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String forecast = arrayAdapter.getItem(position);

        Intent intent = new Intent(getContext(), DetailActivity.class);
        intent.putExtra(DetailActivity.PARAM_FORECAST, forecast);

        startActivity(intent);
    }

    private void updateWeather() {
        FetchWeatherTask task = new FetchWeatherTask();

        String location = getPreferenceValue(getString(R.string.pref_location_key), getString(R.string.pref_location_default_value));
        String unit = getPreferenceValue(getString(R.string.pref_temperature_key), getString(R.string.pref_temperature_default_value));

        task.execute(location, unit);
    }

    public String getPreferenceValue(String key, String defaultValue) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        return sharedPreferences.getString(key, defaultValue);
    }

    private class FetchWeatherTask extends AsyncTask<String, Void, List<String>> {

        final String QUERY_PARAM = "q";
        final String FORMAT_PARAM = "mode";
        final String UNITS_PARAM = "units";
        final String DAYS_PARAM = "cnt";
        final String KEY_PARAM = "appid";

        @Override
        protected List<String> doInBackground(String... params) {
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String forecastJsonStr;

            List<String> result = null;

            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast

                if (params.length < 2) return null;

                String location = params[0];
                String unit = params[1];
                String format = "json";
                String days = "7";
                String key = "bc198534cb37f8fc38c6de947adeb97a";

                Uri builtUri = Uri.parse("http://api.openweathermap.org/data/2.5/forecast/daily?").buildUpon()
                        .appendQueryParameter(QUERY_PARAM, location)
                        .appendQueryParameter(FORMAT_PARAM, format)
                        .appendQueryParameter(UNITS_PARAM, unit)
                        .appendQueryParameter(DAYS_PARAM, days)
                        .appendQueryParameter(KEY_PARAM, key).build();

                Log.v(LOG_TAG, "URI : " + builtUri.toString());

                URL url = new URL(builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }

                forecastJsonStr = buffer.toString();

                result = Arrays.asList(SunshineUtils.getWeatherDataFromJson(getContext(), forecastJsonStr, 7, unit));

            } catch (Exception e) {
                Log.e("PlaceholderFragment", "Error ", e);
                return null;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(List<String> forecast) {
            arrayAdapter.clear();
            if (forecast != null) arrayAdapter.addAll(forecast);
        }

    }

}
