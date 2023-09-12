package com.smart.checkinrecycle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class Recycle_a extends AppCompatActivity {

    private static String TAG = "checkinrecycle";
    private TextView mRecycleTitle;
    private String mJsonString;
    ImageView imgGarbageCan;
    TextView distanceText;
    Button btnConnect;
    Button btnSend;
    ImageView btnHome;
    private int img;
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
    String percent;
    double distance = 0;

    public Recycle_a(BluetoothAdapter bluetoothAdapter) {
        this.bluetoothAdapter = bluetoothAdapter;
    }
    public Recycle_a(){

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycle_a);

        mRecycleTitle = (TextView)findViewById(R.id.title);

        String result_query = ((MapsActivity) MapsActivity.context_maps).result_query;
        Log.d("result_query_Recycle_a", result_query);
        if (result_query != null) { //받아온 json값이 널이 아니면
           mJsonString = result_query; //json 값을 mJsonString 변수에 넣고
            showResult(); //결과 값을 출력하는 함수 호출*/
        }

        imgGarbageCan = (ImageView)findViewById(R.id.imgGarbageCan);
        distanceText = (TextView)findViewById(R.id.distanceText);
//        btnConnect = (Button)findViewById(R.id.btnConnect);
        btnHome = (ImageView)findViewById(R.id.btnHome);
//        btnConnect.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if(view.getId()==R.id.btnConnect) // 다시 연결하고 싶을 때
//                {
//                    if(bluetoothAdapter.isEnabled())
//                    { // 블루투스가 활성화 상태라면
//                        selectBluetoothDevice(); // 블루투스 디바이스 선택 함수 호출
//                    }
//                }
//            }
//        });

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(view.getId()==R.id.btnHome)
                {
                    finish();
                }
            }
        });

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ENABLE_BT :
                if(requestCode == RESULT_OK) { // '사용'을 눌렀을 때
                    selectBluetoothDevice(); // 블루투스 디바이스 선택 함수 호출
                }
                else { // '취소'를 눌렀을 때
                    // 여기에 처리 할 드를 작성하세요.
                }
                break;
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
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("분리수거장 상황이 궁금하시다면 연결하여 확인해주세요.");
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
            AlertDialog alertDialog = builder.create();
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
            // 데이터 수신 함수 호출
            final Handler handler = new Handler();
            // 데이터를 수신하기 위한 버퍼를 생성
            readBufferPosition = 0;
            readBuffer = new byte[1024];
            // 데이터를 수신하기 위한 쓰레드 생성
            workerThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (true) {
                        try {
                            // 데이터를 수신했는지 확인합니다.
                            int byteAvailable = inputStream.available();
                            //데이터가 수신 된 경우
                            if (byteAvailable > 0) {
                                // 입력 스트림에서 바이트 단위로 읽어 옵니다.
                                byte[] bytes = new byte[byteAvailable];
                                inputStream.read(bytes);
                                // 입력 스트림 바이트를 한 바이트씩 읽어 옵니다.
                                for (int i = 0; i < byteAvailable; i++) {
                                    byte tempByte = bytes[i];
                                    // 개행문자를 기준으로 받음(한줄)
                                    if (tempByte == '\n') {
                                        // readBuffer 배열을 encodedBytes로 복사
                                        byte[] encodedBytes = new byte[readBufferPosition];
                                        System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                        // 인코딩 된 바이트 배열을 문자열로 변환
                                        final String text = new String(encodedBytes, "US-ASCII");
                                        Log.d("text", text);
                                        readBufferPosition = 0;
                                        String[] textArray = text.split("\n"); // 받아온 데이터를 공백을 기준으로 자름
                                        // 습도 온도 순으로 전송되게 하였으므로 공백을 기준으로 잘라 각각 값을 넣어준다
                                        distance = Double.parseDouble(textArray[0]);
                                        setLayoutBySensorValue(distance);

                                    } // 개행 문자가 아닐 경우
                                    else {
                                        readBuffer[readBufferPosition++] = tempByte;
                                    }
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        try {
                            // 1초마다 받아옴
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            workerThread.start();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    String calculate(double _distance)
    {
        String _percent;
        if(_distance <= 4)
        {
            _percent = "100%";
        }
        else if(_distance <= 8)
        {
            _percent = "80%";
        }
        else if(_distance <= 12 )
        {
            _percent = "60%";
        }
        else if(_distance <= 16)
        {
            _percent = "40%";
        }
        else if(_distance <= 20)
        {
            _percent = "20%";
        }
        else
        {
            _percent = "거의 없음";
        }
        return _percent;
    }

    int setImg(double _distance)
    {
        int img;
        if(_distance <= 4)
        {
            img=R.drawable.hundred;
        }
        else if(_distance <= 8)
        {
            img=R.drawable.eighty;
        }
        else if(_distance <= 12 )
        {
            img=R.drawable.sixty;
        }
        else if(_distance <= 16)
        {
            img=R.drawable.forty;
        }
        else if(_distance <= 20)
        {
            img=R.drawable.twenty;
        }
        else
        {
            img=R.drawable.trash;
        }
        return img;
    }
    private void setLayoutBySensorValue(final double _distance){
        percent = calculate(_distance);
        img = setImg(_distance);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                distanceText.setText(percent);
                imgGarbageCan.setImageResource(img);

            }
        });
    }
    private void showResult() {
        String TAG_JSON = "Recycle";          //Student에 저장되어 있는 값들(전체 값)을 TAG_JSON 변수에 저장
        String TAG_RECODE = "Recode";     //Snumber로 저장되어 있는 값들(전체 값)을 TAG_SNUMBER 변수에 저장
        String TAG_RECODE_NAME = "Recode_name";     //Rnumber로 저장되어 있는 값들(전체 값)을 TAG_RNUMBER 변수에 저장

        //JSONObject JSON형태의 데이터 관리
        try {
            JSONObject jsonObject = new JSONObject(mJsonString); //json값을 저장하고 있는 변수(mJsonString)로 jsonObject 객체를 초기화
            JSONArray jsonArray = jsonObject.getJSONArray(TAG_JSON);//Student에 저장되어 있는 값들(전체 값)을 jsonArray에 넣음

            for (int i = 0; i < jsonArray.length(); i++) {//student에 저장되어 있는 값만큼 반복

                JSONObject item = jsonArray.getJSONObject(i);//jsonArray에서 JSONObject값을 배열 값들 수만큼 반복하면서 가져와 item에 저장

                String Recode = item.getString(TAG_RECODE);//item에서 TAG_SNUMBER(Snumber)값만 가져와 Snumber 변수에 넣는다.
                String Recode_name = item.getString(TAG_RECODE_NAME);//item에서 TAG_RNUMBER(Rnumber)값만 가져와 Rnumber 변수에 넣는다.
                mRecycleTitle.setText(Recode_name);
            }

        } catch (JSONException e) {

            Log.d(TAG, "showResult : ", e);
        }
    }
}