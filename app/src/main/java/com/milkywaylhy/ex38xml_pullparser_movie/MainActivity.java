package com.milkywaylhy.ex38xml_pullparser_movie;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    ArrayList<String> items= new ArrayList<String>();
    ListView listView;
    ArrayAdapter adapter;

    //영화진흥위원회 api 사이트에서 발급받은 key
    String apiKey="f5eef3421c602c6cb7ea224104795888";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //리스트뷰 테스트를 위해 더미데이터...
//        items.add("aaa");
//        items.add("bbb");
//        items.add("ccc");

        listView= findViewById(R.id.listView);
        adapter= new ArrayAdapter(this, android.R.layout.simple_list_item_1, items);
        listView.setAdapter(adapter);

    }


    public void clickBtn(View view) {
        //네트워크를 통해서 xml문서를 읽어오기..[Internet permission 주의!]
        //네트워크 작업은 Main Thread가 수행하지 못한다.
        //별도의 Thread에게 네트워크 작업을 수행하도록..
        Thread t= new Thread(){
            @Override
            public void run() {
                //이 앱을 실행하는 날자의 하루전
                Date date= new Date();//현제날짜를 가진 객체
                date.setTime(date.getTime()-(1000*60*60*24));
                //현제시간을 "yyyy/mm/dd"이 형태의 문자열로 만들어야 함.
                //현제시간을 특정 포멧으롬 만들어주는 클레스객체
                SimpleDateFormat sdf= new SimpleDateFormat("yyyyMMdd");
//                int year= date.getYear();
//                int mon = date.getMonth();
//                int day = date.getDate();
//                String s= String.format()

                String dateStr="20201228";
                //api28버전 디바이스 부터는 http주소를 사용하려면..
                //androidNanifest.xml에 http 에

                String address="http://www.kobis.or.kr/kobisopenapi/webservice/rest/boxoffice/searchDailyBoxOfficeList.xml"
                        +"?key="+apiKey
                        +"&targetDt="+dateStr
                        +"&itemPerPage=5";

                try {
                    //위에서 만든 데이터의 인터넷주소(url)에
                    //접속하기 위해 무지개로드 만들어 주는...
                    //해임달(URL) 객체 생성
                    URL url= new URL(address);
                    //해임달에게 무지개로드(InputStream) 열기
                    InputStream is= url.openStream();//바이트 스트림
                    //바이트단위로 읽으면 사용하기 불편하므로 문자단위로
                    //읽어들이는 문자스트림으로 변환!!
                    InputStreamReader isr= new InputStreamReader(is);//문자 스트림

                    //isr를 통해 서버의 데이터를 모두 읽어들여서..
                    //분석해 주는 객체(Parser)에 분석(parse) 의뢰!
                    //분석가 객체를 만들어주는 공장객체에게 분석가 객체 의뢰
                    XmlPullParserFactory factory= XmlPullParserFactory.newInstance();
                    XmlPullParser xpp= factory.newPullParser();

                    //만들어진 분석가에게 isr을 통해 데이터를 읽어오도록..
                    xpp.setInput(isr);

                    //분석작업 시작!!
                    int eventType= xpp.getEventType();

                    //Item 한개의 String 데이터
                    StringBuffer buffer= null;

                    while (eventType!=XmlPullParser.END_DOCUMENT){

                        switch (eventType){
                            case XmlPullParser.START_DOCUMENT:
                                //별도의 Thread는 UI변경이 불가!!
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(MainActivity.this, "파싱 시작!!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                                break;

                            case XmlPullParser.START_TAG:
                                String tagName= xpp.getName();
                                if(tagName.equals("dailyBoxOffice")){
                                    buffer= new StringBuffer();
                                }else if(tagName.equals("rank")){
                                    xpp.next();
                                    buffer.append("순위:"+xpp.getText()+"\n");

                                }else if(tagName.equals("movieNm")){
                                    buffer.append("제목:");
                                    xpp.next();
                                    buffer.append(xpp.getText()+"\n");

                                }else if(tagName.equals("openDt")){
                                    buffer.append("개봉일:");
                                    xpp.next();
                                    buffer.append(xpp.getText()+"\n");

                                }else if(tagName.equals("audiAcc")){
                                    buffer.append("누적관객수:");
                                    xpp.next();
                                    buffer.append(xpp.getText()+"\n");

                                }

                                break;

                            case XmlPullParser.TEXT:
                                break;

                            case XmlPullParser.END_TAG:
                                String tagName2= xpp.getName();
                                if(tagName2.equals("dailyBoxOffice")){
                                    //영화정보 항목1개가 종료...
                                    //그 때까지 StringBuffer에 append 한
                                    //데이터를 리스트뷰가 보여주는 ArrayList에 추가
                                    items.add(buffer.toString());
                                    //화면 변경은 별도 Thread가 할 수 없다!!
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            adapter.notifyDataSetChanged();
                                        }
                                    });

                                }
                                break;

                        }//switch

                        eventType= xpp.next();
                    }//while



                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                }


            }
        };
        t.start();//자동 run메소드 발동
    }
}