package com.example.pintest1.navigation;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
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
import androidx.fragment.app.FragmentTransaction;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pintest1.ContentActivity;
import com.example.pintest1.MainActivity;
import com.example.pintest1.R;
import com.example.pintest1.model.ContentDTO;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.ar.core.Anchor;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Plane;
import com.google.ar.core.Pose;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.ArSceneView;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.Node;
import com.google.ar.sceneform.Scene;
import com.google.ar.sceneform.math.Quaternion;
import com.google.ar.sceneform.math.QuaternionEvaluator;
import com.google.ar.sceneform.math.Vector3;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.OnPausedListener;
import com.kyleduo.switchbutton.SwitchButton;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Random;

import static java.lang.Math.PI;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link arsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class arsFragment extends Fragment implements SensorEventListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    ArFragment arFragment;

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

    private FirebaseFirestore firestore;

    private double difflat;
    private double difflong;

    private boolean alreadyin = false;
    private Anchor anchor;
    private ObjectAnimator orbitAnimation;

    private Config config;

    private ImageView reset;
    private ImageView resetlocation;
    private SwitchButton switchButton;

    private Animation tranlateRightAnim;

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

    private class SlidingPageAnimationListener implements Animation.AnimationListener {
        @Override
        public void onAnimationStart(Animation animation) {

        }

        public void onAnimationEnd(Animation animation) {

        }

        @Override
        public void onAnimationRepeat(Animation animation) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        tranlateRightAnim = AnimationUtils.loadAnimation(getActivity(), R.anim.translate_right);
        SlidingPageAnimationListener animListener = new SlidingPageAnimationListener();
        tranlateRightAnim.setAnimationListener(animListener);


        firestore = FirebaseFirestore.getInstance();

        googleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        providerClient = LocationServices.getFusedLocationProviderClient(getActivity());
        googleApiClient.connect();


        providerClient.getLastLocation()
                .addOnSuccessListener(getActivity(), location -> {
                    if (location != null) {
                        for (int i = 0; i < pininfos.size(); i++) {
                            latitude = location.getLatitude();
                            longitude = location.getLongitude();
                            String s1 = latitude + "," + longitude;

                            Log.d("firstlocation", s1);
                            degree(i);
                            distance2(i);
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

                            degree(i); //degree 변경
                            distance2(i); //distance 변경


                        }
                    }
                }
            }
        };
        difflat = LatitudeInDifference(10);
        difflong = LongitudeInDifference(latitude, 10);

        View view = inflater.inflate(R.layout.fragment_ars,
                container, false);
        reset = (ImageView) view.findViewById(R.id.image_button1);
        resetlocation = (ImageView) view.findViewById(R.id.image_button2);
        reset.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {


                for (int i = 0; i < anchorNodes.size(); i++) {
                    anchorNodes.get(i).getAnchor().detach();
                }
                anchorNodes.clear();
                nodes.clear();
                pininfos.clear();

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.detach(arsFragment.this).attach(arsFragment.this).commit();



                providerClient.getLastLocation()
                        .addOnSuccessListener(getActivity(), location -> {
                            if (location != null) {
                                for (int i = 0; i < pininfos.size(); i++) {
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                    String s1 = latitude + "," + longitude;

                                    Log.d("firstlocation", s1);
                                    degree(i);
                                    distance2(i);
                                }
                            }
                        });

                return false;
            }

        });

        resetlocation.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                providerClient.getLastLocation()
                        .addOnSuccessListener(getActivity(), location -> {
                            if (location != null) {
                                for (int i = 0; i < pininfos.size(); i++) {
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                    String s1 = latitude + "," + longitude;

                                    Log.d("firstlocation", s1);
                                    degree(i);
                                    distance2(i);
                                }
                            }
                        });
                String s="현재 좌표는 ["+latitude+","+longitude+"]입니다.";
                Snackbar.make(view,s,Snackbar.LENGTH_SHORT).show();

                return false;
            }

        });
        reset.setVisibility(View.GONE);
        resetlocation.setVisibility(View.GONE);

        SwitchButton switchButton = (SwitchButton) view.findViewById(R.id.sb_use_listener);
        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    reset.setVisibility(View.VISIBLE);
                    reset.startAnimation(tranlateRightAnim);
                    resetlocation.setVisibility(View.VISIBLE);
                    resetlocation.startAnimation(tranlateRightAnim);
                } else {
                    reset.setVisibility(View.GONE);
                    resetlocation.setVisibility(View.GONE);
                }
            }
        });
        switchButton.setChecked(true);
        return view;
    }

    //아래는 범위를 만들기 위한 계산 함수 자신의 latitude-LatitudeInDifference ~ latitude+LatitudeInDifference 가 범위가 된다
    //diff는 거리 여기서는 30m 가 들어갈 예정
    //반경 m이내의 위도차(degree)
    public double LatitudeInDifference(int diff) {
        //지구반지름
        final int earth = 6371000;    //단위m

        return (diff * 360.0) / (2 * Math.PI * earth);
    }

    //반경 m이내의 경도차(degree)
    public double LongitudeInDifference(double _latitude, int diff) {
        //지구반지름
        final int earth = 6371000;    //단위m

        double ddd = Math.cos(0);
        double ddf = Math.cos(Math.toRadians(_latitude));

        return (diff * 360.0) / (2 * Math.PI * earth * Math.cos(Math.toRadians(_latitude)));
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        arFragment = (ArFragment) getChildFragmentManager().findFragmentById(R.id.ux_fragment);

        // arFragment= new ArFragment();

        /*Pininfo pininfo = new Pininfo(latDestination, lngDestination);
        Pininfo pininfo2 = new Pininfo(37.513916, 126.910383);
        pininfos.add(pininfo);
        pininfos.add(pininfo2);*/

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

                    //Toast.makeText(getActivity(), "unable to load", Toast.LENGTH_SHORT).show();
                    Snackbar.make(getActivity().findViewById(android.R.id.content),"unable to load",Snackbar.LENGTH_SHORT).show();
                    return null;
                });

        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdate);


       /* for (int i = 0; i < pininfos.size(); i++) {
            distance2(i);
            degree(i);

            Node node = new Node();
            Context c = getActivity();
            String str = "dist : " + pininfos.get(i).distance;
            node.setOnTouchListener((v, event) -> {
                Toast.makeText(
                        c, str, Toast.LENGTH_LONG).show();

                //anchorNode.removeChild(node);
                return false;

            });
            //anchorNodes.add(anchorNode);
            nodes.add(node);


        }*/
    }

    private void distance2(int i) {
        Location A = new Location("point A");

        A.setLatitude(latitude);
        A.setLongitude(longitude);
        Location B = new Location("point B");
        B.setLatitude(pininfos.get(i).pinlatitude);
        B.setLongitude(pininfos.get(i).pinlontitude);
        String ss = pininfos.get(i).pinlatitude + "," + pininfos.get(i).pinlontitude;
        Log.d("disttt", ss);
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
/*                config = arFragment.getArSceneView().getSession().getConfig();
                config.setFocusMode(Config.FocusMode.AUTO);
                arFragment.getArSceneView().getSession().configure(config);*/
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

    public static float random(float min, float max, int count) {
        float value = new Random().nextFloat() * (max - min) + min;
        return Float.valueOf(String.format("%." + count + "f", value));
    }

    private void getpins(Plane plane, Frame frame) {


        Log.d("sizing", Integer.toString(pininfos.size()));
        Log.d("sessons", String.valueOf(arFragment.getArSceneView().getSession()));
        for (int i = 0; i < pininfos.size(); i++) {
            //distance2(i);
            distance2(i);
            degree(i);

            String check = azimuthinDegress + "," + pininfos.get(i).degree;
            String sss = i + ":" + pininfos.get(i).pinplaced;
            Log.d("heading", Integer.toString((int) azimuthinDegress));
            Log.d("anchor", sss);
            Log.d("degree", Integer.toString((int) pininfos.get(i).degree));
            Log.d("checkequal", check);
            Log.d("distance", String.valueOf(pininfos.get(i).distance));


            if (pininfos.size() > 0) {

                if (pininfos.get(i).pinplaced != true) {
                    if ((pininfos.get(i).degree >= azimuthinDegress - 10) && (pininfos.get(i).degree <= azimuthinDegress + 10)) {  //pin이 주변에 있을때
                        Snackbar.make(getActivity().findViewById(android.R.id.content),"Pinsert",Snackbar.LENGTH_SHORT).show();
                        Vector3 cameraPos = arFragment.getArSceneView().getScene().getCamera().getWorldPosition();
                        Vector3 cameraForward = arFragment.getArSceneView().getScene().getCamera().getForward();
                        Vector3 position = Vector3.add(cameraPos, cameraForward.scaled((float) pininfos.get(i).distance));

                  /*  float x = (float) (pininfos.get(i).distance * Math.cos(DegreeToRadian(pininfos.get(i).degree)));
                    float y = 0;
                    float z = (float) (pininfos.get(i).distance * Math.sin(DegreeToRadian(pininfos.get(i).degree)));*/

                        Pose pose = Pose.makeTranslation(position.x + random(0.1f, 0.3f, 2), 0, position.z + random(0.1f, 0.3f, 2));

                        anchor = null;
                        anchor = arFragment.getArSceneView().getSession().createAnchor(pose);

                        AnchorNode anchorNode = new AnchorNode(anchor);
                        anchorNode.setParent(arFragment.getArSceneView().getScene());
                        nodes.get(i).setParent(anchorNode);
                        nodes.get(i).setRenderable(arsFragment.this.modelRenderable);
                        // nodes.get(i).setLocalRotation(Quaternion.axisAngle(new Vector3(0, 1f, 0), 0));
                        orbitAnimation = createAnimator();
                        orbitAnimation.setTarget(nodes.get(i));
                        orbitAnimation.setDuration(1000);
                        orbitAnimation.start();

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

/*            if(pininfos.get(i).pinplaced == true && Math.abs(pininfos.get(i).degree - azimuthinDegress) >=180 ){
                anchorNodes.get(i).removeChild(nodes.get(i));
                pininfos.get(i).pinplaced=false;
            }*/


        }
    }

    private static ObjectAnimator createAnimator() {
        // Node's setLocalRotation method accepts Quaternions as parameters.
        // First, set up orientations that will animate a circle.
        Quaternion orientation1 = Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), 0);
        Quaternion orientation2 = Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), 120);
        Quaternion orientation3 = Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), 240);
        Quaternion orientation4 = Quaternion.axisAngle(new Vector3(0.0f, 1.0f, 0.0f), 360);

        ObjectAnimator orbitAnimation = new ObjectAnimator();
        orbitAnimation.setObjectValues(orientation1, orientation2, orientation3, orientation4);

        // Next, give it the localRotation property.
        orbitAnimation.setPropertyName("localRotation");

        // Use Sceneform's QuaternionEvaluator.
        orbitAnimation.setEvaluator(new QuaternionEvaluator());

        //  Allow orbitAnimation to repeat forever
        orbitAnimation.setRepeatCount(ObjectAnimator.INFINITE);
        orbitAnimation.setRepeatMode(ObjectAnimator.RESTART);
        orbitAnimation.setInterpolator(new LinearInterpolator());
        orbitAnimation.setAutoCancel(true);

        return orbitAnimation;
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
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();


                    String s2 = latitude + "," + longitude;
                    Log.d("nowgps", s2);

                    firestore.collection("images")
                            .whereGreaterThanOrEqualTo("Latitude", latitude - difflat)
                            .whereLessThanOrEqualTo("Latitude", latitude + difflat).addSnapshotListener(
                            new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@javax.annotation.Nullable QuerySnapshot queryDocumentSnapshots, @javax.annotation.Nullable FirebaseFirestoreException e) {

                                    if (queryDocumentSnapshots == null) return;
                                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                        if (document.toObject(ContentDTO.class).Longitude >= (longitude - difflong)) {
                                            if (document.toObject(ContentDTO.class).Longitude <= (longitude + difflong)) {
                                                if (document.toObject(ContentDTO.class).Latitude != 0 && document.toObject(ContentDTO.class).Longitude != 0) {

                                                    alreadyin = false;
                                                    for (int i = 0; i < pininfos.size(); i++) {
                                                        degree(i); //degree 변경
                                                        distance2(i); //distance 변경

                                                        if (pininfos.get(i).pid.equals(document.getId()) == true) {
                                                            alreadyin = true;
                                                        }
                                                    }

                                                    if (alreadyin == false) {
                                                        Pininfo pininfo = new Pininfo(document.toObject(ContentDTO.class).Latitude, document.toObject(ContentDTO.class).Longitude);
                                                        pininfo.pid = document.getId();
                                                        pininfos.add(pininfo);
                                                        Node node = new Node();
                                                        Context c = getActivity();
                                                        node.setOnTouchListener((v, event) -> {

                                  /*                          config=arFragment.getArSceneView().getSession().getConfig();
                                                            config.setPlaneFindingMode(Config.PlaneFindingMode.DISABLED);
                                                            arFragment.getArSceneView().getSession().configure(config);*/
                                                            Intent intent = new Intent(getContext(), ContentActivity.class);
                                                            intent.putExtra("Content", document.toObject(ContentDTO.class));
                                                            intent.putExtra("pID", document.getId());
                                                            startActivity(intent);
                                                            return false;

                                                        });
                                                        nodes.add(node);
                                                    }


                                                    String s1 = latitude - difflat + "," + latitude + difflat;
                                                    String s = document.toObject(ContentDTO.class).Latitude + "," + document.toObject(ContentDTO.class).Longitude;
                                                    Log.d("checking", "thisis");
                                                    Log.d("bumweey", s1);
                                                    Log.d("thisgps", s);

                                                }
                                            }
                                        }

                                    }
                                }
                            });
                    for (int i = 0; i < pininfos.size(); i++) {

                        degree(i); //degree 변경
                        distance2(i); //distance 변경
                        if (pininfos.size() > 0) {
                            if (pininfos.get(i).distance > 13) { //특정거리 이상시 해당 핀 제거 차후 보정 예정
                                anchorNodes.get(i).removeChild(nodes.get(i));
                                pininfos.get(i).pinplaced = false;
                                anchorNodes.remove(i);
                                nodes.remove(i);
                                pininfos.remove(i);
                            }
                        }
                    }


                }
            }
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        Log.d("pausedd", "pused");

        mSensorManager.unregisterListener((SensorEventListener) this, mAccelerometer);
        mSensorManager.unregisterListener((SensorEventListener) this, mMagnetometer);
        providerClient.removeLocationUpdates(listener);
        if(pininfos.size()>0){
            for (int i = 0; i < anchorNodes.size(); i++) {
                anchorNodes.get(i).getAnchor().detach();
            }
            anchorNodes.clear();
            nodes.clear();
            pininfos.clear();

        }
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.attach(arsFragment.this).commit();

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
        requestingLocationUpdates = true;
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}