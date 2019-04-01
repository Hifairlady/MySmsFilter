package com.edgar.mysmsfilter;

import android.content.ContentResolver;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Telephony;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private ContentObserver mObserver;
    private TextView tvBody;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvBody = findViewById(R.id.tv_msg_body);

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

                    tvBody.setText(msgBody);

                    if (msgBody.contains("服务厅")) {
                        int delCount = resolver.delete(Telephony.Sms.CONTENT_URI,
                                "_id=" + msgId, null);
                        if (delCount == 1) {
                            Toast.makeText(MainActivity.this, "Message delete!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }


                }
                cursor.close();
            }
        };

        getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, mObserver);

    }
}
