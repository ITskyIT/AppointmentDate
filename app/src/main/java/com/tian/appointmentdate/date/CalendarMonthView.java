package com.tian.appointmentdate.date;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.tian.appointmentdate.adapter.CalendarAdapter;
import com.tian.appointmentdate.util.CalendarUtils;
import com.tian.appointmentdate.util.DisplayUtil;

import java.security.InvalidParameterException;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by tian on 2017/7/7.
 */

public class CalendarMonthView extends View{
    public static final String VIEW_PARAMS_SELECTED_BEGIN_DATE = "selected_begin_date";
    public static final String VIEW_PARAMS_SELECTED_LAST_DATE = "selected_last_date";
    public static final String VIEW_PARAMS_NEAREST_DATE = "mNearestDay";

    //    public static final String VIEW_PARAMS_HEIGHT = "height";
    public static final String VIEW_PARAMS_MONTH = "month";
    public static final String VIEW_PARAMS_YEAR = "year";
    public static final String VIEW_PARAMS_WEEK_START = "week_start";
    private OnDayClickListener mOnDayClickListener;
    private Calendar mCalendar = null;
    private Calendar mDayLabelCalendar = null;           // 用于显示星期几
    private final Boolean isPrevDayEnabled = false;             // 今天以前的日期是否能被操作
    private  Context context;
    private List<CalendarAdapter.CalendarDay> mInvalidDays;          // 禁用的日期
    private List<CalendarAdapter.CalendarDay> mBusyDays;             // 被占用的日期
    private CalendarAdapter.CalendarDay mNearestDay;                 // 比离入住日期大且是最近的已被占用或者无效日期
    private List<CalendarAdapter.CalendarDay> mCalendarTags;         // 日期下面的标签
    private String mDefTag = "";
    protected Paint mWeekTextPaint;                     // 头部星期几的字体画笔
    protected Paint mDayTextPaint;
    protected Paint mTagTextPaint;                      // 日期底部的文字画笔
    protected Paint mYearMonthPaint;                    // 头部的画笔
    protected Paint mSelectedDayBgPaint;
    protected Paint mBusyDayBgPaint;
    protected Paint mInValidDayBgPaint;
    protected static int MONTH_HEADER_SIZE;                             // 头部的高度（包括年份月份，星期几）
    protected static int YEAR_MONTH_TEXT_SIZE;                         // 头部年份月份的字体大小
    protected static int WEEK_TEXT_SIZE;                                // 头部年份月份的字体大小
    protected int mWidth;                       // simpleMonthView的宽度
    protected int mYear;
    protected int mMonth;
    protected int mPadding = 0;
    protected int mNumDays = 7;                 // 一行几列
    protected int mWeekStart = 1;               // 一周的第一天（不同国家的一星期的第一天不同）
    protected int mNumCells;                    // 一个月有多少天
    CalendarAdapter.CalendarDay mStartDate;          // 入住日期
    CalendarAdapter.CalendarDay mEndDate;            // 退房日期
    private int mNumRows;
    CalendarAdapter.CalendarDay cellCalendar;        // cell的对应的日期
    private int mDayOfWeekStart = 0;            // 日期对应星期几
    Time today;
    protected boolean mHasToday = false;
    protected int mToday = -1;
    private DateFormatSymbols mDateFormatSymbols = new DateFormatSymbols();
    protected int mRowHeight = 32;  // 行高
    protected static int ROW_SEPARATOR = 40;                            // 每行中间的间距
    private static final int SELECTED_CIRCLE_ALPHA = 128;
    private int mDayTextColor;
    private int mPreviousDayTextColor ;  //已过去颜色
    private String mainColor ="#fe8374";
    protected static int DAY_SELECTED_RECT_SIZE;                        // 选中圆角矩形半径
    private int MINI_DAY_NUMBER_TEXT_SIZE;
    private int TAG_TEXT_SIZE ;
    private StringBuilder mStringBuilder = new StringBuilder(50);
    public CalendarMonthView(Context context,DayPickerView.DataModel dataModel) {
        super(context);
        this.context = context;
        today = new Time(Time.getCurrentTimezone());
        today.setToNow();
        mDayLabelCalendar = Calendar.getInstance();
        mCalendar = Calendar.getInstance();
        mInvalidDays = dataModel.invalidDays;
        mBusyDays = dataModel.busyDays;
        mCalendarTags = dataModel.tags;
        mDefTag = dataModel.defTag;
        cellCalendar = new CalendarAdapter.CalendarDay();
        MONTH_HEADER_SIZE = DisplayUtil.dp2px(context,40);
        WEEK_TEXT_SIZE = DisplayUtil.dp2px(context,12);
        DAY_SELECTED_RECT_SIZE =  DisplayUtil.dp2px(context,16);
        MINI_DAY_NUMBER_TEXT_SIZE =  DisplayUtil.dp2px(context,16);
        TAG_TEXT_SIZE =  DisplayUtil.dp2px(context,14);
        YEAR_MONTH_TEXT_SIZE =  DisplayUtil.dp2px(context,16);
        mRowHeight = (((DisplayUtil.dp2px(context,310) - MONTH_HEADER_SIZE - ROW_SEPARATOR)) / 6);
        initView();
    }

