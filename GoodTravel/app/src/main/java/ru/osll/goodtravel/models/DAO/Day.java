package ru.osll.goodtravel.models.DAO;


import java.util.Date;
import java.util.ArrayList;


public class Day {

    private long id;

    private Plan plan;
    private Date date;
    private ArrayList<Place> places;

    public Day(long id, Plan plan, Date date) {
        this.id = id;
        this.plan = plan;
        this.date = date;
    }

    public Plan getPlan() {
        return plan;
    }

    public void setPlan(Plan plan) {
        this.plan = plan;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public ArrayList<Place> getPlaces() {
        return places;
    }

    public void setPlaces(ArrayList<Place> places) {
        this.places = places;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Day() {
        places = new ArrayList<>();
    }

    public static Day getById(long id){
        // TODO: 28.01.17 Реализовать
//        return DBHelper.getInstance()
//                .getDefaultInstance()
//                .where(Day.class)
//                .equalTo("id", id)
//                .findFirst();
        return null;
    }
}
