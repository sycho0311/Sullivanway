package blacksmith.sullivanway.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import blacksmith.sullivanway.MainActivity;

public class FavoritePath {
    private static SQLiteDatabase db;
    public static String TB_NAME = "FavoritePath";
    static String SQL_CREATE = String.format("CREATE TABLE %s(" +
            "_id INTEGER primary key," +
            "type INTEGER NOT NULL," +
            "startLineNm TEXT NOT NULL," +
            "endLineNm TEXT NOT NULL," +
            "startStnNm TEXT NOT NULL," +
            "endStnNm TEXT NOT NULL);", TB_NAME);
    static String SQL_DROP = String.format("DROP TABLE IF EXISTS %s", TB_NAME);
    static String SQL_DELETE_ALL = String.format("DELETE FROM %s", TB_NAME);


    /* Database */
    public static boolean insert(boolean isFavorite, String startLineNm, String endLineNm, String startStnNm, String endStnNm) {
        /* ex.
         * favorite 설정 시 insertFvInfo(0, ...)
         * favorite 설정해제 시 insertFvInfo(0, ...)
         * history 저장 시 insertFvInfo(1, ...)   ***/
        String sql = String.format("SELECT _id, type FROM %s WHERE" +
                        " startLineNm='%s' AND endLineNm='%s' AND" +
                        " startStnNm='%s' AND endStnNm='%s'",
                TB_NAME, startLineNm, endLineNm, startStnNm, endStnNm);
        Cursor cursor = db.rawQuery(sql, null);
        if (cursor.moveToNext()) { //data가 이미 존재하는 경우...
            int type = cursor.getInt(1); //현재 설정된 type (favorite / history)

            if (isFavorite) { //favorite 설정하려고 하는데...
                if (type == 1) { //저장된 data가 history이면...
                    // favorite(type=0)로 UPDATE 한다
                    sql = String.format(MainActivity.DEFAULT_LOCALE, "UPDATE %s SET type=%d WHERE _id=%d", TB_NAME, 0, cursor.getLong(0));
                    db.execSQL(sql);
                    return true; //favorite로 설정 시 true를 반환한다
                }
            } else { //history 저장하려고 하는데...
                if (type == 0) //저장된 data가 favorite이면...
                    return true; //아무것도 하지 않고 종료. true는 현재 data가 favorite임을 알린다
            }

            // favorite 저장하려고 하는데, 저장된 data가 favorite인 경우 (favorite에서 삭제하고 history로 다시 저장하여 _id 갱신)
            // history 저장하려고 하는데, 저장된 data가 history인 경우 (history에서 삭제하고 history로 다시 저장하여 _id 갱신)
            sql = String.format(MainActivity.DEFAULT_LOCALE, "DELETE FROM %s WHERE _id=%d", TB_NAME, cursor.getLong(0));
            db.execSQL(sql);
        }
        cursor.close();

        // history 데이터 INSERT
        ContentValues values = new ContentValues();
        values.put("type", 1);
        values.put("startLineNm", startLineNm);
        values.put("endLineNm", endLineNm);
        values.put("startStnNm", startStnNm);
        values.put("endStnNm", endStnNm);
        db.insert(TB_NAME, null, values);

        // histroy는 최대 10개까지 저장한다. _id가 작은(오래된) history부터 삭제된다
        sql = String.format(MainActivity.DEFAULT_LOCALE, "SELECT _id FROM %s WHERE type=%d", TB_NAME, 1);
        cursor = db.rawQuery(sql, null);
        if (cursor.getCount() > 10 && cursor.moveToNext()) {
            // 오래된 데이터 DELETE
            sql = String.format(MainActivity.DEFAULT_LOCALE, "DELETE FROM %s WHERE _id=%d", TB_NAME, cursor.getLong(0));
            db.execSQL(sql);
        }
        cursor.close();

        return false;

    }

    public static void delete(String startLineNm, String endLineNm, String startStnNm, String endStnNm) {
        // 즐겨찾기/히스토리 삭제 기능에 사용....
        String sql = String.format("DELETE FROM %s WHERE" +
                        " startLineNm='%s' AND endLineNm='%s' AND" +
                        " startStnNm='%s' AND endStnNm='%s'",
                        TB_NAME, startLineNm, endLineNm, startStnNm, endStnNm);
        db.execSQL(sql);
    }

    static void setDatabase(SQLiteDatabase db) {
        FavoritePath.db = db;
    }

}
