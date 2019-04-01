package com.edgar.mysmsfilter;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Telephony;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private ContentObserver mObserver;
    private Button btnAddConfirm;
    private Button btnViewAll;
    private final ArrayList<String> finalKeywords = new ArrayList<>();
    private Button btnReset;
    private EditText etInputWord;
    private TextView tvShowAll;
    private String defaultKeywords[] = {"服务厅"};
    private Button btnSave;
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_add_confirm:
                    String inputWord = etInputWord.getText().toString();
                    if (inputWord.length() > 0) {
                        finalKeywords.add(etInputWord.getText().toString());
                        etInputWord.setText("");
                        Toast.makeText(MainActivity.this, "Text added: " + inputWord,
                                Toast.LENGTH_SHORT).show();
                    }
                    break;

                case R.id.btn_view_all:
                    String outputWords = "";
                    for (String str : finalKeywords) {
                        outputWords = outputWords.concat(str + ", ");
                    }
                    tvShowAll.setText(outputWords);
                    break;

                case R.id.btn_reset:
                    finalKeywords.clear();
                    finalKeywords.addAll(Arrays.asList(defaultKeywords));
                    break;

                case R.id.btn_save:
                    saveWords(getApplicationContext(), finalKeywords);
                    break;

                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAddConfirm = findViewById(R.id.btn_add_confirm);
        btnViewAll = findViewById(R.id.btn_view_all);
        btnSave = findViewById(R.id.btn_save);
        btnReset = findViewById(R.id.btn_reset);
        etInputWord = findViewById(R.id.et_enter_words);
        tvShowAll = findViewById(R.id.tv_show_all);

        btnAddConfirm.setOnClickListener(mOnClickListener);
        btnViewAll.setOnClickListener(mOnClickListener);
        btnReset.setOnClickListener(mOnClickListener);
        btnSave.setOnClickListener(mOnClickListener);

        mObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                super.onChange(selfChange);

                ContentResolver resolver = getContentResolver();
                Cursor cursor = resolver.query(Uri.parse("content://sms"),
                        new String[]{"_id", "date", "body"}, null, null,
                        "date desc");
                if (cursor == null) return;
                long msgId;
                long msgDate = 0;
                String msgBody;
                if (cursor.getCount() > 0 && cursor.moveToFirst()) {
                    msgId = cursor.getLong(0);
                    msgDate = cursor.getLong(1);
                    msgBody = cursor.getString(2);

                    int delCount = 0;
                    for (String word : finalKeywords) {
                        if (msgBody.contains(word)) {
                            delCount = resolver.delete(Telephony.Sms.CONTENT_URI,
                                    "_id=" + msgId, null);
                            break;
                        }
                    }
                    if (delCount == 1) {
                        Toast.makeText(MainActivity.this, "Message deleted!", Toast.LENGTH_SHORT).show();
                    }
                }
                cursor.close();
            }
        };
        getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, mObserver);

    }

    @Override
    protected void onResume() {
        super.onResume();

        ArrayList<String> temp = getWords(getApplicationContext());
        finalKeywords.clear();
        if (temp == null) {
            finalKeywords.addAll(Arrays.asList(defaultKeywords));
            Toast.makeText(this, "Empty SharedPreference!", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Non empty SharedPreference!", Toast.LENGTH_SHORT).show();
            finalKeywords.addAll(temp);
        }
    }

    private void saveWords(Context context, ArrayList<String> wordsList) {
        if (wordsList.size() == 0) return;
        SharedPreferences sharedPreferences = context.getSharedPreferences("KEYWORDS_LIST",
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.putInt("LENGTH", wordsList.size());
        for (String word : wordsList) {
            editor.putString("INDEX_" + String.valueOf(wordsList.indexOf(word)), word);
        }
        Toast.makeText(context, "All keywords saved!", Toast.LENGTH_SHORT).show();
        editor.apply();
    }

    private ArrayList<String> getWords(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("KEYWORDS_LIST",
                Context.MODE_PRIVATE);
        int length = sharedPreferences.getInt("LENGTH", 0);
        if (length == 0) return null;

        final ArrayList<String> result = new ArrayList<>();
        for (int i = 0; i < length; i++) {
            String key = "INDEX_" + String.valueOf(i);
            result.add(i, sharedPreferences.getString(key, "ERROR_EMPTY_WORD"));
        }
        return result;
    }

}
