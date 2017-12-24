package blacksmith.sullivanway.display;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import blacksmith.sullivanway.R;
import blacksmith.sullivanway.database.FavoritePath;
import blacksmith.sullivanway.database.MyDBOpenHelper;

public class FavoriteActivity extends AppCompatActivity {
    private MyDBOpenHelper myDBOpenHelper = new MyDBOpenHelper(this);
    private ArrayList<FvItem> fvItems = new ArrayList<>();

    private ListView fvListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        fvListView = (ListView) findViewById(R.id.fvListView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        initListItems();
    }

    private void initListItems() {
        fvItems.clear();
        String sql = String.format("SELECT * FROM %s ORDER BY type, _id DESC", FavoritePath.TB_NAME);
        Cursor cursor = myDBOpenHelper.getReadableDatabase().rawQuery(sql, null);
        int count = cursor.getCount();
        while (cursor.moveToNext()) { //_id(0), type(1), startLineNm(2), endLineNm, startStnNm, endStnNm
            int type = cursor.getInt(1);
            String startLineNm = cursor.getString(2);
            String endLineNm = cursor.getString(3);
            String startStnNm = cursor.getString(4);
            String endStnNm = cursor.getString(5);
            fvItems.add(new FvItem(type, startLineNm, endLineNm, startStnNm, endStnNm));
        }
        cursor.close();

        if (count != 0) {
            FvAdapter adapter = new FvAdapter(this, R.layout.item_favorite, fvItems);
            fvListView.setAdapter(adapter);
        } else {
            TextView tmp = new TextView(this);
            tmp.setText(R.string.no_fv_item);
            tmp.setTextSize(18);
            tmp.setPadding(0, 15, 0, 0);
            tmp.setGravity(Gravity.CENTER);
            addContentView(tmp, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            fvListView.setAdapter(null);
        }
    }

    private class FvItem {
        int type;
        String startLineNm, endLineNm;
        String startStnNm, endStnNm;

        FvItem(int type, String startLineNm, String endLineNm, String startStnNm, String endStnNm) {
            this.type = type;
            this.startLineNm = startLineNm;
            this.endLineNm = endLineNm;
            this.startStnNm = startStnNm;
            this.endStnNm = endStnNm;
        }
    }

    private class FvAdapter extends BaseAdapter {
        private Context context;
        private int itemResId;
        private ArrayList<FvItem> fvItems;

        FvAdapter(Context context, int itemResId, ArrayList<FvItem> fvItems) {
            this.context = context;
            this.itemResId = itemResId;
            this.fvItems = fvItems;
        }

        @Override
        public int getCount() {
            return fvItems.size();
        }

        @Override
        public Object getItem(int position) {
            return fvItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final Holder holder;
            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(itemResId, parent, false);

                holder = new Holder();
                holder.favorite = (ImageView) convertView.findViewById(R.id.favoriteImg);
                holder.stnNms = (TextView) convertView.findViewById(R.id.stnNms);
                holder.delete = (ImageView) convertView.findViewById(R.id.deleteImg);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }

            // data
            final String startLineNm = fvItems.get(position).startLineNm;
            final String endLineNm = fvItems.get(position).endLineNm;
            final String startStnNm = fvItems.get(position).startStnNm;
            final String endStnNm = fvItems.get(position).endStnNm;

            // favorite image
            int resId = (fvItems.get(position).type == 0) ?
                    R.drawable.ic_star_black_24dp : R.drawable.ic_star_border_black_24dp;
            holder.favorite.setImageResource(resId);
            holder.favorite.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (FavoritePath.insert(true, startLineNm, endLineNm, startStnNm, endStnNm)) //history를 favorite로
                        holder.favorite.setImageResource(R.drawable.ic_star_black_24dp);
                    else //favorite를 history로
                        holder.favorite.setImageResource(R.drawable.ic_star_border_black_24dp);
                    initListItems();
                }
            });

            // stnNm text
            String stnNms = startStnNm + " → " + endStnNm;
            holder.stnNms.setText(stnNms);
            holder.stnNms.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = getIntent();
                    intent.putExtra("startLineNm", startLineNm);
                    intent.putExtra("endLineNm", endLineNm);
                    intent.putExtra("startStnNm", startStnNm);
                    intent.putExtra("endStnNm", endStnNm);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            });

            // delete image
            holder.delete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FavoritePath.delete(startLineNm, endLineNm, startStnNm, endStnNm);
                    initListItems();
                }
            });

            return convertView;
        }

        private class Holder {
            ImageView favorite;
            TextView stnNms;
            ImageView delete;
        }
    }
}
