package com.izv.geolocalizacion;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Path;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.db4o.Db4oEmbedded;
import com.db4o.ObjectContainer;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;


public class Principal extends Activity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, OnMapReadyCallback {

    private GoogleApiClient cliente;
    private Location ultimaLocalizacion;
    private String ultimaDireccion;
    private LocationRequest peticionLocalizaciones;
    double longitud, latitud;
    private ObjectContainer bd;
    int contador = 0;
    private static TextView tvFecha;
    private List<Localizacion> localizaciones;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        bd = Db4oEmbedded.openFile(Db4oEmbedded.newConfiguration(), getExternalFilesDir(null) + "/bd.db4o");
        tvFecha = (TextView) findViewById(R.id.tvFecha);
        if (GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {
            cliente = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            cliente.connect();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
//            tv.setText("");
            List<Localizacion>loc = getLocalizaciones();
            String s = "";
            for(Localizacion l : loc){
                String latitud = l.getLocalizacion().getLatitude()+"";
                String longitud = l.getLocalizacion().getLongitude()+"";
                String fecha = l.getFecha();
                String localidad = l.getLocalidad();
                s+=("Dia: "+fecha+"\n");
//                tv.append("[Latitud: "+latitud+" - Longitud: "+longitud+"]\n");
//                tv.append("Localidad: "+localidad+"\n");
                s+=("/*********************************/ \n");
            }
            Log.v("AAAAAAAAAAAAA",s);
            return true;
        }
        if (id == R.id.action_map) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnected(Bundle bundle) {
        ultimaLocalizacion = LocationServices.FusedLocationApi.getLastLocation(cliente);
        if (ultimaLocalizacion != null) {
            //...
        }
        peticionLocalizaciones = new LocationRequest();
        peticionLocalizaciones.setInterval(10000);
        peticionLocalizaciones.setFastestInterval(5000);
        peticionLocalizaciones.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        LocationServices.FusedLocationApi.requestLocationUpdates(cliente, peticionLocalizaciones, this);
    }

    @Override
    public void onConnectionSuspended(int i) {
        cliente.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        cliente.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        ultimaLocalizacion = location;
        latitud = location.getLatitude();
        longitud = location.getLongitude();
        Log.v("onLocationChanged", location + " " + latitud + " " + longitud);
        ServicioIntent.startActionGeoCode(this, location);
//        if(contador == 0){
//            setContentView(R.layout.mapa);
//            MapFragment mapFragment = (MapFragment) getFragmentManager()
//                    .findFragmentById(R.id.map);
//            mapFragment.getMapAsync(this);
//            contador++;
//        }
//        tv.append(location.getAccuracy()+"\n");
//        tv.append(location.getAltitude()+"\n");
//        tv.append(location.getLatitude()+"\n");
////        location.bearingTo("destino");
//        tv.append(location.getLongitude()+"\n");
//        tv.append("------------------------------------"+"\n");
    }

    private BroadcastReceiver receptor = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle bundle = intent.getExtras();
            Address direccion = bundle.getParcelable("direccion");
            ultimaDireccion = direccion.getLocality();
            String formato = "dd/MM/yyyy";
            SimpleDateFormat sdf = new SimpleDateFormat(formato);
            String fecha = sdf.format(new GregorianCalendar().getTime());
            Localizacion l = new Localizacion(fecha, ultimaLocalizacion);
            l.setLocalidad(ultimaDireccion);
            insert(l);
//            tv.append(direccion.getLocality()+"\n");
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receptor, new IntentFilter("intent"));
    }

    @Override
    protected void onPause() {
        super.onPause();
        bd.close();
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(receptor);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        LatLng localizacion = new LatLng(localizaciones.get(0).getLocalizacion().getLatitude(),
                localizaciones.get(0).getLocalizacion().getLongitude());
        map.setMyLocationEnabled(true);
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(localizacion, 13));
        map.addMarker(new MarkerOptions()
                .title("Inicio")
                .snippet("El recorrido empieza aquí")
                .position(new LatLng(localizaciones.get(0).getLocalizacion().getLatitude(),
                        localizaciones.get(0).getLocalizacion().getLongitude())));
        PolylineOptions rectOptions = new PolylineOptions();
        for (Localizacion l : localizaciones) {
            String latitud = l.getLocalizacion().getLatitude() + "";
            String longitud = l.getLocalizacion().getLongitude() + "";
            rectOptions.add(new LatLng(Double.parseDouble(latitud), Double.parseDouble(longitud)));
        }
        Polyline polyline = map.addPolyline(rectOptions);

    }

    public void insert(Localizacion l) {
        Log.v("insrt", "Entro en el insert: " + l.toString());
        bd.store(l);
        Log.v("insert", "Inserto: " + l.toString());
    }

    public List<Localizacion> getLocalizaciones() {
        Localizacion l = new Localizacion(null, null);
        List<Localizacion> localizaciones = bd.queryByExample(l);
        return localizaciones;
    }

    public List<Localizacion> getLocalizaciones(String fecha) {
        Localizacion l = new Localizacion(fecha, null);
        List<Localizacion> localizaciones = bd.queryByExample(l);
        return localizaciones;
    }

    public void verRecorrido(View v) {
        localizaciones = getLocalizaciones(tvFecha.getText().toString());
        if (localizaciones.size() > 0) {
            setContentView(R.layout.mapa);
            MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        } else {
            Toast.makeText(this, "No hay localizaciones para esa fecha", Toast.LENGTH_LONG).show();
        }
    }

    public void fecha(View v) {
        DialogFragment newFragment = new DatePickerFragment();
        newFragment.show(getFragmentManager(), "fecha");
    }

    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            if (getTag().toString().compareTo("fecha") == 0) {
                month += 1;
                String dia="",mes="";
                if(day<10){
                    dia = 0+""+day;
                }
                if(month<10){
                    mes = 0+""+month;
                }
                tvFecha.setText(dia + "/" + (mes) + "/" + year);
            }
        }
    }
}
