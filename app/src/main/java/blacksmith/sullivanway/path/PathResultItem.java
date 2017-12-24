package blacksmith.sullivanway.path;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Calendar;

import blacksmith.sullivanway.data.LineInfo;
import blacksmith.sullivanway.database.StnInfo;

/* 1호선 창동 - 4호선 창동 환승
 *  i    sL    sS       j   eL    eS
 *  0   1호선 소요산    1  1호선 동두천
 *                  ....
 *  0   1호선 소요산   16  1호선 창동
 *  0   1호선 소요산   17  4호선 창동
 *  17  4호선 창동     18  4호선 쌍문
 *                  ....                  **/
public class PathResultItem implements Parcelable {
    public int type;

    // item_path_line
    public String lineSymbol;
    public String startStnNm;
    public String nextStnNm;
    public String direction;
    public int numOfStn, time;
    public String endStnNm, door;

    // item_walk_line
    public String transStnNm;
    public String transStartLineNm, transStartNextStnNm;
    public String transEndLineNm, transEndNextStnNm;
    public int walkTime;

    private PathResultItem(String lineSymbol, String startStnNm, String nextStnNm, String direction,
                           int numOfStn, int time, String endStnNm, String door) {
        type = 0;
        this.lineSymbol = lineSymbol;
        this.startStnNm = startStnNm;
        this.nextStnNm = nextStnNm;
        this.direction = direction;
        this.numOfStn = numOfStn;
        this.time = time;
        this.endStnNm = endStnNm;
        this.door = door;
    }

    private PathResultItem(String transStnNm, String transStartLineNm, String transStartNextStnNm,
                           String transEndLineNm, String transEndNextStnNm, int walkTime) {
        type = 1;
        this.transStnNm = transStnNm;
        this.transStartLineNm = transStartLineNm;
        this.transStartNextStnNm = transStartNextStnNm;
        this.transEndLineNm = transEndLineNm;
        this.transEndNextStnNm = transEndNextStnNm;
        this.walkTime = walkTime;
    }

    public static ArrayList<PathResultItem> createArrayListInstance(Context context, ArrayList<StnInfo> stnIdx, Calendar calendar, PathInfo pathInfo) {
        ArrayList<PathResultItem> items = new ArrayList<>();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        int sec = calendar.get(Calendar.SECOND);
// TODO 시간표 동기화.
        int numOfStns = 0;
        int time = 0;
        int transCnt = 0;
        StnInfo startStn = null, nextStn = null, endStn = null;
        String direction = null;
        ArrayList<Integer> path = pathInfo.getPath();
        ArrayList<Integer> times = pathInfo.getTimes();
        ArrayList<Integer> transStns = pathInfo.getTransStns();

        for (int i = 0, j = 1; j < path.size(); j++) {
            if (i == j - 1) { //출발역 (target: 출발역의 다음역)
                int start = path.get(i);
                int next = path.get(j); //i+1
                startStn = stnIdx.get(start);
                nextStn = stnIdx.get(next);
                direction = getDirection(startStn, nextStn);
                numOfStns = 0;
                time = 0;
            }

            int target = path.get(j);
            numOfStns++;
            time += times.get(j) - times.get(j - 1);

            assert startStn != null;
            if (transCnt < transStns.size() && target == transStns.get(transCnt)) { //환승역
                transCnt++;

                String lineSymbol = LineInfo.getLineSymbol(startStn.getLineNm());
                endStn = stnIdx.get(target);
                String door = endStn.getDoor();
                PathResultItem pathLineItem = new PathResultItem(lineSymbol, startStn.getStnNm(), nextStn.getStnNm(), direction, numOfStns, time, endStn.getStnNm(), door);
                items.add(pathLineItem);

                i = j + 1; //1->4호선이면 i=4호선역, j=1호선역

                StnInfo targetStn = stnIdx.get(target);
                int nextTarget = path.get(j + 1);
                StnInfo nextTargetStn = stnIdx.get(nextTarget);
                int walkTime = times.get(j + 1) - times.get(j);
                String transStartNextStnNm = StnInfo.getNextStnNm(targetStn.getLineNm(), targetStn.getStnNm(), StnInfo.getToward(stnIdx.get(path.get(j - 1)), targetStn));
                String transEndNextStnNm = StnInfo.getNextStnNm(nextTargetStn.getLineNm(), nextTargetStn.getStnNm(), StnInfo.getToward(nextTargetStn, stnIdx.get(path.get(j + 2))));
                PathResultItem walkLineItem = new PathResultItem(targetStn.getStnNm(), targetStn.getLineNm(), transStartNextStnNm, nextTargetStn.getLineNm(), transEndNextStnNm, walkTime);
                items.add(walkLineItem);
                j++;//i=j, 다음 루프에서 j는 i 다음역을 의미한다
            } else if (j == path.size() - 1) { //마지막 역
                String lineSymbol = LineInfo.getLineSymbol(startStn.getLineNm());
                endStn = stnIdx.get(target);
                String door = endStn.getDoor();
                PathResultItem pathLineItem = new PathResultItem(lineSymbol, startStn.getStnNm(), nextStn.getStnNm(), direction, numOfStns, time, endStn.getStnNm(), door);
                items.add(pathLineItem);
            }

        }

        return items;
    }

    private static String getDirection(StnInfo mStn, StnInfo nextStn) {
        return "XX";
    }


    /* Parcelable */
    private PathResultItem(Parcel in) {
        type = in.readInt();
        lineSymbol = in.readString();
        startStnNm = in.readString();
        nextStnNm = in.readString();
        direction = in.readString();
        numOfStn = in.readInt();
        time = in.readInt();
        endStnNm = in.readString();
        door = in.readString();
        transStnNm = in.readString();
        transStartLineNm = in.readString();
        transStartNextStnNm = in.readString();
        transEndLineNm = in.readString();
        transEndNextStnNm = in.readString();
        walkTime = in.readInt();
    }

    public static final Creator<PathResultItem> CREATOR = new Creator<PathResultItem>() {
        @Override
        public PathResultItem createFromParcel(Parcel in) {
            return new PathResultItem(in);
        }

        @Override
        public PathResultItem[] newArray(int size) {
            return new PathResultItem[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        dest.writeString(lineSymbol);
        dest.writeString(startStnNm);
        dest.writeString(nextStnNm);
        dest.writeString(direction);
        dest.writeInt(numOfStn);
        dest.writeInt(time);
        dest.writeString(endStnNm);
        dest.writeString(door);
        dest.writeString(transStnNm);
        dest.writeString(transStartLineNm);
        dest.writeString(transStartNextStnNm);
        dest.writeString(transEndLineNm);
        dest.writeString(transEndNextStnNm);
        dest.writeInt(walkTime);
    }

}