package com.lvstudio.videotrimslider;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * @author lvstudio
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initBgView();
        initSlider();
    }

    private void initSlider() {
        RangeSlider slider = (RangeSlider) findViewById(R.id.range_slider);
        slider.setRangeChangeListener(new RangeSlider.OnRangeChangeListener() {
            @Override
            public void onRangeChange(RangeSlider view, int startPercentage, int contentPercentage, int totalCount) {
                // TODO something...
            }
        });
    }

    private void initBgView() {
        final int itemCount = 5;
        int padding = getResources().getDimensionPixelOffset(R.dimen.activity_horizontal_margin);
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int thumbWidth = getResources().getDimensionPixelOffset(R.dimen.range_thumb_width);
        final int itemWidth = (screenWidth - (2 * (padding + thumbWidth))) / itemCount;

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.range_rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recyclerView.setAdapter(new RecyclerView.Adapter() {
            @Override
            public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                ImageView view = new ImageView(parent.getContext());
                view.setLayoutParams(new ViewGroup.LayoutParams(itemWidth, ViewGroup.LayoutParams.MATCH_PARENT));
                return new ViewHolder(view);
            }

            @Override
            public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
                ((ViewHolder) holder).setImage();
            }

            @Override
            public int getItemCount() {
                return itemCount;
            }
        });
    }

    private static class ViewHolder extends RecyclerView.ViewHolder {

        private ViewHolder(View itemView) {
            super(itemView);
        }

        private void setImage() {
            ((ImageView) itemView).setScaleType(ImageView.ScaleType.FIT_XY);
            ((ImageView) itemView).setImageResource(R.drawable.image);
        }
    }
}
