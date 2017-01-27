package ru.osll.goodtravel.ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import io.realm.Realm;
import ru.osll.goodtravel.R;
import ru.osll.goodtravel.models.CategoryOfService;
import ru.osll.goodtravel.models.Service;
import ru.osll.goodtravel.ui.activities.RouteMakerActivity;
import ru.osll.goodtravel.bundles.RouteMakerInfoBundle;
import ru.osll.goodtravel.models.TravelPlace;
import ru.osll.goodtravel.utils.DBHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by artem96 on 13.10.16.
 */

public class MakerTravelListFragment extends BaseFragment {

    RouteMakerInfoBundle routeInfo;
    RouteMakerActivity maker;

    RecyclerView recyclerView;

    public static List<Service> serviceList;

    public MakerTravelListFragment(RouteMakerActivity maker, RouteMakerInfoBundle routeInfo) {
        this.routeInfo = routeInfo;
        this.maker = maker;
    }
    public MakerTravelListFragment(){

    }

    public static MakerTravelListFragment createInstance(
            RouteMakerActivity maker, RouteMakerInfoBundle routeInfo) {
        MakerTravelListFragment fragment = new MakerTravelListFragment(maker, routeInfo);
        serviceList = new ArrayList<>();

        // here we can add some information with bundle class

        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View v = (View) inflater.inflate(R.layout.maker_travel_list_fragment, container, false);
        recyclerView = (RecyclerView) v.findViewById(R.id.route_maker_list);
        recyclerView.setNestedScrollingEnabled(false);

        Log.e("LIST FRAGMENT","Places: " + maker.getFakePlaces().size());

        return v;
    }

    @Override
    public void request()
    {
        serviceList.clear();

        Realm realm = DBHelper.getInstance();

        final List<Service> serviceArrayList = new ArrayList<>();

        for(int i = 0; i < RouteMakerActivity.categoryOfServiceList.size(); i++)
        {
            serviceArrayList.addAll(Service.getServices(realm, RouteMakerActivity.categoryOfServiceList.get(i), RouteMakerActivity.progress));
        }

        TravelListAdapter adapter = new TravelListAdapter(new ArrayList<>(serviceArrayList));

        adapter.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l)
            {
                Service selectedService = serviceArrayList.get(i);

                if(serviceList.contains(selectedService))
                {
                    serviceList.remove(selectedService);
                }
                else
                {
                    serviceList.add(selectedService);
                }
            }
        });

        recyclerView.setAdapter(adapter);

        LinearLayoutManager manager = new LinearLayoutManager(getActivity());
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
    }

    private class TravelListItemHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        private ImageView placePicture;
        private TextView placeTitle;
        private TextView placeDescription;
        private TextView averageBill;
        private AdapterView.OnItemClickListener onItemClickListener;

        TravelListItemHolder(View view) {
            super(view);

            placePicture = (ImageView) view.findViewById(R.id.maker_list_event_image);
            placeTitle = (TextView) view.findViewById(R.id.maker_list_event_title);
            placeDescription = (TextView) view.findViewById(R.id.maker_list_event_description);
            averageBill = (TextView) view.findViewById(R.id.maker_list_event_average_bill);

            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View view)
        {
            view = view.findViewById(R.id.tickImageView);

            if(view.getVisibility() == View.GONE)
            {
                view.setVisibility(View.VISIBLE);
            }
            else
            {
                view.setVisibility(View.GONE);
            }

            if(onItemClickListener != null)
                onItemClickListener.onItemClick(null, view, getAdapterPosition(), getAdapterPosition());
        }

        public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener)
        {
            this.onItemClickListener = onItemClickListener;
        }
    }

    private class TravelListAdapter extends RecyclerView.Adapter<TravelListItemHolder> {

        private List<Service> services;
        private AdapterView.OnItemClickListener onItemClickListener;

        TravelListAdapter(List<Service> services) {

            if (services == null) {
                throw new IllegalArgumentException("places must not be null");
            }

            this.services = services;
        }

        @Override
        public TravelListItemHolder onCreateViewHolder(ViewGroup viewHolder, int viewType) {

            View view = LayoutInflater.from(viewHolder.getContext()).
                    inflate(R.layout.maker_travel_list_item, viewHolder, false);

            return new TravelListItemHolder(view);
        }

        @Override
        public void onBindViewHolder(TravelListItemHolder itemHolder, int position) {

            Service model = services.get(position);

            itemHolder.placeTitle.setText(model.getName());
            itemHolder.placeDescription.setText(model.getName());
            itemHolder.averageBill.setText(model.getPrice()+" Руб");
            itemHolder.setOnItemClickListener(onItemClickListener);
            if(model.getSrcToImg() != null && !model.getSrcToImg().isEmpty())
            {
                Picasso
                        .with(getContext())
                        .load(model.getSrcToImg())
                        .into(itemHolder.placePicture);
            }
            else
            {
                itemHolder.placePicture.setImageResource(android.R.drawable.sym_def_app_icon);
            }
        }

        public void setOnItemClickListener(AdapterView.OnItemClickListener onItemClickListener)
        {
            this.onItemClickListener = onItemClickListener;
        }

        @Override
        public int getItemCount() {
            return services.size();
        }
    }
}
