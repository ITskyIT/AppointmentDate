package com.tian.appointmentdate;

/**
 * Created by tian on 2017/5/16.
 */

public class CalendarBean {

        /**
         * id : 4
         * date : 2017-05-18
         * cityId : 1
         * type : 1
         * acount : 1
         * pcount : 1
         */

        private int id;
        private String date;
        private int cityId;
        private int type;
        private int acount;
        private int pcount;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public int getCityId() {
            return cityId;
        }

        public void setCityId(int cityId) {
            this.cityId = cityId;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }

        public int getAcount() {
            return acount;
        }

        public void setAcount(int acount) {
            this.acount = acount;
        }

        public int getPcount() {
            return pcount;
        }

        public void setPcount(int pcount) {
            this.pcount = pcount;
        }

    @Override
    public String toString() {
        return "CalendarBean{" +
                "id=" + id +
                ", date='" + date + '\'' +
                ", cityId=" + cityId +
                ", type=" + type +
                ", acount=" + acount +
                ", pcount=" + pcount +
                '}';
    }
}
