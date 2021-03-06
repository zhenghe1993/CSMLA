package com.jlu.edu.personalspace.wordbook;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.jlu.edu.csmla.R;

import org.json.JSONArray;
import org.json.JSONObject;

import SQLite.MySQLiteImpl;
import utils.MyApplication;
import utils.SysActivity;

/**
 * Created by zhengheming on 2016/2/3.
 */
public class WordsDetailsActivity extends Activity {
    private LinearLayout linearLayout;
    private TextView title;
    private TextView result;
    private ImageView save;
    private TextView back;
    private RequestQueue mQueue;
    private String query;
    String content;
    private boolean isTrue = false;
    private String url = "http://fanyi.youdao.com/openapi.do?keyfrom=jlueducsmla&key=334866769&type=data&doctype=json&version=1.1&q=";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wordsdetails);
        SysActivity.getInstance().addActivity("WordsDetailsActivity", WordsDetailsActivity.this);
        init();
        query();
        click();

    }

    private void click() {
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isTrue) {
                    save.setImageResource(R.mipmap.btn_favorite_nor);
                    new MySQLiteImpl().addWord(content);
                } else {
                    save.setImageResource(R.mipmap.btn_favorite_sel);
                    new MySQLiteImpl().deleteWord(content);
                }


            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }


    private void init() {
        mQueue = Volley.newRequestQueue(this);
        linearLayout = (LinearLayout) findViewById(R.id.words_linear_);
        linearLayout.setVisibility(View.INVISIBLE);
        title = (TextView) findViewById(R.id.words_result_title);
        result = (TextView) findViewById(R.id.words_result);
        save = (ImageView) findViewById(R.id.words_save);
        back = (TextView) findViewById(R.id.words_back);
    }

    private void query() {
        {
            Intent intent = getIntent();
            content = intent.getStringExtra("word");
            String path = url + content.trim();
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(path, null, new Response.Listener<JSONObject>() {

                @Override
                public void onResponse(JSONObject jsonObject) {
                    StringBuilder sb = new StringBuilder();
                    try {
                        String errorCode = jsonObject.getString("errorCode");
                        switch (errorCode) {
                            case "20":
                                Toast.makeText(MyApplication.getAppContext(), "要翻译的文本过长", Toast.LENGTH_SHORT).show();
                                return;
                            case "30":
                                Toast.makeText(MyApplication.getAppContext(), "无法进行有效的翻译", Toast.LENGTH_SHORT).show();
                                return;
                            case "40":
                                Toast.makeText(MyApplication.getAppContext(), "不支持的语言类型", Toast.LENGTH_SHORT).show();
                                return;
                            case "50":
                                Toast.makeText(MyApplication.getAppContext(), "无效的key", Toast.LENGTH_SHORT).show();
                                return;
                            case "60":
                                Toast.makeText(MyApplication.getAppContext(), "无词典结果" + content, Toast.LENGTH_SHORT).show();
                                return;
                            case "0":
                                break;
                            default:
                                return;
                        }

                        query = jsonObject.getString("query");
                        if (jsonObject.has("basic")) {
                            JSONObject basic = jsonObject
                                    .getJSONObject("basic");
                            if (basic.has("us-phonetic")) {
                                String us_phonetic = basic.getString("us-phonetic");
                                sb.append("美式音标：[");
                                sb.append(us_phonetic);
                                sb.append("]    ");
                            }
                            if (basic.has("uk-phonetic")) {
                                String uk_phonetic = basic.getString("uk-phonetic");
                                sb.append("英式音标：[");
                                sb.append(uk_phonetic);
                                sb.append("]\n");
                            }
                            sb.append("\n");
                            sb.append("释义:  ");
                            sb.append(jsonObject.getString("translation"));
                            sb.append("\n");
                            sb.append("\n");
                            if (basic.has("explains")) {
                                JSONArray explains = basic.getJSONArray("explains");
                                for (int i = 0; i < explains.length(); i++) {
                                    sb.append(explains.getString(i));
                                    sb.append("\n");
                                }
                            }
                        }
                        sb.append("网络释义:\n");
                        sb.append("\n");
                        if (jsonObject.has("web")) {
                            JSONArray arr2 = jsonObject.getJSONArray("web");
                            for (int i = 0; i < arr2.length(); i++) {
                                String key = arr2.getJSONObject(i).getString("key");
                                sb.append(key);
                                sb.append(":\n");
                                JSONArray values = arr2.getJSONObject(i).getJSONArray("value");
                                for (int j = 0; j < values.length(); j++) {
                                    sb.append(values.getString(j));
                                }
                                sb.append("\n");
                                sb.append("\n");
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    isTrue = new MySQLiteImpl().queryWord(content);
                    if (isTrue) {
                        save.setImageResource(R.mipmap.btn_favorite_nor);
                    } else {
                        save.setImageResource(R.mipmap.btn_favorite_sel);
                    }
                    title.setText(query);
                    result.setText(sb.toString());
                    linearLayout.setVisibility(View.VISIBLE);
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    System.out.println(volleyError.toString());
                }
            });
            mQueue.add(jsonObjectRequest);

        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            finish();
            return true;
        }
        return false;

    }
}
