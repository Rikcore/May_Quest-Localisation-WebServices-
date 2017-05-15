package bugbusterzcorp.localisationquest;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.common.io.Resources;
import com.octo.android.robospice.request.googlehttpclient.GoogleHttpClientSpiceRequest;

import roboguice.util.temp.Ln;

/**
 * Created by apprenti on 11/05/17.
 */

public class ForecastWeatherRequest extends GoogleHttpClientSpiceRequest<ForecastWeatherModel> {

    private String baseUrl;

    public ForecastWeatherRequest(double latitude, double longitude, String apiKey) {
        super(ForecastWeatherModel.class);
        this.baseUrl = "http://api.openweathermap.org/data/2.5/forecast?lat="+latitude+"&lon="+longitude+"&appid="+apiKey+"&lang=fr";
    }

    @Override
    public ForecastWeatherModel loadDataFromNetwork() throws Exception {
        Ln.d( "Call web service " + baseUrl );
        ForecastWeatherModel request = getHttpRequestFactory()
                .buildGetRequest(new GenericUrl(this.baseUrl))
                .setParser(new GsonFactory().createJsonObjectParser())
                .execute()
                .parseAs(getResultType());
        return request;

    }
}
