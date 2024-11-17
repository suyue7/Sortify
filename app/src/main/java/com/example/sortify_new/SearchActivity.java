package com.example.sortify_new;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.widget.*;
import androidx.appcompat.app.AppCompatActivity;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchActivity extends AppCompatActivity {
    private EditText etSearchItemName;
    private Button btnSearch;
    private ListView lvItems;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Log.d("AddItemActivity", "AddItemActivityonCreate() is called!!!");

        etSearchItemName = findViewById(R.id.etSearchItemName);
        btnSearch = findViewById(R.id.btnSearch);
        lvItems = findViewById(R.id.lvItems);

        btnSearch.setOnClickListener(v -> {
            String name = etSearchItemName.getText().toString();
            if (!name.isEmpty()) {
                // 创建数据库助手实例
                dbHelper = new DatabaseHelper(this);
                // 查询符合条件的项目信息
                Cursor cursor = dbHelper.searchItemByName(name);
                if (cursor != null && cursor.getCount() > 0) {
                    Log.d("Debug", "查询返回了 " + cursor.getCount() + " 条数据");
                    // 创建一个临时列表用于保存查询结果
                    List<Map<String, String>> listData = new ArrayList<>();

                    // 遍历 cursor 获取每一项数据
                    while (cursor.moveToNext()) {
                        // 获取 Iname, Fid, Sid 列的索引
                        int itemNameIndex = cursor.getColumnIndex("Iname");
                        int fidIndex = cursor.getColumnIndex("Fid");
                        int sidIndex = cursor.getColumnIndex("Sid");

                        if (itemNameIndex == -1 || fidIndex == -1 || sidIndex == -1) {
                            Log.e("CursorError", "Required columns not found");
                        }

                        // 获取 Fid 和 Sid 的值
                        String itemName1 = cursor.getString(itemNameIndex);
                        String fid1 = cursor.getString(fidIndex);
                        String sid1 = cursor.getString(sidIndex);

                        // 打印 Fid 和 Sid
                        Log.d("Debug", "Fid: " + fid1+ ", Sid: " + sid1);

                        if (fid1 == null || fid1.isEmpty() || sid1 == null || sid1.isEmpty()) {
                            Log.e("CursorError", "Fid 或 Sid 为空！");
                            continue;  // Skip rows with invalid Fid or Sid
                        }

                        // 根据 Fid 查询 Fame
                        String fname = dbHelper.searchFnameByFid(fid1);
                        Log.e("CursorError", "找到 fname：" + fname);

                        // 根据 Fid 和 Sid 查询 Sname
                        String sname = dbHelper.searchSnameBySid(fid1, sid1);
                        Log.e("CursorError", "找到 sname：" + sname);

                        // 创建一个 map 来保存每行数据
                        Map<String, String> data = new HashMap<>();
                        data.put("Iname", itemName1);
                        data.put("Fname", fname);
                        data.put("Sname", sname);

                        // 将数据添加到 listData 中
                        listData.add(data);
                    }

                    // 关闭 cursor
                    cursor.close();

                    // 创建适配器并设置到 ListView
                    String[] columns = {"Iname", "Fname", "Sname"};
                    int[] viewIds = {R.id.itemName, R.id.fidName, R.id.sidName};

                    // 使用 SimpleAdapter 来显示数据
                    SimpleAdapter adapter = new SimpleAdapter(
                            this,               // 上下文
                            listData,           // 数据源
                            R.layout.list_item, // 显示每项数据的布局文件
                            columns,            // 键值数组，对应 map 中的键
                            viewIds             // 布局文件中的视图 ID
                    );
                    lvItems.setAdapter(adapter);

                    // 设置长按事件监听器
                    lvItems.setOnItemLongClickListener((parent, view, position, id) -> {
                        // 获取当前被长按的项目的 Iname
                        Map<String, String> selectedItem = listData.get(position);
                        String inameToDel = selectedItem.get("Iname");
                        Log.e("DatabaseHelper", "获取了要删除的Iname");

                        // 弹出确认删除的对话框
                        new AlertDialog.Builder(SearchActivity.this)
                                .setTitle("确认删除")
                                .setMessage("确定要删除 " + inameToDel + " 吗？")
                                .setPositiveButton("删除", (dialog, which) -> {
                                    // 调用数据库的删除方法

                                    String fname = selectedItem.get("Fname");
                                    // 检查 Fname 是否获取成功
                                    if (fname != null) {
                                        Log.d("SelectedItem", "Fname 获取成功: " + fname);
                                    } else {
                                        Log.e("SelectedItem", "Fname 获取失败");
                                    }
                                    String FidToDel = dbHelper.searchFidByFname(fname);  // 获取 Fid
                                    Log.e("DatabaseHelper", "获取了要删除的fid");

                                    String sname = selectedItem.get("Sname");
                                    String SidToDel = dbHelper.searchSidBySname(sname);  // 获取 Sid
                                    Log.e("DatabaseHelper", "获取了要删除的sid");

                                    String IidToDel = dbHelper.searchIid(inameToDel, FidToDel, SidToDel);  // 获取物品 id
                                    dbHelper.deleteItem(IidToDel);
                                    Log.e("DatabaseHelper", "物品删除成功！");

                                    // 重新加载数据
                                    Toast.makeText(SearchActivity.this, "已删除: " + inameToDel, Toast.LENGTH_SHORT).show();
                                    btnSearch.performClick();
                                })
                                .setNegativeButton("取消", null)
                                .show();
                        return true; // 返回 true 表示事件已处理
                    });
                } else {
                    // 没有找到相关物品时，清空 ListView
                    lvItems.setAdapter(null);
                    Toast.makeText(this, "没有找到相关物品", Toast.LENGTH_SHORT).show();
                }
            } else {
                // 没有输入物品名称时，清空 ListView
                lvItems.setAdapter(null);
                Toast.makeText(this, "请输入物品名称", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
