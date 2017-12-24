package blacksmith.sullivanway.database;

import android.database.sqlite.SQLiteDatabase;

import blacksmith.sullivanway.MainActivity;
import blacksmith.sullivanway.R;

public class TransMap {
    private static SQLiteDatabase db;
    public static String TB_NAME = "TransMap";
    static String SQL_CREATE = String.format("CREATE TABLE %s(" +
                    "res INTEGER primary key," +
                    "stnNm TEXT NOT NULL," +
                    "startLineNm TEXT NOT NULL," +
                    "startNextStnNm TEXT NOT NULL," +
                    "endLineNm TEXT NOT NULL," +
                    "endNextStnNm TEXT NOT NULL," +
                    "floor TEXT NOT NULL," +
                    "time INTEGER NOT NULL);", TB_NAME);
    static String SQL_DROP = String.format("DROP TABLE IF EXISTS %s", TB_NAME);
    static String SQL_DELETE_ALL = String.format("DELETE FROM %s", TB_NAME);


    /* Database */
    private static void insert(int res, String stnNm, String startLine, String startNextStnNm, String endLineNm, String endNextStnNm, String floor, int time) {
        //DB에 입력한 값으로 행 추가
        String sql=String.format(
                MainActivity.DEFAULT_LOCALE,
                "INSERT INTO %s VALUES(%d,'%s','%s'," +
                        "'%s','%s','%s','%s',%d);",
                TB_NAME, res, stnNm, startLine, startNextStnNm,
                endLineNm, endNextStnNm, floor, time);
        db.execSQL(sql);
    }

    static void setDatabase(SQLiteDatabase db) {
        TransMap.db = db;
    }

