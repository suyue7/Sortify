package com.example.sortify_new;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class FnameDetailsActivity extends AppCompatActivity {

    private ListView lvItems;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fname_details);  // 对应的XML文件

        lvItems = findViewById(R.id.lvItems);  // 获取ListView组件
        dbHelper = new DatabaseHelper(this);   // 实例化数据库帮助类

        ImageView ivAddFname = findViewById(R.id.ivAddFname);  // 获取+按钮的引用
        // 为 + 按钮设置点击事件
        ivAddFname.setOnClickListener(v -> showAddFnameDialog());


        // 获取所有Fname的数据
        loadFnames();
    }

    // 弹出对话框以添加新的 Fname
    private void showAddFnameDialog() {
        // 创建对话框视图
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_fname, null);
        EditText etFname = dialogView.findViewById(R.id.etFname);  // 输入框
        etFname.setHint("请输入地点名称：");

        // 创建 AlertDialog
        new AlertDialog.Builder(this)
                .setTitle("添加地点")
                .setView(dialogView)
                .setPositiveButton("添加", (dialog, which) -> {
                    String fname = etFname.getText().toString().trim();

                    if (!fname.isEmpty()) {
                        // 调用数据库方法插入新 Fname
                        boolean success = dbHelper.insertFname(fname);

                        if (success) {
                            Toast.makeText(this, "添加成功！", Toast.LENGTH_SHORT).show();
                            // 刷新列表
                            loadFnames();
                        } else {
                            Toast.makeText(this, "添加失败，该地点已存在！", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(this, "名称不能为空！", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void loadFnames() {
        // 获取所有Fname
        List<String> fnames = dbHelper.getAllFnames();  // 从数据库获取所有Fname

        if (fnames != null && !fnames.isEmpty()) {
            // 创建适配器，将List中的数据绑定到ListView中
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    this,                 // 上下文
                    R.layout.fname_item,  // 每个Item的布局文件
                    R.id.fidName,         // list_item.xml中的TextView的ID
                    fnames                // 数据源
            );
            // 设置适配器到ListView
            lvItems.setAdapter(adapter);
            // 设置点击事件监听器
            lvItems.setOnItemClickListener((parent, view, position, id) -> {
                // 获取被点击的Fname
                String selectedFname = fnames.get(position);

                // 跳转到显示对应Sname的Activity
                Intent intent = new Intent(FnameDetailsActivity.this, SnameDetailsActivity.class);
                intent.putExtra("Fname", selectedFname);  // 传递选中的Fname
                startActivity(intent);
            });
            // 设置长按事件监听器
            lvItems.setOnItemLongClickListener((parent, view, position, id) -> {
                // 获取被长按的Fname
                String selectedFname = fnames.get(position);

                // 弹出删除确认框
                new AlertDialog.Builder(FnameDetailsActivity.this)
                        .setTitle("确认删除")
                        .setMessage("你确定要删除该地点及其所有相关数据吗？")
                        .setPositiveButton("删除", (dialog, which) -> {
                            // 获取Fname对应的 Fid
                            String fid = dbHelper.searchFidByFname(selectedFname);

                            // 执行删除操作
                            boolean success = deleteFnameAndRelatedData(fid);

                            if (success) {
                                Toast.makeText(FnameDetailsActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                                // 删除成功后刷新列表
                                loadFnames();
                            } else {
                                Toast.makeText(FnameDetailsActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();

                return true;  // 表示事件被处理，不再继续传播
            });

        } else {
            // 如果没有数据，提示用户
            Toast.makeText(this, "没有找到数据", Toast.LENGTH_SHORT).show();
        }
    }

    // 删除指定 Fname 和相关数据
    private boolean deleteFnameAndRelatedData(String fid) {
        boolean success = false;

        try {
            // 删除所有与 Fname 相关的 secondpos 数据
            int secondPosDeleted = dbHelper.deleteSecondposByFid(fid);

            // 删除所有与 Fname 相关的物品数据
            int itemsDeleted = dbHelper.deleteItemsByFid(fid);

            // 删除 Fname
            int fnameDeleted = dbHelper.deleteFid(fid);

            // 检查是否删除成功
            if (secondPosDeleted > 0 || itemsDeleted > 0 || fnameDeleted > 0) {
                success = true;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return success;
    }

}
