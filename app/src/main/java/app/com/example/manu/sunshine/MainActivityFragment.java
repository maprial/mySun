package app.com.example.manu.sunshine;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
 * A placeholder fragment containing a simple view.
 */


public class MainActivityFragment extends Fragment {

    ArrayAdapter<String> miAdaptadorArray;
   // ArrayList miArray;


    public MainActivityFragment() {
    }

    String arraymio [] = {"Uno","Dos","Tres"};

    List<String> miArray = new ArrayList<String>(Arrays.asList(arraymio));


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {




        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

      miAdaptadorArray = new ArrayAdapter<String>(getActivity(),R.layout.list_item_forecast,R.id.list_item_forecast_textview,miArray);

        ListView miListView = (ListView)rootView.findViewById(R.id.listView_forecast);

        miListView.setAdapter(miAdaptadorArray);

//declaro fuera del try-catch para cerrarlos en el bloque finally
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        //respuesta Json como string
        String forecastJsonStr = null;

        try{

            URL url = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?q=94043&mode=json&units=metric&cnt=7");

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





        }catch(IOException ex){

            Log.e("MainActivityFragment", "Error ", ex);
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

                    Log.e("PlaceholderFragment", "Error closing stream", e);
                }
            }
        }


        return rootView;
    }
}
