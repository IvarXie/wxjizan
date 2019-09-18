package com.nverrbug.wxjizan.utils;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Title: 日期工具类
 * Description: </p>
 * Email is Wenbo.Xie@b-and-qchina.com<
 * Company: http://www.bnq.com.cn
 *
 * @author xie.wenbo
 * @date 2019-01-07 16:16
 */
public class DateUtil {
    public final static String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    public static final SimpleDateFormat default_format = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS);
    /**
     * 通过制定的格式，将Date对象格式化为字符串
     *
     * @param date 需要转换的Date对象
     * @param formatStr 转换的格式
     * @return 转换之后的字符串
     */
    public static String dateToStr(Date date, String formatStr) {
        String result = null;
        if (date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat(formatStr);
            result = sdf.format(date);
        }
        return result;
    }
    /**
     * @author xie.wenbo
     * @date Created on 2019-01-08 10:32
     * @Description 将date转换为字符串  YYYY_MM_DD_HH_MM_SS
     * @param date 要转换的Date对象
     * @return java.lang.String
     */
    public static String dateToStr(Date date) {
        return dateToStr(date,YYYY_MM_DD_HH_MM_SS);
    }
    /**
     * @author xie.wenbo
     * @date Created on 2019-01-07 16:21
     * @Description 获取指定日期为周几
     * @param date 日期
     * @return java.lang.String
     */
    public static String getWeekOfDate(Date date) {
        String[] weekDays = {"周日", "周一", "周二", "周三", "周四", "周五", "周六"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0)
            w = 0;
        return weekDays[w];
    }

    /**
     * @author xie.wenbo
     * @date Created on 2019-01-08 15:46
     * @Description 判断选择的日期是否是今天
     * @param date
     * @return boolean
     */

    public static boolean isToday(Date date) {
        return isThisTime(date, "yyyy-MM-dd");
    }

    /**
     * @author xie.wenbo
     * @date Created on 2019-01-08 15:46
     * @Description 判断选择的日期是否是本周
     * @param date
     * @return boolean
     */
    public static boolean isThisWeek(Date date) {
        Calendar calendar = Calendar.getInstance();
        int currentWeek = calendar.get(Calendar.WEEK_OF_YEAR);
        calendar.setTime(date);
        int paramWeek = calendar.get(Calendar.WEEK_OF_YEAR);
        if (paramWeek == currentWeek) {
            return true;
        }
        return false;
    }


    /**
     * @author xie.wenbo
     * @date Created on 2019-01-08 15:47
     * @Description 判断选择的日期是否是本月
     * @param date
     * @return boolean
     */
    public static boolean isThisMonth(Date date) {
        return isThisTime(date, "yyyy-MM");
    }

    public static boolean isThisTime(Date date, String pattern) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern);
        String param = sdf.format(date);//参数时间
        String now = sdf.format(new Date());//当前时间
        if (param.equals(now)) {
            return true;
        }
        return false;
    }
    /**
     * @author xie.wenbo
     * @date Created on 2019-01-08 16:04
     * @Description 日期和今天差的天数
     * @param date 日期
     * @return int
     */
     public static int differentTodayDays(Date date){
         return differentDays(new Date(),date);
     }

    /**
     * date2比date1多的天数
     * @param date1
     * @param date2
     * @return
     */
    public static int differentDays(Date date1,Date date2)
    {
        Calendar cal1 = Calendar.getInstance();
        cal1.setTime(date1);

        Calendar cal2 = Calendar.getInstance();
        cal2.setTime(date2);
        int day1= cal1.get(Calendar.DAY_OF_YEAR);
        int day2 = cal2.get(Calendar.DAY_OF_YEAR);

        int year1 = cal1.get(Calendar.YEAR);
        int year2 = cal2.get(Calendar.YEAR);
        if(year1 != year2) {//同一年
            int timeDistance = 0 ;
            for(int i = year1 ; i < year2 ; i ++) {
                if(i%4==0 && i%100!=0 || i%400==0) {//闰年
                    timeDistance += 366;
                } else{//不是闰年
                    timeDistance += 365;
                }
            }

            return timeDistance + (day2-day1) ;
        }else{//不同年
            return day2-day1;
        }
    }


}