    static void initDatabase() {
        insert(R.drawable.trans_bungjum1, "병점", "1호선", "세류", "1호선", "서동탄", "1F", 8);
        insert(R.drawable.trans_bungjum2, "병점", "1호선", "세류", "1호선", "서동탄", "2F", 8);
        insert(R.drawable.trans_bungjum3, "병점", "1호선", "세류", "1호선", "서동탄", "1F", 8);
        insert(R.drawable.trans_derim1, "대림", "2호선", "신도림", "7호선", "남구로", "3F", 18);
        insert(R.drawable.trans_derim2, "대림", "2호선", "신도림", "7호선", "남구로", "2F", 18);
        insert(R.drawable.trans_derim3, "대림", "2호선", "신도림", "7호선", "남구로", "1F", 18);
        insert(R.drawable.trans_derim4, "대림", "2호선", "신도림", "7호선", "남구로", "B1", 18);
        insert(R.drawable.trans_derim5, "대림", "2호선", "신도림", "7호선", "남구로", "B2", 18);
        insert(R.drawable.trans_dongyuksa1, "동대문역사문화공원", "5호선", "청구", "2호선", "신당", "B5", 14);
        insert(R.drawable.trans_dongyuksa2, "동대문역사문화공원", "5호선", "청구", "2호선", "신당", "B3", 14);
        insert(R.drawable.trans_dongyuksa3, "동대문역사문화공원", "5호선", "청구", "2호선", "신당", "B1", 14);
        insert(R.drawable.trans_dongyuksa4, "동대문역사문화공원", "5호선", "청구", "2호선", "신당", "B2", 14);
        insert(R.drawable.trans_gasan1, "가산디지털단지", "1호선", "구로", "7호선", "남구로", "1F", 22);
        insert(R.drawable.trans_gasan2, "가산디지털단지", "1호선", "구로", "7호선", "남구로", "2F", 22);
        insert(R.drawable.trans_gasan3, "가산디지털단지", "1호선", "구로", "7호선", "남구로", "1F", 22);
        insert(R.drawable.trans_gasan4, "가산디지털단지", "1호선", "구로", "7호선", "남구로", "B1", 22);
        insert(R.drawable.trans_gasan5, "가산디지털단지", "1호선", "구로", "7호선", "남구로", "B2", 22);
        insert(R.drawable.trans_gasan6, "가산디지털단지", "1호선", "구로", "7호선", "남구로", "B3", 22);
        insert(R.drawable.trans_gosokterminal1, "고속터미널", "9호선", "사평", "7호선", "반포", "B5", 16);
        insert(R.drawable.trans_gosokterminal2, "고속터미널", "9호선", "사평", "7호선", "반포", "B4", 16);
        insert(R.drawable.trans_gosokterminal3, "고속터미널", "9호선", "사평", "7호선", "반포", "B2", 16);
        insert(R.drawable.trans_gosokterminal4, "고속터미널", "9호선", "사평", "7호선", "반포", "B2", 16);
        insert(R.drawable.trans_gosokterminal5, "고속터미널", "9호선", "사평", "7호선", "반포", "B3", 16);
        insert(R.drawable.trans_gumjoung1, "금정", "1호선", "명학", "4호선", "범계", "1F", 7);
        insert(R.drawable.trans_gumjoung2, "금정", "1호선", "명학", "4호선", "범계", "2F", 7);
        insert(R.drawable.trans_gumjoung3, "금정", "1호선", "명학", "4호선", "범계", "1F", 7);
        insert(R.drawable.trans_gunde1, "건대입구", "2호선", "성수", "7호선", "어린이대공원", "3F", 16);
        insert(R.drawable.trans_gunde2, "건대입구", "2호선", "성수", "7호선", "어린이대공원", "2F", 16);
        insert(R.drawable.trans_gunde3, "건대입구", "2호선", "성수", "7호선", "어린이대공원", "1F", 16);
        insert(R.drawable.trans_gunde4, "건대입구", "2호선", "성수", "7호선", "어린이대공원", "B2", 16);
        insert(R.drawable.trans_joungrosamga1, "종로3가", "1호선", "종로5가", "5호선", "광화문", "B2", 16);
        insert(R.drawable.trans_joungrosamga2, "종로3가", "1호선", "종로5가", "5호선", "광화문", "B3", 16);
        insert(R.drawable.trans_joungrosamga3, "종로3가", "1호선", "종로5가", "5호선", "광화문", "B4", 16);
        insert(R.drawable.trans_joungrosamga4, "종로3가", "1호선", "종로5가", "5호선", "광화문", "B5", 16);
        insert(R.drawable.trans_kimpoairport1, "김포공항", "5호선", "개화산", "인천국제공항철도", "계양", "B3", 8);
        insert(R.drawable.trans_kimpoairport2, "김포공항", "5호선", "개화산", "인천국제공항철도", "계양", "B4", 8);
        insert(R.drawable.trans_leesu1, "총신대입구(이수)", "7호선", "내방", "4호선", "사당", "B4", 14);
        insert(R.drawable.trans_leesu2, "총신대입구(이수)", "7호선", "내방", "4호선", "사당", "B2", 14);
        insert(R.drawable.trans_mangwo1, "망우", "경춘선", "신내", "경의중앙", "상봉", "1F", 8);
        insert(R.drawable.trans_mangwo2, "망우", "경춘선", "신내", "경의중앙", "상봉", "2F", 8);
        insert(R.drawable.trans_mangwo3, "망우", "경춘선", "신내", "경의중앙", "상봉", "1F", 8);
        insert(R.drawable.trans_nowon1, "노원", "4호선", "상계", "7호선", "중계", "3F", 21);
        insert(R.drawable.trans_nowon2, "노원", "4호선", "상계", "7호선", "중계", "2F", 21);
        insert(R.drawable.trans_nowon3, "노원", "4호선", "상계", "7호선", "중계", "1F", 21);
        insert(R.drawable.trans_nowon4, "노원", "4호선", "상계", "7호선", "중계", "B1", 21);
        insert(R.drawable.trans_nowon5, "노원", "4호선", "상계", "7호선", "중계", "B2", 21);
        insert(R.drawable.trans_seoul1, "서울", "4호선", "회현", "1호선", "남영", "B2", 20);
        insert(R.drawable.trans_seoul2, "서울", "4호선", "회현", "1호선", "남영", "B1", 20);
        insert(R.drawable.trans_seoul3, "서울", "4호선", "회현", "1호선", "남영", "B1", 20);
        insert(R.drawable.trans_seoul4, "서울", "4호선", "회현", "1호선", "남영", "B2", 20);
        insert(R.drawable.trans_sindorim1, "신도림", "2호선", "문래", "1호선", "구로", "B2",8);
        insert(R.drawable.trans_sindorim2, "신도림", "2호선", "문래", "1호선", "구로", "B1",8);
        insert(R.drawable.trans_sindorim3, "신도림", "2호선", "문래", "1호선", "구로", "1F",8);
        insert(R.drawable.trans_sukge1, "석계", "6호선", "태릉입구", "1호선", "신이문", "B2", 14);
        insert(R.drawable.trans_sukge2, "석계", "6호선", "태릉입구", "1호선", "신이문", "B1", 14);
        insert(R.drawable.trans_sukge3, "석계", "6호선", "태릉입구", "1호선", "신이문", "1F", 14);
        insert(R.drawable.trans_sukge4, "석계", "6호선", "태릉입구", "1호선", "신이문", "2F", 14);
        insert(R.drawable.trans_wangsimri1, "왕십리", "5호선", "행당", "경의중앙", "청량리", "B5", 19);
        insert(R.drawable.trans_wangsimri2, "왕십리", "5호선", "행당", "경의중앙", "청량리", "B4", 19);
        insert(R.drawable.trans_wangsimri3, "왕십리", "5호선", "행당", "경의중앙", "청량리", "B1", 19);
        insert(R.drawable.trans_wangsimri4, "왕십리", "5호선", "행당", "경의중앙", "청량리", "B1", 19);
        insert(R.drawable.trans_wangsimri5, "왕십리", "5호선", "행당", "경의중앙", "청량리", "1F", 19);
        insert(R.drawable.trans_yaksu1, "약수", "3호선", "금호", "6호선", "버티고개", "B2", 14);
        insert(R.drawable.trans_yaksu2, "약수", "3호선", "금호", "6호선", "버티고개", "B1", 14);
        insert(R.drawable.trans_yaksu3, "약수", "3호선", "금호", "6호선", "버티고개", "B1", 14);
        insert(R.drawable.trans_yaksu4, "약수", "3호선", "금호", "6호선", "버티고개", "B3", 14);
        insert(R.drawable.trans_youngdunpogujong1, "영등포구청", "5호선", "양평", "2호선", "당산", "B5", 18);
        insert(R.drawable.trans_youngdunpogujong2, "영등포구청", "5호선", "양평", "2호선", "당산", "1F", 18);
        insert(R.drawable.trans_youngdunpogujong3, "영등포구청", "5호선", "양평", "2호선", "당산", "B1", 18);
        insert(R.drawable.trans_youngdunpogujong4, "영등포구청", "5호선", "양평", "2호선", "당산", "B2", 18);

    }
}
