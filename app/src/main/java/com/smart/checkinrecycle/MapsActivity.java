package com.smart.checkinrecycle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;


import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static String IP_ADDRESS = "114.71.61.251/~cip1973/waste";
    private static String TAG = "checkinrecycle";
    private GoogleMap mMap;
    public String u_name, u_id, unicode;
    private DrawerLayout mDrawerLayout;
    private Context context = this;
    private TextView tx_name, tx_id;
    public static Context context_maps;
    public String result_query;
    public String kim;
    private Button qrcode_scan;
    private IntentIntegrator qrScan;
    private String res;
    private static final int REQUEST_ENABLE_BT = 10; // 블루투스 활성화 상태
    private BluetoothAdapter bluetoothAdapter; // 블루투스 어댑터
    private Set<BluetoothDevice> devices; // 블루투스 디바이스 데이터 셋
    private BluetoothDevice bluetoothDevice; // 블루투스 디바이스
    private BluetoothSocket bluetoothSocket = null; // 블루투스 소켓
    private OutputStream outputStream = null; // 블루투스에 데이터를 출력하기 위한 출력 스트림
    private InputStream inputStream = null; // 블루투스에 데이터를 입력하기 위한 입력 스트림
    private Thread workerThread = null; // 문자열 수신에 사용되는 쓰레드
    private byte[] readBuffer; // 수신 된 문자열을 저장하기 위한 버퍼
    private int readBufferPosition; // 버퍼 내 문자 저장 위치
    private int pariedDeviceCount; // 페어링 된 Device 숫자 세기

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent intent = getIntent();
        u_name = intent.getExtras().getString("userName");
        u_id = intent.getExtras().getString("userID");
        unicode = intent.getExtras().getString("Unicode");

        Log.d("u_name", u_name);
        Log.d("u_id", u_id);
        Log.d("unicode", unicode);

        Intent intent2 = new Intent(getApplicationContext(), QrcodeScan.class);


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false); // 기존 title 지우기
        actionBar.setDisplayHomeAsUpEnabled(true); // 뒤로가기 버튼 만들기
        actionBar.setHomeAsUpIndicator(R.drawable.greenmenu); //뒤로가기 버튼 이미지 지정

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        qrcode_scan = (Button) findViewById(R.id.qrcode_scan);

        qrScan = new IntentIntegrator(this);
        qrcode_scan.setOnClickListener(new View.OnClickListener() {//안내시작 버튼을 눌렀을 때 하는 기능
            public void onClick(View v) {
                qrScan.setOrientationLocked(false);
                //scan option
                qrScan.setPrompt("Scanning...");
                //qrScan.setOrientationLocked(false);
                qrScan.initiateScan();
                qrScan.setCameraId(0);
//                Intent intent = new Intent(getApplicationContext(), QrcodeScan.class);//SecondActivity로 이동하는 내용을 intent 객체에 담음
//                intent.putExtra("u_id", u_id);
//                startActivity(intent);//이동을 시작해라

                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); // 기본 어댑터로 설정
                if(bluetoothAdapter == null) // 블루투스 지원하지 않을 때
                {
                    //empty
                }
                else { // 디바이스가 블루투스를 지원 할 때
                    if (bluetoothAdapter.isEnabled()) { // 블루투스가 활성화 상태 (기기에 블루투스가 켜져있음)
                        selectBluetoothDevice(); // 블루투스 디바이스 선택 함수 호출
                    } else { // 블루투스가 비 활성화 상태 (기기에 블루투스가 꺼져있음)
                        // 블루투스를 활성화 하기 위한 다이얼로그 출력
                        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        // 선택한 값이 onActivityResult 함수에서 콜백된다.
                        startActivityForResult(intent, REQUEST_ENABLE_BT);
                    }
                }

            }
        });

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        View nav_header_view = navigationView.getHeaderView(0);
        tx_name = (TextView) nav_header_view.findViewById(R.id.tx_name);
        tx_name.setText(u_name);
        tx_id = (TextView) nav_header_view.findViewById(R.id.tx_id);
        tx_id.setText(u_id);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();

                int id = menuItem.getItemId();
                String title = menuItem.getTitle().toString();


                if (id == R.id.notice) {
                    Toast.makeText(context, title + ": 공지사항 정보를 확인합니다.", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.mypage) {
                    Toast.makeText(context, title + ": 마이페이지 정보를 확인합니다.", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.setup) {
                    Toast.makeText(context, title + ": 설정 정보를 확인합니다.", Toast.LENGTH_SHORT).show();
                } else if (id == R.id.logout) {
                    AlertDialog.Builder alt_bld = new AlertDialog.Builder(navigationView.getContext());
                    alt_bld.setMessage("로그아웃 하시겠습니까?")
                            .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Intent intent = new Intent(MapsActivity.this,MainActivity.class);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                    startActivity(intent);
                                }
                            })
                    .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    })
                    .show();
                }
                return true;

            }
        });
    }
    //Getting the scan results
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
//        switch (requestCode) {
//            case REQUEST_ENABLE_BT :
//                if(requestCode == RESULT_OK) { // '사용'을 눌렀을 때
//                    selectBluetoothDevice(); // 블루투스 디바이스 선택 함수 호출
//                }
//                else { // '취소'를 눌렀을 때
//                    // 여기에 처리 할 드를 작성하세요.
//                }
//                break;
//        }
        if (result != null) {
            //qrcode 가 없으면
            if (result.getContents() == null) {
                Toast.makeText(MapsActivity.this, "취소!", Toast.LENGTH_SHORT).show();
            } else {
                //qrcode 결과가 있으면
                Toast.makeText(MapsActivity.this, "스캔완료!", Toast.LENGTH_SHORT).show();
                try {
                    //data를 json으로 변환
                    JSONObject obj = new JSONObject(result.getContents());
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(MapsActivity.this, result.getContents(), Toast.LENGTH_LONG).show();
                    //res값은 분리수거장 코드
                    res = result.getContents();
                    Log.d("res",res);
                    GetData task = new GetData();//AsyncTask( GetData) 클래스(틀)의 객체(찍어낸 내용) 생성
                    //스캔 받은 값을 서버로 넘겨줘서 DB검색에 이용(select)
                    AsyncTask<String, Void, String> at = task.execute("http://"+IP_ADDRESS+"/query.php",res);//AsyncTask 기능 실행
                    try {
                        result_query = at.get();
                        Log.d("result_query:",result_query);
                        int intResult = Integer.parseInt(result_query);
                        if(intResult== 1)
                        {
                            Toast.makeText(MapsActivity.this, "인증에 성공하였습니다.", Toast.LENGTH_LONG).show();
                            long now = System.currentTimeMillis();
                            Date date = new Date(now);
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                            String getTime = sdf.format(date);

                            Log.d("getTime",getTime);
                            AddData task2 = new AddData();//AsyncTask( GetData) 클래스(틀)의 객체(찍어낸 내용) 생성
                            task2.execute( "http://"+IP_ADDRESS+"/visitor.php",u_id, res,getTime);
                            //u_id : 사용자 코드, res : 분리수거장 코드, getTime : 현재시간

                        }
                        else{
                            Toast.makeText(MapsActivity.this, "인증에 실패하였습니다.", Toast.LENGTH_LONG).show();

                        }

                    } catch (ExecutionException e1) {
                        e1.printStackTrace();
                    } catch (InterruptedException e1) {
                        e1.printStackTrace();
                    }

                }
            }

        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: { // 왼쪽 상단 버튼 눌렀을 때
                mDrawerLayout.openDrawer(GravityCompat.START);
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        context_maps = this;


        Polyline polyline1 = mMap.addPolyline(new PolylineOptions()
                .clickable(true)
                .add(
                        new LatLng(36.350691, 127.456136),
                        new LatLng(36.349160, 127.456540),
                        new LatLng(36.348769, 127.454668),
                        new LatLng(36.350291, 127.454345),
                        new LatLng(36.350691, 127.456136)));

        polyline1.setColor(Color.parseColor("#27B752"));
        // Add a marker in Sydney and move the camera
        LatLng center_bus = new LatLng(36.349664, 127.455552);

        LatLng recycle_A = new LatLng(36.350281, 127.455000);
        LatLng recycle_B = new LatLng(37.497579, 127.050434);
        LatLng recycle_C = new LatLng(37.496873, 127.050053);


        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(center_bus, 18));
        MarkerOptions markerOptions_A = new MarkerOptions();
        markerOptions_A.position(recycle_A);
        markerOptions_A.title("RecA");

        BitmapDrawable bitmapdraw_A=(BitmapDrawable)getResources().getDrawable(R.drawable.rc_marker);
        Bitmap a=bitmapdraw_A.getBitmap();
        Bitmap smallMarker_A = Bitmap.createScaledBitmap(a, 80, 100, false);
        markerOptions_A.icon(BitmapDescriptorFactory.fromBitmap(smallMarker_A));

        mMap.addMarker(markerOptions_A);

        MarkerOptions markerOptions_B = new MarkerOptions();
        markerOptions_B.position(recycle_B);
        markerOptions_B.title("RecC");

        BitmapDrawable bitmapdraw_B=(BitmapDrawable)getResources().getDrawable(R.drawable.rc_marker);
        Bitmap b=bitmapdraw_B.getBitmap();
        Bitmap smallMarker_B = Bitmap.createScaledBitmap(b, 80, 100, false);
        markerOptions_B.icon(BitmapDescriptorFactory.fromBitmap(smallMarker_B));

        mMap.addMarker(markerOptions_B);


        MarkerOptions markerOptions_C = new MarkerOptions();
        markerOptions_C.position(recycle_C);
        markerOptions_C.title("RecC");

        BitmapDrawable bitmapdraw_C=(BitmapDrawable)getResources().getDrawable(R.drawable.rc_marker);
        Bitmap c=bitmapdraw_C.getBitmap();
        Bitmap smallMarker_C = Bitmap.createScaledBitmap(c, 80, 100, false);
        markerOptions_C.icon(BitmapDescriptorFactory.fromBitmap(smallMarker_C));

        mMap.addMarker(markerOptions_C);

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                Toast.makeText(getApplicationContext(), marker.getTitle()+"클릭", Toast.LENGTH_SHORT).show();

                if(marker.getTitle().equals("RecA")){
                    Intent intent = new Intent(getApplicationContext(), Recycle_a.class);//SecondActivity로 이동하는 내용을 intent 객체에 담음
                    startActivity(intent);//이동을 시작해라

                }

                String recycle_a_code = unicode+marker.getTitle();
                GetData task = new GetData();//AsyncTask( GetData) 클래스(틀)의 객체(찍어낸 내용) 생성
                AsyncTask<String, Void, String> at = task.execute( "http://"+IP_ADDRESS+"/recycle_data.php", recycle_a_code);//AsyncTask 기능 실행
                try {
                    result_query = at.get();

                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Log.d("result_query:",result_query);
                return false;
            }
        });
    }

    private class GetData extends AsyncTask<String, Void, String>{

        ProgressDialog progressDialog;//사용자에게 실시간 진행 상태를 알릴수 있다.
        String errorString = null;

        //<실시간 진행 상태 >
        @Override
        //--작업시작, progressDialog 객체를 생성하고 시작한다.--
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(MapsActivity.this,
                    "Please Wait", null, true, true);
        }

        //<디버깅 결과 창에 json값으로 세팅 >
        @Override
        //--작업종료, progressDialog 종료 기능을 구현한다.--
        protected void onPostExecute(String result) { //doInBackground에서 리턴한 값(총 json값)을 새로운 result 문자열에 삽입
            super.onPostExecute(result);

            progressDialog.dismiss();

        }

        //http(php)와 연결하여 값을 보내고 받는 부분
        @Override
        //--작업진행중, progressDialog의 진행 정도를 표현해 준다.--
        protected String doInBackground(String... params) { //execute의 매개변수를 받아와서 사용

            String serverURL = params[0];   //"http://"+IP_ADDRESS+"/query.php"와 "http://"+IP_ADDRESS+"/getjson.php" 값이 매개변수로 날아옴
            String postParameters = "Recode=" + params[1];//Keyword(입력받은 학번값)와 "" 값이 매개변수로 날아옴


            try {
                //<안드로이드와 php연결 >
                URL url = new URL(serverURL); //url객체를 생성하여 serverURL로 초기화함
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();//url과 연결 =  HttpURLConnection


                httpURLConnection.setReadTimeout(5000);//url과 연결,  읽을 시 연결 시간 5000초로 지정
                httpURLConnection.setConnectTimeout(5000);//url과 연결, 서버 접속시 연결 시간 5000초로 지정
                httpURLConnection.setRequestMethod("POST");//요청 방식  post로 지정, 파라미터 값이 url주소에 포함되어 가지 않음
                httpURLConnection.setDoInput(true);//InputStream으로 서버로 부터 응답을 받겠다는 옵션
                httpURLConnection.connect();//url과 연결하겠다

                //<입력한 내용을 php에 전달 >
                OutputStream outputStream = httpURLConnection.getOutputStream();//RequestBody(사용자가 입력한 내용,요청한 내용)에 데이터를 담기 위해 OutputStream 객체를 생성
                outputStream.write(postParameters.getBytes("euc-kr"));//사용자가 입력한 내용을 euc-kr로 세팅
                outputStream.flush();//입력한 내용을 서버에 전송한다.
                outputStream.close();//OutputStream 종료
                //앱에서 데이터 전송

                //<php에서 처리한 값(json)을 안드로이드에 전달 >
                int responseStatusCode = httpURLConnection.getResponseCode();//실제 서버에서 응답한 내용을 받아온다(json)
                Log.d(TAG, "response code - " + responseStatusCode);

                InputStream inputStream; //stream이란 외부에서 들어오는 데이터를 입력받고 출력하는 터널과 같은 중간자 역할 inputStream : 입력을 받아오는 스트림
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {//연결 상태를 확인한다. 연결이 잘 되었다면
                    inputStream = httpURLConnection.getInputStream(); //스트림에 읽어온 값을 넣음
                }
                else{//연결이 잘 안 되었다면
                    inputStream = httpURLConnection.getErrorStream();//스트림에 에러 값을 넣음
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "euc-kr"); //서버에서 읽어온 값(json )을 "euc-kr"형식으로 변환하여 inputStreamReader 객체를 초기화
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);//euc-kr로 변환된 읽어온 값으로 초기화된 객체로 bufferedReader 객체를 초기화함
                //BufferedReader는 버퍼를 이용하기 때문에 이함수를 이용하면 입출력의 효울이 비교할 수 없을 정도로 좋아진다. 그래서 이 함수를 사용한다.

                StringBuilder sb = new StringBuilder();//StringBuilder는 String과 문자열을 더할 때 새로운 객체를 만들지 않고 기존의 데이터에 더하는 방식을 사용하기 때문에 속도가 빠르다.
                String line;//문자열 line 변수를 생 성

                while((line = bufferedReader.readLine()) != null){//버퍼에서 읽은 값을 line 변수에 넣고 이값이 널값이 아니면
                    sb.append(line);//StringBuilder 객체인 sb에 line(버퍼에서 읽은 값)을 추가한다.
                }

                bufferedReader.close();//읽어오는 역할을 하는 버퍼를 종료한다.

                return sb.toString().trim();
            }
            catch (Exception e) {//에러가 발생하면

                Log.d(TAG, "InsertData: Error ", e);//에러 값을 e에 넣는다.
                errorString = e.toString();//e(에러 값)을 문자열로 변환하고 errorString 변수에 넣는다.
                return null;
            }
        }
    }
    private class AddData extends AsyncTask<String, Void, String>{

        ProgressDialog progressDialog;//사용자에게 실시간 진행 상태를 알릴수 있다.
        String errorString = null;

        //<실시간 진행 상태 >
        @Override
        //--작업시작, progressDialog 객체를 생성하고 시작한다.--
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(MapsActivity.this,
                    "Please Wait", null, true, true);
        }

        //<디버깅 결과 창에 json값으로 세팅 >
        @Override
        //--작업종료, progressDialog 종료 기능을 구현한다.--
        protected void onPostExecute(String result) { //doInBackground에서 리턴한 값(총 json값)을 새로운 result 문자열에 삽입
            super.onPostExecute(result);
            progressDialog.dismiss();

        }

        //http(php)와 연결하여 값을 보내고 받는 부분
        @Override
        //--작업진행중, progressDialog의 진행 정도를 표현해 준다.--
        protected String doInBackground(String... params) { //execute의 매개변수를 받아와서 사용

            String serverURL = params[0];   //"http://"+IP_ADDRESS+"/query.php"와 "http://"+IP_ADDRESS+"/getjson.php" 값이 매개변수로 날아옴
            String postParameters = "u_code=" + params[1] + "&r_code=" + params[2] + "&c_time=" + params[3];//Keyword(입력받은 학번값)와 "" 값이 매개변수로 날아옴
            Log.d("param[1]", postParameters);

            try {
                //<안드로이드와 php연결 >
                URL url = new URL(serverURL); //url객체를 생성하여 serverURL로 초기화함
                HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();//url과 연결 =  HttpURLConnection


                httpURLConnection.setReadTimeout(5000);//url과 연결,  읽을 시 연결 시간 5000초로 지정
                httpURLConnection.setConnectTimeout(5000);//url과 연결, 서버 접속시 연결 시간 5000초로 지정
                httpURLConnection.setRequestMethod("POST");//요청 방식  post로 지정, 파라미터 값이 url주소에 포함되어 가지 않음
                httpURLConnection.setDoInput(true);//InputStream으로 서버로 부터 응답을 받겠다는 옵션
                httpURLConnection.connect();//url과 연결하겠다

                //<입력한 내용을 php에 전달 >
                OutputStream outputStream = httpURLConnection.getOutputStream();//RequestBody(사용자가 입력한 내용,요청한 내용)에 데이터를 담기 위해 OutputStream 객체를 생성
                outputStream.write(postParameters.getBytes("euc-kr"));//사용자가 입력한 내용을 euc-kr로 세팅
                outputStream.flush();//입력한 내용을 서버에 전송한다.
                outputStream.close();//OutputStream 종료
                //앱에서 데이터 전송

                //<php에서 처리한 값(json)을 안드로이드에 전달 >
                int responseStatusCode = httpURLConnection.getResponseCode();//실제 서버에서 응답한 내용을 받아온다(json)
                Log.d(TAG, "response code - " + responseStatusCode);

                InputStream inputStream; //stream이란 외부에서 들어오는 데이터를 입력받고 출력하는 터널과 같은 중간자 역할 inputStream : 입력을 받아오는 스트림
                if(responseStatusCode == HttpURLConnection.HTTP_OK) {//연결 상태를 확인한다. 연결이 잘 되었다면
                    inputStream = httpURLConnection.getInputStream(); //스트림에 읽어온 값을 넣음
                }
                else{//연결이 잘 안 되었다면
                    inputStream = httpURLConnection.getErrorStream();//스트림에 에러 값을 넣음
                }


                InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "euc-kr"); //서버에서 읽어온 값(json )을 "euc-kr"형식으로 변환하여 inputStreamReader 객체를 초기화
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);//euc-kr로 변환된 읽어온 값으로 초기화된 객체로 bufferedReader 객체를 초기화함
                //BufferedReader는 버퍼를 이용하기 때문에 이함수를 이용하면 입출력의 효울이 비교할 수 없을 정도로 좋아진다. 그래서 이 함수를 사용한다.

                StringBuilder sb = new StringBuilder();//StringBuilder는 String과 문자열을 더할 때 새로운 객체를 만들지 않고 기존의 데이터에 더하는 방식을 사용하기 때문에 속도가 빠르다.
                String line;//문자열 line 변수를 생 성

                while((line = bufferedReader.readLine()) != null){//버퍼에서 읽은 값을 line 변수에 넣고 이값이 널값이 아니면
                    sb.append(line);//StringBuilder 객체인 sb에 line(버퍼에서 읽은 값)을 추가한다.
                }

                bufferedReader.close();//읽어오는 역할을 하는 버퍼를 종료한다.
                Log.d("sb",sb.toString().trim());
                return sb.toString().trim();

            }
            catch (Exception e) {//에러가 발생하면

                Log.d(TAG, "InsertData: Error ", e);//에러 값을 e에 넣는다.
                errorString = e.toString();//e(에러 값)을 문자열로 변환하고 errorString 변수에 넣는다.
                return null;
            }
        }
    }

    public void selectBluetoothDevice() {
        // 이미 페어링 되어있는 블루투스 기기를 찾습니다.
        devices = bluetoothAdapter.getBondedDevices();
        // 페어링 된 디바이스의 크기를 저장
        pariedDeviceCount = devices.size();
        // 페어링 되어있는 장치가 없는 경우
        if(pariedDeviceCount == 0) {
            // 페어링을 하기위한 함수 호출
        }
        // 페어링 되어있는 장치가 있는 경우
        else {
            // 디바이스를 선택하기 위한 다이얼로그 생성
            androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
            builder.setTitle("분리수거장 출입을 위해 연결해주세요.");
            // 페어링 된 각각의 디바이스의 이름과 주소를 저장
            List<String> list = new ArrayList<>();
            // 모든 디바이스의 이름을 리스트에 추가
            for(BluetoothDevice bluetoothDevice : devices) {
                list.add(bluetoothDevice.getName());
            }
            list.add("나가기");
            // List를 CharSequence 배열로 변경
            final CharSequence[] charSequences = list.toArray(new CharSequence[list.size()]);
            list.toArray(new CharSequence[list.size()]);

            // 해당 아이템을 눌렀을 때 호출 되는 이벤트 리스너
            builder.setItems(charSequences, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 해당 디바이스와 연결하는 함수 호출
                    connectDevice(charSequences[which].toString());
                }
            });

            // 뒤로가기 버튼 누를 때 창이 안닫히도록 설정
            builder.setCancelable(false);
            // 다이얼로그 생성
            androidx.appcompat.app.AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }

    public void connectDevice(String deviceName) {
        // 페어링 된 디바이스들을 모두 탐색
        for (BluetoothDevice tempDevice : devices) {
            // 사용자가 선택한 이름과 같은 디바이스로 설정하고 반복문 종료
            if (deviceName.equals(tempDevice.getName())) {
                bluetoothDevice = tempDevice;
                break;
            }
        }
        // UUID 생성
        UUID uuid = java.util.UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        // SPP 통신
        try {
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            bluetoothSocket.connect();
            // 블루투스 소켓 연결
            // 데이터 송,수신 스트림
            outputStream = bluetoothSocket.getOutputStream();
            inputStream = bluetoothSocket.getInputStream();

            workerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Log.d("result_query",result_query);
                        outputStream.write(Integer.parseInt(result_query));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }

            });
            workerThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

