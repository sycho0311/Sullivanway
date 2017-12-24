package blacksmith.sullivanway.database;

import android.database.sqlite.SQLiteDatabase;

public class TransInfo {
    private static SQLiteDatabase db;
    public static String TB_NAME = "TransInfo";
    static String SQL_CREATE = String.format("CREATE TABLE %s(" +
            "stnNm TEXT NOT NULL," +
            "startLineNm TEXT NOT NULL," +
            "endLineNm TEXT NOT NULL," +
            "primary key(stnNm, startLineNm, endLineNm));", TB_NAME);
    static String SQL_DROP = String.format("DROP TABLE IF EXISTS %s", TB_NAME);
    static String SQL_DELETE_ALL = String.format("DELETE FROM %s", TB_NAME);


    /* Database */
    private static void insert(String stnNm, String... lineNms) {
        for (int i = 0; i < lineNms.length; i+=2) {
            String sql = String.format(
                    "INSERT INTO %s VALUES('%s','%s','%s');",
                    TB_NAME, stnNm, lineNms[i], lineNms[i+1]);
            db.execSQL(sql);
        }
    }

    static void setDatabase(SQLiteDatabase db) {
        TransInfo.db = db;
    }

    static void initDatabase() {
// 1호선
        insert("회룡", "1호선", "의정부경전철");
        insert("도봉산", "1호선", "7호선");
        insert("창동", "1호선", "4호선");
        insert("석계", "1호선", "6호선");
        insert("회기", "1호선", "경춘선", "1호선", "경의중앙선", "경춘선", "경의중앙선");
        insert("청량리", "1호선", "경춘선", "1호선", "경의중앙선", "경춘선", "경의중앙선");
        insert("신설동", "1호선", "2호선", "1호선", "우이신설선", "2호선", "우이신설선");
        insert("동묘앞", "1호선", "6호선");
        insert("동대문", "1호선", "4호선");
        insert("종로3가", "1호선", "3호선", "1호선", "5호선", "3호선", "5호선");
        insert("시청", "1호선", "2호선");
        insert("서울", "1호선", "4호선", "1호선", "인천국제공항철도", "1호선", "경의중앙선",
                "4호선", "인천국제공항철도", "4호선", "경의중앙선", "인천국제공항철도", "경의중앙선");
        insert("용산", "1호선", "경의중앙선");
        insert("노량진", "1호선", "9호선");
        insert("신길", "1호선", "5호선");
        insert("신도림", "1호선", "2호선");
        insert("온수", "1호선", "7호선");
        insert("부평", "1호선", "인천1호선");
        insert("주안", "1호선", "인천2호선");
        insert("인천", "1호선", "수인선");
        insert("가산디지털단지", "1호선", "7호선");
        insert("금정", "1호선", "4호선");
        insert("수원", "1호선", "분당선");

// 2호선
        insert("을지로3가", "2호선", "3호선");
        insert("을지로4가", "2호선", "5호선");
        insert("동대문역사문화공원", "2호선", "4호선", "2호선", "5호선", "4호선", "5호선");
        insert("신당", "2호선", "6호선");
        insert("왕십리", "2호선", "5호선", "2호선", "분당선", "2호선", "경의중앙선",
                "5호선", "분당선", "5호선", "경의중앙선", "분당선", "경의중앙선");
        insert("건대입구", "2호선", "7호선");
        insert("잠실", "2호선", "8호선");
        insert("종합운동장", "2호선", "9호선");
        insert("선릉", "2호선", "분당선");
        insert("강남", "2호선", "신분당선");
        insert("교대", "2호선", "3호선");
        insert("사당", "2호선", "4호선");
        insert("대림", "2호선", "7호선");
        insert("까치산", "2호선", "5호선");
        insert("영등포구청", "2호선", "5호선");
        insert("당산", "2호선", "9호선");
        insert("합정", "2호선", "6호선");
        insert("홍대입구", "2호선", "인천국제공항철도", "2호선", "경의중앙선", "인천국제공항철도", "경의중앙선");
        insert("충정로", "2호선", "5호선");

// 3호선
        insert("대곡", "3호선", "경의중앙선");
        insert("연신내", "3호선", "6호선");
        insert("불광", "3호선", "6호선");
        insert("충무로", "3호선", "4호선");
        insert("약수", "3호선", "6호선");
        insert("옥수", "3호선", "경의중앙선");
        insert("고속터미널", "3호선", "7호선", "3호선", "9호선", "7호선", "9호선");
        insert("양재", "3호선", "신분당선");
        insert("도곡", "3호선", "분당선");
        insert("수서", "3호선", "분당선");
        insert("가락시장", "3호선", "8호선");
        insert("오금", "3호선", "5호선");

// 4호선
        insert("노원", "4호선", "7호선");
        insert("성신여대입구", "4호선", "우이신설선");
        insert("삼각지", "4호선", "6호선");
        insert("이촌", "4호선", "경의중앙선");
        insert("동작", "4호선", "9호선");
        insert("총신대입구(이수)", "4호선", "7호선");
        insert("오이도", "4호선", "수인선");

// 5호선
        insert("김포공항", "5호선", "9호선", "5호선", "인천국제공항철도", "9호선", "인천국제공항철도");
        insert("공덕", "5호선", "6호선", "5호선", "인천국제공항철도", "5호선", "경의중앙선",
                "6호선", "인천국제공항철도", "6호선", "경의중앙선", "인천국제공항철도", "경의중앙선");
        insert("청구", "5호선", "6호선");
        insert("군자", "5호선", "7호선");
        insert("천호", "5호선", "8호선");

// 6호선
        insert("디지털미디어시티", "6호선", "인천국제공항철도", "6호선", "경의중앙선", "인천국제공항철도", "경의중앙선");
        insert("효창공원앞", "6호선", "경의중앙선");
        insert("보문", "6호선", "우이신설선");
        insert("태릉입구", "6호선", "7호선");

// 7호선
        insert("상봉", "7호선", "경춘선", "7호선", "경의중앙선", "경춘선", "경의중앙선");
        insert("부평구청", "7호선", "인천1호선");

// 8호선
        insert("복정", "8호선", "수인선");
        insert("모란", "8호선", "분당선");

// 9호선
        insert("선정릉", "9호선", "분당선");

// 인천국제공항철도
        insert("계양", "인천국제공항철도", "인천1호선");
        insert("검암", "인천국제공항철도", "인천2호선");
        insert("인천국제공항", "인천국제공항철도", "자기부상철도");

// 분당선
        insert("이매", "분당선", "경강선");
        insert("정자", "분당선", "신분당선");
        insert("기흥", "분당선", "에버라인");

// 에버라인

// 경춘선
        insert("망우", "경춘선", "경의중앙선");
        insert("중랑", "경춘선", "경의중앙선");

// 인천1호선
        insert("인천시청", "인천1호선", "인천2호선");
        insert("원인재", "인천1호선", "수인선");

// 인천2호선

// 경의중앙선

// 경강선
        insert("판교", "경강선", "신분당선");

// 신분당선

// 수인선

// 의정부경전철

// 우이신설선

    }
}
