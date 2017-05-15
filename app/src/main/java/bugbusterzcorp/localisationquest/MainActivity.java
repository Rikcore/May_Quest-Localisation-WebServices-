package bugbusterzcorp.localisationquest;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.octo.android.robospice.GsonGoogleHttpClientSpiceService;
import com.octo.android.robospice.SpiceManager;
import com.octo.android.robospice.persistence.DurationInMillis;
import com.octo.android.robospice.persistence.exception.SpiceException;
import com.octo.android.robospice.request.listener.RequestListener;

import java.util.ArrayList;

import pl.droidsonroids.gif.GifTextView;

public class MainActivity extends AppCompatActivity {

    public LocationManager locationManager = null;
    public LocationListener locationListener;
    public final static int REQUEST_LOCATION_PERMISSION = 1;
    private double latitude;
    private double longitude;

    private String apiKey;
    private TextView textViewCurrent;
    private TextView textViewForecast;
    private TextView textViewToday;
    private ListView listView;
    private GifTextView gifTextView;

    ProgressDialog dialog;

    protected SpiceManager spiceManager = new SpiceManager(GsonGoogleHttpClientSpiceService.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        textViewCurrent = (TextView)findViewById(R.id.textViewCurrent);
        textViewForecast = (TextView)findViewById(R.id.textViewForecast);
        textViewForecast.setVisibility(View.INVISIBLE);
        textViewToday = (TextView)findViewById(R.id.textViewToday);
        textViewToday.setVisibility(View.INVISIBLE);
        listView = (ListView)findViewById(R.id.listViewForecast);
        gifTextView = (GifTextView)findViewById(R.id.gifTextViewResult);

        dialog = new ProgressDialog(MainActivity.this);
        dialog.setMessage("Recherche de ta localisation");
        dialog.show();

        Toast.makeText(MainActivity.this, "Déplace toi pour connaître ta position", Toast.LENGTH_LONG).show();

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        apiKey = getString(R.string.ApiKey);


        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
                Toast.makeText(MainActivity.this, location.getLatitude() + " " + location.getLongitude(), Toast.LENGTH_LONG).show();
                performRequest();
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };


    }

    @Override
    protected void onStart() {
        super.onStart();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_LOCATION_PERMISSION);
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 20, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 20, locationListener);

        spiceManager.start(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationManager.removeUpdates(locationListener);

        spiceManager.shouldStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 20, locationListener);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 20, locationListener);
    }

    private void performRequest() {
        MainActivity.this.setProgressBarIndeterminateVisibility(true);

        ForecastWeatherRequest requestCurrent = new ForecastWeatherRequest(latitude, longitude, apiKey);
        spiceManager.execute(requestCurrent, new ForecastWeatherRequestListener());

        CurrentWeatherRequest requestForecast = new CurrentWeatherRequest(latitude, longitude, apiKey);
        spiceManager.execute(requestForecast, new CurrentWeatherRequestListener());
    }


    private class ForecastWeatherRequestListener implements RequestListener<ForecastWeatherModel> {

        @Override
        public void onRequestFailure(SpiceException e) {
            //update your UI
        }

        @Override
        public void onRequestSuccess(ForecastWeatherModel forecastWeatherModel) {
            dialog.dismiss();
            textViewForecast.setVisibility(View.VISIBLE);
            final ArrayList<List> weatherList = (ArrayList<List>) forecastWeatherModel.getList();
            final WeatherAdapter weatherAdapter = new WeatherAdapter(MainActivity.this, weatherList);
            listView.setAdapter(weatherAdapter);

        }


    }

    private class CurrentWeatherRequestListener implements RequestListener<CurrentWeatherModel> {

        private String city;

        @Override
        public void onRequestFailure(SpiceException e) {
            //update your UI
        }

        @Override
        public void onRequestSuccess(CurrentWeatherModel currentWeatherModel) {

            textViewToday.setVisibility(View.VISIBLE);
            int windSpeed = (int)(currentWeatherModel.getWind().getSpeed()*3.6);

            if (currentWeatherModel.getName().length() > 0) {
                city = currentWeatherModel.getName();
                textViewCurrent.setText("Ville : "+city +"\n"+"Vitesse du vent : "+windSpeed+ " Km/h");
                if(windSpeed > 10){
                    gifTextView.setBackgroundResource(R.drawable.wind);
                }
                else if(windSpeed > 30){
                    gifTextView.setBackgroundResource(R.drawable.tornado);
                }
                else {
                    gifTextView.setBackgroundResource(R.drawable.fart);
                }
            }
            else{
            textViewCurrent.setText("Vitesse du vent : "+windSpeed+" Km/h");
                if(windSpeed > 10){
                    gifTextView.setBackgroundResource(R.drawable.wind);
                }
                else if(windSpeed > 30){
                    gifTextView.setBackgroundResource(R.drawable.tornado);
                }
                else {
                    gifTextView.setBackgroundResource(R.drawable.fart);
                }

            }
        }



    }

}
