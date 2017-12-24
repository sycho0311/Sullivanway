package blacksmith.sullivanway.display;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import blacksmith.sullivanway.R;

public class TransMapViewFragment extends Fragment { //지도맵을 보여줄 fragment
    private int mapRes;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mapRes = getArguments().getInt("map");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_trans_map_view, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (getView() != null) {
            ImageView imageView = (ImageView) getView().findViewById(R.id.transMapImageView);
            imageView.setImageResource(mapRes);//mapRes: transferMapViewActivtiy에서 받아온 리소스ID
        }

    }
}
