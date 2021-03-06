package ru.osll.goodtravel.models;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import ru.osll.goodtravel.enums.PartnerType;
import ru.osll.goodtravel.enums.TypeOfGroupEnum;
import ru.osll.goodtravel.models.DAO.Day;
import ru.osll.goodtravel.models.DAO.Place;
import ru.osll.goodtravel.models.DAO.PlaceCategory;
import ru.osll.goodtravel.models.DAO.Plan;

/**
 * Created by denis on 28.01.17.
 */

public class DataBase {

    private Context context;
    private static SQLiteDatabase database;
    private static DataBase sDataBase;
    private static final String TAG = "DataBase.class";
    private static final String ID = "id";

    //Таблица PLACE_CATEGORIES
    private static final String TABLE_PLACE_CATEGORIES = "place_categories";
    private static final String ROW_PLACE_CATEGORIES_NAME = "name";
    private static final String ROW_PLACE_CATEGORIES_IMG = "image";

    //Таблица PLACES
    private static final String TABLE_PLACES = "places";

    private static final String ROW_PLACES_NAME = "name";
    private static final String ROW_PLACES_IMG = "image";
    private static final String ROW_PLACES_DESCRIPTION = "desc";
    private static final String ROW_PLACES_PLACE_NAME = "place_name";
    private static final String ROW_PLACES_ADDRESS = "address";
    private static final String ROW_PLACES_COORDINATE = "coordinate";
    private static final String ROW_PLACES_PRICE = "price";
    private static final String ROW_PLACES_TYPEOFGROUP = "typeOfGroup";
    private static final String ROW_PLACES_PLACE_CATEGORY = "id_category";

    //Таблица DAYS
    private static final String TABLE_DAYS = "days";
    private static final String ROW_DAYS_DATE = "date";
    private static final String ROW_DAYS_PLAN_ID = "id_plan";

    //Таблица DAYS IN PLAN
    private static final String TABLE_PLACE_IN_DAYS = "days_in_plan";
    private static final String ROW_PLACE_IN_DAYS_ID_PLACE = "id_place";
    private static final String ROW_PLACE_IN_DAYS_ID_DAY = "id_day";

    //Таблица PLANS
    private static final String TABLE_PLANS = "plans";
    private static final String ROW_PLANS_NAME = "name";
    private static final String ROW_PLANS_MONEY = "money";

    public static DataBase getInstance(Context context) {
        if (sDataBase==null){
            sDataBase=new DataBase(context);
        }
        return sDataBase;
    }


    private DataBase(Context context){
        this.context = context;
        myDataBaseHelper newDBH = new myDataBaseHelper(context);
        database = newDBH.getWritableDatabase();
    }

    public static void clearData() {
        database.delete(TABLE_PLACE_IN_DAYS, "", null);
        database.delete(TABLE_PLACES, "", null);
        database.delete(TABLE_PLACE_CATEGORIES, "", null);
        database.delete(TABLE_DAYS, "", null);
        database.delete(TABLE_PLANS, "", null);
    }

