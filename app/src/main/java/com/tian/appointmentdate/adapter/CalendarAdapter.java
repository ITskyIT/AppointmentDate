package com.tian.appointmentdate.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.tian.appointmentdate.MainActivity;
import com.tian.appointmentdate.date.CalendarMonthView;
import com.tian.appointmentdate.date.DatePickerController;
import com.tian.appointmentdate.date.DayPickerView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by tian on 2017/7/7.
 */

public class CalendarAdapter  extends RecyclerView.Adapter<CalendarAdapter.CalendarHolder> implements CalendarMonthView.OnDayClickListener {
    private Context mContext;
    protected static final int MONTHS_IN_YEAR = 12;
    private final DatePickerController mController;             // 回调
    private Calendar calendar;
    private SelectedDays<CalendarDay> rangeDays;                // 选择日期范围
    private List<CalendarDay> mBusyDays;                        // 被占用的日期
    private List<CalendarDay> mTags;                            // 日期下面的标签
    private String mDefTag;                                     // 默认标签
    private int mLeastDaysNum;                                  // 至少选择几天
    private int mMostDaysNum;                                   // 至多选择几天
    private List<CalendarDay> mInvalidDays;                     // 无效的日期
    private CalendarDay mNearestDay;                            // 比离入住日期大且是最近的已被占用或者无效日期
    private DayPickerView.DataModel dataModel;
    private int positionIndex;
    public CalendarAdapter(Context context, DatePickerController datePickerController, DayPickerView.DataModel dataModel, int positionIndex) {
        mContext = context;
        mController = datePickerController;
        this.dataModel = dataModel;
        this.positionIndex = positionIndex;
        initData();
    }
    private void initData() {
        calendar = Calendar.getInstance();

        if (dataModel.invalidDays == null) {
            dataModel.invalidDays = new ArrayList<>();
        }

        if (dataModel.busyDays == null) {
            dataModel.busyDays = new ArrayList<>();
        }

        if (dataModel.tags == null) {
            dataModel.tags = new ArrayList<>();
        }

        if (dataModel.selectedDays == null) {
            dataModel.selectedDays = new SelectedDays<>();
        }

        if (dataModel.yearStart <= 0) {
            dataModel.yearStart = calendar.get(Calendar.YEAR);
        }
        if (dataModel.monthStart <= 0) {
            dataModel.monthStart = calendar.get(Calendar.MONTH);
        }

        if (dataModel.leastDaysNum <= 0) {
            dataModel.leastDaysNum = 0;
        }

        if (dataModel.mostDaysNum <= 0) {
            dataModel.mostDaysNum = 100;
        }

        if (dataModel.leastDaysNum > dataModel.mostDaysNum) {
            Log.e("error", "可选择的最小天数不能小于最大天数");
            throw new IllegalArgumentException("可选择的最小天数不能小于最大天数");
        }

        if(dataModel.showMonthCount <= 0) {
            dataModel.showMonthCount = 12;
        }
        if(dataModel.defTag == null) {
            dataModel.defTag = "标签";
        }
        mLeastDaysNum = dataModel.leastDaysNum;
        mMostDaysNum = dataModel.mostDaysNum;
        mBusyDays = dataModel.busyDays;
        mInvalidDays = dataModel.invalidDays;
        rangeDays = dataModel.selectedDays;
        mTags = dataModel.tags;
        mDefTag = dataModel.defTag;
    }

