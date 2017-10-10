package com.tian.appointmentdate.date;

import com.tian.appointmentdate.adapter.CalendarAdapter;

public interface DatePickerController {
    void onDayOfMonthSelected(CalendarAdapter.CalendarDay calendarDay);          // 点击日期回调函数，月份记得加1
}