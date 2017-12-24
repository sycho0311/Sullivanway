package blacksmith.sullivanway.path;

public class PathFinder {
    private int[][] matrix; //시간 가중치인접행렬
    private int[][] transMatrix; //환승횟수 가중치인접행렬
    private int n; //행렬 한 행의 크기
    private PathInfo pathInfo; //경로정보 결과

    private int[] distance;
    private int[] prev;

    public PathFinder(int type, StationMatrix sm) { //type으로 최소환승, 최단경로를 구분

        matrix = sm.getMatrix();
        transMatrix = sm.getTransMatrix();
        n = sm.getN(); //행렬크기

        //int start = sm.getIndex(startLineNm, startStnNm);
        //int end = sm.getIndex(endLineNm, endStnNm);
        int vstart = n - 2;
        int vend = n - 1;

        if (type == 0)
            lessTransDijkstra(vstart);
        else
            dijkstra(vstart);
        pathInfo = new PathInfo(sm.getStnIdx(), distance, prev, vstart, vend);
    }

    public PathInfo getPathInfo() {
        return pathInfo;
    }

    private int lessTransChoose(int[] transDistance, boolean[] found) {
        int minDist = StationMatrix.INF;
        int minTrans = StationMatrix.INF;
        int choose = -1;

        for (int i = 0; i < transDistance.length; i++) {
            if (!found[i]) {
                if (transDistance[i] < minTrans) {
                    minDist = distance[i];
                    minTrans = transDistance[i];
                    choose = i;
                } else if (transDistance[i] == minTrans) {
                    if (distance[i] < minDist) {
                        minDist = distance[i];
                        choose = i;
                    }
                }
            }
        }
        return choose;
    }

    private int choose(int[] transDistance, boolean[] found) {
        int minDist = StationMatrix.INF;
        int minTrans = StationMatrix.INF;
        int choose = -1;

        for (int i = 0; i < distance.length; i++) {
            if (!found[i]) {
                if (distance[i] < minDist) {
                    minDist = distance[i];
                    minTrans = transDistance[i];
                    choose = i;
                } else if (distance[i] == minDist) {
                    if (transDistance[i] < minTrans) {
                        minDist = distance[i];
                        minTrans = transDistance[i];
                        choose = i;
                    }
                }
            }
        }
        return choose;
    }

    private void  lessTransDijkstra(int start) {
        distance = matrix[start].clone();
        int[] transDistance = transMatrix[start].clone();
        prev = new int[n];
        boolean[] found = new boolean[n];
        for (int i = 0; i < n; i++) {
            prev[i] = -1;
            found[i] = false;
        }
        found[start] = true;

        int loop = n - 1;
        while (loop-- != 0) {
            int choose = lessTransChoose(transDistance, found);
            found[choose] = true;
            for (int i = 0; i < n; i++) {
                if (!found[i]) {
                    boolean crit1 = transDistance[choose] + transMatrix[choose][i] < transDistance[i]; //choose 경유시 환승횟수 감소
                    boolean crit2 = transDistance[choose] + transMatrix[choose][i] == transDistance[i];
                    boolean crit3 = distance[choose] + matrix[choose][i] < distance[i];
                    if (crit1) {
                        distance[i] = distance[choose] + matrix[choose][i];
                        transDistance[i] = transDistance[choose] + transMatrix[choose][i];
                        prev[i] = choose;
                    } else if (crit2) {
                        if (crit3) {
                            distance[i] = distance[choose] + matrix[choose][i];
                            transDistance[i] = transDistance[choose] + transMatrix[choose][i];
                            prev[i] = choose;
                        }
                    }
                }
            }
        }
    }

    private void dijkstra(int start) {
        distance = matrix[start].clone();
        int[] transDistance = transMatrix[start].clone();
        prev = new int[n];
        boolean[] found = new boolean[n];
        for (int i = 0; i < n; i++) {
            prev[i] = -1;
            found[i] = false;
        }
        found[start] = true;

        int loop = n - 1;
        while (loop-- != 0) {
            int choose = choose(transDistance, found);
            found[choose] = true;
            for (int i = 0; i < n; i++) {
                if (!found[i]) {
                    boolean crit1 = distance[choose] + matrix[choose][i] < distance[i];
                    boolean crit2 = distance[choose] + matrix[choose][i] == distance[i];
                    boolean crit3 = transDistance[choose] + transMatrix[choose][i] < transDistance[i]; //choose 경유시 환승횟수 감소
                    if (crit1) {
                        distance[i] = distance[choose] + matrix[choose][i];
                        transDistance[i] = transDistance[choose] + transMatrix[choose][i];
                        prev[i] = choose;
                    } else if (crit2) {
                        if (crit3) {
                            distance[i] = distance[choose] + matrix[choose][i];
                            transDistance[i] = transDistance[choose] + transMatrix[choose][i];
                            prev[i] = choose;
                        }
                    }
                }
            }
        }
    }

}