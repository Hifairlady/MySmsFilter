package com.edgar.mysmsfilter;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Telephony;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private ContentObserver mObserver;
    private Button btnAddConfirm;
    private String defaultKeywords[] = {"服务厅"};
    private ArrayList<String> finalKeywords = new ArrayList<>();
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_add_confirm:
                    break;

                case R.id.btn_view_all:
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

        finalKeywords.addAll(Arrays.asList(defaultKeywords));
        btnAddConfirm = findViewById(R.id.btn_add_confirm);

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
}
