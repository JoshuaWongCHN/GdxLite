package com.joshua.gdx.gdxlite;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.joshua.gdx.gdxlite.alphamatte.DemoActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ListView mListView;
    ArrayList<String> mData = new ArrayList<>();
    ArrayList<Class<? extends Activity>> mActivities = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mListView = findViewById(R.id.list);

        initData();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, mData);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, mActivities.get(position));
                startActivity(intent);
            }
        });
    }

    private void initData() {
        mData.add("删除");
        mActivities.add(DemoActivity.class);
    }
}
