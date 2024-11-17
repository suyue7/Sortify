package com.example.sortify_new;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class SnameDetailsActivity extends AppCompatActivity {

    private ListView lvSnames;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fname_details);  // 对应的XML文件
        Log.d("SelectedItem", "Sname page 加载成功: ");

        lvSnames = findViewById(R.id.lvItems);  // 获取ListView组件
        lvSnames.setAdapter(null);
        dbHelper = new DatabaseHelper(this);    // 实例化数据库帮助类

        // 获取传递过来的Fname
        String fname = getIntent().getStringExtra("Fname");

        if (fname != null) {
            // 获取该Fname对应的所有Sname
            loadSnames(fname);
        } else {
            // 如果没有Fname，提示用户
            Toast.makeText(this, "没有找到对应的数据", Toast.LENGTH_SHORT).show();
        }

        Log.d("DatabaseHelper", "获取了“+”按钮！");
        // 为 + 按钮设置点击事件
        ImageView ivAddFname = findViewById(R.id.ivAddFname);  // 获取+按钮的引用
        ivAddFname.setOnClickListener(v -> showAddSnameDialog(fname));
    }

    // 弹出对话框以添加新的 Sname
    private void showAddSnameDialog(String fname) {
        // 创建对话框视图
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_fname, null);
        EditText etFname = dialogView.findViewById(R.id.etFname);  // 输入框
        etFname.setHint("请输入位置名称：");

        // 创建 AlertDialog
        new AlertDialog.Builder(this)
                .setTitle("添加位置")
                .setView(dialogView)
                .setPositiveButton("添加", (dialog, which) -> {
                    String newSname = etFname.getText().toString().trim();
                    Log.d("DatabaseHelper", "输入的 sname：" + newSname);

                    if (!newSname.isEmpty()) {
                        // 调用数据库方法添加新 Sname
                        boolean isAdded = dbHelper.addSname(fname, newSname);

                        if (isAdded) {
                            Toast.makeText(SnameDetailsActivity.this, "位置添加成功", Toast.LENGTH_SHORT).show();
                            loadSnames(fname);  // 刷新列表
                        } else {
                            Toast.makeText(SnameDetailsActivity.this, "添加失败，该位置已存在！", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(SnameDetailsActivity.this, "名称不能为空", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void loadSnames(String fname) {
        // 获取该Fname对应的所有Sname
        List<String> snames = dbHelper.getAllSnames(fname);  // 假设你有这个方法来查询Sname

        if (snames != null && !snames.isEmpty()) {
            // 创建适配器，将List中的数据绑定到ListView中
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                    this,                 // 上下文
                    R.layout.fname_item,  // 每个Item的布局文件
                    R.id.fidName,         // list_item.xml中的TextView的ID
                    snames                // 数据源
            );
            // 设置适配器到ListView
            lvSnames.setAdapter(adapter);
            // 设置Item点击事件监听器
            lvSnames.setOnItemClickListener((parent, view, position, id) -> {
                // 获取点击的 Sname
                String selectedSname = snames.get(position);
                Log.d("SnameDetails", "Selected Sname: " + selectedSname);

                // 获取 Fname
                String fnameToShow = getIntent().getStringExtra("Fname");

                // 跳转到 ItemDetailsActivity，传递 Fname 和 Sname
                Intent intent = new Intent(SnameDetailsActivity.this, ItemDetailsActivity.class);
                intent.putExtra("Fname", fnameToShow);    // 传递 Fname
                intent.putExtra("Sname", selectedSname);  // 传递 Sname
                startActivity(intent);  // 启动 ItemDetailsActivity
            });

            // 设置Item长按事件监听器
            lvSnames.setOnItemLongClickListener((parent, view, position, id) -> {
                // 获取长按的 Sname 和 Fname
                String selectedSname = snames.get(position);
                String fname1 = getIntent().getStringExtra("Fname");

                // 弹出确认删除对话框
                if (fname1 != null && selectedSname != null) {
                    showDeleteConfirmationDialog(fname1, selectedSname);
                }

                // 返回 true 表示已处理长按事件
                return true;
            });
        } else {
            // 如果没有数据，提示用户
            Toast.makeText(this, "没有找到位置数据！", Toast.LENGTH_SHORT).show();
        }
    }

    // 弹出删除确认框
    private void showDeleteConfirmationDialog(String fname, String sname) {
        // 创建一个 AlertDialog.Builder 对象
        new AlertDialog.Builder(this)
                .setTitle("确认删除")
                .setMessage("你确定要删除 " + sname + " 吗？删除后 " + sname + " 的物品都会被删除哦~")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 用户点击"确定"按钮时，执行删除操作
                        boolean isDeleted = dbHelper.deleteSname(fname, sname);

                        if (isDeleted) {
                            // 删除成功后提示并刷新列表
                            Toast.makeText(SnameDetailsActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                            // 重新加载Sname列表
                            loadSnames(fname);
                        } else {
                            // 删除失败提示
                            Toast.makeText(SnameDetailsActivity.this, "删除失败", Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 用户点击"取消"按钮时，什么都不做
                        dialog.dismiss();
                    }
                })
                .show(); // 显示对话框
    }

}
