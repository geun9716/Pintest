package com.example.pintest1.navigation;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pintest1.MainActivity;
import com.example.pintest1.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.ar.core.Anchor;
import com.google.ar.core.Frame;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;

import java.util.ArrayList;
import java.util.Iterator;

import static java.lang.Math.PI;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link arsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class arsFragment extends Fragment implements SensorEventListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    ArFragment arFragment;
    Scene scene;
    ArSceneView arSceneView;
    private FusedLocationProviderClient providerClient;
    private GoogleApiClient googleApiClient;

    private ModelRenderable modelRenderable = null;

    private TextView txtResult;
    private TextView degResult;
    private TextView headResult;

    double longitude;
    double latitude;

    double degrees = 0;
    double lngDestination = 126.910366;
    double latDestination = 37.513901;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;
    private float[] mLastAccelorometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelorometerSet = false;
    private boolean mLastMagnetometerSet = false;
    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];
    private float mCurrentDegree = 0f;

    float azimuthinDegress = 0;

    boolean requestingLocationUpdates = false;

    LocationRequest locationRequest = new LocationRequest();

    private ArrayList<Pininfo> pininfos = new ArrayList<Pininfo>();
    private ArrayList<AnchorNode> anchorNodes = new ArrayList<AnchorNode>();
    private ArrayList<Node> nodes = new ArrayList<Node>();

    public arsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment arsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static arsFragment newInstance(String param1, String param2) {
        arsFragment fragment = new arsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        //arFragments.getArSceneView().getScene();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment



        return inflater.inflate(R.layout.fragment_ars, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        arFragment = (ArFragment) getChildFragmentManager().findFragmentById(R.id.ux_fragment);
        // arFragment= new ArFragment();
        arSceneView=arFragment.getArSceneView();
        scene=arFragment.getArSceneView().getScene();



        googleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        providerClient = LocationServices.getFusedLocationProviderClient(getActivity());

        Pininfo pininfo = new Pininfo(latDestination, lngDestination);
        Pininfo pininfo2 = new Pininfo(37.513916, 126.910383);
        pininfos.add(pininfo);
        pininfos.add(pininfo2);

        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        providerClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            for (int i = 0; i < pininfos.size(); i++) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                                degree(i);
                                distance2(i);
                            }
                        }
                    }
                });
        LocationCallback listener = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                if (locationResult != null) {
                    for (Location location : locationResult.getLocations()) {
                        for (int i = 0; i < pininfos.size(); i++) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            degree(i);
                            distance2(i);
                            String txt;
                            txt = "좌표 :" + "latitude :" + latitude + "longtitude : " + longitude;
                            txtResult.setText(txt);
                        }
                    }
                }
            }
        };
        googleApiClient.connect();

        mSensorManager = (SensorManager) getActivity().getSystemService(getActivity().SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);


        ModelRenderable.builder()
                .setSource(getActivity(), R.raw.thispin)
                .build()

                .thenAccept(
                        modelRenderable ->
                                this.modelRenderable = modelRenderable)
                .exceptionally(throwable ->
                {
                    Toast.makeText(getActivity(), "unable to load", Toast.LENGTH_SHORT).show();
                    return null;
                });

        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdate);

        for (int i = 0; i < pininfos.size(); i++) {
            distance2(i);
            degree(i);
            /*float x = (float) (pininfos.get(i).distance * Math.cos(degrees));
            float y = 0;
            float z = (float) (pininfos.get(i).distance * Math.sin(degrees));

           Vector3 cameraPos = arFragment.getArSceneView().getScene().getCamera().getWorldPosition();
            Vector3 cameraForward = arFragment.getArSceneView().getScene().getCamera().getForward();
            Vector3 position = Vector3.add(cameraPos, cameraForward.scaled((float) pininfos.get(i).distance));


            Pose pose = Pose.makeTranslation(position.x, 0, position.z);
            //Anchor anchor = arFragment.getArSceneView().getSession().createAnchor(pose);
            //AnchorNode anchorNode = new AnchorNode(anchor);
            //anchorNode.setParent(arFragment.getArSceneView().getScene());*/



            Node node = new Node();
            Context c =getActivity();
            String str = "dist : " +  pininfos.get(i).distance;
            node.setOnTouchListener((v, event) -> {
                Toast.makeText(
                        c, str, Toast.LENGTH_LONG).show();

                //anchorNode.removeChild(node);
                return false;

            });
            //anchorNodes.add(anchorNode);
            nodes.add(node);


        }
    }
    private void distance2(int i) {
        Location A = new Location("point A");

        A.setLatitude(latitude);
        A.setLongitude(longitude);
        Location B = new Location("point B");
        B.setLatitude(pininfos.get(i).pinlatitude);
        B.setLongitude(pininfos.get(i).pinlontitude);

        double distanced = A.distanceTo(B);
        pininfos.get(i).distance = distanced;
    }
    private void degree(int i) {


        double lat1 = latitude / 180 * PI;
        double lng1 = longitude / 180 * PI;
        double lat2 = (pininfos.get(i).pinlatitude) / 180 * PI;
        double lng2 = (pininfos.get(i).pinlontitude) / 180 * PI;


        double x = Math.sin(lng2 - lng1) * Math.cos(lat2);
        double y = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1) * Math.cos(lat2) * Math.cos(lng2 - lng1);

        double tan2 = Math.atan2(x, y);
        double degre = tan2 * 180 / PI;

        if (degre < 0) {
            degre = degre + 360;
        }

        pininfos.get(i).degree = degre;
    }

    public void onUpdate(FrameTime frameTime) {

        Frame frame = arFragment.getArSceneView().getArFrame();

        if (frame != null) {
            //Iterator var3=frame.getUpdatedTrackables(Plane.class).iterator();

            for (Object o : frame.getUpdatedTrackables(Plane.class)) {
                //Object o = frame.getUpdatedTrackables(Plane.class);
                Plane plane = (Plane) o;

                if (plane.getTrackingState() == TrackingState.TRACKING) {
                    arFragment.getPlaneDiscoveryController().hide();
                    //=degree();
                    Iterator iterableAnchor = frame.getUpdatedAnchors().iterator();
                    if (!iterableAnchor.hasNext()) {
                        getpins(plane, frame);
                    }
                }
            }
        }

    }
    private void getpins(Plane plane, Frame frame) {


        for (int i = 0; i < pininfos.size(); i++) {
            //distance2(i);
            distance2(i);
            degree(i);

           /* if (pininfos.size()>0 && pininfos.get(i).distance > 13) { //특정거리 이상시 해당 핀 제거 차후 보정 예정
                anchorNodes.get(i).removeChild(nodes.get(i));
                anchorNodes.remove(i);
                nodes.remove(i);
                pininfos.remove(i);
            }*/
/*            if(pininfos.get(i).pinplaced == true && Math.abs(pininfos.get(i).degree - azimuthinDegress) >=180 ){
                anchorNodes.get(i).removeChild(nodes.get(i));
                pininfos.get(i).pinplaced=false;
            }*/
            if (pininfos.get(i).pinplaced != true) {
                if ((pininfos.get(i).degree >= azimuthinDegress - 3) && (pininfos.get(i).degree <= azimuthinDegress + 3)) {  //pin이 주변에 있을때

                    //Toast.makeText(getActivity(), "walk", Toast.LENGTH_SHORT).show();
                    Vector3 cameraPos = arFragment.getArSceneView().getScene().getCamera().getWorldPosition();
                    Vector3 cameraForward = arFragment.getArSceneView().getScene().getCamera().getForward();
                    Vector3 position = Vector3.add(cameraPos, cameraForward.scaled((float) pininfos.get(i).distance));

                  /*  float x = (float) (pininfos.get(i).distance * Math.cos(DegreeToRadian(pininfos.get(i).degree)));
                    float y = 0;
                    float z = (float) (pininfos.get(i).distance * Math.sin(DegreeToRadian(pininfos.get(i).degree)));*/

                    Pose pose = Pose.makeTranslation(position.x, 0,position.z);

                    Anchor anchor = arFragment.getArSceneView().getSession().createAnchor(pose);
                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());

                    nodes.get(i).setLocalRotation(Quaternion.axisAngle(new Vector3(0, 1f, 0), 90));
                    nodes.get(i).setRenderable(this.modelRenderable);
                    nodes.get(i).setParent(anchorNode);
                    anchorNodes.add(anchorNode);
                    pininfos.get(i).pinplaced = true;
             /*       Toast.makeText(this, "walk", Toast.LENGTH_SHORT).show();
                    // List<HitResult> hitTest = frame.hitTest(getScreenCenter().x, getScreenCenter().y);
                    Log.d(TAG, "here2");
                    // Iterator hitTestIterator = hitTest.iterator();
                    //  if (hitTestIterator.hasNext()) {
                    //HitResult hitResult = (HitResult) hitTestIterator.next();



                    float x = (float) (pininfos.get(i).distance * Math.cos(degrees));
                    float y = 0;
                    float z = (float) (pininfos.get(i).distance * Math.sin(degrees));

                    //float y=modelAnchor.getPose().compose(Pose.makeTranslation(0f, 0.05f, 0)).ty();

                    //float z=modelAnchor.getPose().tz();
                    // float y=modelAnchor.getPose().ty();
                    //거리 맞춰서 표시해야함
                    String ck = "xis : " + pininfos.get(i).distance;
                    String ck2 = "xis2 : " + y;
                    String ck3 = "xis3 : " + z;
                    Log.d(TAG, ck);
                    Log.d(TAG, ck2);
                    Log.d(TAG, ck3);



                    Vector3 cameraPos = arFragment.getArSceneView().getScene().getCamera().getWorldPosition();
                    Vector3 cameraForward = arFragment.getArSceneView().getScene().getCamera().getForward();
                    Vector3 position = Vector3.add(cameraPos, cameraForward.scaled((float) pininfos.get(i).distance));
;

                    Pose pose = Pose.makeTranslation(position.x, 0, position.z);
                    Anchor anchor = arFragment.getArSceneView().getSession().createAnchor(pose);

                    AnchorNode anchorNode = new AnchorNode(anchor);
                    anchorNode.setParent(arFragment.getArSceneView().getScene());

                    Vector3 direction = Vector3.subtract(cameraPos,anchorNode.getWorldPosition());
                    Quaternion lookRotation=Quaternion.lookRotation(direction,Vector3.up());


                    Node node = new Node();
                    node.setLocalRotation(Quaternion.axisAngle(new Vector3(0, 1f, 0), 90));
                    node.setRenderable(MainActivity.this.modelRenderable);

                    node.setParent(anchorNode);


                    Context c = this;

                    String str = "dist : " +  pininfos.get(i).distance;

                    node.setOnTouchListener((v, event) -> {
                        Toast.makeText(
                                c, str, Toast.LENGTH_LONG).show();

                        //anchorNode.removeChild(node);
                        return false;


                    });
                    anchorNodes.add(anchorNode);
                    nodes.add(node);
                    pininfos.get(i).pinplaced=true;
*/
                    //   }

                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (requestingLocationUpdates) {
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            providerClient.requestLocationUpdates(locationRequest, listener, null);
        }
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mSensorManager.registerListener((SensorEventListener) this, mAccelerometer, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener((SensorEventListener) this, mMagnetometer, SensorManager.SENSOR_DELAY_GAME);
    }
    private LocationCallback listener = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult != null) {
                for (Location location : locationResult.getLocations()) {
                    for (int i = 0; i < pininfos.size(); i++) {
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();

                        degree(i); //degree 변경
                        distance2(i); //distance 변경


                    }
                }
            }
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener((SensorEventListener) this, mAccelerometer);
        mSensorManager.unregisterListener((SensorEventListener) this, mMagnetometer);
        providerClient.removeLocationUpdates(listener);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mAccelerometer) {
            System.arraycopy(event.values, 0, mLastAccelorometer, 0, event.values.length);
            mLastAccelorometerSet = true;
        } else if (event.sensor == mMagnetometer) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }

        if (mLastAccelorometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(mR, null, mLastAccelorometer, mLastMagnetometer);
            azimuthinDegress = (int) (Math.toDegrees(SensorManager.getOrientation(mR, mOrientation)[0]) + 360) % 360;

            mCurrentDegree = -azimuthinDegress;



            //degrees=degree();
            // String check2;
            // check2="bearing : "+degree();
            // headResult.setText(check2);

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        providerClient.requestLocationUpdates(locationRequest, listener, null);
        requestingLocationUpdates=true;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}