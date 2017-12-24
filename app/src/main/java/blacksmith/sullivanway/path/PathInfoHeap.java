package blacksmith.sullivanway.path;

public class PathInfoHeap {
    private PathInfo[] tree;
    private int size = 0;

    public PathInfoHeap(int maxSize) {
        tree = new PathInfo[maxSize + 1];
    }

    public void insertWithMinDist(PathInfo mPath) {
        int i = ++size;

        if (i < 2) {
            tree[1] = mPath;
        } else {
            boolean criteria1 = mPath.getTime() < tree[i / 2].getTime();
            boolean criteria2 = mPath.getTime() == tree[i / 2].getTime() && mPath.getTransCnt() < tree[i / 2].getTransCnt();
            while ((i > 1) && (criteria1 || criteria2)) {
                tree[i] = tree[i / 2];
                i /= 2;
            }

            tree[i] = mPath;
        }
    }

    public void insertWithLessTrans(PathInfo mPath) {
        int i = ++size;

        if (i < 2) {
            tree[1] = mPath;
        } else {
            boolean criteria1 = mPath.getTransCnt() < tree[i / 2].getTransCnt();
            boolean criteria2 = mPath.getTransCnt() == tree[i / 2].getTransCnt() && mPath.getTime() < tree[i / 2].getTime();
            while ((i > 1) && (criteria1 || criteria2)) {
                tree[i] = tree[i / 2];
                i /= 2;
            }

            tree[i] = mPath;
        }
    }

    public PathInfo deleteWithMinDist() {
        PathInfo item = tree[1], temp = tree[size--];
        int parent = 1, child = 2;

        while (child <= size) {
            boolean criteria1 = tree[child].getTime() > tree[child + 1].getTime();
            boolean criteria2 = tree[child].getTime() == tree[child + 1].getTime() && tree[child].getTransCnt() > tree[child + 1].getTransCnt();
            if ((child < size) && (criteria1 || criteria2))
                child++; //R-child 선택

            criteria1 = temp.getTime() < tree[child].getTime();
            criteria2 = temp.getTime() == tree[child].getTime() && temp.getTransCnt() < tree[child].getTransCnt();
            if (criteria1 || criteria2)
                break;

            tree[parent] = tree[child];
            parent = child;
            child *= 2;
        }

        tree[parent] = temp;
        return item;
    }

    public PathInfo deleteWithLessTrans() {
        PathInfo item = tree[1], temp = tree[size--];
        int parent = 1, child = 2;

        while (child <= size) {
            boolean criteria1 = tree[child].getTransCnt() > tree[child + 1].getTransCnt();
            boolean criteria2 = tree[child].getTransCnt() == tree[child + 1].getTransCnt() && tree[child].getTime() > tree[child + 1].getTime();
            if ((child < size) && (criteria1 || criteria2))
                child++; //R-child 선택

            criteria1 = temp.getTransCnt() < tree[child].getTransCnt();
            criteria2 = temp.getTransCnt() == tree[child].getTransCnt() && temp.getTime() < tree[child].getTime();
            if (criteria1 || criteria2)
                break;

            tree[parent] = tree[child];
            parent = child;
            child *= 2;
        }

        tree[parent] = temp;
        return item;
    }

    public int getSize() {
        return size;
    }
}
