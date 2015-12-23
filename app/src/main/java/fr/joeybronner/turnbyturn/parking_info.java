package fr.joeybronner.turnbyturn;

import android.app.ListActivity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class parking_info extends ListActivity {


    public static ArrayList al = new ArrayList();
    //in layman terms , the number of objects are basically the  parkings associated with the company whose allocation id input in the textField.
    public static int no_of_objects = 0;
    public static double current_lat = 0.0, current_lon = 0.0;
    JSONObject jsonObject1;
    TextView choose;


    String[] total = new String[no_of_objects];
    // arrays for location and name of parkings
    Double[] latitude = new Double[no_of_objects];
    Double[] longitude = new Double[no_of_objects];
    String[] parking = new String[no_of_objects];
    // arrays for dist and time from current location to the parking
    String[] dist = new String[no_of_objects];
    String[] time = new String[no_of_objects];
    // arrays for location and name of the office
    String[] office = new String[no_of_objects];
    Double[] office_lat = new Double[no_of_objects];
    Double[] office_lon = new Double[no_of_objects];

    // first method to be called
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // sets the look to that specified in the xml file.
        setContentView(R.layout.activity_parking_info);


        //these 4 lines are for seeting the custom font for the text "choose text"
        choose = (TextView) findViewById(R.id.textView8);
        Typeface mFont = Typeface.createFromAsset(getAssets(), "fonts/f1.ttf");
        choose.setTypeface(mFont);
        choose.setTypeface(mFont);


        //this piece of code in the for loop will extract all the info from json format and put it into arrays as declared above
        for (int i = 0; i < no_of_objects; i++) {

            JSONObject jo = (JSONObject) al.get(i);

            try {
                Double la = jo.getDouble("latitude");
                Double lo = jo.getDouble("longitude");
                String park = jo.getString("parking");

                latitude[i] = la;
                longitude[i] = lo;
                parking[i] = park;


                Double of_la = jo.getDouble("office_lat");
                Double of_lo = jo.getDouble("office_lon");
                String of = jo.getString("office");

                office_lat[i] = of_la;
                office_lon[i] = of_lo;
                office[i] = of;


            } catch (JSONException e) {
                e.printStackTrace();
            }


        }

// just for logging purpose
        for (int i = 0; i < no_of_objects; i++) {

            Log.d("latitude", latitude[i] + "");
            Log.d("longitude", longitude[i] + "");
            Log.d("parking", parking[i] + "");

            Log.d("office_lat", office_lat[i] + "");
            Log.d("office_lon", office_lon[i] + "");
            Log.d("office", office[i] + "");


        }

// this piece of code in the async task class will calculate the distance and time from the current location to the parking

        class Sess extends AsyncTask<Void, Integer, Long> {
            public Long doInBackground(Void... arg0) {
                try {


                    for (int i = 0; i < no_of_objects; i++) {


                        // calls the getjsonobject function to get the json response which contains the info of dist and time from current location to parking
                        jsonObject1 = getjsonobject(current_lat, current_lon, latitude[i], longitude[i]);

                        // retrives the data from json response
                        JSONArray jsa = jsonObject1.getJSONArray("rows");
                        JSONObject jo = jsa.getJSONObject(0);
                        JSONArray ja = jo.getJSONArray("elements");
                        JSONObject jjoo = ja.getJSONObject(0);
                        JSONObject jjo = jjoo.getJSONObject("distance");
                        // assign the distances to the array which will be shown in the list view.
                        dist[i] = jjo.getString("text");

                        JSONObject jjo_t = jjoo.getJSONObject("duration");
                        //asign the time taken to get to each parking to the array which will be shown in the list view
                        time[i] = jjo_t.getString("text");


                    }
                } catch (JSONException e) {
                    // Nothing.
                }
                return null;
            }

            protected void onPostExecute(Long result) {
                // Toast.makeText(parking_info.this, " distance " + distance, Toast.LENGTH_LONG).show();

                // puts the parking name, distance, time in the list
                for (int i = 0; i < no_of_objects; i++) {

                    total[i] = parking[i] + " (" + dist[i] + ")" + "  Time : " + time[i];
                    Log.d("distance", dist[i]);


                }

            }


        }

        //executes the asynch task
        new Sess().execute();


        // mail thread sleeps till our another thread is done with its onpostexecute as it contains the total array which has to be initialized in the next step itself
        while (dist[no_of_objects - 1] == null) ;


        //Listview adapter
        setListAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, total));


    }

    // function for calculating the dist and time from the current location.
    public JSONObject getjsonobject(double startLat, double startLng, double endLat, double endLng) {


        StringBuilder stringBuilder = new StringBuilder();
        try {

// api provided by google which is called to get the distance and time from current location
            HttpPost httppost = new HttpPost("http://maps.googleapis.com/maps/api/distancematrix/json?origins=" + startLat + "," + startLng + "&destinations=" + endLat + "," + endLng + "&mode=driving&language=en-EN&sensor=false");

            HttpClient client = new DefaultHttpClient();
            HttpResponse response;
            stringBuilder = new StringBuilder();


            response = client.execute(httppost);
            HttpEntity entity = response.getEntity();
            InputStream stream = entity.getContent();
            int b;
            while ((b = stream.read()) != -1) {
                stringBuilder.append((char) b);
            }
        } catch (ClientProtocolException e) {
        } catch (IOException e) {
        }

        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject = new JSONObject(stringBuilder.toString());
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return jsonObject;
    }

    // listener for the list view.
    //this will assign the longitude and latitude of the item clicked(parking) to the static variables of Mainactivity class
    public void onListItemClick(ListView parent, View v,
                                int position, long id) {

        MainActivity.LAT = latitude[position];
        MainActivity.LON = longitude[position];
        MainActivity.PARK = parking[position];


        MainActivity.OF_LAT = office_lat[position];
        MainActivity.OF_LON = office_lon[position];
        MainActivity.OF_PARK = office[position];

        // starts the MainActivity
        startActivity(new Intent(this, MainActivity.class));


    }


}
