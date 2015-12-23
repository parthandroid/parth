package fr.joeybronner.turnbyturn;

import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

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
import java.util.HashMap;
import java.util.Map;

import fr.joeybronner.turnbyturn.utilsapi.AsyncTaskDrawDirection;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnMarkerClickListener {

    GoogleMap mMap;
    private int once = 1;
    private LatLngBounds latlngBounds;
    private Polyline newPolyline;
    private double startLat, startLng, endLat, endLng;
    JSONObject jsonObject1, jsonObject2;
    String distance;
    String time;
    public static double LON = 0.0, LAT = 0.0;
    public static String PARK = "";
    public static double OF_LON = 0.0, OF_LAT = 0.0;
    public static String OF_PARK = "";
    TextView tv;
    Marker mr;
    String distance1;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //this function calculates the distance as well as the shortest path from starting point to ending point
        getDistance(LAT, LON);


    }


    public void getDistance(final Double lat, final Double lon) {

        class Sess extends AsyncTask<Void, Integer, Long> {
            protected Long doInBackground(Void... arg0) {
                try {
                    // the thread will be engaged in the while loop till the value of start location is found.
                    // so that there is no error in calculation of path.
                    while (startLat == 0.0) ;
                    Log.d("HEY", "asynch task" + startLat);

                    endLat = lat;
                    endLng = lon;
                    jsonObject1 = getjsonobject(startLat, startLng, endLat, endLng);


                    JSONArray jsa = jsonObject1.getJSONArray("rows");
                    JSONObject jo = jsa.getJSONObject(0);
                    JSONArray ja = jo.getJSONArray("elements");
                    JSONObject jjoo = ja.getJSONObject(0);
                    JSONObject jjo = jjoo.getJSONObject("distance");
                    distance = jjo.getString("text");
                } catch (JSONException e) {
                    // Nothing.
                }
                return null;
            }

            protected void onPostExecute(Long result) {

                // this function is called to draw the distance from starting point to end point.
                // the "driving" argument means that the best path is drawn assuming that u r in a motor vehicle.
                findDirections(startLat, startLng, endLat, endLng, "driving");

                //this adds the marker(the p icon in this case) at the end point i.e the parking
                mr = mMap.addMarker(new MarkerOptions().position(new LatLng(endLat, endLng)).snippet(PARK).icon(BitmapDescriptorFactory.fromResource(R.drawable.p)));

                // this sets the listener to the marker
                mMap.setOnMarkerClickListener(MainActivity.this);
                 // this adds the marker at the office location
                 mMap.addMarker(new MarkerOptions().position(new LatLng(OF_LAT, OF_LON)).title("OFFICE").snippet(OF_PARK).icon(BitmapDescriptorFactory.fromResource(R.drawable.of)));



            }
        }

        // starts the async task
        new Sess().execute();

    }


