package blacksmith.sullivanway.path;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;

import blacksmith.sullivanway.database.StnInfo;

public class StationMatrix {
    public final static int INF = 99999; //Infinity (연결되지 않은 두 역 사이의 가중치)

    private ArrayList<StnInfo> stnIdx; //행|열 번호에 해당하는 역이름
    private int n;
    private int[][] rawMatrix;
    private int[][] matrix; //가중치 인접행렬
    private int[][] transMatrix;
    private int numOfStns;

    public StationMatrix(SQLiteDatabase db) {
        stnIdx = new ArrayList<>();
        String sql = String.format("SELECT * FROM %s ORDER BY lineNm, id", StnInfo.TB_NAME);
        Cursor cursor = db.rawQuery(sql, null);

        // stnIdx: 역 이름셋 작성
        while (cursor.moveToNext()) {
            String lineNm = cursor.getString(0);
            int id = cursor.getInt(1);
            String stnNm = cursor.getString(2);
            double km = cursor.getDouble(3);
            double x = cursor.getDouble(4);
            double y = cursor.getDouble(5);
            String door = cursor.getString(6);
            String contact = cursor.getString(7);
            String toilet = cursor.getString(8);
            String elevator = cursor.getString(9);
            String escalator = cursor.getString(10);
            String wheelLift = cursor.getString(11);
            int pointx = cursor.getInt(12);
            int pointy = cursor.getInt(13);
            stnIdx.add(new StnInfo(lineNm, id, stnNm, km, x, y, door, contact, toilet, elevator, escalator, wheelLift, pointx, pointy));
        }
        cursor.close();
        numOfStns = stnIdx.size(); //N=전체 역 개수
        n = numOfStns + 2;

        // rawMatrix: 가중치 인접행렬 MAX_VALUE로 초기화
        rawMatrix = new int[n][n];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                rawMatrix[i][j] = INF; //INF 초기화
        for (int i = 0; i < n; i++)
            rawMatrix[i][i] = 0; //대각선 0

        // matrix: 가중치 인접행렬에 가중치(역간 소요시간) 입력
        setMatrix();

        initMatrix(rawMatrix);
    }

    public void setVirtualNodes(ArrayList<Integer> startLineIdxs, ArrayList<Integer> endLineIdxs) {
        // 초기화
        int start = n - 2, end = n - 1;
        for (int i = 0; i < n; i++) {
            matrix[i][start] = INF;
            matrix[start][i] = INF;
            transMatrix[i][start] = INF;
            transMatrix[start][i] = INF;
        }
        matrix[start][start] = 0;
        for (int i = 0; i < n; i++) {
            matrix[i][end] = INF;
            matrix[end][i] = INF;
            transMatrix[i][end] = INF;
            transMatrix[end][i] = INF;
        }
        matrix[end][end] = 0;
        // 가상시작점, 가상도착점 설정
        for (int i : startLineIdxs) {
            matrix[i][start] = 0;
            matrix[start][i] = 0;
            transMatrix[i][start] = 0;
            transMatrix[start][i] = 0;
        }
        for (int i : endLineIdxs) {
            matrix[i][end] = 0;
            matrix[end][i] = 0;
            transMatrix[i][end] = 0;
            transMatrix[end][i] = 0;
        }
    }

