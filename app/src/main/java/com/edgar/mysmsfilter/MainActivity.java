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
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private ContentObserver mObserver;
    private Button btnAddConfirm;
    private Button btnReset;
    private final ArrayList<String> mKeywordsList = new ArrayList<>();
    private Switch swViewAll;
    private EditText etInputWord;
    private TextView tvShowAll;
    private Button btnSave;
    //小米金融, 还款, 借, 贷, 钱, 信用, 征信, 征收, 欠, 债, 快易花, 360借条, 钱包, 贷款, 人民币, RMB, rmb, 小花, 催缴
    private String defaultKeywords[] = {"小米金融", "还款", "借", "贷", "钱", "信用", "征信", "征收", "欠", "债", "快易花", "360借条", "钱包", "贷款", "人民币", "RMB", "rmb", "小花", "催缴"};
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_add_confirm:
                    String inputWord = etInputWord.getText().toString();
                    if (inputWord.length() > 0) {
                        mKeywordsList.add(etInputWord.getText().toString());
                        etInputWord.setText("");
                        Toast.makeText(MainActivity.this, "Word added: " + inputWord,
                                Toast.LENGTH_SHORT).show();
                    }
                    showAllWords(tvShowAll, mKeywordsList);
                    break;

                case R.id.btn_reset:
                    mKeywordsList.clear();
                    mKeywordsList.addAll(Arrays.asList(defaultKeywords));
                    showAllWords(tvShowAll, mKeywordsList);
                    btnSave.performClick();
                    break;

                case R.id.btn_save:
                    saveWords(MainActivity.this, mKeywordsList);
                    showAllWords(tvShowAll, mKeywordsList);
                    break;

                default:
                    break;
            }
        }
    };

    private CompoundButton.OnCheckedChangeListener mOnCheckListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            switch (buttonView.getId()) {
                case R.id.sw_view_all:
                    if (isChecked) {
                        tvShowAll.setVisibility(View.VISIBLE);
                        showAllWords(tvShowAll, mKeywordsList);
                    } else {
                        tvShowAll.setVisibility(View.INVISIBLE);
                    }
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

        boolean isFromReboot = getIntent().getBooleanExtra("FROM_REBOOT", false);
        if (isFromReboot) {
            Toast.makeText(this, "Sms is rebooted!", Toast.LENGTH_SHORT).show();
        }

        btnAddConfirm = findViewById(R.id.btn_add_confirm);
        btnSave = findViewById(R.id.btn_save);
        btnReset = findViewById(R.id.btn_reset);
        swViewAll = findViewById(R.id.sw_view_all);
        etInputWord = findViewById(R.id.et_enter_words);
        tvShowAll = findViewById(R.id.tv_show_all);

        btnAddConfirm.setOnClickListener(mOnClickListener);
        btnReset.setOnClickListener(mOnClickListener);
        btnSave.setOnClickListener(mOnClickListener);
        swViewAll.setOnCheckedChangeListener(mOnCheckListener);

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
                    msgId = cursor.getLong(cursor.getColumnIndex("_id"));
                    msgDate = cursor.getLong(cursor.getColumnIndex("date"));
                    msgBody = cursor.getString(cursor.getColumnIndex("body"));

                    int delCount = 0;
                    for (String word : mKeywordsList) {
                        if (msgBody.contains(word)) {
                            delCount = resolver.delete(Telephony.Sms.CONTENT_URI,
                                    "_id=" + msgId, null);
                            break;
                        }
                    }
//                    if (delCount == 1) {
//                        Toast.makeText(MainActivity.this, "Message deleted!", Toast.LENGTH_SHORT).show();
//                    }
                }
                cursor.close();
            }
        };
        getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, mObserver);

    }

    @Override
    protected void onResume() {
        super.onResume();

        ArrayList<String> temp = getWords(this);
        mKeywordsList.clear();
        if (temp == null) {
            mKeywordsList.addAll(Arrays.asList(defaultKeywords));
//            Toast.makeText(this, "Empty SharedPreference!", Toast.LENGTH_SHORT).show();
        } else {
//            Toast.makeText(this, "Non empty SharedPreference!", Toast.LENGTH_SHORT).show();
            mKeywordsList.addAll(temp);
        }
        swViewAll.setChecked(false);
        showAllWords(tvShowAll, mKeywordsList);
    }

    @Override
    protected void onPause() {
        super.onPause();
        deleteAllBadMsgs(this);
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

    private void deleteAllBadMsgs(Context context) {
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(Uri.parse("content://sms"),
                new String[]{"_id", "date", "body"}, null, null,
                "date desc");
        if (cursor == null) return;
        long msgId;
        long msgDate = 0;
        String msgBody;
        if (cursor.getCount() > 0 && cursor.moveToFirst()) {
            int index_id = cursor.getColumnIndex("_id");
            int index_date = cursor.getColumnIndex("date");
            int index_body = cursor.getColumnIndex("body");

            int delCount = 0;

            do {
                msgId = cursor.getLong(index_id);
                msgDate = cursor.getLong(index_date);
                msgBody = cursor.getString(index_body);
                for (String word : mKeywordsList) {
                    if (msgBody.contains(word)) {
                        delCount += resolver.delete(Telephony.Sms.CONTENT_URI,
                                "_id=" + msgId, null);
                    }
                }
            } while (cursor.moveToNext());
//            if (delCount > 0) {
//                Toast.makeText(context, String.valueOf(delCount) +
//                        " Messages deleted!", Toast.LENGTH_SHORT).show();
//            }
        }
        cursor.close();
    }

    private void showAllWords(TextView textView, ArrayList<String> keyWords) {
        if (textView == null || keyWords.size() == 0) return;
        String outputWords = "";
        for (String str : keyWords) {
            outputWords = outputWords.concat(str + ", ");
        }
        textView.setText(outputWords);
    }

}
