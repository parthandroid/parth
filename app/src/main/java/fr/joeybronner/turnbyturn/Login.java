package fr.joeybronner.turnbyturn;

import android.app.ProgressDialog;
import android.content.Intent;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class Login extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    // all the variables used in this class

    ActionProcessButton login;
    EditText username, password;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    ProgressDialog dialog;
    Double latt, lonn;

    // onCreate method is the first method to be called in each class.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // this function sets the look of the activity.
        // as it takes the xml file as the parameter.
        setContentView(R.layout.activity_login);


        // initialization of all the buttons , text fields.
        login = (ActionProcessButton) findViewById(R.id.login);
        username = (EditText) findViewById(R.id.editText);
        password = (EditText) findViewById(R.id.editText2);


        // the below function calls will just build and connect the clients .

        buildGoogleApiClient();

        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        } else {
            Toast.makeText(Login.this, "Not connected...", Toast.LENGTH_SHORT).show();
        }


    }


    // this method will be called when the login button is clicked.

    public void login_click(View v) {


        // these set of lines will add the progress dialog which will be there till our next activity is opened.
        dialog = new ProgressDialog(Login.this);
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Loading. Please wait...");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();


        String user_name = username.getText().toString();
        String pass_word = password.getText().toString();


        // creates asynch task object
        MyTask myTask1 = new MyTask();

        //starts asynch task
        myTask1.execute(user_name, pass_word, "", "", "");
// note1 :  for more info on the async task used in this class refer to the the word document sent to sir's mail.

    }


    class MyTask extends AsyncTask<String, Integer, String> {

        @Override
        protected String doInBackground(String... params) {


            String user = params[0];
            String pass = params[1];

// url of the php script 
            String retrive_url = "http://parth.herobo.com/readdata.php";


            ///Retrive data from database

            //  will try to connect to our database using httpurlconnection.
            // it sends the allocation id of the employee to the php file using post method.
            // the php file will then reterive the details needed(longitude , latitude ,parking name , office name ....) using that allocation id.
            // the response generated(which contains all the details such as longitude, latitud...) is in the form of json. so we have to use certain classes and functions to reterive data from that response
            try {
                URL url = new URL(retrive_url);
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                httpURLConnection.setRequestMethod("POST");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);

                OutputStream outputStream = httpURLConnection.getOutputStream();
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(outputStream, "UTF-8"));
                String data = URLEncoder.encode("user", "UTF-8") + "=" + URLEncoder.encode(user, "UTF-8") + "&" + URLEncoder.encode("pass", "UTF-8") + "=" + URLEncoder.encode(pass, "UTF-8");

                bufferedWriter.write(data);
                bufferedWriter.flush();
                bufferedWriter.close();
                outputStream.close();

                InputStream is = httpURLConnection.getInputStream();
                InputStreamReader isr = new InputStreamReader(is, "iso-8859-1");
                BufferedReader bufferedReader = new BufferedReader(isr);

                int c;
                StringBuilder response = new StringBuilder();

                while ((c = bufferedReader.read()) != -1) {
                    //Since c is an integer, cast it to a char. If it isn't -1, it will be in the correct range of char.
                    response.append((char) c);
                }
                String result = response.toString();


                bufferedReader.close();
                httpURLConnection.disconnect();

                return result;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            return null;

        }

        // will be executed after doinbackground is finished
        @Override
        protected void onPostExecute(String s) {


            try {
//array list created
                ArrayList al = new ArrayList();
// json array is created
                JSONArray jsonArray = new JSONArray(s);
// for finding number of objects in the array
                int v = jsonArray.length();

                for (int i = 0; i < v; i++) {


                    al.add(i, jsonArray.getJSONObject(i));


                }

// assigning the static variables of parking_info class the arraylist and the number of objects.

                parking_info.al = al;
                parking_info.no_of_objects = v;


// assigning the static variables of the class parking_info to current latitude and current longitude
                // the value of latt and lonn are found by the function onConnected() on line 225.
                parking_info.current_lat = latt;
                parking_info.current_lon = lonn;


// starts a new activity known as parking_info.
                startActivity(new Intent(Login.this, parking_info.class));
                // the dialog is destroyed.
                dialog.dismiss();

            } catch (JSONException e) {
                e.printStackTrace();

            }

        }


    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
        Toast.makeText(this, "Failed to connect...", Toast.LENGTH_SHORT).show();

    }


    // this function finds current latitude and longitude and stores it in latt and lonn variables.
    @Override
    public void onConnected(Bundle arg0) {

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mGoogleApiClient);

        if (mLastLocation != null) {

            latt = mLastLocation.getLatitude();
            lonn = mLastLocation.getLongitude();


        }

    }

    @Override
    public void onConnectionSuspended(int arg0) {
        Toast.makeText(Login.this, "Connection suspended...", Toast.LENGTH_SHORT).show();

    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


}
