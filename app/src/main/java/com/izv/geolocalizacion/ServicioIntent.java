package com.izv.geolocalizacion;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class ServicioIntent extends IntentService {

    private static final String ACCION_GEOCODE = "com.izv.geolocalizacion.action.GEOCODE";
    private static final String EXTRA_PARAM_LOC= "com.izv.geolocalizacion.action.LOCATION";

    // TODO: Customize helper method
    public static void startActionGeoCode(Context context, Location location) {
        Intent intent = new Intent(context, ServicioIntent.class);
        intent.setAction(ACCION_GEOCODE);
        intent.putExtra(EXTRA_PARAM_LOC, location);
        context.startService(intent);
        /**-------------Servicio Foreground---------------**/
//        Intent i=new Intent(context, ServicioIntent.class);
//        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|
//                Intent.FLAG_ACTIVITY_SINGLE_TOP);
//        Notification.Builder constructorNotificacion = new
//                Notification.Builder(context)
//                .setSmallIcon(R.drawable.ic_launcher)
//                .setContentTitle("notificación servicio")
//                .setContentText("texto servicio")
//                .setContentIntent(PendingIntent.getActivity(context, 0, i, 0));
//        NotificationManager gestorNotificacion = (NotificationManager) getSystemService(context.NOTIFICATION_SERVICE);
//        startForeground(1, constructorNotificacion.build());
    }


    public ServicioIntent() {
        super("ServicioIntent");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        //Su ejecución es en una hebra
        if (intent != null) {
            final String action = intent.getAction();
            Bundle b = intent.getExtras();
            Location location = b.getParcelable(EXTRA_PARAM_LOC);
            if(location != null){
                handleAccionGeodecode(location);
            }
        }
    }

    private void handleAccionGeodecode(Location location) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> direcciones =geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            Intent i = new Intent("intent");
            i.putExtra("direccion",direcciones.get(0));
            sendBroadcast(i);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
