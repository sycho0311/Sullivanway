package blacksmith.sullivanway.path;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

import blacksmith.sullivanway.database.StnInfo;

public class PathInfo implements Parcelable {
    private ArrayList<Integer> path = new ArrayList<>();
    private ArrayList<Integer> times = new ArrayList<>();
    private ArrayList<Integer> transStns = new ArrayList<>();
    private ArrayList<Integer> transTimes = new ArrayList<>();
    private int time = 0, transCnt = 0;
    private int cost = 0;

    PathInfo(ArrayList<StnInfo> stnIdx, int[] distance, int[] prev, int start, int end) {
        int target = prev[end];
        while (true) {
            path.add(0, target);
            times.add(0, distance[target]);

            if (prev[target] != -1) {
                if (StnInfo.compare(stnIdx.get(prev[target]), stnIdx.get(target)) == StnInfo.TRANS) { //환승
                    transStns.add(0, prev[target]);
                    transTimes.add(0, distance[target]);
                }
                target = prev[target];
            } else {
                break;
            }
        }

        this.time = times.get(times.size() - 1);
        transCnt = transStns.size();

        for (Integer idx : path) {
            StnInfo stn = stnIdx.get(idx);
            stn.getKm(); //TODO
        }
    }

    public ArrayList<Integer> getPath() {
        return path;
    }

    public ArrayList<Integer> getTimes() { //구간별 시간반환
        return times;
    }

    public ArrayList<Integer> getTransStns() {
        return transStns;
    }

    public int getCost() {
        return cost;
    }

    public int getTime() { //총 소요시간 반환
        return time;
    }

    public int getTransCnt() { //환승횟수
        return transCnt;
    }


    /* Parcelable */
    @SuppressWarnings("unchecked warning")
    private PathInfo(Parcel in) {
        path = (ArrayList<Integer>) in.readSerializable();
        times = (ArrayList<Integer>) in.readSerializable();
        transStns = (ArrayList<Integer>) in.readSerializable();
        transTimes = (ArrayList<Integer>) in.readSerializable();

        time = in.readInt();
        transCnt = in.readInt();
    }

    public static final Creator<PathInfo> CREATOR = new Creator<PathInfo>() {
        @Override
        public PathInfo createFromParcel(Parcel in) {
            return new PathInfo(in);
        }

        @Override
        public PathInfo[] newArray(int size) {
            return new PathInfo[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(path);
        dest.writeSerializable(times);
        dest.writeSerializable(transStns);
        dest.writeSerializable(transTimes);

        dest.writeInt(time);
        dest.writeInt(transCnt);
    }
}
