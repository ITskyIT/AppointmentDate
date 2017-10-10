package com.tian.appointmentdate;

import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tian.appointmentdate.adapter.CalendarAdapter;
import com.tian.appointmentdate.card.CardViewPager;
import com.tian.appointmentdate.date.DatePickerController;
import com.tian.appointmentdate.date.DayPickerView;
import com.tian.appointmentdate.util.CalendarUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    CardViewPager viewPager;
    ImageView ivLeft;
    ImageView ivRight;
    TextView tvTitle,selectDate;
    private int monthCount = 12;   //显示月份的个数
    public static int position = 0; //当前pickView下标
    private int viewPageIndex = 0; // viewPager下标
    private DayPickerView dayPickerView = null;
    public static Map<String,CalendarAdapter> adapters = new HashMap<>(); //adapter集合
    private static final long ONE_DAY_MS=24*60*60*1000; //以天为单位
    private List<DayPickerView.DataModel> listModel = new ArrayList<>(); //model集合
    private List<String> allDate = new ArrayList<>(); //所有日期的集合
    private CalendarCardAdapter adapter;
    private List<String> serverBackDate = new ArrayList<>(); //服务器返回的日期集合
    private List<String> busyDate = new ArrayList<>(); // 繁忙的日期
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initComponent();
        getAllDate();
        initDate(); //  用本地代替网络获取到的
    }

    private void initDate() {
        //  服务器返回的是可以选取的日期实体集合，还分上下午（这里不做分别）
        //返回的
        serverBackDate.add("2017-10-15");
        serverBackDate.add("2017-10-20");
        serverBackDate.add("2017-10-21");
        serverBackDate.add("2017-10-22");
        serverBackDate.add("2017-11-10");
        serverBackDate.add("2017-11-11");
        serverBackDate.add("2018-01-22"); //太多 随便填几个
        //  满的
        busyDate.add("2017-10-16");

        Calendar calendar=Calendar.getInstance();
        int startYear = calendar.get(Calendar.YEAR);
        int  startMonth = calendar.get(Calendar.MONTH);
        int year = startYear;
        int month = startMonth;
        List<String> xunDateList = new ArrayList<>(); //每次循环 重置
        for (int i = 0; i < monthCount; i++) {
            if (month > 12) {
                year = year+1;
                month = 1;
            }
            DayPickerView.DataModel dataModel = new DayPickerView.DataModel();
            dataModel.yearStart =  year;
            dataModel.monthStart = month;
            dataModel.showMonthCount = 1;
            dataModel.defTag = "";
            dataModel.leastDaysNum = 0;
            dataModel.mostDaysNum = 1;
            month = month+1;
            List<CalendarAdapter.CalendarDay> invalidDays = new ArrayList<>();
            List<CalendarAdapter.CalendarDay> busyDays = new ArrayList<>();
            xunDateList = allDate;
            xunDateList.removeAll(serverBackDate);
            for (int j = 0; j < xunDateList.size(); j++) {
                CalendarAdapter.CalendarDay invalidDay1 = new CalendarAdapter.CalendarDay(xunDateList.get(j));
                invalidDays.add(invalidDay1);//无法选取的日期
            }
            for (int z = 0; z < busyDate.size(); z++) {
                CalendarAdapter.CalendarDay byzs = new CalendarAdapter.CalendarDay(busyDate.get(z));
                busyDays.add(byzs);//   已满的日期
            }
            dataModel.busyDays = busyDays;
            dataModel.invalidDays = invalidDays;
            listModel.add(dataModel);
        }
        initViewPager();
    }

    private void initComponent() {
        viewPager = (CardViewPager) findViewById(R.id.viewPager);
        ivLeft = (ImageView) findViewById(R.id.arrow_left);
        ivRight = (ImageView) findViewById(R.id.arrow_right);
        tvTitle = (TextView) findViewById(R.id.title_date);
        selectDate = (TextView) findViewById(R.id.select_date);
        ivLeft.setOnClickListener(this);
        ivRight.setOnClickListener(this);
    }

    private void initViewPager() {

        viewPager.setTransform();
        adapter = new CalendarCardAdapter();
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(monthCount-1);
        viewPager.setCurrentItem(0);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                viewPageIndex = position;
                Calendar calendar=Calendar.getInstance();
                int startY = calendar.get(Calendar.YEAR);
                int startM = calendar.get(Calendar.MONTH);
                if (position == 0) {
                    ivLeft.setImageResource(R.mipmap.turn_left_off);
                    ivLeft.setEnabled(false);
                } else if (position == monthCount-1){
                    ivRight.setEnabled(false);
                    ivRight.setImageResource(R.mipmap.turn_right_off);
                } else {
                    ivLeft.setImageResource(R.mipmap.turn_left);
                    ivRight.setImageResource(R.mipmap.turn_right);
                    ivLeft.setEnabled(true);
                    ivRight.setEnabled(true);
                }
                if ((startM+1)+position> 12 ) {
                    startY = startY+1;
                    startM = ((startM+1)+position)%12;
                } else {
                    startM =  (startM+1)+position;
                }
                tvTitle.setText(startY+"年"+getMonth(startM)+"月");
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.arrow_left:
                viewPager.setCurrentItem(--viewPageIndex);
                break;
            case R.id.arrow_right:
                viewPager.setCurrentItem(++viewPageIndex);
                break;
        }
    }

    public class CalendarCardAdapter extends PagerAdapter {

        @Override
        public int getCount() {
            return monthCount;
        }
        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            dayPickerView = new DayPickerView(MainActivity.this,position);
            dayPickerView.setParameter(listModel.get(position), new DatePickerController() {
                @Override
                public void onDayOfMonthSelected(CalendarAdapter.CalendarDay calendarDay) {
                    selectDate.setText(calendarDay.year + "-"
                            + getMonth(calendarDay.month + 1)
                            + "-" + getMonth(calendarDay.day));
                }
            });
            container.addView(dayPickerView);
            return dayPickerView;
        }
        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
    /**
     * 获取所有日期
     */
    private void getAllDate() {
        Calendar calendar= Calendar.getInstance();
        int startYears = calendar.get(Calendar.YEAR);
        int startMonths = calendar.get(Calendar.MONTH);
        tvTitle.setText(startYears+"年"+getMonth(startMonths+1)+"月");
        int month = startMonths;
        int year = startYears;
        for (int i = 0; i < monthCount; i++) {
            if (month > 12) {
                year = year + 1;
                month = 1;
            }
            month = month+1;
        }
        long startTime=Long.parseLong(data
                (calendar.get(Calendar.YEAR)+"-"+getMonth(calendar.get(Calendar.MONTH)+1)+"-"+calendar.get(Calendar.DAY_OF_MONTH)));
        int endDay = CalendarUtils.getDaysInMonth(month-1,year);
        long endTime =Long.parseLong(data(year+"-"+getMonth(month)+"-"+endDay));
        betweenDays(startTime,endTime);
    }
    /**
     * 时间戳换字符串
     * @param time
     * @return
     */
    public static String getDate(long time) {
        Date date = new Date(time);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }
    /**
     * 根据输入日期转成时间戳（String化）
     * @param time
     * @return
     */
    public String data(String time) {
        SimpleDateFormat sdr = new SimpleDateFormat("yyyy-MM-dd");
        Date date;
        String times = null;
        try {
            date = sdr.parse(time);
            long l = date.getTime();
            String stf = String.valueOf(l);
            times = stf;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return times;
    }
    /**
     * 获取两个时间戳之间的所有日期
     * @param startTime
     * @param endTime
     */
    private void betweenDays(long startTime,long endTime){
        Date date_start=new Date(startTime);
        Date date_end=new Date(endTime);
        //计算日期从开始时间于结束时间的0时计算
        Calendar fromCalendar = Calendar.getInstance();
        fromCalendar.setTime(date_start);
        fromCalendar.set(Calendar.HOUR_OF_DAY, 0);
        fromCalendar.set(Calendar.MINUTE, 0);
        fromCalendar.set(Calendar.SECOND, 0);
        fromCalendar.set(Calendar.MILLISECOND, 0);

        Calendar toCalendar = Calendar.getInstance();
        toCalendar.setTime(date_end);
        toCalendar.set(Calendar.HOUR_OF_DAY, 0);
        toCalendar.set(Calendar.MINUTE, 0);
        toCalendar.set(Calendar.SECOND, 0);
        toCalendar.set(Calendar.MILLISECOND, 0);

        int s = (int) ((toCalendar.getTimeInMillis() - fromCalendar.getTimeInMillis())/ (ONE_DAY_MS));
        if(s>0){
            for(int i = 0;i<=s;i++){
                long todayDate = fromCalendar.getTimeInMillis() + i * ONE_DAY_MS;
                //获取所有时间
                allDate.add(getDate(todayDate));
            }
        }
    }

    /**
     * 月数小于10  十位补0
     * @param month
     * @return
     */
    public String getMonth(int month){
        if (month<10){
            return "0"+month;
        }
        return month+"";
    }
}