    @Override
    public CalendarHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final CalendarMonthView simpleMonthView = new CalendarMonthView(mContext, dataModel);
        return new CalendarHolder(simpleMonthView, this);
    }

    @Override
    public void onBindViewHolder(CalendarHolder viewHolder, int position) {
        final CalendarMonthView v = viewHolder.simpleMonthView;
        final HashMap<String, Object> drawingParams = new HashMap<String, Object>();
        int month;          // 月份
        int year;           // 年份

        int monthStart = dataModel.monthStart;
        int yearStart = dataModel.yearStart;

        month = (monthStart + (position % MONTHS_IN_YEAR)) % MONTHS_IN_YEAR;
        year = position / MONTHS_IN_YEAR + yearStart + ((monthStart + (position % MONTHS_IN_YEAR)) / MONTHS_IN_YEAR);

//        v.reuse();

        drawingParams.put(CalendarMonthView.VIEW_PARAMS_SELECTED_BEGIN_DATE, rangeDays.getFirst());
        drawingParams.put(CalendarMonthView.VIEW_PARAMS_SELECTED_LAST_DATE, rangeDays.getLast());
        drawingParams.put(CalendarMonthView.VIEW_PARAMS_NEAREST_DATE, mNearestDay);
        drawingParams.put(CalendarMonthView.VIEW_PARAMS_YEAR, year);
        drawingParams.put(CalendarMonthView.VIEW_PARAMS_MONTH, month);
        drawingParams.put(CalendarMonthView.VIEW_PARAMS_WEEK_START, calendar.getFirstDayOfWeek());
        v.setMonthParams(drawingParams);
        v.invalidate();
    }

    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return dataModel.showMonthCount;
    }

    @Override
    public void onDayClick(CalendarMonthView simpleMonthView, CalendarDay calendarDay) {
        if (calendarDay != null) {
            onDayTapped(calendarDay);
        }
    }

    private void onDayTapped(CalendarDay calendarDay) {
        rangeDays.setFirst(calendarDay);
        if (this != MainActivity.adapters.get(""+ MainActivity.position)){
           //点击不同页面中的日期  清除之前的选择
            CalendarAdapter adapter = MainActivity.adapters.get(""+MainActivity.position);
            adapter.rangeDays.setFirst(null);
            adapter.notifyDataSetChanged();
        }
        if(mController != null) {
            mController.onDayOfMonthSelected(calendarDay);
        }
        notifyDataSetChanged();
        MainActivity.position = positionIndex; // 记录当前页面adapter下标
    }

    class CalendarHolder extends RecyclerView.ViewHolder {
        CalendarMonthView simpleMonthView;
        public CalendarHolder(View itemView,CalendarMonthView.OnDayClickListener onDayClickListener) {
            super(itemView);
            simpleMonthView = (CalendarMonthView) itemView;
            simpleMonthView.setLayoutParams(new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            simpleMonthView.setClickable(true);
            simpleMonthView.setOnDayClickListener(onDayClickListener);
        }
    }
    public static class CalendarDay implements Serializable, Comparable<CalendarDay> {
        private static final long serialVersionUID = -5456695978688356202L;
        private Calendar calendar;

        public int day;
        public int month;
        public int year;
        public String tag;

        public CalendarDay(Calendar calendar, String tag) {
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);
            this.tag = tag;
        }

        public CalendarDay() {
            setTime(System.currentTimeMillis());
        }

        public CalendarDay(int year, int month, int day) {
            setDay(year, month, day);
        }
        public CalendarDay(String date) {
            String[] dates=date.split("-");
            setDay(Integer.parseInt(dates[0]), Integer.parseInt(dates[1])-1,Integer.parseInt(dates[2]));
        }
        public CalendarDay(long timeInMillis) {
            setTime(timeInMillis);
        }

        public CalendarDay(Calendar calendar) {
            year = calendar.get(Calendar.YEAR);
            month = calendar.get(Calendar.MONTH);
            day = calendar.get(Calendar.DAY_OF_MONTH);
        }

        private void setTime(long timeInMillis) {
            if (calendar == null) {
                calendar = Calendar.getInstance();
            }
            calendar.setTimeInMillis(timeInMillis);
            month = this.calendar.get(Calendar.MONTH);
            year = this.calendar.get(Calendar.YEAR);
            day = this.calendar.get(Calendar.DAY_OF_MONTH);
        }

        public void set(CalendarDay calendarDay) {
            year = calendarDay.year;
            month = calendarDay.month;
            day = calendarDay.day;
        }

        public void setDay(int year, int month, int day) {
            this.year = year;
            this.month = month;
            this.day = day;
        }

        public Date getDate() {
            if (calendar == null) {
                calendar = Calendar.getInstance();
            }
            calendar.clear();
            calendar.set(year, month, day);
            return calendar.getTime();
        }

        public void setTag(String tag) {
            this.tag = tag;
        }

        @Override
        public String toString() {
            final StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("{ year: ");
            stringBuilder.append(year);
            stringBuilder.append(", month: ");
            stringBuilder.append(month);
            stringBuilder.append(", day: ");
            stringBuilder.append(day);
            stringBuilder.append(" }");

            return stringBuilder.toString();
        }

        /**
         * 只比较年月日
         *
         * @param calendarDay
         * @return
         */
        @Override
        public int compareTo(CalendarDay calendarDay) {
//            return getDate().compareTo(calendarDay.getDate());
            if (calendarDay == null) {
                throw new IllegalArgumentException("被比较的日期不能是null");
            }

            if (year == calendarDay.year && month == calendarDay.month && day == calendarDay.day) {
                return 0;
            }

            if (year < calendarDay.year ||
                    (year == calendarDay.year && month < calendarDay.month) ||
                    (year == calendarDay.year && month == calendarDay.month && day < calendarDay.day)) {
                return -1;
            }
            return 1;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof CalendarDay) {
                CalendarDay calendarDay = (CalendarDay) o;
                if (compareTo(calendarDay) == 0) {
                    return true;
                }
            }
            return false;
        }

        /**
         * 大于比较的日期（只比较年月日）
         *
         * @param o
         * @return
         */
        public boolean after(Object o) {
            if (o instanceof CalendarDay) {
                CalendarDay calendarDay = (CalendarDay) o;
                if (compareTo(calendarDay) == 1) {
                    return true;
                }
            }
            return false;
        }

        /**
         * 小于比较的日期（只比较年月日）
         *
         * @param o
         * @return
         */
        public boolean before(Object o) {
            if (o instanceof CalendarDay) {
                CalendarDay calendarDay = (CalendarDay) o;
                if (compareTo(calendarDay) == -1) {
                    return true;
                }
            }
            return false;
        }
    }

    public static class SelectedDays<K> implements Serializable {
        private static final long serialVersionUID = 3942549765282708376L;
        private K first;
        private K last;

        public SelectedDays() {
        }

        public SelectedDays(K first, K last) {
            this.first = first;
            this.last = last;
        }

        public K getFirst() {
            return first;
        }

        public void setFirst(K first) {
            this.first = first;
        }

        public K getLast() {
            return last;
        }

        public void setLast(K last) {
            this.last = last;
        }
    }
}
