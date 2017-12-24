package blacksmith.sullivanway.data;

import android.content.Context;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.StringTokenizer;

import blacksmith.sullivanway.database.StnInfo;
import blacksmith.sullivanway.path.StationMatrix;

public class TimeTableInfo {
    /* 데이터 예시
     * i | 도착시간 | 출발시간 | 시발역 | 종착역 | 요일 | 방향 | 급행
     * 0 | 00:00:00 | 00:00:00 | X X 역 | Y Y 역 |   1  |   2  |  N
     * 1 | 00:00:00 | 00:00:00 | Z Z 역 | W W 역 |   2  |   1  |  Y
     * 2 | 00:00:00 | 00:00:00 | X X 역 | W W 역 |   2  |   1  |  Y
     *                          ....
     * n | 00:00:00 | 00:00:00 | X X 역 | W W 역 |   3  |   2  |  Y   */
    //private int arrivalHour, arrivalMin, arrivalSec; //현재 역에 도착하는 시간
    private int leftHour, leftMin, leftSec; //현재 역에서 출발하는 시간
    private String startStnNm;   //시발역
    private String endStnNm;     //종착역
    private String weekTag;      //운행요일 (1: 평일, 2: 토요일, 3: 일/휴일)
    private String inoutTag;     //운행방향 (1: 상행/내선, 2: 하행/외선)
    private boolean isExpress;    //급행여부 (Y: 급행, N: 급행아님)

    private TimeTableInfo(int leftHour, int leftMin, int leftSec, String startStnNm, String endStnNm, String weekTag, String inoutTag, boolean isExpress) {
        this.leftHour = leftHour;        this.leftMin = leftMin;        this.leftSec = leftSec;
        this.startStnNm = startStnNm;        this.endStnNm = endStnNm;
        this.weekTag = weekTag;        this.inoutTag = inoutTag;
        this.isExpress = isExpress;
    }

    public static ArrayList<TimeTableInfo> createArrayListInstance(Context context, String lineNm, String stnNm, int weekTag) throws IOException {
        ArrayList<TimeTableInfo> ttInfos = new ArrayList<>();
        String fileNm = lineNm + " " + stnNm + ".txt";
        InputStream in = context.getResources().getAssets().open(fileNm);
        InputStreamReader reader = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(reader);
        String line;
        while ((line = br.readLine()) != null) {
            StringTokenizer cutter = new StringTokenizer(line, ";", false);
            if (cutter.hasMoreTokens()) {
                cutter.nextToken(); //arrivalTime
                String leftTime = cutter.nextToken();
                String startStnNm = cutter.nextToken();
                String endStnNm = cutter.nextToken();
                String mWeekTag = cutter.nextToken();
                String inoutTag = cutter.nextToken();
                boolean isExpress = cutter.hasMoreTokens();
                if (weekTag == Integer.parseInt(mWeekTag)) {
                    int[] tokLeftTime = getTokenizedTime(leftTime);
                    ttInfos.add(new TimeTableInfo(tokLeftTime[0], tokLeftTime[1], tokLeftTime[2], startStnNm, endStnNm, mWeekTag, inoutTag, isExpress));
                }
            }
        }

        br.close();
        reader.close();
        in.close();

        return ttInfos;
    }

    public static TimeTableInfo getNextTrain(Context context, StationMatrix matrix, ArrayList<Integer> path, String lineNm, String stnNm, String mInoutTag, Calendar calendar) throws IOException {
        TimeTableInfo ttInfo = null;

        String mWeekTag = null;
        switch (calendar.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:case Calendar.TUESDAY:case Calendar.WEDNESDAY:
            case Calendar.THURSDAY:case Calendar.FRIDAY:
                mWeekTag = "1";
                break;
            case Calendar.SATURDAY:
                mWeekTag = "2";
                break;
            case Calendar.SUNDAY:
                mWeekTag = "3";
                break;
        }
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int min = calendar.get(Calendar.MINUTE);
        int sec = calendar.get(Calendar.SECOND);

        String fileNm = lineNm + " " + stnNm + ".txt";
        InputStream in = context.getResources().getAssets().open(fileNm);
        InputStreamReader reader = new InputStreamReader(in);
        BufferedReader br = new BufferedReader(reader);
        String line;
        while ((line = br.readLine()) != null) {
            StringTokenizer cutter = new StringTokenizer(line, ";", false);
            if (cutter.hasMoreTokens()) {
                cutter.nextToken(); //arrivalTime
                String leftTime = cutter.nextToken();
                String startStnNm = cutter.nextToken();
                String endStnNm = cutter.nextToken();
                String weekTag = cutter.nextToken();
                String inoutTag = cutter.nextToken();
                boolean isExpress = cutter.hasMoreTokens();

                if (weekTag.equals(mWeekTag) && inoutTag.equals(mInoutTag)) {
                    int[] tokLeftTime = getTokenizedTime(leftTime);
                    int leftHour = tokLeftTime[0];
                    int leftMin = tokLeftTime[1];
                    int leftSec = tokLeftTime[2];
                    if (leftHour * 3600 + leftMin * 60 + leftSec < hour * 3600 + min * 60 + sec && !isExpress) {
                        boolean isExist = false; //path에 endStnNm이 포함되어 있는지
                        for (int i = 0; i < path.size() - 1; i++) {
                            int index = path.get(i);
                            StnInfo stnInfo = matrix.getStnIdx().get(index);
                            String mLineNm = stnInfo.getLineNm();
                            String mStnNm = stnInfo.getStnNm();
                            if (mLineNm.equals(lineNm) && mStnNm.equals(endStnNm)) { //이 방면으로 가는 지하철을 타면 중도에서 환승해야 한다
                                isExist = true;
                                break;
                            }
                        }
                        if (!isExist)
                            ttInfo = new TimeTableInfo(leftHour, leftMin, leftSec, startStnNm, endStnNm, weekTag, inoutTag, isExpress);

                    } else {
                        if (ttInfo == null) {
                            if (hour == 0) {
                                hour += 24;
                            } else {
                                ttInfo = new TimeTableInfo(leftHour, leftMin, leftSec, startStnNm, endStnNm, weekTag, inoutTag, isExpress);
                                break;
                            }
                        }
                    }
                }
            }
        }

        br.close();
        reader.close();
        in.close();

        return ttInfo;
    }

    private static int[] getTokenizedTime(String time) {
        int[] tokenizedTime = new int[3];
        StringTokenizer timeCutter = new StringTokenizer(time, ":", false);
        for (int i = 0; i < tokenizedTime.length; i++)
            tokenizedTime[i] = Integer.parseInt(timeCutter.nextToken());
        return tokenizedTime;
    }

    public int getLeftHour() {
        return leftHour;
    }

    public int getLeftMin() {
        return leftMin;
    }

    public int getLeftSec() {
        return leftSec;
    }

    public String getStartStnNm() {
        return startStnNm;
    }

    public String getEndStnNm() {
        return endStnNm;
    }

    public String getWeekTag() {
        return weekTag;
    }

    public String getInoutTag() {
        return inoutTag;
    }

    public boolean getIsExpress() {
        return isExpress;
    }
}
