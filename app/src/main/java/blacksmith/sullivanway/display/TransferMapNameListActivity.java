package blacksmith.sullivanway.display;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import blacksmith.sullivanway.R;
import blacksmith.sullivanway.database.MyDBOpenHelper;
import blacksmith.sullivanway.database.TransMap;

public class TransferMapNameListActivity extends AppCompatActivity {
    private ListView listView;

    private MyAdapter adapter;
    private ArrayList<MyItem> transMaps = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transfer_map_namelist);

        SearchView searchView = (SearchView) findViewById(R.id.transfer_map_search);
        listView = (ListView) findViewById(R.id.transfer_map_list);

        MyDBOpenHelper dbOpenHelper = new MyDBOpenHelper(this);
        String sql = String.format("SELECT DISTINCT stnNm, startLineNm, startNextStnNm, endLineNm, endNextStnNm FROM %s ORDER BY stnNm",
                TransMap.TB_NAME);
        Cursor cursor = dbOpenHelper.getReadableDatabase().rawQuery(sql, null);
        while (cursor.moveToNext()) {
            String stnNm = cursor.getString(0);
            String startLineNm = cursor.getString(1);
            String startNextStnNm = cursor.getString(2);
            String endLineNm = cursor.getString(3);
            String endNextStnNm = cursor.getString(4);
            transMaps.add(new MyItem(stnNm, startLineNm, startNextStnNm, endLineNm, endNextStnNm));
        }
        cursor.close();

        adapter = new MyAdapter(this, transMaps);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new OnListItemClick());

        searchView.setOnQueryTextListener(new OnSearchViewQueryText());
    }

    private class MyItem {
        String stnNm, startLineNm, startNextStnNm, endLineNm, endNextStnNm;

        MyItem(String stnNm, String startLineNm, String startNextStnNm, String endLineNm, String endNextStnNm) {
            this.stnNm = stnNm;
            this.startLineNm = startLineNm;
            this.startNextStnNm = startNextStnNm;
            this.endLineNm = endLineNm;
            this.endNextStnNm = endNextStnNm;
        }
    }

    private class MyAdapter extends BaseAdapter {
        private Context context;
        private int itemResId = R.layout.item_transfer_map_namelist;
        private ArrayList<MyItem> myItems = new ArrayList<>();

        MyAdapter(Context context, ArrayList<MyItem> myItems) {
            this.context = context;
            this.myItems.addAll(myItems);
        }

        void add(MyItem myItem) {
            myItems.add(myItem);
        }

        void addAll(ArrayList<MyItem> transMaps) {
            myItems.addAll(transMaps);
        }

        void clear() {
            myItems.clear();
        }

        @Override
        public int getCount() {
            return myItems.size();
        }

        @Override
        public MyItem getItem(int position) {
            return myItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            Holder holder;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(itemResId, parent, false);

                // Holder에 뷰를 저장하여, 참조할 뷰를 처음 한번만 받아온다
                holder = new Holder();
                holder.stnNm = (TextView) convertView.findViewById(R.id.stnNm);
                holder.startLine = (TextView) convertView.findViewById(R.id.startLine);
                holder.startNextStnNm = (TextView) convertView.findViewById(R.id.startNextStnNm);
                holder.endLine = (TextView) convertView.findViewById(R.id.endLine);
                holder.endNextStnNm = (TextView) convertView.findViewById(R.id.endNextStnNm);
                convertView.setTag(holder); //현재 convertView에 holder 저장
            } else {
                holder = (Holder) convertView.getTag(); //재사용되는 convertView라면 저장해둔 holder를 가져온다
            }

            holder.stnNm.setText(filterString(myItems.get(position).stnNm));
            holder.startLine.setText(myItems.get(position).startLineNm);
            holder.startNextStnNm.setText(myItems.get(position).startNextStnNm);
            holder.endLine.setText(myItems.get(position).endLineNm);
            holder.endNextStnNm.setText(myItems.get(position).endNextStnNm);

            return convertView;
        }

        String filterString(String string) {
            String newString = string;
            int length = newString.length();
            if (length > 5) { //hamburger (9)
                String a = newString.substring(0, 5); //hambu
                String b = newString.substring(5, length); //rger
                newString = a + "\n" + b;
            }
            return newString;
        }

        private class Holder {
            TextView stnNm, startLine, startNextStnNm, endLine, endNextStnNm;
        }

    }

    private class OnListItemClick implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            MyItem myItem = adapter.getItem(position);
            Intent intent = new Intent(TransferMapNameListActivity.this, TransferMapViewActivity.class);
            intent.putExtra("stnNm", myItem.stnNm);
            intent.putExtra("startLineNm", myItem.startLineNm);
            intent.putExtra("startNextStnNm", myItem.startNextStnNm);
            intent.putExtra("endLineNm", myItem.endLineNm);
            intent.putExtra("endNextStnNm", myItem.endNextStnNm);
            startActivity(intent);
        }

    }

    private class OnSearchViewQueryText implements SearchView.OnQueryTextListener {

        @Override
        public boolean onQueryTextSubmit(String query) {
            if (adapter.getCount() == 0)
                return false;
            AdapterView.OnItemClickListener listener = listView.getOnItemClickListener();
            if (listener != null)
                listener.onItemClick(null, null, 0, 0);
            return true;
        }

        @Override
        public boolean onQueryTextChange(String mText) {
            adapter.clear();
            if (mText.equals("")) {
                adapter.addAll(transMaps);
            } else {
                for (MyItem myItem : transMaps)
                    if (myItem.stnNm.startsWith(mText))
                        adapter.add(myItem);
            }
            listView.setAdapter(adapter);
            return true;
        }

    }

}
