package com.example.drawpose;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import it.sauronsoftware.ftp4j.FTPAbortedException;
import it.sauronsoftware.ftp4j.FTPClient;
import it.sauronsoftware.ftp4j.FTPDataTransferException;
import it.sauronsoftware.ftp4j.FTPException;
import it.sauronsoftware.ftp4j.FTPIllegalReplyException;


public class MainActivity extends AppCompatActivity {
    JSONArray arrayObj=null;
    public static String jsonStr = null;
    Float circleRadius = 8.0f;
    int cnt = -1;

    private List<String> jsonBodyPart = Arrays.asList(
            "Nose",
            "LEFT_EYE",
            "RIGHT_EYE",
            "LEFT_EAR",
            "RIGHT_EAR",
            "LEFT_SHOULDER",
            "RIGHT_SHOULDER",
            "LEFT_ELBOW",
            "RIGHT_ELBOW",
            "LEFT_WRIST",
            "RIGHT_WRIST",
            "LEFT_HIP",
            "RIGHT_HIP",
            "LEFT_KNEE",
            "RIGHT_KNEE",
            "LEFT_ANKLE",
            "RIGHT_ANKLE"
    );

    /** List of body joints that should be connected.    */
    private List<Pair> bodyJoints = Arrays.asList(
            new Pair(BodyPart.LEFT_WRIST, BodyPart.LEFT_ELBOW),
            new Pair(BodyPart.LEFT_ELBOW, BodyPart.LEFT_SHOULDER),
            new Pair(BodyPart.LEFT_SHOULDER, BodyPart.RIGHT_SHOULDER),
            new Pair(BodyPart.RIGHT_SHOULDER, BodyPart.RIGHT_ELBOW),
            new Pair(BodyPart.RIGHT_ELBOW, BodyPart.RIGHT_WRIST),
            new Pair(BodyPart.LEFT_SHOULDER, BodyPart.LEFT_HIP),
            new Pair(BodyPart.LEFT_HIP, BodyPart.RIGHT_HIP),
            new Pair(BodyPart.RIGHT_HIP, BodyPart.RIGHT_SHOULDER),
            new Pair(BodyPart.LEFT_HIP, BodyPart.LEFT_KNEE),
            new Pair(BodyPart.LEFT_KNEE, BodyPart.LEFT_ANKLE),
            new Pair(BodyPart.RIGHT_HIP, BodyPart.RIGHT_KNEE),
            new Pair(BodyPart.RIGHT_KNEE, BodyPart.RIGHT_ANKLE)
    );

    private String getJsonString() {
        String json = "";
        try {

            InputStream is = getAssets().open("SendDataResult.json");
            int fileSize = is.available();

            byte[] buffer = new byte[fileSize];
            is.read(buffer);
            is.close();

            json = new String(buffer, "UTF-8");
        }
        catch (IOException ex)
        {
            ex.printStackTrace();
        }
        return json;
    }
    private JSONArray getJasonArray(String jsonStr) throws ParseException {
        Object object=null;
        JSONParser jsonParser=new JSONParser();
        object=jsonParser.parse(jsonStr);
        arrayObj=(JSONArray) object;
        return arrayObj;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewEx viewEx = new ViewEx(this);
        setContentView(viewEx);             //전체를 그릴 canvas로 지정

        //app 시작할때 pose 좌표를 읽어주기(main Thread에서 읽으면 안됨)
        new FTPJsonFileDownLoad().execute();
    }

    protected class ViewEx extends View {
        public ViewEx(Context context) {
            super(context);
        }

        //화면이 지워질 때 마다 다시 호출되는 콜백함수
        public void onDraw(Canvas canvas) {
            if(cnt == -1) {
                if(jsonStr == null) {
                    System.out.println("Null!!");
                    try {
                        TimeUnit.MILLISECONDS.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    invalidate();
                }
                else{   //서버로 부터 jsonStr이 읽히게 되면 그때무터 화면에 그리기를 반복
                    cnt = 0;
                    try {
                        arrayObj = getJasonArray(jsonStr);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            }
            int screenWidth = canvas.getWidth();
            int screenHeight = canvas.getHeight();

            canvas.drawColor(Color.BLACK);  // 화면의 배경 색 결정

            Paint myPaint = new Paint();                        // 그릴 도구 생성
            myPaint.setColor(Color.WHITE);                      // 팬의 색 지정
            myPaint.setStrokeWidth(5f);                         // 팬의 굵기 지정
            if(arrayObj !=null){
                //읽힌 object가 있다면(서버로부터 전송된 json파일이 있다면)
                JSONObject singleJsonObj = (JSONObject) arrayObj.get(cnt);

                //점 찍기
                for(int i = 0 ; i < jsonBodyPart.size();i++){
                    Object value = singleJsonObj.get(jsonBodyPart.get(i));
                    if (!value.equals("None")){
                        JSONArray valueArr = (JSONArray) value;
                        Double xpos = (Double) ((JSONArray) value).get(0);
                        Double ypos = (Double) ((JSONArray) value).get(1);
                        Double adjustedX = xpos * screenWidth;
                        Double adjustedY = ypos * screenHeight;
                        canvas.drawCircle(new Float(adjustedX),new Float(adjustedY),circleRadius,myPaint);
                    }
                }
                //점 연결하기
                for(Pair<BodyPart,BodyPart> line : bodyJoints){
                    if(!singleJsonObj.get(jsonBodyPart.get(line.first.ordinal())).equals("None") &&
                            !singleJsonObj.get(jsonBodyPart.get(line.second.ordinal())).equals("None")){
                        JSONArray one = (JSONArray)singleJsonObj.get(jsonBodyPart.get(line.first.ordinal()));
                        JSONArray two = (JSONArray)singleJsonObj.get(jsonBodyPart.get(line.second.ordinal()));
                        Double oneXpos = (Double) ((JSONArray) one).get(0);
                        Double oneYpos = (Double) ((JSONArray) one).get(1);
                        Double oneAdjustedX = oneXpos * screenWidth;
                        Double oneAdjustedY = oneYpos * screenHeight;

                        Double twoXpos = (Double) ((JSONArray) two).get(0);
                        Double twoYpos = (Double) ((JSONArray) two).get(1);
                        Double twoAdjustedX = twoXpos * screenWidth;
                        Double twoAdjustedY = twoYpos * screenHeight;

                        canvas.drawLine(
                                new Float(oneAdjustedX),
                                new Float(oneAdjustedY),
                                new Float(twoAdjustedX),
                                new Float(twoAdjustedY),
                                myPaint
                        );
                    }
                }
                cnt = (cnt + 1) % arrayObj.size();

                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                invalidate();

            }
        }
    }
}