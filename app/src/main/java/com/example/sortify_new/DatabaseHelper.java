package com.example.sortify_new;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "sortify.db";
    private static final int DATABASE_VERSION = 23;

    // 字段名
    private static final String COLUMN_FID = "Fid";
    private static final String COLUMN_FNAME = "Fname";
    private static final String COLUMN_SID = "Sid";
    private static final String COLUMN_SNAME = "Sname";
    private static final String COLUMN_IID = "Iid";
    private static final String COLUMN_INAME = "Iname";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DatabaseHelper", "DatabaseHelperonCreate() is called!!!!!!");
//        db.beginTransaction();  // 开始事务
        // 确保数据库是可写的
        if (db != null) {
            try {
                // 创建 firstpos 表
                String createFirstPosTable = "CREATE TABLE firstpos (" +
                        COLUMN_FID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_FNAME + " TEXT UNIQUE);";
                db.execSQL(createFirstPosTable);
                Log.d("DatabaseHelper", "当前类路径: " + getClass().getName());
                Log.d("DatabaseHelper", "执行了 createFirstPosTable");

                // 插入示例数据到 firstpos 表
                String sql= "INSERT INTO firstpos (" + COLUMN_FID + "," + COLUMN_FNAME + ") VALUES (1, '宿舍');";
                db.execSQL(sql);
                Log.d("DatabaseHelper", "Inserted into firstpos: '宿舍'");
                sql = "INSERT INTO firstpos (" + COLUMN_FID + "," + COLUMN_FNAME + ") VALUES (2, '家');";
                db.execSQL(sql);
                sql = "INSERT INTO firstpos (" + COLUMN_FID + "," + COLUMN_FNAME + ") VALUES (3, '自习室');";
                db.execSQL(sql);
                Log.d("DatabaseHelper", "Inserted into firstpos: '家'");

                // 创建 secondpos 表，使用联合主键 (Fid, Sid) 并将 Fid 设为外键
                String createSecondPosTable = "CREATE TABLE secondpos (" +
                        COLUMN_FID + " INTEGER, " +
                        COLUMN_SID + " INTEGER, " +
                        COLUMN_SNAME + " TEXT, " +
                        "PRIMARY KEY(" + COLUMN_FID + ", " + COLUMN_SID + "), " +
                        "FOREIGN KEY(" + COLUMN_FID + ") REFERENCES firstpos (" + COLUMN_FID + "));";
                db.execSQL(createSecondPosTable);

                // 插入示例数据到 secondpos 表
                sql = "INSERT INTO secondpos (" + COLUMN_FID + ", " + COLUMN_SID + ", " + COLUMN_SNAME + ") VALUES (1, 1, '书桌');";
                db.execSQL(sql);
                sql = "INSERT INTO secondpos (" + COLUMN_FID + ", " + COLUMN_SID + ", " + COLUMN_SNAME + ") VALUES (1, 2, '书柜');";
                db.execSQL(sql);
                sql = "INSERT INTO secondpos (" + COLUMN_FID + ", " + COLUMN_SID + ", " + COLUMN_SNAME + ") VALUES (2, 1, '书桌抽屉');";
                db.execSQL(sql);
                sql = "INSERT INTO secondpos (" + COLUMN_FID + ", " + COLUMN_SID + ", " + COLUMN_SNAME + ") VALUES (2, 2, '床下柜1');";
                db.execSQL(sql);
                sql = "INSERT INTO secondpos (" + COLUMN_FID + ", " + COLUMN_SID + ", " + COLUMN_SNAME + ") VALUES (2, 3, '床下柜2');";
                db.execSQL(sql);
                sql = "INSERT INTO secondpos (" + COLUMN_FID + ", " + COLUMN_SID + ", " + COLUMN_SNAME + ") VALUES (2, 4, '床下柜3');";
                db.execSQL(sql);
                sql = "INSERT INTO secondpos (" + COLUMN_FID + ", " + COLUMN_SID + ", " + COLUMN_SNAME + ") VALUES (2, 5, '书架');";
                db.execSQL(sql);
                sql = "INSERT INTO secondpos (" + COLUMN_FID + ", " + COLUMN_SID + ", " + COLUMN_SNAME + ") VALUES (3, 1, '工位');";
                db.execSQL(sql);

                // 创建 item 表，包含外键 Fid 和 Sid
                String createItemTable = "CREATE TABLE item (" +
                        COLUMN_IID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        COLUMN_INAME + " TEXT, " +
                        COLUMN_FID + " INTEGER, " +
                        COLUMN_SID + " INTEGER, " +
                        "FOREIGN KEY(" + COLUMN_FID + ", " + COLUMN_SID + ") REFERENCES secondpos (" + COLUMN_FID + ", " + COLUMN_SID + "));";
                db.execSQL(createItemTable);

                // 插入示例数据到 item 表
                sql = "INSERT INTO item (" + COLUMN_INAME + ", " + COLUMN_FID + ", " + COLUMN_SID + ") " +
                        "VALUES ('高数教材', 1, 1);";
                db.execSQL(sql);
                sql = "INSERT INTO item (" + COLUMN_INAME + ", " + COLUMN_FID + ", " + COLUMN_SID + ") " +
                        "VALUES ('操作系统教材', 1, 2);";
                db.execSQL(sql);

            } catch (Exception e) {
                Log.e("Database", "Error inserting data: " + e.getMessage());
            } finally {
//            db.endTransaction();  // 结束事务
            }
        }
        else {
            Log.e("DatabaseHelper", "数据库未打开");
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DatabaseHelper", "onUpgrade() 被调用");
        db.execSQL("DROP TABLE IF EXISTS item");
        db.execSQL("DROP TABLE IF EXISTS secondpos");
        db.execSQL("DROP TABLE IF EXISTS firstpos");
        onCreate(db);
    }

    // 插入新物品
    public boolean insertItem(String name, String location, String storageLocation) {
        SQLiteDatabase db = this.getWritableDatabase();

        int fid = getOrInsertFirstPosId(location);
        int sid = getOrInsertSecondPosId(fid, storageLocation);

        // 检查是否已经存在相同名称的物品
        String query = "SELECT COUNT(*) FROM item WHERE Iname = ? AND Fid = ? AND Sid = ?";
        Cursor cursor = db.rawQuery(query, new String[]{name, String.valueOf(fid), String.valueOf(sid)});

        boolean exists = false;
        if (cursor != null && cursor.moveToFirst()) {
            // 已存在，关闭 Cursor 并抛出提示
            exists = cursor.getInt(0) > 0;  // 是否已存在
            cursor.close();
        }
        if (exists) {
            return false;  // 已存在，插入失败
        }


        ContentValues values = new ContentValues();
        values.put(COLUMN_INAME, name);
        values.put(COLUMN_FID, fid);
        values.put(COLUMN_SID, sid);

        long result = db.insert("item", null, values);
        db.close();
        Log.i("DatabaseHelper", "物品插入成功！");
        return result != -1;  // 返回是否插入成功
    }

    // 获取或插入 firstpos 表中的 Fid
    private int getOrInsertFirstPosId(String locationName) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query("firstpos", new String[]{COLUMN_FID}, COLUMN_FNAME + "=?",
                new String[]{locationName}, null, null, null);
        if (cursor.moveToFirst()) {
            int fid = cursor.getInt(0);
            cursor.close();
            return fid;
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put(COLUMN_FNAME, locationName);
        return (int) db.insert("firstpos", null, values);
    }

    // 获取或插入 secondpos 表中的 Sid
    private int getOrInsertSecondPosId(int fid, String storageLocation) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query("secondpos", new String[]{COLUMN_SID}, COLUMN_FID + "=? AND " + COLUMN_SNAME + "=?",
                new String[]{String.valueOf(fid), storageLocation}, null, null, null);
        if (cursor.moveToFirst()) {
            int sid = cursor.getInt(0);
            cursor.close();
            return sid;
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put(COLUMN_FID, fid);
        values.put(COLUMN_SNAME, storageLocation);
        return (int) db.insert("secondpos", null, values);
    }

    // 获取 firstpos 表中的所有 Fname，加载 Add 界面第一个 Spinner 用
    public List<String> getAllFnames() {
        List<String> fnames = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Log.i("DatabaseHelper", "正在查找所有 Fname...");

        Cursor cursor = db.query("firstpos", new String[]{COLUMN_FNAME}, null, null, null, null, null);

        while (cursor.moveToNext()) {
            String fname =cursor.getString(0);
            fnames.add(fname);
            Log.i("DatabaseHelper", "打印 Fname: " + fname);  // 打印每个 Fname
        }
        cursor.close();
        return fnames;
    }

    // 获取 secondpos 表中的所有 Sname，加载 Add 界面第二个 Spinner 用
    public List<String> getAllSnames(String fname) {
        List<String> snames = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // 确保查询到数据
        Log.i("DatabaseHelper", "正在查找所有 Sname...");
        // 第一步：通过 Fname 查找 Fid
        Cursor cursor = db.query("firstpos", new String[]{COLUMN_FID}, COLUMN_FNAME + "=?",
                new String[]{fname}, null, null, null);
        int fid = -1;
        if (cursor.moveToFirst()) {
            fid = cursor.getInt(0);
        }
        cursor.close();

        // 如果 Fid 找到，则使用 Fid 查找对应的 Sname 列表
        if (fid != -1) {
            Cursor secondPosCursor = db.query("secondpos", new String[]{COLUMN_SNAME}, COLUMN_FID + "=?",
                    new String[]{String.valueOf(fid)}, null, null, null);
            while (secondPosCursor.moveToNext()) {
                snames.add(secondPosCursor.getString(0));
            }
            secondPosCursor.close();
        }
        return snames;
    }

    // 搜索方法：按名称搜索项目，加载 Search 界面 ListView 用
    public Cursor searchItemByName(String name) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM item WHERE " + COLUMN_INAME + " LIKE ?";
        String[] selectionArgs = new String[]{"%" + name + "%"};

        return db.rawQuery(query, selectionArgs);
    }

    // 通过 Fid 查 Fname，加载 Search 界面 ListView 用
    public String searchFnameByFid(String fid) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT Fname FROM firstpos WHERE Fid = ?";
        String[] selectionArgs = new String[]{fid};

        Cursor cursor = db.rawQuery(query, selectionArgs);

        // 检查查询结果
        String fname = null;
        if (cursor != null && cursor.moveToFirst()) {
            // 获取 Fname 列的索引
            int fnameIndex = cursor.getColumnIndex("Fname");
            if (fnameIndex != -1) {
                fname = cursor.getString(fnameIndex);
            }
        }
        // 关闭 Cursor
        if (cursor != null) {
            cursor.close();
        }
        return fname;
    }

    // 通过 Sid 查 Sname，加载 Search 界面 ListView 用
    public String searchSnameBySid(String fid, String sid) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT Sname FROM secondpos WHERE Fid = ? AND Sid = ?";
        String[] selectionArgs = new String[]{fid, sid};  // 传入 Fid 和 Sid 作为查询条件

        Cursor cursor = db.rawQuery(query, selectionArgs);

        // 检查查询结果
        String sname = null;
        if (cursor != null && cursor.moveToFirst()) {
            // 获取 Sname 列的索引
            int snameIndex = cursor.getColumnIndex("Sname");
            if (snameIndex != -1) {
                sname = cursor.getString(snameIndex);
            }
        }
        // 关闭 Cursor
        if (cursor != null) {
            cursor.close();
        }
        return sname;
    }

    // 通过 Fname 查 Fid，删除 Search 界面 ListView 用
    public String searchFidByFname(String fname) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT Fid FROM firstpos WHERE Fname = ?";
        String[] selectionArgs = new String[]{fname};

        Cursor cursor = db.rawQuery(query, selectionArgs);

        // 检查查询结果
        String fid = null;
        if (cursor != null && cursor.moveToFirst()) {
            // 获取 Fid 列的索引
            int fidIndex = cursor.getColumnIndex("Fid");
            if (fidIndex != -1) {
                fid = cursor.getString(fidIndex);
            }
        }
        // 关闭 Cursor
        if (cursor != null) {
            cursor.close();
        }
        return fid;
    }

    // 通过 Sname 查 Sid，删除 Search 界面 ListView 用
    public String searchSidBySname(String sname) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT Sid FROM secondpos WHERE Sname = ?";
        String[] selectionArgs = new String[]{sname};

        Cursor cursor = db.rawQuery(query, selectionArgs);

        // 检查查询结果
        String sid = null;
        if (cursor != null && cursor.moveToFirst()) {
            // 获取 Fid 列的索引
            int sidIndex = cursor.getColumnIndex("Sid");
            if (sidIndex != -1) {
                sid = cursor.getString(sidIndex);
            }
        }
        // 关闭 Cursor
        if (cursor != null) {
            cursor.close();
        }
        return sid;
    }

    // 通过 Iname,Fid,Sid 查 Iid，删除 Search 界面 ListView 用
    public String searchIid(String iname, String fid, String sid) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT Iid FROM item WHERE Iname = ? AND Fid = ? AND Sid = ?";
        String[] selectionArgs = new String[]{iname, fid, sid};

        Cursor cursor = db.rawQuery(query, selectionArgs);

        // 检查查询结果
        String Iid = null;
        if (cursor != null && cursor.moveToFirst()) {
            // 获取 Fid 列的索引
            int IidIndex = cursor.getColumnIndex("Iid");
            if (IidIndex != -1) {
                Iid = cursor.getString(IidIndex);
            }
        }
        // 关闭 Cursor
        if (cursor != null) {
            cursor.close();
        }
        return Iid;
    }

    // 删除物品，Search 界面长按用
    public void deleteItem(String iid) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsDeleted = db.delete("item", "Iid = ?", new String[]{iid});
        // 检查删除是否成功
        if (rowsDeleted > 0) {
            Log.d("DatabaseHelper", "Item with Iid " + iid + " deleted successfully.");
        } else {
            Log.d("DatabaseHelper", "No item found with Iid " + iid + ".");
        }
        db.close();
    }

    // 用 Fid 和 Sid 查询 Iname，ItemDetailsActivity 用
    public List<String> getItemsByFidAndSid(String fid, String sid) {
        List<String> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // 使用占位符来避免SQL注入
        String selection = "Fid = ? AND Sid = ?";
        String[] selectionArgs = { fid, sid };

        // 查询 Item 表，获取 Iname 字段
        Cursor cursor = db.query("item", new String[]{"Iname"}, selection, selectionArgs, null, null, null);

        while (cursor.moveToNext()) {
            int columnIndex = cursor.getColumnIndex("Iname");
            if (columnIndex == -1) {
                Log.e("Database", "Column 'Iname' not found.");
            } else {
                Log.d("Database", "Column 'Iname' found at index: " + columnIndex);
            }
            String itemName = cursor.getString(columnIndex);
            items.add(itemName);  // 将查询到的 Iname 添加到列表中
        }
        cursor.close();
        return items;  // 返回所有匹配的 Iname
    }

    public boolean deleteItemByFidSidAndIname(String fid, String sid, String iname) {
        SQLiteDatabase db = this.getWritableDatabase();
        // 定义删除条件
        String whereClause = "Fid = ? AND Sid = ? AND Iname = ?";
        String[] whereArgs = {fid, sid, iname};

        // 执行删除操作
        int rowsDeleted = db.delete("item", whereClause, whereArgs);

        return rowsDeleted > 0;  // 如果删除了行数大于0，表示删除成功
    }

    // 删除 sname，sname 详情页长按用
    public boolean deleteSname(String fname, String sname) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;

        try {
            // 根据 Fname 查找对应的 Fid
            String fid = searchFidByFname(fname);  // 根据 Fname 查找 Fid
            if (fid == null) {
                return false;  // 如果没有找到 Fid，返回 false
            }

            // 根据 Fid 和 Sid 删除所有相关的物品
            String sid = searchSidBySname(sname);  // 根据 Sname 查找 Sid
            String whereClause = "Fid = ? AND Sid = ?";
            String[] whereArgs = { fid, sid };

            // 删除所有与该 Fid 和 Sid 关联的物品
            int itemsDeleted = db.delete("Item", whereClause, whereArgs);

            // 第二步：删除 Sid 记录
            String whereClauseSid = "Fid = ? AND Sid = ?";
            String[] whereArgsSid = { fid, sid };
            int sidDeleted = db.delete("secondpos", whereClauseSid, whereArgsSid);

            // 如果物品和 Sname 都删除成功，则返回 true
            success = (itemsDeleted > 0 || sidDeleted > 0);  // 如果删除的物品行数大于 0 则返回 true
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            db.close();  // 关闭数据库连接
        }

        return success;
    }

    // 根据Fid删除对应的secondpos记录
    public int deleteSecondposByFid(String fid) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = "Fid = ?";
        String[] whereArgs = {fid};
        return db.delete("secondpos", whereClause, whereArgs);
    }

    // 根据Fid删除对应的Item记录
    public int deleteItemsByFid(String fid) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = "Fid = ?";
        String[] whereArgs = {fid};
        return db.delete("item", whereClause, whereArgs);
    }

    // 根据Fid删除Fname记录
    public int deleteFid(String fid) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = "Fid = ?";
        String[] whereArgs = {fid};
        return db.delete("firstpos", whereClause, whereArgs);
    }

    // 插入新的 Fname
    public boolean insertFname(String fname) {
        SQLiteDatabase db = this.getWritableDatabase();

        // 检查是否已经存在相同的 Fname
        String query = "SELECT COUNT(*) FROM firstpos WHERE Fname = ?";
        Cursor cursor = db.rawQuery(query, new String[]{fname});

        boolean exists = false;
        if (cursor != null && cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0;  // 是否已存在
            cursor.close();
        }
        if (exists) {
            return false;  // 已存在，插入失败
        }

        // 插入新 Fname
        ContentValues values = new ContentValues();
        values.put("Fname", fname);
        long result = db.insert("firstpos", null, values);

        return result != -1;  // 返回是否插入成功
    }

    // 添加新的 Sname
    public boolean addSname(String fname, String sname) {
        Log.d("DatabaseHelper", "传入 add 的 fname：" + fname);
        Log.d("DatabaseHelper", "传入 add 的 sname：" + sname);
        if (fname == null || fname.trim().isEmpty() || sname == null || sname.trim().isEmpty()) {
            Log.e("DatabaseHelper", "addSname: Fname 或 Sname 不能为空");
            return false;
        }

        SQLiteDatabase db = this.getWritableDatabase();

        // 获取 Fname 的 Fid
        String fid = searchFidByFname(fname);
        Log.d("DatabaseHelper", "获取的 fid：" + fid);

        // 检查 Sname 是否已存在
        Cursor cursor = db.rawQuery("SELECT * FROM secondpos WHERE Fid = ? AND Sname = ?", new String[]{fid, sname});
        boolean exists = false;
        if (cursor != null && cursor.moveToFirst()) {
            exists = cursor.getInt(0) > 0;  // 是否已存在
            cursor.close();
        }
        if (exists) {
            return false;  // 已存在，插入失败
        }

        int sid = getNextSid(db); // 获取下一个唯一 Sid

        // 插入新的 Sname
        ContentValues values = new ContentValues();
        values.put("Fid", fid);
        values.put("Sid", sid); // 显式设置 Sid
        values.put("Sname", sname);
        long result = db.insert("secondpos", null, values);

        return result != -1;  // 返回插入结果
    }

    public int getNextSid(SQLiteDatabase db) {
        Cursor cursor = db.rawQuery("SELECT MAX(Sid) FROM secondpos", null);
        int nextSid = 1; // 默认从 1 开始
        if (cursor.moveToFirst()) {
            nextSid = cursor.getInt(0) + 1; // 获取当前最大值，并加 1
        }
        cursor.close();
        return nextSid;
    }

}