    /**
     * Класс для CRUD операций с категориями
     */
    public static class PlaceCategoryRepository {
        public static PlaceCategory save(PlaceCategory pl){
            ContentValues cv = new ContentValues();
            cv.put(ROW_PLACE_CATEGORIES_NAME, pl.getName());
            cv.put(ROW_PLACE_CATEGORIES_IMG, pl.getStrImg());
            pl.setId( database.insert(TABLE_PLACE_CATEGORIES, null, cv));
            return pl;
        }
        public static void save(ArrayList<PlaceCategory> placeCategories){
            for (PlaceCategory pl: placeCategories ) {
                save(pl);
            }
        }
        public static PlaceCategory get(long id){
            Cursor c = database.query(TABLE_PLACE_CATEGORIES,
                    null,
                    ID + "=" + id,
                    null, null, null, null);
            PlaceCategory placeCategory;

            if (c.moveToFirst()) {
                placeCategory = new PlaceCategory(
                        c.getLong(c.getColumnIndex(ID)),
                        c.getString(c.getColumnIndex(ROW_PLACE_CATEGORIES_NAME)),
                        c.getString(c.getColumnIndex(ROW_PLACE_CATEGORIES_IMG))
                );
                c.close();
                return placeCategory;
            } else {
                return null;
            }
        }
        public static ArrayList<PlaceCategory> getAll(){
            ArrayList<PlaceCategory> arrayList = new ArrayList<PlaceCategory>();
            Cursor c = database.rawQuery("Select " + ID + ", " +
                    ROW_PLACE_CATEGORIES_NAME + ", " +
                    ROW_PLACE_CATEGORIES_IMG +" from " + TABLE_PLACE_CATEGORIES + " " +
                    "order by " + ID, null);
            if (c.moveToFirst()) {
                do {
                    arrayList.add(new PlaceCategory(
                            c.getLong(c.getColumnIndex(ID)),
                            c.getString(c.getColumnIndex(ROW_PLACE_CATEGORIES_NAME)),
                            c.getString(c.getColumnIndex(ROW_PLACE_CATEGORIES_IMG))
                    ));
                    c.moveToNext();
                } while (!c.isAfterLast());
            }
            c.close();
            return arrayList;
        }
        public static void update(PlaceCategory pl){
            ContentValues cv = new ContentValues();

            cv.put(ROW_PLACE_CATEGORIES_NAME, pl.getName());
            cv.put(ROW_PLACE_CATEGORIES_IMG, pl.getStrImg());
            database.update(TABLE_PLACE_CATEGORIES, cv, ID + " = ?",
                    new String[]{Long.toString(pl.getId())});
        }
        public static void delete(PlaceCategory pl){

            database.delete(TABLE_PLACES,ROW_PLACES_PLACE_CATEGORY + " = "+ pl.getId(),null);
            database.delete(TABLE_PLACE_CATEGORIES, ID + " = " + pl.getId(), null);
        }
    }

