package ru.osll.goodtravel.models;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import ru.osll.goodtravel.utils.ModelUtils;

/**
 * Created by denis on 10/30/16.
 */

public class Plan extends RealmObject {

    @PrimaryKey
    private String name;
    private int money;
    private RealmList<Day> days;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMoney() {
        return money;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public RealmList<Day> getDays() {
        return days;
    }

    public void setDays(RealmList<Day> days) {
        this.days = days;
    }

    public Plan(String name, RealmList<Day> days) {
        this.name = name;
        this.days=days;
        this.money = ModelUtils.costPrice(days);
    }
    public Plan(){
        name = "unknown plan";
    }

}
