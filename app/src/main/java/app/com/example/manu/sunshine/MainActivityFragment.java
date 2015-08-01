package app.com.example.manu.sunshine;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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


        return rootView;
    }
}
