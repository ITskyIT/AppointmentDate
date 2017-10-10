package com.tian.appointmentdate.date;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.RelativeLayout;

import com.tian.appointmentdate.MainActivity;
import com.tian.appointmentdate.R;
import com.tian.appointmentdate.adapter.CalendarAdapter;

import java.io.Serializable;
import java.util.List;

/**
 *
 */

public class DayPickerView extends RelativeLayout {
    private RecyclerView recyclerView;
    private DataModel dataModel;
    private DatePickerController mController;
    private CalendarAdapter mAdapter;
    private int positionIndex;
    public DayPickerView(Context context,int position) {
        super(context);
        this.positionIndex = position;
        initView(context);
    }
    private void initView(Context context) {
        View.inflate(context, R.layout.item_calendar_layout,DayPickerView.this);
        recyclerView = (RecyclerView) this.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
    }


    /**
     * 设置参数
     *
     * @param dataModel   数据
     * @param mController 回调监听
     */
    public void setParameter(DataModel dataModel, DatePickerController mController) {
        if(dataModel == null) {
            return;
        }
        this.dataModel = dataModel;
        this.mController = mController;
        setUpAdapter();
    }
    protected void setUpAdapter() {
        if (mAdapter == null) {
            mAdapter = new CalendarAdapter(getContext(),mController, dataModel,positionIndex);
            recyclerView.setAdapter(mAdapter);
        }
        MainActivity.adapters.put(positionIndex+"",mAdapter);
        mAdapter.notifyDataSetChanged();
    }
    public static class DataModel implements Serializable {
        public int yearStart;                                      // 日历开始的年份
        public int monthStart;                                     // 日历开始的月份
        public int showMonthCount;                                     // 要显示几个月
        public List<CalendarAdapter.CalendarDay> invalidDays;   // 无效的日期
        public List<CalendarAdapter.CalendarDay> busyDays;      // 被占用的日期
        public CalendarAdapter.SelectedDays<CalendarAdapter.CalendarDay> selectedDays;  // 默认选择的日期
        public int leastDaysNum;                                   // 至少选择几天
        public int mostDaysNum;                                    // 最多选择几天
        public List<CalendarAdapter.CalendarDay> tags;          // 日期下面对应的标签
        public String defTag;                                      // 默认显示的标签
    }
}