    /**
     * Класс для CRUD операций с местами
     */
    public static class PlaceRepository {
        public static Place save(Place pl){
            ContentValues cv = new ContentValues();
            cv.put(ROW_PLACES_NAME, pl.getName());
            cv.put(ROW_PLACES_IMG, pl.getSrcToImg());
            cv.put(ROW_PLACES_DESCRIPTION, pl.getDescription());
            cv.put(ROW_PLACES_PLACE_NAME, pl.getPlaceName());
            cv.put(ROW_PLACES_ADDRESS, pl.getAddress());
            cv.put(ROW_PLACES_COORDINATE, pl.getCoordinate());
            cv.put(ROW_PLACES_PRICE, pl.getPrice());
            cv.put(ROW_PLACES_TYPEOFGROUP, pl.getTypeOfGroup().toString());
            cv.put(ROW_PLACES_PLACE_CATEGORY, pl.getCategory().getId());
            pl.setId(database.insert(TABLE_PLACES, null, cv));
            return pl;
        }
        public static Place saveOrUpdate(Place pl){

            if (get(pl.getId())!=null){
                update(pl);
            }else {
                return save(pl);
            }
            return pl;
        }
        public static ArrayList<Place> saveOrUpdate(ArrayList<Place> places){
            for(int i=0;i<places.size();i++){
                places.set(i,saveOrUpdate(places.get(i)));
            }
            return places;
        }
        public static void update(Place pl){
            ContentValues cv = new ContentValues();

            cv.put(ROW_PLACES_NAME, pl.getName());
            cv.put(ROW_PLACES_DESCRIPTION, pl.getDescription());
            cv.put(ROW_PLACES_PLACE_NAME, pl.getPlaceName());
            cv.put(ROW_PLACES_ADDRESS, pl.getAddress());
            cv.put(ROW_PLACES_COORDINATE, pl.getCoordinate());
            cv.put(ROW_PLACES_PRICE, pl.getCoordinate());
            cv.put(ROW_PLACES_IMG, pl.getSrcToImg());
            cv.put(ROW_PLACES_TYPEOFGROUP, pl.getTypeOfGroup().toString());
            cv.put(ROW_PLACES_PLACE_CATEGORY, pl.getCategory().getId());
            database.update(TABLE_PLACE_CATEGORIES, cv, ID + " = ?",
                    new String[]{Long.toString(pl.getId())});
        }
        public static void delete(Place pl){
            //// TODO: 28.01.17 не реализованно каскадное удаление
            database.delete(TABLE_PLACES, ID + " = " + pl.getId(), null);
        }
        public static Place get(long id){
            Cursor c = database.query(TABLE_PLACE_CATEGORIES,
                    null,
                    ID + "=" + id,
                    null, null, null, null);
            Place place;

            if (c.moveToFirst()) {

                PlaceCategory placeCategory = PlaceCategoryRepository
                        .get(c.getLong(c.getColumnIndex(ROW_PLACES_PLACE_CATEGORY)));
                place = new Place(
                        c.getLong(c.getColumnIndex(ID)),
                        c.getString(c.getColumnIndex(ROW_PLACES_NAME)),
                        c.getString(c.getColumnIndex(ROW_PLACES_DESCRIPTION)),
                        c.getString(c.getColumnIndex(ROW_PLACES_PLACE_NAME)),
                        c.getString(c.getColumnIndex(ROW_PLACES_ADDRESS)),
                        c.getString(c.getColumnIndex(ROW_PLACES_COORDINATE)),
                        c.getLong(c.getColumnIndex(ROW_PLACES_PRICE)),
                        c.getString(c.getColumnIndex(ROW_PLACES_IMG)),
                        c.getString(c.getColumnIndex(ROW_PLACES_TYPEOFGROUP)),
                        placeCategory);
                c.close();
                return place;
            } else {
                return null;
            }
        }
        public static ArrayList<Place> getAll(){
            ArrayList<Place> arrayList = new ArrayList<Place>();
            Cursor c = database.rawQuery("Select " + ID + ", " +
                    ROW_PLACES_NAME + ", " +
                    ROW_PLACES_DESCRIPTION + ", " +
                    ROW_PLACES_PLACE_NAME + ", " +
                    ROW_PLACES_ADDRESS + ", " +
                    ROW_PLACES_COORDINATE + ", " +
                    ROW_PLACES_PLACE_CATEGORY + ", " +
                    ROW_PLACES_PRICE + ", " +
                    ROW_PLACES_IMG + ", " +
                    ROW_PLACES_TYPEOFGROUP +" from " + TABLE_PLACES + " " +
                    "order by " + ID, null);
            if (c.moveToFirst()) {
                do {
                    PlaceCategory placeCategory = PlaceCategoryRepository
                            .get(c.getLong(c.getColumnIndex(ROW_PLACES_PLACE_CATEGORY)));
                    arrayList.add(new Place(
                            c.getLong(c.getColumnIndex(ID)),
                            c.getString(c.getColumnIndex(ROW_PLACES_NAME)),
                            c.getString(c.getColumnIndex(ROW_PLACES_DESCRIPTION)),
                            c.getString(c.getColumnIndex(ROW_PLACES_PLACE_NAME)),
                            c.getString(c.getColumnIndex(ROW_PLACES_ADDRESS)),
                            c.getString(c.getColumnIndex(ROW_PLACES_COORDINATE)),
                            c.getLong(c.getColumnIndex(ROW_PLACES_PRICE)),
                            c.getString(c.getColumnIndex(ROW_PLACES_IMG)),
                            c.getString(c.getColumnIndex(ROW_PLACES_TYPEOFGROUP)),
                            placeCategory));
                    c.moveToNext();
                } while (!c.isAfterLast());
            }
            c.close();
            return arrayList;
        }
        public static ArrayList<Place> getFromFilter(
                @NotNull ArrayList<PlaceCategory> categories,
                Long price,
                PartnerType pt){
            ArrayList<Place> arrayList = new ArrayList<Place>();
            StringBuilder stringBuilder = new StringBuilder();
            for (int i = 0; i < categories.size(); i++) {
                if (categories.size()-1==i) stringBuilder.append(Long.toString(categories.get(i).getId()));
                else stringBuilder.append(Long.toString(categories.get(i).getId())).append(", ");
            }
            String partnerQuery=" and "+ ROW_PLACES_TYPEOFGROUP + " in ( "+ TypeOfGroupEnum.ALL.toString();
            if (pt ==PartnerType.SINGLE) partnerQuery+=
                    ", " +  TypeOfGroupEnum.ONLY_SINGLE.toString() +
                    ", " + TypeOfGroupEnum.NO_FAMILY.toString() +
                    ", " + TypeOfGroupEnum.NO_COUPLE.toString();
            else if(pt==PartnerType.COUPLE) partnerQuery+=
                    ", " +  TypeOfGroupEnum.ONLY_COUPLE.toString() +
                    ", " + TypeOfGroupEnum.NO_FAMILY.toString() +
                    ", " + TypeOfGroupEnum.NO_SINGLE.toString();
            else  partnerQuery+=
                    ", " +  TypeOfGroupEnum.ONLY_FAMILY.toString() +
                    ", " + TypeOfGroupEnum.NO_SINGLE.toString() +
                    ", " + TypeOfGroupEnum.NO_SINGLE.toString();
            partnerQuery+=" ) ";


            String sql = "Select " + ID + ", " +
                    ROW_PLACES_NAME + ", " +
                    ROW_PLACES_DESCRIPTION + ", " +
                    ROW_PLACES_PLACE_NAME + ", " +
                    ROW_PLACES_ADDRESS + ", " +
                    ROW_PLACES_COORDINATE + ", " +
                    ROW_PLACES_PRICE + ", " +
                    ROW_PLACES_IMG + ", " +
                    ROW_PLACES_PLACE_CATEGORY + ", " +
                    ROW_PLACES_TYPEOFGROUP + " from " + TABLE_PLACES + " " +
                    " where " + ROW_PLACES_PLACE_CATEGORY + " in ( " + stringBuilder.toString() + " ) " +
                    "and " + ROW_PLACES_PRICE + " <= " + price.toString() +
                    //partnerQuery+
                    " order by " + ID;
            Log.d(TAG, sql);
            Cursor c = database.rawQuery(sql, null);

            if (c.moveToFirst()) {
                do {
                    PlaceCategory placeCategory = PlaceCategoryRepository
                            .get(c.getLong(c.getColumnIndex(ROW_PLACES_PLACE_CATEGORY)));
                    arrayList.add(new Place(
                            c.getLong(c.getColumnIndex(ID)),
                            c.getString(c.getColumnIndex(ROW_PLACES_NAME)),
                            c.getString(c.getColumnIndex(ROW_PLACES_DESCRIPTION)),
                            c.getString(c.getColumnIndex(ROW_PLACES_PLACE_NAME)),
                            c.getString(c.getColumnIndex(ROW_PLACES_ADDRESS)),
                            c.getString(c.getColumnIndex(ROW_PLACES_COORDINATE)),
                            c.getLong(c.getColumnIndex(ROW_PLACES_PRICE)),
                            c.getString(c.getColumnIndex(ROW_PLACES_IMG)),
                            c.getString(c.getColumnIndex(ROW_PLACES_TYPEOFGROUP)),
                            placeCategory));
                    c.moveToNext();
                } while (c.isAfterLast() == false);
            }
            return arrayList;
        }
    }