    private void initView() {
        // 头部年份和月份的字体paint
        mYearMonthPaint = new Paint();
        mYearMonthPaint.setFakeBoldText(true);
        mYearMonthPaint.setAntiAlias(true);
        mYearMonthPaint.setTextSize(YEAR_MONTH_TEXT_SIZE);
        mYearMonthPaint.setColor(Color.parseColor("#666666"));
        mYearMonthPaint.setTextAlign(Paint.Align.CENTER);
        mYearMonthPaint.setStyle(Paint.Style.FILL);
        // 头部星期几字体paint
        mWeekTextPaint = new Paint();
        mWeekTextPaint.setAntiAlias(true);
        mWeekTextPaint.setTextSize(WEEK_TEXT_SIZE);
        mWeekTextPaint.setColor(Color.parseColor("#878787"));
        mWeekTextPaint.setStyle(Paint.Style.FILL);
        mWeekTextPaint.setTextAlign(Paint.Align.CENTER);
        mWeekTextPaint.setFakeBoldText(true);

        mDayTextPaint = new Paint();
        mDayTextPaint.setAntiAlias(true);
        mDayTextPaint.setColor(mDayTextColor);
        mDayTextPaint.setTextSize(MINI_DAY_NUMBER_TEXT_SIZE);
        mDayTextPaint.setStyle(Paint.Style.FILL);
        mDayTextPaint.setTextAlign(Paint.Align.CENTER);
        mDayTextPaint.setFakeBoldText(false);

        mSelectedDayBgPaint = new Paint();
        mSelectedDayBgPaint.setFakeBoldText(true);
        mSelectedDayBgPaint.setAntiAlias(true);
        mSelectedDayBgPaint.setColor(Color.parseColor(mainColor));
        mSelectedDayBgPaint.setTextAlign(Paint.Align.CENTER);
        mSelectedDayBgPaint.setStyle(Paint.Style.FILL);
        mSelectedDayBgPaint.setAlpha(255);
        // 标签字体paint
        mTagTextPaint = new Paint();
        mTagTextPaint.setAntiAlias(true);
        mTagTextPaint.setColor(mDayTextColor);
        mTagTextPaint.setTextSize(TAG_TEXT_SIZE);
        mTagTextPaint.setStyle(Paint.Style.FILL);
        mTagTextPaint.setTextAlign(Paint.Align.CENTER);
        mTagTextPaint.setFakeBoldText(false);
        mDayTextColor = Color.parseColor("#fe8374");
        mPreviousDayTextColor = Color.parseColor("#D9D9D9");
        // 被占用的日期paint
        mBusyDayBgPaint = new Paint();
        mBusyDayBgPaint.setFakeBoldText(true);
        mBusyDayBgPaint.setAntiAlias(true);
        mBusyDayBgPaint.setColor(Color.parseColor("#D9D9D9"));
        mBusyDayBgPaint.setTextSize(TAG_TEXT_SIZE);
        mBusyDayBgPaint.setTextAlign(Paint.Align.CENTER);
        mBusyDayBgPaint.setStyle(Paint.Style.FILL);
        mBusyDayBgPaint.setAlpha(SELECTED_CIRCLE_ALPHA);

        // 禁用的日期paint
        mInValidDayBgPaint = new Paint();
        mInValidDayBgPaint.setFakeBoldText(true);
        mInValidDayBgPaint.setAntiAlias(true);
        mInValidDayBgPaint.setColor(Color.parseColor("#D9D9D9"));
        mInValidDayBgPaint.setTextSize(TAG_TEXT_SIZE);
        mInValidDayBgPaint.setTextAlign(Paint.Align.CENTER);
        mInValidDayBgPaint.setStyle(Paint.Style.FILL);
        mInValidDayBgPaint.setAlpha(SELECTED_CIRCLE_ALPHA);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        //drawMonthTitle(canvas);
        drawMonthDayLabels(canvas);
        drawMonthCell(canvas);
    }

