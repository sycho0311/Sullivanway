package blacksmith.sullivanway.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDBOpenHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DB_NAME = "subway.db";

    public MyDBOpenHelper(Context context) {
        super(context, DB_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        dropTables(db);
        onCreate(db);
    }

    private void createTables(SQLiteDatabase db) {
        db.execSQL(CongestionInfo.SQL_CREATE);
        db.execSQL(EvInfo.SQL_CREATE);
        db.execSQL(FavoritePath.SQL_CREATE);
        db.execSQL(InoutInfo.SQL_CREATE);
        db.execSQL(StnInfo.SQL_CREATE);
        db.execSQL(TransInfo.SQL_CREATE);
        db.execSQL(TransMap.SQL_CREATE);
    }

    private void dropTables(SQLiteDatabase db) {
        db.execSQL(CongestionInfo.SQL_DROP);
        db.execSQL(EvInfo.SQL_DROP);
        db.execSQL(FavoritePath.SQL_DROP);
        db.execSQL(InoutInfo.SQL_DROP);
        db.execSQL(StnInfo.SQL_DROP);
        db.execSQL(TransInfo.SQL_DROP);
        db.execSQL(TransMap.SQL_DROP);
    }

    public void initDatabase(SQLiteDatabase db, boolean retry) {
        try {
            // 데이터 삭제&재삽입
            db.execSQL(CongestionInfo.SQL_DELETE_ALL);
            db.execSQL(EvInfo.SQL_DELETE_ALL);
            db.execSQL(FavoritePath.SQL_DELETE_ALL);
            db.execSQL(InoutInfo.SQL_DELETE_ALL);
            db.execSQL(StnInfo.SQL_DELETE_ALL);
            db.execSQL(TransInfo.SQL_DELETE_ALL);
            db.execSQL(TransMap.SQL_DELETE_ALL);

            CongestionInfo.initDatabase();
            EvInfo.initDatabase();
            InoutInfo.initDatabase();
            StnInfo.initDatabase();
            TransInfo.initDatabase();
            TransMap.initDatabase();
        } catch (SQLiteException e) {
            if (retry) {
                // 테이블 삭제&재생성
                // TODO 유저 데이터는 따로 백업이 필요하다...
                dropTables(db);
                createTables(db);
                initDatabase(db, false);
            } else {
                e.printStackTrace();
                System.exit(1);
            }

        }

    }

    public void setDatabase(SQLiteDatabase db) {
        CongestionInfo.setDatabase(db);
        EvInfo.setDatabase(db);
        FavoritePath.setDatabase(db);
        InoutInfo.setDatabase(db);
        StnInfo.setDatabase(db);
        TransInfo.setDatabase(db);
        TransMap.setDatabase(db);
    }
}
