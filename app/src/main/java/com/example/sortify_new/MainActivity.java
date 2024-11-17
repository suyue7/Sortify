package com.example.sortify_new;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private Button btnAddItem, btnSearchItem, btnViewItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAddItem = findViewById(R.id.btnAddItem);
        btnSearchItem = findViewById(R.id.btnSearchItem);
        btnViewItems = findViewById(R.id.btnViewItems);

        btnAddItem.setOnClickListener(v -> {
            Log.d("MainActivity", "Button clicked, starting AddItemActivity!!!");
            Intent intent = new Intent(MainActivity.this, AddItemActivity.class);
            startActivity(intent);
        });

        btnSearchItem.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            startActivity(intent);
        });

        btnViewItems.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, FnameDetailsActivity.class);
            startActivity(intent);
        });
    }
}