    public void initMatrix(int[][] mMatrix) {
        // 수정된 행렬(mMatrix)가 null이 아니면 matrix에 mMatrix를 복제한다
        matrix = new int[n][n];
        for (int i = 0; i < n; i++)
            matrix[i] = mMatrix[i].clone();

        // transMatrix: matrix로 가중치가 환승이면 1, 아니면 0으로 초기화
        transMatrix = new int[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                int weight = matrix[i][j];
                if (weight == INF) {
                    transMatrix[i][j] = INF;
                } else {
                    if (i < numOfStns && j < numOfStns && stnIdx.get(i).getStnNm().equals(stnIdx.get(j).getStnNm()))
                        transMatrix[i][j] = 1;
                    else
                        transMatrix[i][j] = 0;
                }
            }
        }
    }

    private ArrayList<Integer> getIndexes(String stnNm) {
        ArrayList<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < numOfStns; i++)
            if (stnNm.equals(stnIdx.get(i).getStnNm()))
                indexes.add(i);
        return indexes;
    }

    public int getIndex(String mLineNm, String mStnNm) {
        for (int i = 0; i < numOfStns; i++) { //lineNm호선 stnNm1역에 맞는 행|열 번호 찾기
            StnInfo stnInfo = stnIdx.get(i);
            if (mLineNm.equals(stnInfo.getLineNm()) && mStnNm.equals(stnInfo.getStnNm()))
                return i;
        }
        return -1;
    }

    public int[][] getRawMatrix() {
        return rawMatrix;
    }

    int[][] getMatrix() {
        return matrix;
    }

    int[][] getTransMatrix() {
        return transMatrix;
    }

    public ArrayList<StnInfo> getStnIdx() {
        return stnIdx;
    }

    public int getN() {
        return n;
    }

    private void setWeight(String lineNm, String stnNm1, String stnNm2, int weight, boolean isBothWay) {
        /* lineNm           두 역의 호선
         * stnNm1, stnNm2   연결할 역
         * weight           소요시간
         * isBothWay        true이면 양방향, false이면 단방향(예:6호선)
         * 예) 소요산, 동두천
         *     setWeight("1호선", "소요산", "동두천", 2, true); */

        // 1. 첫번째 역 index 찾기
        int a = getIndex(lineNm, stnNm1); //stnNm1의 행|열 번호

        // 2. 두번째 역 index 찾기
        int b = getIndex(lineNm, stnNm2); //stnNm2의 행|열 번호

        // 3. 첫번째 역(a, stnNm1)와 두번째 역(b, stnNm2) 사이의 소요시간(가중치) 작성
        rawMatrix[a][b] = weight;
        if (isBothWay) //양방향이면....
            rawMatrix[b][a] = weight; //반대방향도 작성
    }

    private void setTrans(String stnNm, int...time) {
        /* stnNm 환승역 이름     예) 동대문
         *                           setTrans("동대문"); */
        ArrayList<Integer> indexSet = getIndexes(stnNm); //stnNm이 존재하는 행|열 번호 목록
        int t = 0; //time의 index
        for (int i = 0; i < indexSet.size(); i++) {
            for (int j = i + 1; j < indexSet.size(); j++) {
                int a = indexSet.get(i), b = indexSet.get(j);
                // 1호선 동대문과 4호선 동대문의 역간거리
                rawMatrix[a][b] = time[t++];
                rawMatrix[b][a] = time[t++];
            }
        }
    }

    private void setMatrix() {
        // 가중치(역간 소요시간) 입력
        // 1호선
        setWeight("1호선", "소요산", "동두천", 3, true);
        setWeight("1호선", "동두천", "보산", 3, true);
        setWeight("1호선", "보산", "동두천중앙", 2, true);
        setWeight("1호선", "동두천중앙", "지행", 2, true);
        setWeight("1호선", "지행", "덕정", 5, true);
        setWeight("1호선", "덕정", "덕계", 4, true);
        setWeight("1호선", "덕계", "양주", 5, true);
        setWeight("1호선", "양주", "녹양", 2, true);
        setWeight("1호선", "녹양", "가능", 2, true);
        setWeight("1호선", "가능", "의정부", 3, true);
        setWeight("1호선", "의정부", "회룡", 3, true);
        setWeight("1호선", "회룡", "망월사", 2, true);
        setWeight("1호선", "망월사", "도봉산", 3, true);
        setWeight("1호선", "도봉산", "도봉", 2, true);
        setWeight("1호선", "도봉", "방학", 2, true);
        setWeight("1호선", "방학", "창동", 2, true);
        setWeight("1호선", "창동", "녹천", 2, true);
        setWeight("1호선", "녹천", "월계", 2, true);
        setWeight("1호선", "월계", "광운대", 3, true);
        setWeight("1호선", "광운대", "석계", 3, true);
        setWeight("1호선", "석계", "신이문", 2, true);
        setWeight("1호선", "신이문", "외대앞", 1, true);
        setWeight("1호선", "외대앞", "회기", 2, true);
        setWeight("1호선", "회기", "청량리", 3, true);
        setWeight("1호선", "청량리", "제기동", 2, true);
        setWeight("1호선", "제기동", "신설동", 2, true);
        setWeight("1호선", "신설동", "동묘앞", 2, true);
        setWeight("1호선", "동묘앞", "동대문", 2, true);
        setWeight("1호선", "동대문", "종로5가", 2, true);
        setWeight("1호선", "종로5가", "종로3가", 2, true);
        setWeight("1호선", "종로3가", "종각", 2, true);
        setWeight("1호선", "종각", "시청", 2, true);
        setWeight("1호선", "시청", "서울", 2, true);
        setWeight("1호선", "서울", "남영", 3, true);
        setWeight("1호선", "남영", "용산", 3, true);
        setWeight("1호선", "용산", "노량진", 3, true);
        setWeight("1호선", "노량진", "대방", 2, true);
        setWeight("1호선", "대방", "신길", 2, true);
        setWeight("1호선", "신길", "영등포", 3, true);
        setWeight("1호선", "영등포", "신도림", 3, true);
        setWeight("1호선", "신도림", "구로", 3, true);
        setWeight("1호선", "구로", "구일", 2, true);
        setWeight("1호선", "구일", "개봉", 2, true);
        setWeight("1호선", "개봉", "오류동", 3, true);
        setWeight("1호선", "오류동", "온수", 3, true);
        setWeight("1호선", "온수", "역곡", 2, true);
        setWeight("1호선", "역곡", "소사", 3, true);
        setWeight("1호선", "소사", "부천", 2, true);
        setWeight("1호선", "부천", "중동", 2, true);
        setWeight("1호선", "중동", "송내", 2, true);
        setWeight("1호선", "송내", "부개", 2, true);
        setWeight("1호선", "부개", "부평", 2, true);
        setWeight("1호선", "부평", "백운", 3, true);
        setWeight("1호선", "백운", "동암", 2, true);
        setWeight("1호선", "동암", "간석", 2, true);
        setWeight("1호선", "간석", "주안", 2, true);
        setWeight("1호선", "주안", "도화", 2, true);
        setWeight("1호선", "도화", "제물포", 2, true);
        setWeight("1호선", "제물포", "도원", 2, true);
        setWeight("1호선", "도원", "동인천", 2, true);
        setWeight("1호선", "동인천", "인천", 4, true);
        setWeight("1호선", "구로", "가산디지털단지", 5, true);
        setWeight("1호선", "가산디지털단지", "독산", 2, true);
        setWeight("1호선", "독산", "금천구청", 2, true);
        setWeight("1호선", "금천구청", "광명", 4, true);
        setWeight("1호선", "금천구청", "석수", 3, true);
        setWeight("1호선", "석수", "관악", 2, true);
        setWeight("1호선", "관악", "안양", 3, true);
        setWeight("1호선", "안양", "명학", 3, true);
        setWeight("1호선", "명학", "금정", 2, true);
        setWeight("1호선", "금정", "군포", 3, true);
        setWeight("1호선", "군포", "당정", 2, true);
        setWeight("1호선", "당정", "의왕", 3, true);
        setWeight("1호선", "의왕", "성균관대", 4, true);
        setWeight("1호선", "성균관대", "화서", 3, true);
        setWeight("1호선", "화서", "수원", 3, true);
        setWeight("1호선", "수원", "세류", 3, true);
        setWeight("1호선", "세류", "병점", 4, true);
        setWeight("1호선", "병점", "서동탄", 6, true);
        setWeight("1호선", "병점", "세마", 3, true);
        setWeight("1호선", "세마", "오산대", 3, true);
        setWeight("1호선", "오산대", "오산", 4, true);
        setWeight("1호선", "오산", "진위", 3, true);
        setWeight("1호선", "진위", "송탄", 4, true);
        setWeight("1호선", "송탄", "서정리", 3, true);
        setWeight("1호선", "서정리", "지제", 5, true);
        setWeight("1호선", "지제", "평택", 4, true);
        setWeight("1호선", "평택", "성환", 8, true);
        setWeight("1호선", "성환", "직산", 5, true);
        setWeight("1호선", "직산", "두정", 4, true);
        setWeight("1호선", "두정", "천안", 4, true);
        setWeight("1호선", "천안", "봉명", 3, true);
        setWeight("1호선", "봉명", "쌍용(나사렛대)", 3, true);
        setWeight("1호선", "쌍용(나사렛대)", "아산", 3, true);
        setWeight("1호선", "아산", "배방", 5, true);
        setWeight("1호선", "배방", "온양온천", 5, true);
        setWeight("1호선", "온양온천", "신창(순천향대)", 5, true);

// 2호선
        setWeight("2호선", "시청", "을지로입구", 2, true);
        setWeight("2호선", "을지로입구", "을지로3가", 2, true);
        setWeight("2호선", "을지로3가", "을지로4가", 1, true);
        setWeight("2호선", "을지로4가", "동대문역사문화공원", 2, true);
        setWeight("2호선", "동대문역사문화공원", "신당", 2, true);
        setWeight("2호선", "신당", "상왕십리", 1, true);
        setWeight("2호선", "상왕십리", "왕십리", 1, true);
        setWeight("2호선", "왕십리", "한양대", 2, true);
        setWeight("2호선", "한양대", "뚝섬", 2, true);
        setWeight("2호선", "뚝섬", "성수", 1, true);
        setWeight("2호선", "성수", "용답", 3, true);
        setWeight("2호선", "용답", "신답", 2, true);
        setWeight("2호선", "신답", "용두", 2, true);
        setWeight("2호선", "용두", "신설동", 2, true);
        setWeight("2호선", "성수", "건대입구", 2, true);
        setWeight("2호선", "건대입구", "구의", 2, true);
        setWeight("2호선", "구의", "강변", 2, true);
        setWeight("2호선", "강변", "잠실나루", 2, true);
        setWeight("2호선", "잠실나루", "잠실", 2, true);
        setWeight("2호선", "잠실", "잠실새내", 2, true);
        setWeight("2호선", "잠실새내", "종합운동장", 2, true);
        setWeight("2호선", "종합운동장", "삼성", 2, true);
        setWeight("2호선", "삼성", "선릉", 3, true);
        setWeight("2호선", "선릉", "역삼", 3, true);
        setWeight("2호선", "역삼", "강남", 1, true);
        setWeight("2호선", "강남", "교대", 3, true);
        setWeight("2호선", "교대", "서초", 2, true);
        setWeight("2호선", "서초", "방배", 3, true);
        setWeight("2호선", "방배", "사당", 3, true);
        setWeight("2호선", "사당", "낙성대", 3, true);
        setWeight("2호선", "낙성대", "서울대입구", 5, true);
        setWeight("2호선", "서울대입구", "봉천", 2, true);
        setWeight("2호선", "봉천", "신림", 2, true);
        setWeight("2호선", "신림", "신대방", 3, true);
        setWeight("2호선", "신대방", "구로디지털단지", 2, true);
        setWeight("2호선", "구로디지털단지", "대림", 2, true);
        setWeight("2호선", "대림", "신도림", 3, true);
        setWeight("2호선", "신도림", "도림천", 2, true);
        setWeight("2호선", "도림천", "양천구청", 3, true);
        setWeight("2호선", "양천구청", "신정네거리", 3, true);
        setWeight("2호선", "신정네거리", "까치산", 3, true);
        setWeight("2호선", "신도림", "문래", 3, true);
        setWeight("2호선", "문래", "영등포구청", 1, true);
        setWeight("2호선", "영등포구청", "당산", 2, true);
        setWeight("2호선", "당산", "합정", 3, true);
        setWeight("2호선", "합정", "홍대입구", 3, true);
        setWeight("2호선", "홍대입구", "신촌", 3, true);
        setWeight("2호선", "신촌", "이대", 2, true);
        setWeight("2호선", "이대", "아현", 1, true);
        setWeight("2호선", "아현", "충정로", 2, true);
        setWeight("2호선", "충정로", "시청", 1, true);

// 3호선
        setWeight("3호선", "대화", "주엽", 2, true);
        setWeight("3호선", "주엽", "정발산", 2, true);
        setWeight("3호선", "정발산", "마두", 2, true);
        setWeight("3호선", "마두", "백석", 3, true);
        setWeight("3호선", "백석", "대곡", 4, true);
        setWeight("3호선", "대곡", "화정", 3, true);
        setWeight("3호선", "화정", "원당", 3, true);
        setWeight("3호선", "원당", "원흥", 3, true);
        setWeight("3호선", "원흥", "삼송", 3, true);
        setWeight("3호선", "삼송", "지축", 3, true);
        setWeight("3호선", "지축", "구파발", 3, true);
        setWeight("3호선", "구파발", "연신내", 2, true);
        setWeight("3호선", "연신내", "불광", 2, true);
        setWeight("3호선", "불광", "녹번", 2, true);
        setWeight("3호선", "녹번", "홍제", 2, true);
        setWeight("3호선", "홍제", "무악재", 2, true);
        setWeight("3호선", "무악재", "독립문", 2, true);
        setWeight("3호선", "독립문", "경복궁", 2, true);
        setWeight("3호선", "경복궁", "안국", 2, true);
        setWeight("3호선", "안국", "종로3가", 2, true);
        setWeight("3호선", "종로3가", "을지로3가", 1, true);
        setWeight("3호선", "을지로3가", "충무로", 1, true);
        setWeight("3호선", "충무로", "동대입구", 2, true);
        setWeight("3호선", "동대입구", "약수", 2, true);
        setWeight("3호선", "약수", "금호", 1, true);
        setWeight("3호선", "금호", "옥수", 2, true);
        setWeight("3호선", "옥수", "압구정", 3, true);
        setWeight("3호선", "압구정", "신사", 3, true);
        setWeight("3호선", "신사", "잠원", 2, true);
        setWeight("3호선", "잠원", "고속터미널", 2, true);
        setWeight("3호선", "고속터미널", "교대", 2, true);
        setWeight("3호선", "교대", "남부터미널", 2, true);
        setWeight("3호선", "남부터미널", "양재", 3, true);
        setWeight("3호선", "양재", "매봉", 3, true);
        setWeight("3호선", "매봉", "도곡", 2, true);
        setWeight("3호선", "도곡", "대치", 1, true);
        setWeight("3호선", "대치", "학여울", 1, true);
        setWeight("3호선", "학여울", "대청", 2, true);
        setWeight("3호선", "대청", "일원", 2, true);
        setWeight("3호선", "일원", "수서", 2, true);
        setWeight("3호선", "수서", "가락시장", 2, true);
        setWeight("3호선", "가락시장", "경찰병원", 1, true);
        setWeight("3호선", "경찰병원", "오금", 1, true);

// 4호선
        setWeight("4호선", "당고개", "상계", 2, true);
        setWeight("4호선", "상계", "노원", 2, true);
        setWeight("4호선", "노원", "창동", 2, true);
        setWeight("4호선", "창동", "쌍문", 2, true);
        setWeight("4호선", "쌍문", "수유(강북구청)", 3, true);
        setWeight("4호선", "수유(강북구청)", "미아", 4, true);
        setWeight("4호선", "미아", "미아사거리", 2, true);
        setWeight("4호선", "미아사거리", "길음", 2, true);
        setWeight("4호선", "길음", "성신여대입구", 3, true);
        setWeight("4호선", "성신여대입구", "한성대입구", 2, true);
        setWeight("4호선", "한성대입구", "혜화", 2, true);
        setWeight("4호선", "혜화", "동대문", 2, true);
        setWeight("4호선", "동대문", "동대문역사문화공원", 2, true);
        setWeight("4호선", "동대문역사문화공원", "충무로", 2, true);
        setWeight("4호선", "충무로", "명동", 1, true);
        setWeight("4호선", "명동", "회현", 2, true);
        setWeight("4호선", "회현", "서울", 2, true);
        setWeight("4호선", "서울", "숙대입구", 2, true);
        setWeight("4호선", "숙대입구", "삼각지", 2, true);
        setWeight("4호선", "삼각지", "신용산", 1, true);
        setWeight("4호선", "신용산", "이촌", 2, true);
        setWeight("4호선", "이촌", "동작", 4, true);
        setWeight("4호선", "동작", "총신대입구(이수)", 3, true);
        setWeight("4호선", "총신대입구(이수)", "사당", 2, true);
        setWeight("4호선", "사당", "남태령", 1, true);
        setWeight("4호선", "남태령", "선바위", 3, true);
        setWeight("4호선", "선바위", "경마공원", 3, true);
        setWeight("4호선", "경마공원", "대공원", 2, true);
        setWeight("4호선", "대공원", "과천", 2, true);
        setWeight("4호선", "과천", "정부과천청사", 2, true);
        setWeight("4호선", "정부과천청사", "인덕원", 3, true);
        setWeight("4호선", "인덕원", "평촌", 2, true);
        setWeight("4호선", "평촌", "범계", 2, true);
        setWeight("4호선", "범계", "금정", 3, true);
        setWeight("4호선", "금정", "산본", 4, true);
        setWeight("4호선", "산본", "수리산", 2, true);
        setWeight("4호선", "수리산", "대야미", 3, true);
        setWeight("4호선", "대야미", "반월", 3, true);
        setWeight("4호선", "반월", "상록수", 4, true);
        setWeight("4호선", "상록수", "한대앞", 2, true);
        setWeight("4호선", "한대앞", "중앙", 2, true);
        setWeight("4호선", "중앙", "고잔", 2, true);
        setWeight("4호선", "고잔", "초지", 3, true);
        setWeight("4호선", "초지", "안산", 2, true);
        setWeight("4호선", "안산", "신길온천", 3, true);
        setWeight("4호선", "신길온천", "정왕", 4, true);
        setWeight("4호선", "정왕", "오이도", 2, true);

// 5호선
        setWeight("5호선", "방화", "개화산", 2, true);
        setWeight("5호선", "개화산", "김포공항", 3, true);
        setWeight("5호선", "김포공항", "송정", 2, true);
        setWeight("5호선", "송정", "마곡", 2, true);
        setWeight("5호선", "마곡", "발산", 2, true);
        setWeight("5호선", "발산", "우장산", 2, true);
        setWeight("5호선", "우장산", "화곡", 2, true);
        setWeight("5호선", "화곡", "까치산", 2, true);
        setWeight("5호선", "까치산", "신정", 3, true);
        setWeight("5호선", "신정", "목동", 1, true);
        setWeight("5호선", "목동", "오목교", 2, true);
        setWeight("5호선", "오목교", "양평", 2, true);
        setWeight("5호선", "양평", "영등포구청", 1, true);
        setWeight("5호선", "영등포구청", "영등포시장", 2, true);
        setWeight("5호선", "영등포시장", "신길", 2, true);
        setWeight("5호선", "신길", "여의도", 2, true);
        setWeight("5호선", "여의도", "여의나루", 2, true);
        setWeight("5호선", "여의나루", "마포", 2, true);
        setWeight("5호선", "마포", "공덕", 2, true);
        setWeight("5호선", "공덕", "애오개", 2, true);
        setWeight("5호선", "애오개", "충정로", 1, true);
        setWeight("5호선", "충정로", "서대문", 2, true);
        setWeight("5호선", "서대문", "광화문", 2, true);
        setWeight("5호선", "광화문", "종로3가", 2, true);
        setWeight("5호선", "종로3가", "을지로4가", 2, true);
        setWeight("5호선", "을지로4가", "동대문역사문화공원", 2, true);
        setWeight("5호선", "동대문역사문화공원", "청구", 2, true);
        setWeight("5호선", "청구", "신금호", 2, true);
        setWeight("5호선", "신금호", "행당", 2, true);
        setWeight("5호선", "행당", "왕십리", 1, true);
        setWeight("5호선", "왕십리", "마장", 2, true);
        setWeight("5호선", "마장", "답십리", 2, true);
        setWeight("5호선", "답십리", "장한평", 2, true);
        setWeight("5호선", "장한평", "군자", 2, true);
        setWeight("5호선", "군자", "아차산", 2, true);
        setWeight("5호선", "아차산", "광나루", 2, true);
        setWeight("5호선", "광나루", "천호", 3, true);
        setWeight("5호선", "천호", "강동", 2, true);
        setWeight("5호선", "강동", "길동", 2, true);
        setWeight("5호선", "길동", "굽은다리", 2, true);
        setWeight("5호선", "굽은다리", "명일", 2, true);
        setWeight("5호선", "명일", "고덕", 2, true);
        setWeight("5호선", "고덕", "상일동", 2, true);
        setWeight("5호선", "강동", "둔촌동", 2, true);
        setWeight("5호선", "둔촌동", "올림픽공원", 2, true);
        setWeight("5호선", "올림픽공원", "방이", 2, true);
        setWeight("5호선", "방이", "오금", 2, true);
        setWeight("5호선", "오금", "개롱", 2, true);
        setWeight("5호선", "개롱", "거여", 1, true);
        setWeight("5호선", "거여", "마천", 1, true);

// 6호선
        setWeight("6호선", "응암", "역촌", 2, false);
        setWeight("6호선", "역촌", "불광", 2, false);
        setWeight("6호선", "불광", "독바위", 1, false);
        setWeight("6호선", "독바위", "연신내", 3, false);
        setWeight("6호선", "연신내", "구산", 2, false);
        setWeight("6호선", "구산", "응암", 2, false);
        setWeight("6호선", "응암", "새절", 2, true);
        setWeight("6호선", "새절", "증산", 2, true);
        setWeight("6호선", "증산", "디지털미디어시티", 2, true);
        setWeight("6호선", "디지털미디어시티", "월드컵경기장", 2, true);
        setWeight("6호선", "월드컵경기장", "마포구청", 1, true);
        setWeight("6호선", "마포구청", "망원", 2, true);
        setWeight("6호선", "망원", "합정", 2, true);
        setWeight("6호선", "합정", "상수", 2, true);
        setWeight("6호선", "상수", "광흥창", 1, true);
        setWeight("6호선", "광흥창", "대흥", 2, true);
        setWeight("6호선", "대흥", "공덕", 2, true);
        setWeight("6호선", "공덕", "효창공원앞", 2, true);
        setWeight("6호선", "효창공원앞", "삼각지", 2, true);
        setWeight("6호선", "삼각지", "녹사평(용산구청)", 2, true);
        setWeight("6호선", "녹사평(용산구청)", "이태원", 2, true);
        setWeight("6호선", "이태원", "한강진", 2, true);
        setWeight("6호선", "한강진", "버티고개", 2, true);
        setWeight("6호선", "버티고개", "약수", 1, true);
        setWeight("6호선", "약수", "청구", 2, true);
        setWeight("6호선", "청구", "신당", 2, true);
        setWeight("6호선", "신당", "동묘앞", 1, true);
        setWeight("6호선", "동묘앞", "창신", 2, true);
        setWeight("6호선", "창신", "보문", 2, true);
        setWeight("6호선", "보문", "안암", 1, true);
        setWeight("6호선", "안암", "고려대", 2, true);
        setWeight("6호선", "고려대", "월곡", 2, true);
        setWeight("6호선", "월곡", "상월곡", 2, true);
        setWeight("6호선", "상월곡", "돌곶이", 1, true);
        setWeight("6호선", "돌곶이", "석계", 2, true);
        setWeight("6호선", "석계", "태릉입구", 2, true);
        setWeight("6호선", "태릉입구", "화랑대", 2, true);
        setWeight("6호선", "화랑대", "봉화산(서울의료원)", 1, true);

// 7호선
        setWeight("7호선", "장암", "도봉산", 5, true);
        setWeight("7호선", "도봉산", "수락산", 3, true);
        setWeight("7호선", "수락산", "마들", 3, true);
        setWeight("7호선", "마들", "노원", 2, true);
        setWeight("7호선", "노원", "중계", 2, true);
        setWeight("7호선", "중계", "하계", 2, true);
        setWeight("7호선", "하계", "공릉", 2, true);
        setWeight("7호선", "공릉", "태릉입구", 1, true);
        setWeight("7호선", "태릉입구", "먹골", 2, true);
        setWeight("7호선", "먹골", "중화", 2, true);
        setWeight("7호선", "중화", "상봉", 3, true);
        setWeight("7호선", "상봉", "면목", 2, true);
        setWeight("7호선", "면목", "사가정", 2, true);
        setWeight("7호선", "사가정", "용마산", 2, true);
        setWeight("7호선", "용마산", "중곡", 1, true);
        setWeight("7호선", "중곡", "군자", 2, true);
        setWeight("7호선", "군자", "어린이대공원", 2, true);
        setWeight("7호선", "어린이대공원", "건대입구", 1, true);
        setWeight("7호선", "건대입구", "뚝섬유원지", 2, true);
        setWeight("7호선", "뚝섬유원지", "청담", 2, true);
        setWeight("7호선", "청담", "강남구청", 2, true);
        setWeight("7호선", "강남구청", "학동", 2, true);
        setWeight("7호선", "학동", "논현", 2, true);
        setWeight("7호선", "논현", "반포", 2, true);
        setWeight("7호선", "반포", "고속터미널", 2, true);
        setWeight("7호선", "고속터미널", "내방", 4, true);
        setWeight("7호선", "내방", "총신대입구(이수)", 2, true);
        setWeight("7호선", "총신대입구(이수)", "남성", 2, true);
        setWeight("7호선", "남성", "숭실대입구", 3, true);
        setWeight("7호선", "숭실대입구", "상도", 1, true);
        setWeight("7호선", "상도", "장승배기", 2, true);
        setWeight("7호선", "장승배기", "신대방삼거리", 3, true);
        setWeight("7호선", "신대방삼거리", "보라매", 2, true);
        setWeight("7호선", "보라매", "신풍", 2, true);
        setWeight("7호선", "신풍", "대림", 2, true);
        setWeight("7호선", "대림", "남구로", 2, true);
        setWeight("7호선", "남구로", "가산디지털단지", 2, true);
        setWeight("7호선", "가산디지털단지", "철산", 3, true);
        setWeight("7호선", "철산", "광명사거리", 2, true);
        setWeight("7호선", "광명사거리", "천왕", 3, true);
        setWeight("7호선", "천왕", "온수", 3, true);
        setWeight("7호선", "온수", "까치울", 3, true);
        setWeight("7호선", "까치울", "부천종합운동장", 2, true);
        setWeight("7호선", "부천종합운동장", "춘의", 2, true);
        setWeight("7호선", "춘의", "신중동", 2, true);
        setWeight("7호선", "신중동", "부천시청", 2, true);
        setWeight("7호선", "부천시청", "상동", 1, true);
        setWeight("7호선", "상동", "삼산체육관", 1, true);
        setWeight("7호선", "삼산체육관", "굴포천", 2, true);
        setWeight("7호선", "굴포천", "부평구청", 2, true);

// 8호선
        setWeight("8호선", "암사", "천호", 2, true);
        setWeight("8호선", "천호", "강동구청", 2, true);
        setWeight("8호선", "강동구청", "몽촌토성", 2, true);
        setWeight("8호선", "몽촌토성", "잠실", 2, true);
        setWeight("8호선", "잠실", "석촌", 2, true);
        setWeight("8호선", "석촌", "송파", 2, true);
        setWeight("8호선", "송파", "가락시장", 2, true);
        setWeight("8호선", "가락시장", "문정", 1, true);
        setWeight("8호선", "문정", "장지", 2, true);
        setWeight("8호선", "장지", "복정", 1, true);
        setWeight("8호선", "복정", "산성", 3, true);
        setWeight("8호선", "산성", "남한산성입구", 3, true);
        setWeight("8호선", "남한산성입구", "단대오거리", 2, true);
        setWeight("8호선", "단대오거리", "신흥", 2, true);
        setWeight("8호선", "신흥", "수진", 2, true);
        setWeight("8호선", "수진", "모란", 1, true);

// 9호선
        setWeight("9호선", "개화", "김포공항", 5, true);
        setWeight("9호선", "김포공항", "공항시장", 2, true);
        setWeight("9호선", "공항시장", "신방화", 1, true);
        setWeight("9호선", "신방화", "마곡나루", 2, true);
        setWeight("9호선", "마곡나루", "양천향교", 3, true);
        setWeight("9호선", "양천향교", "가양", 2, true);
        setWeight("9호선", "가양", "증미", 2, true);
        setWeight("9호선", "증미", "등촌", 2, true);
        setWeight("9호선", "등촌", "염창", 2, true);
        setWeight("9호선", "염창", "신목동", 2, true);
        setWeight("9호선", "신목동", "선유도", 2, true);
        setWeight("9호선", "선유도", "당산", 2, true);
        setWeight("9호선", "당산", "국회의사당", 2, true);
        setWeight("9호선", "국회의사당", "여의도", 2, true);
        setWeight("9호선", "여의도", "샛강", 4, true);
        setWeight("9호선", "샛강", "노량진", 4, true);
        setWeight("9호선", "노량진", "노들", 2, true);
        setWeight("9호선", "노들", "흑석", 3, true);
        setWeight("9호선", "흑석", "동작", 3, true);
        setWeight("9호선", "동작", "구반포", 2, true);
        setWeight("9호선", "구반포", "신반포", 2, true);
        setWeight("9호선", "신반포", "고속터미널", 2, true);
        setWeight("9호선", "고속터미널", "사평", 2, true);
        setWeight("9호선", "사평", "신논현", 2, true);
        setWeight("9호선", "신논현", "언주", 2, true);
        setWeight("9호선", "언주", "선정릉", 2, true);
        setWeight("9호선", "선정릉", "삼성중앙", 2, true);
        setWeight("9호선", "삼성중앙", "봉은사", 2, true);
        setWeight("9호선", "봉은사", "종합운동장", 2, true);

// 인천국제공항철도
        setWeight("인천국제공항철도", "서울", "공덕", 4, true);
        setWeight("인천국제공항철도", "공덕", "홍대입구", 3, true);
        setWeight("인천국제공항철도", "홍대입구", "디지털미디어시티", 4, true);
        setWeight("인천국제공항철도", "디지털미디어시티", "김포공항", 10, true);
        setWeight("인천국제공항철도", "김포공항", "계양", 7, true);
        setWeight("인천국제공항철도", "계양", "검암", 6, true);
        setWeight("인천국제공항철도", "검암", "청라국제도시", 5, true);
        setWeight("인천국제공항철도", "청라국제도시", "영종", 9, true);
        setWeight("인천국제공항철도", "영종", "운서", 4, true);
        setWeight("인천국제공항철도", "운서", "공항화물청사", 5, true);
        setWeight("인천국제공항철도", "공항화물청사", "인천국제공항", 4, true);

// 자기부상철도
        setWeight("자기부상철도", "인천국제공항", "장기주차장", 2, true);
        setWeight("자기부상철도", "장기주차장", "합동청사", 1, true);
        setWeight("자기부상철도", "합동청사", "파라다이스시티", 1, true);
        setWeight("자기부상철도", "파라다이스시티", "워터파크", 4, true);
        setWeight("자기부상철도", "워터파크", "용유", 2, true);

// 분당선
        setWeight("분당선", "왕십리", "서울숲", 3, true);
        setWeight("분당선", "서울숲", "압구정로데오", 1, true);
        setWeight("분당선", "압구정로데오", "강남구청", 3, true);
        setWeight("분당선", "강남구청", "선정릉", 2, true);
        setWeight("분당선", "선정릉", "선릉", 2, true);
        setWeight("분당선", "선릉", "한티", 2, true);
        setWeight("분당선", "한티", "도곡", 2, true);
        setWeight("분당선", "도곡", "구룡", 2, true);
        setWeight("분당선", "구룡", "개포동", 2, true);
        setWeight("분당선", "개포동", "대모산입구", 2, true);
        setWeight("분당선", "대모산입구", "수서", 4, true);
        setWeight("분당선", "수서", "복정", 4, true);
        setWeight("분당선", "복정", "가천대", 3, true);
        setWeight("분당선", "가천대", "태평", 2, true);
        setWeight("분당선", "태평", "모란", 2, true);
        setWeight("분당선", "모란", "야탑", 3, true);
        setWeight("분당선", "야탑", "이매", 2, true);
        setWeight("분당선", "이매", "서현", 2, true);
        setWeight("분당선", "서현", "수내", 2, true);
        setWeight("분당선", "수내", "정자", 3, true);
        setWeight("분당선", "정자", "미금", 3, true);
        setWeight("분당선", "미금", "오리", 2, true);
        setWeight("분당선", "오리", "죽전", 3, true);
        setWeight("분당선", "죽전", "보정", 3, true);
        setWeight("분당선", "보정", "구성", 3, true);
        setWeight("분당선", "구성", "신갈", 3, true);
        setWeight("분당선", "신갈", "기흥", 2, true);
        setWeight("분당선", "기흥", "상갈", 2, true);
        setWeight("분당선", "상갈", "청명", 3, true);
        setWeight("분당선", "청명", "영통", 1, true);
        setWeight("분당선", "영통", "망포", 2, true);
        setWeight("분당선", "망포", "매탄권선", 2, true);
        setWeight("분당선", "매탄권선", "수원시청", 3, true);
        setWeight("분당선", "수원시청", "매교", 3, true);
        setWeight("분당선", "매교", "수원", 2, true);

// 에버라인
        setWeight("에버라인", "기흥", "강남대", 1, true);
        setWeight("에버라인", "강남대", "지석", 2, true);
        setWeight("에버라인", "지석", "어정", 1, true);
        setWeight("에버라인", "어정", "동백", 2, true);
        setWeight("에버라인", "동백", "초당", 4, true);
        setWeight("에버라인", "초당", "삼가", 5, true);
        setWeight("에버라인", "삼가", "시청·용인대", 2, true);
        setWeight("에버라인", "시청·용인대", "명지대", 2, true);
        setWeight("에버라인", "명지대", "김량장", 1, true);
        setWeight("에버라인", "김량장", "운동장·송담대", 1, true);
        setWeight("에버라인", "운동장·송담대", "고진", 1, true);
        setWeight("에버라인", "고진", "보평", 2, true);
        setWeight("에버라인", "보평", "둔전", 2, true);
        setWeight("에버라인", "둔전", "전대·에버랜드", 3, true);

// 경춘선
        setWeight("경춘선", "청량리", "회기", 3, true);
        setWeight("경춘선", "회기", "중랑", 3, true);
        setWeight("경춘선", "중랑", "상봉", 2, true);
        setWeight("경춘선", "상봉", "망우", 2, true);
        setWeight("경춘선", "망우", "신내", 2, true);
        setWeight("경춘선", "신내", "갈매", 4, true);
        setWeight("경춘선", "갈매", "별내", 2, true);
        setWeight("경춘선", "별내", "퇴계원", 2, true);
        setWeight("경춘선", "퇴계원", "사릉", 4, true);
        setWeight("경춘선", "사릉", "금곡", 6, true);
        setWeight("경춘선", "금곡", "평내호평", 4, true);
        setWeight("경춘선", "평내호평", "천마산", 4, true);
        setWeight("경춘선", "천마산", "마석", 3, true);
        setWeight("경춘선", "마석", "대성리", 6, true);
        setWeight("경춘선", "대성리", "청평", 10, true);
        setWeight("경춘선", "청평", "상천", 4, true);
        setWeight("경춘선", "상천", "가평", 6, true);
        setWeight("경춘선", "가평", "굴봉산", 5, true);
        setWeight("경춘선", "굴봉산", "백양리", 3, true);
        setWeight("경춘선", "백양리", "강촌", 5, true);
        setWeight("경춘선", "강촌", "김유정", 6, true);
        setWeight("경춘선", "김유정", "남춘천", 6, true);
        setWeight("경춘선", "남춘천", "춘천", 3, true);

// 인천1호선
        setWeight("인천1호선", "계양", "귤현", 2, true);
        setWeight("인천1호선", "귤현", "박촌", 3, true);
        setWeight("인천1호선", "박촌", "임학", 2, true);
        setWeight("인천1호선", "임학", "계산", 2, true);
        setWeight("인천1호선", "계산", "경인교대입구", 2, true);
        setWeight("인천1호선", "경인교대입구", "작전", 2, true);
        setWeight("인천1호선", "작전", "갈산", 2, true);
        setWeight("인천1호선", "갈산", "부평구청", 2, true);
        setWeight("인천1호선", "부평구청", "부평시장", 2, true);
        setWeight("인천1호선", "부평시장", "부평", 2, true);
        setWeight("인천1호선", "부평", "동수", 2, true);
        setWeight("인천1호선", "동수", "부평삼거리", 2, true);
        setWeight("인천1호선", "부평삼거리", "간석오거리", 2, true);
        setWeight("인천1호선", "간석오거리", "인천시청", 3, true);
        setWeight("인천1호선", "인천시청", "예술회관", 2, true);
        setWeight("인천1호선", "예술회관", "인천터미널", 1, true);
        setWeight("인천1호선", "인천터미널", "문학경기장", 2, true);
        setWeight("인천1호선", "문학경기장", "선학", 1, true);
        setWeight("인천1호선", "선학", "신연수", 2, true);
        setWeight("인천1호선", "신연수", "원인재", 2, true);
        setWeight("인천1호선", "원인재", "동춘", 2, true);
        setWeight("인천1호선", "동춘", "동막", 2, true);
        setWeight("인천1호선", "동막", "캠퍼스타운", 2, true);
        setWeight("인천1호선", "캠퍼스타운", "테크노파크", 2, true);
        setWeight("인천1호선", "테크노파크", "지식정보단지", 2, true);
        setWeight("인천1호선", "지식정보단지", "인천대입구", 2, true);
        setWeight("인천1호선", "인천대입구", "센트럴파크", 2, true);
        setWeight("인천1호선", "센트럴파크", "국제업무지구", 2, true);

// 인천2호선
        setWeight("인천2호선", "검단오류", "왕길", 2, true);
        setWeight("인천2호선", "왕길", "검단사거리", 2, true);
        setWeight("인천2호선", "검단사거리", "마전", 2, true);
        setWeight("인천2호선", "마전", "완정", 1, true);
        setWeight("인천2호선", "완정", "독정", 2, true);
        setWeight("인천2호선", "독정", "검암", 3, true);
        setWeight("인천2호선", "검암", "검바위", 2, true);
        setWeight("인천2호선", "검바위", "아시아드경기장", 1, true);
        setWeight("인천2호선", "아시아드경기장", "서구청", 1, true);
        setWeight("인천2호선", "서구청", "가정", 2, true);
        setWeight("인천2호선", "가정", "가정중앙시장", 2, true);
        setWeight("인천2호선", "가정중앙시장", "석남", 2, true);
        setWeight("인천2호선", "석남", "서부여성회관", 1, true);
        setWeight("인천2호선", "서부여성회관", "인천가좌", 2, true);
        setWeight("인천2호선", "인천가좌", "가재울", 3, true);
        setWeight("인천2호선", "가재울", "주안국가산단", 2, true);
        setWeight("인천2호선", "주안국가산단", "주안", 2, true);
        setWeight("인천2호선", "주안", "시민공원", 2, true);
        setWeight("인천2호선", "시민공원", "석바위시장", 2, true);
        setWeight("인천2호선", "석바위시장", "인천시청", 1, true);
        setWeight("인천2호선", "인천시청", "석천사거리", 2, true);
        setWeight("인천2호선", "석천사거리", "모래내시장", 1, true);
        setWeight("인천2호선", "모래내시장", "만수", 2, true);
        setWeight("인천2호선", "만수", "남동구청", 2, true);
        setWeight("인천2호선", "남동구청", "인천대공원", 2, true);
        setWeight("인천2호선", "인천대공원", "운연", 2, true);

// 경의중앙선
        setWeight("경의중앙선", "용산", "이촌", 4, true);
        setWeight("경의중앙선", "이촌", "서빙고", 2, true);
        setWeight("경의중앙선", "서빙고", "한남", 3, true);
        setWeight("경의중앙선", "한남", "옥수", 3, true);
        setWeight("경의중앙선", "옥수", "응봉", 3, true);
        setWeight("경의중앙선", "응봉", "왕십리", 2, true);
        setWeight("경의중앙선", "왕십리", "청량리", 4, true);
        setWeight("경의중앙선", "청량리", "회기", 4, true);
        setWeight("경의중앙선", "회기", "중랑", 3, true);
        setWeight("경의중앙선", "중랑", "상봉", 2, true);
        setWeight("경의중앙선", "상봉", "망우", 2, true);
        setWeight("경의중앙선", "망우", "양원", 3, true);
        setWeight("경의중앙선", "양원", "구리", 4, true);
        setWeight("경의중앙선", "구리", "도농", 7, true);
        setWeight("경의중앙선", "도농", "양정", 4, true);
        setWeight("경의중앙선", "양정", "덕소", 3, true);
        setWeight("경의중앙선", "덕소", "도심", 2, true);
        setWeight("경의중앙선", "도심", "팔당", 4, true);
        setWeight("경의중앙선", "팔당", "운길산", 5, true);
        setWeight("경의중앙선", "운길산", "양수", 3, true);
        setWeight("경의중앙선", "양수", "신원", 4, true);
        setWeight("경의중앙선", "신원", "국수", 4, true);
        setWeight("경의중앙선", "국수", "아신", 4, true);
        setWeight("경의중앙선", "아신", "오빈", 3, true);
        setWeight("경의중앙선", "오빈", "양평", 3, true);
        setWeight("경의중앙선", "양평", "원덕", 5, true);
        setWeight("경의중앙선", "원덕", "용문", 4, true);
        setWeight("경의중앙선", "용문", "지평", 5, true);
        setWeight("경의중앙선", "용산", "효창공원앞", 3, true);
        setWeight("경의중앙선", "효창공원앞", "공덕", 3, true);
        setWeight("경의중앙선", "공덕", "서강대", 3, true);
        setWeight("경의중앙선", "서강대", "홍대입구", 2, true);
        setWeight("경의중앙선", "홍대입구", "가좌", 3, true);
        setWeight("경의중앙선", "가좌", "디지털미디어시티", 2, true);
        setWeight("경의중앙선", "디지털미디어시티", "수색", 2, true);
        setWeight("경의중앙선", "수색", "화전", 4, true);
        setWeight("경의중앙선", "화전", "강매", 3, true);
        setWeight("경의중앙선", "강매", "행신", 2, true);
        setWeight("경의중앙선", "행신", "능곡", 2, true);
        setWeight("경의중앙선", "능곡", "대곡", 3, true);
        setWeight("경의중앙선", "대곡", "곡산", 3, true);
        setWeight("경의중앙선", "곡산", "백마", 3, true);
        setWeight("경의중앙선", "백마", "풍산", 3, true);
        setWeight("경의중앙선", "풍산", "일산", 2, true);
        setWeight("경의중앙선", "일산", "탄현", 3, true);
        setWeight("경의중앙선", "탄현", "야당", 2, true);
        setWeight("경의중앙선", "야당", "운정", 2, true);
        setWeight("경의중앙선", "운정", "금릉", 4, true);
        setWeight("경의중앙선", "금릉", "금촌", 2, true);
        setWeight("경의중앙선", "금촌", "월롱", 4, true);
        setWeight("경의중앙선", "월롱", "파주", 3, true);
        setWeight("경의중앙선", "파주", "문산", 3, true);
        setWeight("경의중앙선", "가좌", "신촌(경의중앙선)", 4, true);
        setWeight("경의중앙선", "신촌(경의중앙선)", "서울", 6, true);

// 경강선
        setWeight("경강선", "판교", "이매", 2, true);
        setWeight("경강선", "이매", "삼동", 5, true);
        setWeight("경강선", "삼동", "경기광주", 5, true);
        setWeight("경강선", "경기광주", "초월", 5, true);
        setWeight("경강선", "초월", "곤지암", 5, true);
        setWeight("경강선", "곤지암", "신둔도예촌", 6, true);
        setWeight("경강선", "신둔도예촌", "이천", 6, true);
        setWeight("경강선", "이천", "부발", 4, true);
        setWeight("경강선", "부발", "세종대왕릉", 7, true);
        setWeight("경강선", "세종대왕릉", "여주", 5, true);

// 신분당선
        setWeight("신분당선", "강남", "양재", 2, true);
        setWeight("신분당선", "양재", "양재시민의숲", 2, true);
        setWeight("신분당선", "양재시민의숲", "청계산입구", 3, true);
        setWeight("신분당선", "청계산입구", "판교", 7, true);
        setWeight("신분당선", "판교", "정자", 3, true);
        setWeight("신분당선", "정자", "동천", 5, true);
        setWeight("신분당선", "동천", "수지구청", 3, true);
        setWeight("신분당선", "수지구청", "성복", 3, true);
        setWeight("신분당선", "성복", "상현", 3, true);
        setWeight("신분당선", "상현", "광교중앙", 3, true);
        setWeight("신분당선", "광교중앙", "광교", 3, true);

// 수인선
        setWeight("수인선", "오이도", "달월", 3, true);
        setWeight("수인선", "달월", "월곶", 2, true);
        setWeight("수인선", "월곶", "소래포구", 2, true);
        setWeight("수인선", "소래포구", "인천논현", 2, true);
        setWeight("수인선", "인천논현", "호구포", 2, true);
        setWeight("수인선", "호구포", "남동인더스파크", 2, true);
        setWeight("수인선", "남동인더스파크", "원인재", 2, true);
        setWeight("수인선", "원인재", "연수", 2, true);
        setWeight("수인선", "연수", "송도", 4, true);
        setWeight("수인선", "송도", "인하대", 2, true);
        setWeight("수인선", "인하대", "숭의", 3, true);
        setWeight("수인선", "숭의", "신포", 2, true);
        setWeight("수인선", "신포", "인천", 2, true);

// 의정부경전철
        setWeight("의정부경전철", "발곡", "회룡", 2, true);
        setWeight("의정부경전철", "회룡", "범골", 1, true);
        setWeight("의정부경전철", "범골", "경전철의정부", 2, true);
        setWeight("의정부경전철", "경전철의정부", "의정부시청", 2, true);
        setWeight("의정부경전철", "의정부시청", "흥선", 2, true);
        setWeight("의정부경전철", "흥선", "의정부중앙", 2, true);
        setWeight("의정부경전철", "의정부중앙", "동오", 2, true);
        setWeight("의정부경전철", "동오", "새말", 1, true);
        setWeight("의정부경전철", "새말", "경기도청북부청사", 2, true);
        setWeight("의정부경전철", "경기도청북부청사", "효자", 1, true);
        setWeight("의정부경전철", "효자", "곤제", 1, true);
        setWeight("의정부경전철", "곤제", "어룡", 2, true);
        setWeight("의정부경전철", "어룡", "송산", 1, true);
        setWeight("의정부경전철", "송산", "탑석", 1, true);

// 우이신설선
        setWeight("우이신설선", "북한산우이", "솔밭공원", 2, true);
        setWeight("우이신설선", "솔밭공원", "4.19민주묘지", 2, true);
        setWeight("우이신설선", "4.19민주묘지", "가오리", 2, true);
        setWeight("우이신설선", "가오리", "화계", 2, true);
        setWeight("우이신설선", "화계", "삼양", 2, true);
        setWeight("우이신설선", "삼양", "삼양사거리", 2, true);
        setWeight("우이신설선", "삼양사거리", "솔샘", 2, true);
        setWeight("우이신설선", "솔샘", "북한산보국문", 2, true);
        setWeight("우이신설선", "북한산보국문", "정릉", 2, true);
        setWeight("우이신설선", "정릉", "성신여대입구", 2, true);
        setWeight("우이신설선", "성신여대입구", "보문", 2, true);
        setWeight("우이신설선", "보문", "신설동", 2, true);

        // 환승역 설정
        // 1호선
        setTrans("회룡", 5, 3); //1-의정부
        setTrans("도봉산", 2, 2); //1-7
        setTrans("창동", 2, 2); //1-4
        setTrans("석계", 4, 4); //1-6
        setTrans("회기", 3, 3, 2, 2, 1, 1); //1-경춘, 1-경의중앙, 경춘-경의중앙
        setTrans("청량리", 5, 5, 5, 5, 3, 2); //1-경춘, 1-경의중앙, 경춘-경의중앙
        setTrans("신설동", 4, 4, 3, 1, 1, 1); //1-2, 1-우이, 2-우이
        setTrans("동묘앞", 3, 3); //1-6
        setTrans("동대문", 5, 5); //1-4
        setTrans("종로3가", 3, 2, 7, 6, 3, 3); //1-3, 1-5, 3-5
        setTrans("시청", 3, 2); //1-2
        setTrans("서울", 3, 3, 11, 11, 6, 6, //1-4, 1-인천국제공항철도, 1-경의중앙,
                9, 10, 7, 7, 10, 10); //4-인천국제공항철도, 4-경의중앙, 인천국제공항철도-경의중앙
        setTrans("용산", 3, 2); //1-경의중앙
        setTrans("노량진", 3, 3); //1-9
        setTrans("신길", 5, 5); //1-5
        setTrans("신도림", 2, 2); //1-2
        setTrans("온수", 3, 3); //1-7
        setTrans("부평", 4, 4); //1-인천1
        setTrans("주안", 4, 4); //1-인천2
        setTrans("인천", 4, 4); //1-수인
        setTrans("가산디지털단지", 4, 3); //1-7
        setTrans("금정", 2, 2); //1-4
        setTrans("수원", 5, 4); //1-분당

        // 2호선
        setTrans("을지로3가", 3, 3); //2-3
        setTrans("을지로4가", 3, 2); //2-5
        setTrans("동대문역사문화공원", 1, 1, 5, 6, 3, 3); //2-4, 2-5, 4-5
        setTrans("신당", 4, 5); //2-6
        setTrans("왕십리", 3, 3, 2, 3, 2, 3, //2-5, 2-분당, 2-경의중앙,
                6, 5, 6, 5, 2, 2); //5-분당, 5-경의중앙, 분당-경의중앙
        setTrans("건대입구", 5, 3); //2-7
        setTrans("잠실", 4, 3); //2-8
        setTrans("종합운동장", 5, 6); //2-9
        setTrans("선릉", 2, 2); //2-분당
        setTrans("강남", 5, 4); //2-신분당
        setTrans("교대", 3, 2); //2-3
        setTrans("사당", 3, 2); //2-4
        setTrans("대림", 5, 5); //2-7
        setTrans("까치산", 2, 1); //2-5
        setTrans("영등포구청", 4, 3); //2-5
        setTrans("당산", 3, 4); //2-9
        setTrans("합정", 2, 2); //2-6
        setTrans("홍대입구", 7, 7, 6, 7, 3, 4); //2-인천국제공항철도, 2-경의중앙, 인천국제공항철도-경의중앙
        setTrans("충정로", 5, 5); //2-5

        // 3호선
        setTrans("대곡", 4, 4); //3-경의중앙
        setTrans("연신내", 5, 4); //3-6
        setTrans("불광", 3, 2); //3-6
        setTrans("충무로", 2, 1); //3-4
        setTrans("약수", 3, 3); //3-6
        setTrans("옥수", 4, 4); //3-경의중앙
        setTrans("고속터미널", 4, 3, 4, 4, 9, 9); //3-7, 3-9, 7-9
        setTrans("양재", 3, 3); //3-신분당
        setTrans("도곡", 2, 1); //3-분당
        setTrans("수서", 2, 3); //3-분당
        setTrans("가락시장", 2, 2); //3-8
        setTrans("오금", 2, 2); //3-5

        // 4호선
        setTrans("노원", 8, 8); //4-7
        setTrans("성신여대입구", 3, 3); //4-우이
        setTrans("삼각지", 3, 3); //4-6
        setTrans("이촌", 2, 3); //4-경의중앙
        setTrans("동작", 6, 7); //4-9
        setTrans("총신대입구(이수)", 4, 5); //4-7
        setTrans("오이도", 2, 2); //4-수인

        // 5호선
        setTrans("김포공항", 3, 4, 4, 3, 0, 0); //5-9, 5-인천국제공항철도, 9-인천국제공항철도(맞은편 환승)
        setTrans("공덕", 2, 2, 6, 5, 4, 4, //5-6, 5-인천국제공항철도, 5-경의중앙,
                6, 5, 4, 4, 3, 3); //6-인천국제공항철도, 6-경의중앙, 인천국제공항철도-경의중앙
        setTrans("청구", 2, 2); //5-6
        setTrans("군자", 3, 3); //5-7
        setTrans("천호", 1, 1); //5-8

        // 6호선
        setTrans("디지털미디어시티", 6, 6, 2, 3, 9, 9); //6-인천국제공항철도, 6-경의중앙, 인천국제공항철도-경의중앙
        setTrans("효창공원앞", 4, 3); //6-경의중앙
        setTrans("보문", 3, 3); //6-우이
        setTrans("태릉입구", 2, 2); //6-7

        // 7호선
        setTrans("상봉", 5, 5, 4, 4, 3, 2); //7-경춘, 7-경의중앙, 경춘-경의중앙
        setTrans("부평구청", 1, 3); //7-인천1

        // 8호선
        setTrans("복정", 1, 1); //8-수인
        setTrans("모란", 3, 3); //8-분당

        // 9호선
        setTrans("선정릉", 2, 3); //9-분당

        // 인천국제공항철도
        setTrans("계양", 3, 2); //인천국제공항철도-인천1
        setTrans("검암", 7, 7); //인천국제공항철도-인천2
        setTrans("인천국제공항", 4, 4); //인천국제공항철도-자기부상철도

        // 분당선
        setTrans("이매", 3, 3); //분당-경강
        setTrans("정자", 3, 3); //분당-신분당
        setTrans("기흥", 5, 5); //분당-에버라인

        // 에버라인

        // 경춘선
        setTrans("망우", 3, 3); //경춘-경의중앙
        setTrans("중랑", 1, 1); //경춘-경의중앙

        // 인천1호선
        setTrans("인천시청", 4, 4); //인천1-인천2
        setTrans("원인재", 5, 4); //인천1-수인

        // 인천2호선

        // 경의중앙선

        // 경강선
        setTrans("판교", 2, 2); //경강-신분당

        // 신분당선

        // 수인선

        // 의정부경전철

        // 우이신설선

    }

}