    /**
     * 绘制头部
     * @param canvas
     */
    private void drawMonthTitle(Canvas canvas) {
        int x = (mWidth + 2 * mPadding) / 2;
        int y = (MONTH_HEADER_SIZE - WEEK_TEXT_SIZE) / 2 + (YEAR_MONTH_TEXT_SIZE / 3);
        StringBuilder stringBuilder = new StringBuilder(getMonthAndYearString().toLowerCase());
        stringBuilder.setCharAt(0, Character.toUpperCase(stringBuilder.charAt(0)));
        canvas.drawText(stringBuilder.toString(), x, y, mYearMonthPaint);
    }

    private String getMonthAndYearString() {
        int flags = DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_NO_MONTH_DAY;
        mStringBuilder.setLength(0);
        long millis = mCalendar.getTimeInMillis();
        return DateUtils.formatDateRange(getContext(), millis, millis, flags);
    }

    //绘制所有的cell
    private void drawMonthCell(Canvas canvas) {
        int y = MONTH_HEADER_SIZE + ROW_SEPARATOR + mRowHeight / 2;
        int paddingDay = (mWidth - 2 * mPadding) / (2 * mNumDays);
        int dayOffset = findDayOffset();
        int day = 1;
        while (day <= mNumCells) {
            int x = paddingDay * (1 + dayOffset * 2) + mPadding;
            mDayTextPaint.setColor(mDayTextColor);
            mDayTextPaint.setTypeface(Typeface.defaultFromStyle(Typeface.NORMAL));
            mTagTextPaint.setColor(mDayTextColor);
            cellCalendar.setDay(mYear, mMonth, day);
            // 当天
            boolean isToady = false;
            if (mHasToday && (mToday == day)) {
                isToady = true;
                mDayTextPaint.setColor(mPreviousDayTextColor);
                canvas.drawText(String.format("%d", day), x, y, mDayTextPaint);
            }
            // 已过去的日期
            boolean isPrevDay = false;
            if (!isPrevDayEnabled && prevDay(day, today)) {
                isPrevDay = true;
                mDayTextPaint.setColor(mPreviousDayTextColor);
                canvas.drawText(String.format("%d", day), x, y, mDayTextPaint);
            }
            boolean isBeginDay = false;
            // 绘制起始日期的方格
            if (mStartDate != null && cellCalendar.equals(mStartDate) && !mStartDate.equals(mEndDate)) {
                isBeginDay = true;
                drawDayCircle(canvas, x, y, mSelectedDayBgPaint);
                mDayTextPaint.setColor(Color.parseColor(mainColor));
                mDayTextPaint.setTextSize(DisplayUtil.dp2px(context,13));
                canvas.drawText("可预约", x, getTextYCenter(mDayTextPaint, y + DAY_SELECTED_RECT_SIZE/2+DisplayUtil.dp2px(context,8)), mDayTextPaint);
                if(isToady) {
                    canvas.drawText("", x, getTextYCenter(mDayTextPaint, y - DAY_SELECTED_RECT_SIZE / 2), mDayTextPaint);
                }
                mDayTextPaint.setTextSize(MINI_DAY_NUMBER_TEXT_SIZE);
            }
            boolean isLastDay = false;
            // 绘制结束日期的方格
            if (mEndDate != null && cellCalendar.equals(mEndDate) && !mStartDate.equals(mEndDate)) {
                Log.e("绘制结束日期的方格","绘制结束日期的方格");
                isLastDay = true;
            }
            // 被占用的日期
            boolean isBusyDay = false;
            for (CalendarAdapter.CalendarDay calendarDay : mBusyDays) {
                if (cellCalendar.equals(calendarDay) && !isPrevDay) {
                    isBusyDay = true;
                    if (mStartDate != null && mEndDate != null && mNearestDay != null &&
                            mEndDate.equals(mNearestDay) && mEndDate.equals(calendarDay)) {

                    } else {
                        if (mStartDate != null && mEndDate == null && mNearestDay != null && cellCalendar.equals(mNearestDay)) {
                            mDayTextPaint.setColor(mDayTextColor);
                        } else {
                            mDayTextPaint.setColor(Color.parseColor("#BEBCBD"));
                        }
                        mDayTextPaint.setTextSize(DisplayUtil.dp2px(context,13));
                        canvas.drawText("已满", x, getTextYCenter(mBusyDayBgPaint, y + DAY_SELECTED_RECT_SIZE / 2+DisplayUtil.dp2px(context,5)), mDayTextPaint);
                        mDayTextPaint.setTextSize(MINI_DAY_NUMBER_TEXT_SIZE);
                    }
                    canvas.drawText(String.format("%d", day), x, getTextYCenter(mTagTextPaint, y - DAY_SELECTED_RECT_SIZE / 2), mDayTextPaint);
                }
            }
            // 禁用的日期
            boolean isInvalidDays = false;
            for (CalendarAdapter.CalendarDay calendarDay : mInvalidDays) {

                if (cellCalendar.equals(calendarDay) && !isPrevDay) {
                    isBusyDay = true;
                    if (mStartDate != null && mEndDate != null && mNearestDay != null &&
                            mEndDate.equals(mNearestDay) && mEndDate.equals(calendarDay)) {

                    } else {
                        // 选择了入住日期，没有选择退房日期，mNearestDay变为可选且不变灰色
                        if (mStartDate != null && mEndDate == null && mNearestDay != null && cellCalendar.equals(mNearestDay)) {
                            mDayTextPaint.setColor(mDayTextColor);
                        } else {
                            mDayTextPaint.setColor(Color.parseColor("#D0D0D0"));
                        }
                        mDayTextPaint.setColor(mPreviousDayTextColor);
                        canvas.drawText("", x, getTextYCenter(mInValidDayBgPaint, y + DAY_SELECTED_RECT_SIZE / 2), mDayTextPaint);
                    }
                    mDayTextPaint.setColor(mPreviousDayTextColor);
                    if(isToady) {
                        canvas.drawText("", x, y, mDayTextPaint);
                        isToady = false;
                    }else {
                        canvas.drawText(String.format("%d", day), x, getTextYCenter(mTagTextPaint, y - DAY_SELECTED_RECT_SIZE / 2), mDayTextPaint);
                    }

                }
            }
            // 绘制标签
            if (!isPrevDay && !isInvalidDays && !isBusyDay && !isBeginDay && !isLastDay) {
                boolean isCalendarTag = false;
                for (CalendarAdapter.CalendarDay calendarDay : mCalendarTags) {
                    if (cellCalendar.equals(calendarDay)) {
                        isCalendarTag = true;
                        canvas.drawText(calendarDay.tag, x, getTextYCenter(mTagTextPaint, y + DAY_SELECTED_RECT_SIZE / 2), mTagTextPaint);
                    }
                }
                if (!isCalendarTag) {
                    canvas.drawText(mDefTag, x, getTextYCenter(mTagTextPaint, y + DAY_SELECTED_RECT_SIZE / 2), mTagTextPaint);
                }
            }
            // 绘制日期
            if (isBeginDay){
                mDayTextPaint.setColor(Color.parseColor("#ffffff"));
                canvas.drawText(String.format("%d", day), x, getTextYCenter(mTagTextPaint, y - DAY_SELECTED_RECT_SIZE / 2), mDayTextPaint);
                isBeginDay=false;

            } else if (!isToady && !isPrevDay && !isInvalidDays && !isBusyDay) {
                canvas.drawText(String.format("%d", day), x, getTextYCenter(mTagTextPaint, y - DAY_SELECTED_RECT_SIZE / 2), mDayTextPaint);
            }

            dayOffset++;
            if (dayOffset == mNumDays) {
                dayOffset = 0;
                y += mRowHeight;
            }
            day++;

        }
    }
    /**
     * 判断是否是已经过去的日期
     *
     * @param monthDay
     * @param time
     * @return
     */
    private boolean prevDay(int monthDay, Time time) {
        return ((mYear < time.year)) || (mYear == time.year && mMonth < time.month) ||
                (mYear == time.year && mMonth == time.month && monthDay < time.monthDay);
    }
    //绘制头部的一行星期几
    private void drawMonthDayLabels(Canvas canvas) {
        int y = MONTH_HEADER_SIZE - (WEEK_TEXT_SIZE / 2)-10;
        // 一个cell的二分之宽度
        int dayWidthHalf = (mWidth - mPadding * 2) / (mNumDays * 2);
        for (int i = 0; i < mNumDays; i++) {
            int calendarDay = (i + mWeekStart) % mNumDays;
            int x = (2 * i + 1) * dayWidthHalf + mPadding;
            mDayLabelCalendar.set(Calendar.DAY_OF_WEEK, calendarDay);
            canvas.drawText(mDateFormatSymbols.getShortWeekdays()[mDayLabelCalendar.get(Calendar.DAY_OF_WEEK)].toUpperCase(Locale.getDefault()),
                    x, y, mWeekTextPaint);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        mWidth = w;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), mRowHeight * mNumRows + MONTH_HEADER_SIZE + ROW_SEPARATOR+20);
    }

    public void setOnDayClickListener(OnDayClickListener onDayClickListener) {
        mOnDayClickListener = onDayClickListener;
    }

    public void setMonthParams(HashMap<String, Object> params) {
        if (!params.containsKey(VIEW_PARAMS_MONTH) && !params.containsKey(VIEW_PARAMS_YEAR)) {
            throw new InvalidParameterException("You must specify month and year for this view");
        }
        setTag(params);
        if (params.containsKey(VIEW_PARAMS_SELECTED_BEGIN_DATE)) {
            mStartDate = (CalendarAdapter.CalendarDay) params.get(VIEW_PARAMS_SELECTED_BEGIN_DATE);
        }
        if (params.containsKey(VIEW_PARAMS_SELECTED_LAST_DATE)) {
            mEndDate = (CalendarAdapter.CalendarDay) params.get(VIEW_PARAMS_SELECTED_LAST_DATE);
        }

        if (params.containsKey("mNearestDay")) {
            mNearestDay = (CalendarAdapter.CalendarDay) params.get("mNearestDay");
        }

        mMonth = (int) params.get(VIEW_PARAMS_MONTH);
        mYear = (int) params.get(VIEW_PARAMS_YEAR);
        mHasToday = false;
        mToday = -1;
        mCalendar.set(Calendar.MONTH, mMonth);
        mCalendar.set(Calendar.YEAR, mYear);
        mCalendar.set(Calendar.DAY_OF_MONTH, 1);
        mDayOfWeekStart = mCalendar.get(Calendar.DAY_OF_WEEK);

        if (params.containsKey(VIEW_PARAMS_WEEK_START)) {
            mWeekStart = (int) params.get(VIEW_PARAMS_WEEK_START);
        } else {
            mWeekStart = mCalendar.getFirstDayOfWeek();
        }

        mNumCells = CalendarUtils.getDaysInMonth(mMonth, mYear);
        for (int i = 0; i < mNumCells; i++) {
            final int day = i + 1;
            if (sameDay(day, today)) {
                mHasToday = true;
                mToday = day;
            }
        }

        mNumRows = calculateNumRows();
    }
    /**
     * 计算每个月的日期占用的行数
     *
     * @return
     */
    private int calculateNumRows() {
        int offset = findDayOffset();
        int dividend = (offset + mNumCells) / mNumDays;
        int remainder = (offset + mNumCells) % mNumDays;
        return (dividend + (remainder > 0 ? 1 : 0));
    }
    /**
     * 每个月第一天是星期几
     *
     * @return
     */
    private int findDayOffset() {
        return (mDayOfWeekStart < mWeekStart ? (mDayOfWeekStart + mNumDays) : mDayOfWeekStart)
                - mWeekStart;
    }
    private boolean sameDay(int monthDay, Time time) {
        return (mYear == time.year) && (mMonth == time.month) && (monthDay == time.monthDay);
    }
    private void drawDayCircle(Canvas canvas, int x, int y, Paint paint) {
        paint.setColor(Color.parseColor(mainColor));
        canvas.drawCircle(x,y- DAY_SELECTED_RECT_SIZE/2, DisplayUtil.dp2px(context,15),paint);
    }
    public interface OnDayClickListener {
        void onDayClick(CalendarMonthView simpleMonthView, CalendarAdapter.CalendarDay calendarDay);
    }
    /**
     * 在使用drawText方法时文字不能根据y坐标居中，所以重新计算y坐标
     * @param paint
     * @param y
     * @return
     */
    private float getTextYCenter(Paint paint, int y) {
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();
        float fontTotalHeight = fontMetrics.bottom - fontMetrics.top;
        float offY = fontTotalHeight / 2 - fontMetrics.bottom;
        return y + offY;
    }
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            CalendarAdapter.CalendarDay calendarDay = getDayFromLocation(event.getX(), event.getY());
            if (calendarDay == null) {
                return true;
            }

            for (CalendarAdapter.CalendarDay day : mInvalidDays) {
                if (calendarDay.equals(day) && !(mEndDate == null && mNearestDay != null && calendarDay.equals(mNearestDay))) {
                    return true;
                }
            }

            for (CalendarAdapter.CalendarDay day : mBusyDays) {
                if (calendarDay.equals(day) && !(mEndDate == null && mNearestDay != null && calendarDay.equals(mNearestDay))) {
                    return true;
                }
            }
            onDayClick(calendarDay);
        }
        return true;
    }
    private void onDayClick(CalendarAdapter.CalendarDay calendarDay) {
        if (mOnDayClickListener != null && (isPrevDayEnabled || !prevDay(calendarDay.day, today))) {
            mOnDayClickListener.onDayClick(this, calendarDay);
        }
    }
    /**
     * 根据坐标获取对应的日期
     * @param x
     * @param y
     * @return
     */
    public CalendarAdapter.CalendarDay getDayFromLocation(float x, float y) {
        int padding = mPadding;
        if ((x < padding) || (x > mWidth - mPadding)) {
            return null;
        }

        int yDay = (int) (y - MONTH_HEADER_SIZE) / mRowHeight;
        int day = 1 + ((int) ((x - padding) * mNumDays / (mWidth - padding - mPadding)) - findDayOffset()) + yDay * mNumDays;

        if (mMonth > 11 || mMonth < 0 || CalendarUtils.getDaysInMonth(mMonth, mYear) < day || day < 1)
            return null;

        CalendarAdapter.CalendarDay calendar = new CalendarAdapter.CalendarDay(mYear, mMonth, day);

        // 获取日期下面的tag
        boolean flag = false;
        for (CalendarAdapter.CalendarDay calendarTag : mCalendarTags) {
            if (calendarTag.compareTo(calendar) == 0) {
                flag = true;
                calendar = calendarTag;
            }
        }
        if (!flag) {
            calendar.tag = mDefTag;
        }
        return calendar;
    }
}
