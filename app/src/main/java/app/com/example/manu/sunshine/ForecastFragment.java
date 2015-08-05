package app.com.example.manu.sunshine;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.format.Time;
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


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A placeholder fragment containing a simple view.
 */


public class ForecastFragment extends Fragment {

    ArrayAdapter<String>  mForecastAdapter;
   // ArrayList miArray;


    public ForecastFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setHasOptionsMenu(true);
    }

    String arraymio [] = {"","",""};

    List<String> miArray = new ArrayList<String>(Arrays.asList(arraymio));


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {




       View rootView = inflater.inflate(R.layout.fragment_main, container, false);

      mForecastAdapter = new ArrayAdapter<String>(getActivity(),R.layout.list_item_forecast,R.id.list_item_forecast_textview,miArray);

        ListView miListView = (ListView)rootView.findViewById(R.id.listView_forecast);

        miListView.setAdapter( mForecastAdapter);




        miListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                String forecast = mForecastAdapter.getItem(position);
            Intent goToDetail = new Intent(getActivity(),DetailActivity.class);
                goToDetail.putExtra("MESSAGE",forecast);
                startActivity(goToDetail);
            }
        });

// si quiero que me cargue los datos directamente y no al darle a erfresh
     //   new FetchWeatherTask().execute("94043");












        return rootView;
    }



    public class FetchWeatherTask extends AsyncTask<String,Void,String[]>{

        private final String LOG_TAG = FetchWeatherTask.class.getSimpleName();

        String [] myArray = null;
        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEE MMM dd");
            return shortenedDateFormat.format(time);
        }



        /**
         * Prepare the weather high/lows for presentation.
         */
        private String formatHighLows(double high, double low) {
            // For presentation, assume the user doesn't care about tenths of a degree.
            long roundedHigh = Math.round(high);
            long roundedLow = Math.round(low);

            String highLowStr = roundedHigh + "/" + roundedLow;
            return highLowStr;
        }


        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getWeatherDataFromJson(String forecastJsonStr, int numDays)
                throws JSONException {

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "list";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            String[] resultStrs = new String[numDays];
            for (int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay + i);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                highAndLow = formatHighLows(high, low);
                resultStrs[i] = day + " - " + description + " - " + highAndLow;
            }

            for (String s : resultStrs) {
                Log.v(LOG_TAG, "Forecast entry: " + s);
            }
            return resultStrs;

        }



        @Override
        protected String[] doInBackground(String... params) {

            // If there's no zip code, there's nothing to look up.  Verify size of params.
            if (params.length == 0) {
                return null;
            }



//declaro fuera del try-catch para cerrarlos en el bloque finally
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            //respuesta Json como string
            String forecastJsonStr = null;


            //Declaro variables a tener en cuenta en la consulta
            String format = "json";
                       String units = "metric";
                      int numDays = 7;

            try{







              //  URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7");


                //Constantes con el nombre del parámetro(q es el codigo postal, cnt el número de días,etc)
                final String FORECAST_BASE_URL =
                                                "http://api.openweathermap.org/data/2.5/forecast/daily?";
                                final String QUERY_PARAM = "q";
                                final String FORMAT_PARAM = "mode";
                               final String UNITS_PARAM = "units";
                               final String DAYS_PARAM = "cnt";

                //Construyo la uri y le voy pasando parametros

             //   con indice 0 pillo el valor introducido en el método execute

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                                                .appendQueryParameter(QUERY_PARAM, params[0])
                                                .appendQueryParameter(FORMAT_PARAM, format)
                                                .appendQueryParameter(UNITS_PARAM, units)
                                                .appendQueryParameter(DAYS_PARAM, Integer.toString(numDays))
                                                .build();


                URL url = new URL(builtUri.toString());

/*
String maxi="";
                try {
                    JSONObject myJson = new JSONObject({"city":{"id":3093133,"name":"Lodz","coord":{"lon":19.466669,"lat":51.75},"country":"PL","population":0},"cod":"200","message":0.0099,"cnt":7,"list":[{"dt":1438596000,"temp":{"day":31.29,"min":21.79,"max":31.29,"night":21.79,"eve":31.29,"morn":31.29},"pressure":1019.18,"humidity":27,"weather":[{"id":800,"main":"Clear","description":"sky is clear","icon":"01d"}],"speed":2.36,"deg":64,"clouds":0},{"dt":1438682400,"temp":{"day":31.86,"min":18.98,"max":33.72,"night":27.88,"eve":33.29,"morn":18.98},"pressure":1017.67,"humidity":34,"weather":[{"id":800,"main":"Clear","description":"sky is clear","icon":"01d"}],"speed":4.2,"deg":99,"clouds":0},{"dt":1438768800,"temp":{"day":31.39,"min":20.82,"max":31.39,"night":26.29,"eve":30.37,"morn":20.82},"pressure":1016.35,"humidity":40,"weather":[{"id":500,"main":"Rain","description":"light rain","icon":"10d"}],"speed":1.46,"deg":222,"clouds":8,"rain":1.78},{"dt":1438855200,"temp":{"day":28.06,"min":21.86,"max":32.12,"night":23.71,"eve":32.12,"morn":21.86},"pressure":1019.66,"humidity":45,"weather":[{"id":800,"main":"Clear","description":"sky is clear","icon":"02d"}],"speed":2.77,"deg":1,"clouds":8},{"dt":1438941600,"temp":{"day":32.75,"min":20.04,"max":36.02,"night":30.55,"eve":36.02,"morn":20.04},"pressure":1019.56,"humidity":38,"weather":[{"id":800,"main":"Clear","description":"sky is clear","icon":"01d"}],"speed":3.4,"deg":118,"clouds":0},{"dt":1439028000,"temp":{"day":35.79,"min":27.47,"max":35.79,"night":27.47,"eve":33.66,"morn":27.83},"pressure":1016.01,"humidity":0,"weather":[{"id":800,"main":"Clear","description":"sky is clear","icon":"01d"}],"speed":3.63,"deg":167,"clouds":0},{"dt":1439114400,"temp":{"day":31.08,"min":21.26,"max":31.08,"night":21.26,"eve":27.62,"morn":28.64},"pressure":1020.53,"humidity":0,"weather":[{"id":800,"main":"Clear","description":"sky is clear","icon":"01d"}],"speed":6.65,"deg":347,"clouds":1}]});

                    JSONArray myArray = myJson.getJSONArray("list");

                 maxi = myArray.getJSONObject(0).getJSONObject("temp").getString("max");


                } catch (JSONException e) {
                    e.printStackTrace();
                }
*/
                Log.v(LOG_TAG, "Built URI " + builtUri.toString());

                //falta incluir el retardo por ejemplo http://developer.android.com/training/basics/network-ops/connecting.html (setReadTimeout,setConnectTimeout)
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

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

                Log.v(LOG_TAG,"ForeCast  " + forecastJsonStr);









              //  Toast.makeText(getActivity(),forecastJsonStr,Toast.LENGTH_LONG).show();





            }catch(IOException ex){

                Log.e(LOG_TAG, "Error ", ex);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;

            }

            //Al final he de cerrar

            finally {


                if(urlConnection !=null){
                    urlConnection.disconnect();
                }

                if(reader!=null){


                    try{


                        reader.close();
                    }catch(IOException e){

                        Log.e("ForecastFragment", "Error closing stream", e);
                    }
                }
            }


            //El array de string con las condiciones de cada dia

            try {

                myArray =getWeatherDataFromJson(forecastJsonStr, numDays);
                         //      return getWeatherDataFromJson(forecastJsonStr, numDays);
                return myArray;
                            } catch (JSONException e) {
                                Log.e(LOG_TAG, e.getMessage(), e);
                                e.printStackTrace();
                            }

            return null;
        }


        @Override
        protected void onPostExecute(String[] result) {



           // super.onPostExecute(strings);

         //   View rootView = inflater.inflate(R.layout.fragment_main, container, false);

         /*   mForecastAdapter = new ArrayAdapter<String>(getActivity(),R.layout.list_item_forecast,R.id.list_item_forecast_textview,myArray);

            ListView miListView = (ListView)getActivity().findViewById(R.id.listView_forecast);

            miListView.setAdapter(mForecastAdapter);*/

            if (result != null) {
                               mForecastAdapter.clear();
                                for(String dayForecastStr : result) {
                                        mForecastAdapter.add(dayForecastStr);
                                    }
                                // New data is back from the server.  Hooray!
                                    }
        }
    }














    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
     //   super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.forecastfragment, menu);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
       // return super.onOptionsItemSelected(item);
        switch (item.getItemId()){

            case  R.id.action_refresh:

                new FetchWeatherTask().execute("94043");
              //  Toast.makeText(getActivity(),"Cuidado",Toast.LENGTH_LONG).show();
               // return true;
        }

        return true;
    }
}