    /**
     * Класс для CRUD операций с днями
     */
    public static class DayRepository {
        public static Day save(Day day){
            ContentValues cv = new ContentValues();
            cv.put(ROW_DAYS_DATE, day.getDate().toString());
            if (day.getPlan()!=null)
                cv.put(ROW_DAYS_PLAN_ID, day.getPlan().getId());
            long id =  database.insert(TABLE_DAYS, null, cv);
            day.setId(id);
            if (!day.getPlaces().isEmpty())
                PlaceInDayRepository.addPlacesInDay(day,day.getPlaces());

            return day;
        }
        public static void save(ArrayList<Day> days){
            for (Day d: days) save(d);
        }
        public static Day get(long id){
            Cursor c = database.rawQuery("Select " + ID + ", " +
                    ROW_DAYS_DATE + ", " +
                    ROW_DAYS_PLAN_ID +" from " + TABLE_DAYS + " " +
                    " where " + ID + " = " + id +
                    " order by " + ID, null);
            Day day;
            if (c.moveToFirst()) {
                Log.d(TAG, "query for day is correct for id=" + id );
                Plan plan = PlanRepository.get(c.getLong(c.getColumnIndex(ROW_DAYS_PLAN_ID)));
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-mm-dd");
                Date date = null;
                try {
                    date = simpleDateFormat.parse(c.getString(c.getColumnIndex(ROW_DAYS_DATE)));
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                day = new Day(
                        id,
                        plan,
                        date
                );
                c.close();

                day.setPlaces(DataBase.PlaceInDayRepository.getPlacesFromDay(day));
                if (day.getPlaces().size()==0){
                    Log.d(TAG, "empty day!" );
                }
                return day;
            } else {
                Log.e(TAG, "query for day is incorrect for id=" + id);
                return null;
            }
        }
        public static ArrayList<Day> getDaysFromPlan(Plan plan){
            return getDaysFromPlan(plan.getId());
        }
        public static ArrayList<Day> getDaysFromPlan(long id){
            ArrayList<Day> arrayList = new ArrayList<Day>();
            Cursor c = database.rawQuery("Select " + ID + ", " +
                    ROW_DAYS_DATE + ", " +
                    ROW_DAYS_PLAN_ID +" from " + TABLE_DAYS + " " +
                    "where " +ROW_DAYS_PLAN_ID + " = " + id + " " +
                    "order by " + ID, null);

            if (c.moveToFirst()) {
                do {
                    Plan plan = PlanRepository.get(c.getLong(c.getColumnIndex(ROW_DAYS_PLAN_ID)));

                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-mm-dd");
                    Date date = null;
                    try {
                        date = simpleDateFormat.parse(c.getString(c.getColumnIndex(ROW_DAYS_DATE)));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    arrayList.add(new Day(
                            c.getLong(c.getColumnIndex(ID)),
                            plan,
                            date
                    ));
                    c.moveToNext();
                } while (!c.isAfterLast());
            }
            c.close();
            return arrayList;
        }


        public static ArrayList<Day> getAll(){
            ArrayList<Day> arrayList = new ArrayList<Day>();
            Cursor c = database.rawQuery("Select " + ID + ", " +
                    ROW_DAYS_DATE + ", " +
                    ROW_DAYS_PLAN_ID +" from " + TABLE_DAYS + " " +
                    "order by " + ID, null);
            if (c.moveToFirst()) {
                do {
                    Plan plan = PlanRepository.get(c.getLong(c.getColumnIndex(ROW_DAYS_PLAN_ID)));
                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-mm-dd");
                    Date date = null;
                    try {
                        date = simpleDateFormat.parse(c.getString(c.getColumnIndex(ROW_DAYS_DATE)));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                    arrayList.add(new Day(
                            c.getLong(c.getColumnIndex(ID)),
                            plan,
                            date
                    ));
                    c.moveToNext();
                } while (!c.isAfterLast());
            }
            c.close();
            return arrayList;
        }
        public static void update(Day day){
            ContentValues cv = new ContentValues();

            if (day.getPlan()!=null)
                cv.put(ROW_DAYS_PLAN_ID, day.getPlan().getId());
            cv.put(ROW_DAYS_DATE, day.getDate().toString());
            database.update(TABLE_DAYS, cv, ID + " = ?",
                    new String[]{Long.toString(day.getId())});
        }
        public static void delete(Day pl){
            //// TODO: 28.01.17 не реализованно каскадное удаление
            database.delete(TABLE_DAYS, ID + " = " + pl.getId(), null);
        }
    }

    /**
     * Класс для CRUD операций с планами
     */
    public static class PlanRepository {
        public static Plan save(Plan pl){
            ContentValues cv = new ContentValues();
            cv.put(ROW_PLANS_NAME, pl.getName());
            cv.put(ROW_PLANS_MONEY, pl.getMoney());
            pl.setId( database.insert(TABLE_PLANS, null, cv));
            return pl;
        }
        public static Plan get(long id){
            Cursor c = database.query(TABLE_PLANS,
                    null,
                    ID + "=" + id,
                    null, null, null, null);
            Plan plan;

            if (c.moveToFirst()) {
                plan = new Plan(
                        id,
                        c.getString(c.getColumnIndex(ROW_PLANS_NAME)),
                        c.getLong(c.getColumnIndex(ROW_PLANS_MONEY))
                );
                c.close();
                return plan;
            } else {
                return null;
            }
        }
        public static ArrayList<Plan> getAll(){
            ArrayList<Plan> arrayList = new ArrayList<Plan>();
            Cursor c = database.rawQuery("Select " + ID + ", " +
                    ROW_PLANS_NAME + ", " +
                    ROW_PLANS_MONEY +" from " + TABLE_PLANS + " " +
                    "order by " + ID, null);
            if (c.moveToFirst()) {
                do {
                    arrayList.add(new Plan(
                            c.getLong(c.getColumnIndex(ID)),
                            c.getString(c.getColumnIndex(ROW_PLANS_NAME)),
                            c.getLong(c.getColumnIndex(ROW_PLANS_MONEY))
                    ));
                    c.moveToNext();
                } while (!c.isAfterLast());
            }
            c.close();
            return arrayList;
        }
        public static void update(Plan pl){
            ContentValues cv = new ContentValues();

            cv.put(ROW_PLANS_NAME, pl.getName());
            cv.put(ROW_PLANS_MONEY, pl.getMoney());
            database.update(TABLE_PLANS, cv, ID + " = ?",
                    new String[]{Long.toString(pl.getId())});
        }
        public static void delete(Plan pl){
            //// TODO: 28.01.17 не реализованно каскадное удаление
            database.delete(TABLE_PLANS, ID + " = " + pl.getId(), null);
        }
    }

    /**
     * Класс для рабоваления мест к дню.
     */
    public static class PlaceInDayRepository{
        public static void addPlaceInDay(Day day,Place place){
            ContentValues cv = new ContentValues();
            cv.put(ROW_PLACE_IN_DAYS_ID_DAY, day.getId());
            cv.put(ROW_PLACE_IN_DAYS_ID_PLACE, place.getId());
            database.insert(TABLE_PLACE_IN_DAYS, null, cv);
        }
        public static void addPlacesInDay(Day day, ArrayList<Place> places){
            for (Place pl : places) {
                addPlaceInDay(day, pl);
            }
        }
        public static ArrayList<Place> getPlacesFromDay(Day day){
            ArrayList<Place> places = new ArrayList<Place>();
            Cursor cr =database.rawQuery("select "+
                            ROW_PLACE_IN_DAYS_ID_PLACE+ " "+
                    " from "+ TABLE_PLACE_IN_DAYS + " "
                    + " where " +ROW_PLACE_IN_DAYS_ID_DAY + " = " +day.getId()
                    ,null);
            long n = cr.getCount();

            long[] ids = new long[cr.getCount()];
            int i=0;
            cr.moveToFirst();
            do {

                ids[i] = cr.getLong(cr.getColumnIndex(ROW_PLACE_IN_DAYS_ID_PLACE));
                i++;
                cr.moveToNext();
            }
            while (!cr.isAfterLast());

            for (int j = 0; j < ids.length; j++) {
                Cursor c = database.rawQuery("Select " + ID + ", " +
                        ROW_PLACES_NAME + ", " +
                        ROW_PLACES_DESCRIPTION + ", " +
                        ROW_PLACES_PLACE_NAME + ", " +
                        ROW_PLACES_ADDRESS + ", " +
                        ROW_PLACES_COORDINATE + ", " +
                        ROW_PLACES_PLACE_CATEGORY + ", " +
                        ROW_PLACES_PRICE + ", " +
                        ROW_PLACES_IMG + ", " +
                        ROW_PLACES_TYPEOFGROUP +" from " + TABLE_PLACES + " " +
                        "where " + ID + " = " + ids[j]+ " " +
                        "order by " + ID, null);

                if (c.moveToFirst()) {
                    do {
                        PlaceCategory placeCategory = PlaceCategoryRepository
                                .get(c.getLong(c.getColumnIndex(ROW_PLACES_PLACE_CATEGORY)));
                        places.add(new Place(
                                c.getLong(c.getColumnIndex(ID)),
                                c.getString(c.getColumnIndex(ROW_PLACES_NAME)),
                                c.getString(c.getColumnIndex(ROW_PLACES_DESCRIPTION)),
                                c.getString(c.getColumnIndex(ROW_PLACES_PLACE_NAME)),
                                c.getString(c.getColumnIndex(ROW_PLACES_ADDRESS)),
                                c.getString(c.getColumnIndex(ROW_PLACES_COORDINATE)),
                                c.getLong(c.getColumnIndex(ROW_PLACES_PRICE)),
                                c.getString(c.getColumnIndex(ROW_PLACES_IMG)),
                                c.getString(c.getColumnIndex(ROW_PLACES_TYPEOFGROUP)),
                                placeCategory));
                        c.moveToNext();
                    } while (!c.isAfterLast());
                }else {
                    Log.e(TAG, "getPlacesFromDay: error with select");
                }
                c.close();
            }

            return places;
        }
        public static void deletePlaceInDay(Day day,Place place){
            database.delete(TABLE_PLACE_IN_DAYS, ROW_PLACE_IN_DAYS_ID_DAY + " = " + day.getId()
                    +" and " + ROW_PLACE_IN_DAYS_ID_PLACE + " = "+place.getId(), null);
        }
    }

    /**
     * Класс отвечающий за создание базы данных
     */
    private class myDataBaseHelper extends SQLiteOpenHelper{

        private static final String TAG = "DataBase.myDataBaseHelper";
        private static final String DATA_BASE_NAME = "PersonManagersDataBase";
        private static final int DATA_BASE_VERSION = 1;

        myDataBaseHelper(Context context) {
            super(context, DATA_BASE_NAME, null, DATA_BASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            createTable(db);
        }

        private void createTable(SQLiteDatabase db) {
            db.execSQL("create table " + TABLE_PLACE_CATEGORIES + "("
                    + ID + " integer primary key AUTOINCREMENT NOT NULL, "
                    + ROW_PLACE_CATEGORIES_NAME + " text, "
                    + ROW_PLACE_CATEGORIES_IMG + " text);");

            db.execSQL("create table " + TABLE_PLACES + "("
                    + ID + " integer primary key AUTOINCREMENT NOT NULL, "
                    + ROW_PLACES_NAME + " text, "
                    + ROW_PLACES_IMG + " text, "
                    + ROW_PLACES_PLACE_NAME + " text, "
                    + ROW_PLACES_COORDINATE + " text, "
                    + ROW_PLACES_DESCRIPTION + " text, "
                    + ROW_PLACES_ADDRESS + " text, "
                    + ROW_PLACES_PRICE + " text, "
                    + ROW_PLACES_PLACE_CATEGORY + " integer, "
                    + ROW_PLACES_TYPEOFGROUP + " text" + ");");

            db.execSQL("create table " + TABLE_DAYS + "("
                    + ID + " integer primary key AUTOINCREMENT NOT NULL, "
                    + ROW_DAYS_DATE + " text, "
                    + ROW_DAYS_PLAN_ID + " integer" + ");");

            db.execSQL("create table " + TABLE_PLACE_IN_DAYS + "("
                    + ID + " integer primary key AUTOINCREMENT NOT NULL, "
                    + ROW_PLACE_IN_DAYS_ID_PLACE + " integer, "
                    + ROW_PLACE_IN_DAYS_ID_DAY + " integer" + ");");

            db.execSQL("create table " + TABLE_PLANS + "("
                    + ID + " integer primary key AUTOINCREMENT NOT NULL, "
                    + ROW_PLANS_NAME + " text, "
                    + ROW_PLANS_MONEY + " integer" + ");");

        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            //без реализации обновления базы данных
        }
    }
}
