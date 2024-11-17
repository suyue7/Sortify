package com.example.sortify_new;


import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class AddItemActivity extends AppCompatActivity {
    private EditText etItemName;
    private Spinner spLocation, spStorageLocation;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_item);
        Log.d("AddItemActivity", "AddItemActivityonCreate() is called!!!");

        etItemName = findViewById(R.id.etItemName);
        spLocation = findViewById(R.id.spLocation);
        spStorageLocation = findViewById(R.id.spStorageLocation);

        // 创建数据库助手实例
        dbHelper = new DatabaseHelper(this);

        // 加载 Spinner
        loadLocationData();

        // 点击保存按钮
        findViewById(R.id.btnSaveItem).setOnClickListener(v -> {
            String name = etItemName.getText().toString();
            String location = spLocation.getSelectedItem().toString();
            String storageLocation = spStorageLocation.getSelectedItem().toString();

            if (!name.isEmpty()) {
                try {
                    dbHelper.insertItem(name, location, storageLocation);
                    Toast.makeText(this, "物品已添加", Toast.LENGTH_SHORT).show();
                    finish();  // 返回上一界面x
                }
                catch (IllegalStateException e) {
                    // 捕获重复物品的异常并显示提示框
                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "请输入物品名称", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 从数据库加载地点信息到 spLocation
    private void loadLocationData() {
        List<String> locations = dbHelper.getAllFnames();  // 获取所有地点名称

        if (locations != null && !locations.isEmpty()) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, locations);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spLocation.setAdapter(adapter);

            // 监听 spLocation 的选项更改
            spLocation.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    String selectedLocation = spLocation.getSelectedItem().toString();
                    Log.d("Spinner", "Selected location: " + selectedLocation);
                    // 加载与选中的 Fname 对应的 Sname 列表
                    loadStorageLocationData(selectedLocation);
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                    // Do nothing
                }
            });
        } else {
            Toast.makeText(this, "暂无任何地点，请添加", Toast.LENGTH_SHORT).show();
        }
    }

    // 从数据库加载位置信息到 spStorageLocation
    private void loadStorageLocationData(String locationName) {
        List<String> storageLocations = dbHelper.getAllSnames(locationName);  // 获取该地点下的所有位置名称

        if (storageLocations != null && !storageLocations.isEmpty()) {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, storageLocations);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spStorageLocation.setAdapter(adapter);
        } else {
            // 如果没有 Sname 数据，清空 Spinner 内容
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[] {});
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spStorageLocation.setAdapter(adapter);
            Toast.makeText(this, "暂无任何位置，请添加", Toast.LENGTH_SHORT).show();
        }
    }
}