/////////////////////////////////////////////////////////////////////////////////// for distance and duration

    public JSONObject getjsonobject(double startLat, double startLng, double endLat, double endLng) {


        StringBuilder stringBuilder = new StringBuilder();
        try {


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


    ///////////////////////////////////////////////////////////////


// this is called as soon as the current location is to be set up.
    @Override
    public void onMapReady(GoogleMap map) {

        mMap = map;

        //this sets the blue circle to the current location.
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationChangeListener(myLocationChangeListener);
    }



    // this function is called on its own whenever your current location is changed .
    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {
            Log.d("HEY", "listener");


            // curent lat and current lon.
            startLat = location.getLatitude();
            startLng = location.getLongitude();

            mMap.setMyLocationEnabled(true);
            LatLng loc = new LatLng(startLat, startLng);
            if (mMap != null) {
                if (once == 1) {
                    mMap.clear();
                    /*mMap.addMarker(new MarkerOptions()
                            .position(loc)
                            .title("Joey")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.moto)));*/
                    // focus the camera on the current location
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 16.0f));
                    once = 0;
                }
            }


        }
    };

    public void handleGetDirectionsResult(ArrayList<LatLng> directionPoints) {
        PolylineOptions rectLine = new PolylineOptions().width(10).color(Color.BLUE);

        for (int i = 0; i < directionPoints.size(); i++) {
            rectLine.add(directionPoints.get(i));
        }
       /* if (newPolyline != null) {
            newPolyline.remove();
        } */
        newPolyline = mMap.addPolyline(rectLine);

        latlngBounds = createLatLngBoundsObject(new LatLng(startLat, startLng), new LatLng(endLat, endLng));
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(latlngBounds,
                getWindowManager().getDefaultDisplay().getWidth(),
                getWindowManager().getDefaultDisplay().getHeight(), 150));
    }

    private LatLngBounds createLatLngBoundsObject(LatLng firstLocation, LatLng secondLocation) {
        if (firstLocation != null && secondLocation != null) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(firstLocation).include(secondLocation);

            return builder.build();
        }
        return null;
    }

    public void findDirections(double fromPositionDoubleLat, double fromPositionDoubleLong, double toPositionDoubleLat, double toPositionDoubleLong, String mode) {
        Map<String, String> map = new HashMap<String, String>();
        map.put(AsyncTaskDrawDirection.USER_CURRENT_LAT, String.valueOf(fromPositionDoubleLat));
        map.put(AsyncTaskDrawDirection.USER_CURRENT_LONG, String.valueOf(fromPositionDoubleLong));
        map.put(AsyncTaskDrawDirection.DESTINATION_LAT, String.valueOf(toPositionDoubleLat));
        map.put(AsyncTaskDrawDirection.DESTINATION_LONG, String.valueOf(toPositionDoubleLong));
        map.put(AsyncTaskDrawDirection.DIRECTIONS_MODE, mode);

        AsyncTaskDrawDirection asyncTask = new AsyncTaskDrawDirection(this);
        asyncTask.execute(map);
    }


    public void refreshDistance(final Double lat, final Double lon) {

        class Sess extends AsyncTask<Void, Integer, Long> {
            protected Long doInBackground(Void... arg0) {
                try {


                    endLat = lat;
                    endLng = lon;
// json object of distance and duration
                    jsonObject2 = getjsonobject(startLat, startLng, endLat, endLng);
                    Log.d("everything", "everything = " + startLat + startLng + endLat + endLng);
                    JSONArray jsa = jsonObject2.getJSONArray("rows");
                    JSONObject jo = jsa.getJSONObject(0);
                    JSONArray ja = jo.getJSONArray("elements");
                    JSONObject jjoo = ja.getJSONObject(0);
                    JSONObject jjo = jjoo.getJSONObject("distance");
                    distance1 = jjo.getString("text");
                    Log.d("DIST", distance1 + "");

                    JSONObject par = ja.getJSONObject(0);
                    JSONObject par2 = par.getJSONObject("duration");
                    time = par2.getString("text");
                    Log.d("time",time+"");

                } catch (JSONException e) {
                    // Nothing.
                }
                return null;
            }

            protected void onPostExecute(Long result) {
                //mr = mMap.addMarker(new MarkerOptions().position(new LatLng(endLat, endLng)).title(distance).snippet(PARK));
                //mMap.setOnMarkerClickListener(MainActivity.this);
                // Toast.makeText(MainActivity.this, " distance " + distance, Toast.LENGTH_LONG).show();


            }
        }
        new Sess().execute();

    }


    @Override
    public boolean onMarkerClick(Marker marker) {

        // Toast.makeText(MainActivity.this, "marker clicked", Toast.LENGTH_LONG).show();

        if (marker.equals(mr)) {

            refreshDistance(LAT, LON);

            while (distance1 == null) ;
            while (time == null);

            mr.setTitle(distance1+" "+time);


            Toast.makeText(MainActivity.this, "dist = " + distance1, Toast.LENGTH_LONG).show();

        }
        return false;
    }
}
