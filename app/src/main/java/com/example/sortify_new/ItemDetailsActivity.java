package com.example.sortify_new;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;
import java.util.Map;

public class ItemDetailsActivity extends AppCompatActivity {

    private ListView lvItems;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fname_details);  // 对应的XML文件

        lvItems = findViewById(R.id.lvItems);  // 获取ListView组件
        dbHelper = new DatabaseHelper(this);   // 实例化数据库帮助类

        // 获取传递过来的 Fname 和 Sname
        String fname = getIntent().getStringExtra("Fname");
        String sname = getIntent().getStringExtra("Sname");

        if (fname != null && sname != null) {
            ImageView ivAddItem = findViewById(R.id.ivAddFname);
            ivAddItem.setOnClickListener(v -> showAddItemDialog(fname, sname));
            // 加载对应 Fname 和 Sname 的物品数据
            loadItems(fname, sname);
        } else {
            // 如果没有 Fname 或 Sname，提示用户
            Toast.makeText(this, "没有找到对应的数据", Toast.LENGTH_SHORT).show();
        }
    }

    // 弹出对话框以添加新的 Item
    private void showAddItemDialog(String fname, String sname) {
        // 创建对话框视图
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_fname, null);
        EditText etItem = dialogView.findViewById(R.id.etFname);  // 输入框
        etItem.setHint("请输入物品名称：");

        // 创建 AlertDialog
        new AlertDialog.Builder(this)
                .setTitle("添加物品")
                .setView(dialogView)
                .setPositiveButton("添加", (dialog, which) -> {
                    String newIname = etItem.getText().toString().trim();
                    Log.d("DatabaseHelper", "输入的 Iname：" + newIname);

                    if (!newIname.isEmpty()) {
                        // 调用数据库方法添加新 Sname
                        boolean isAdded = dbHelper.insertItem(newIname, fname, sname);

                        if (isAdded) {
                            Toast.makeText(ItemDetailsActivity.this, "物品添加成功！", Toast.LENGTH_SHORT).show();
                            loadItems(fname, sname);  // 刷新列表
                        } else {
                            Toast.makeText(ItemDetailsActivity.this, "添加失败，该物品已存在！", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ItemDetailsActivity.this, "物品名称不能为空~", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void loadItems(String fname, String sname) {
        // 获取 Fname 和 Sname 对应的所有物品
        String FidToDel = dbHelper.searchFidByFname(fname);  // 获取 Fid
        String SidToDel = dbHelper.searchSidBySname(sname);  // 获取 Sid

        List<String> items = dbHelper.getItemsByFidAndSid(FidToDel, SidToDel);  // 查询对应物品

        if (items != null && !items.isEmpty()) {
            // 创建适配器，将 List 中的数据绑定到 ListView 中
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    this,                 // 上下文
                    R.layout.fname_item,  // 每个Item的布局文件
                    R.id.fidName,         // list_item.xml中的TextView的ID
                    items                 // 数据源
            );
            // 设置长按事件监听器
            lvItems.setOnItemLongClickListener((parent, view, position, id) -> {
                // 获取长按的物品名称
                String selectedItem = items.get(position);

                // 显示删除确认框
                showDeleteDialog(FidToDel, SidToDel, selectedItem, fname, sname);

                return true;  // 返回true表示事件已经处理
            });
            // 设置适配器到 ListView
            lvItems.setAdapter(adapter);

        } else {
            // 如果没有数据，提示用户
            Toast.makeText(this, "没有找到对应的物品", Toast.LENGTH_SHORT).show();
        }
    }
    // 弹出删除确认框
    private void showDeleteDialog(String fid, String sid, String itemName, String fname, String sname) {
        new AlertDialog.Builder(this)
                .setTitle("删除物品")
                .setMessage("确定要删除物品 \"" + itemName + "\" 吗？")
                .setPositiveButton("删除", (dialog, which) -> {
                    // 删除物品
                    deleteItem(fid, sid, itemName);
                    Log.d("DatabaseHelper", "执行了删除，现在开始重新 load：" + fname + sname);
                    loadItems(fname, sname);
                })
                .setNegativeButton("取消", (dialog, which) -> {
                    // 取消操作
                    dialog.dismiss();
                })
                .show();
    }

    // 删除物品的方法
    private void deleteItem(String fid, String sid, String itemName) {
        boolean isDeleted = dbHelper.deleteItemByFidSidAndIname(fid, sid, itemName);

        if (isDeleted) {
            Toast.makeText(this, "物品已删除", Toast.LENGTH_SHORT).show();
            // 删除成功后刷新列表
            loadItems(getIntent().getStringExtra("Fname"), getIntent().getStringExtra("Sname"));
        } else {
            Toast.makeText(this, "删除失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }
}
