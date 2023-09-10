package my.edu.utar.grp_nav;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class CarpoolAdapter extends ArrayAdapter<CarpoolData> {

    private Context context;
    private List<CarpoolData> data;

    public CarpoolAdapter(Context context, List<CarpoolData> data) {
        super(context, R.layout.carpool_offer_item_list, data);
        this.context = context;
        this.data = data;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.carpool_offer_item_list, parent, false);

        LinearLayout ll = rowView.findViewById(R.id.llCarpoolOffer);
        ImageView dropdown = rowView.findViewById(R.id.item_dropDown);
        TextView itemText = rowView.findViewById(R.id.itemText);
        TextView detailText = rowView.findViewById(R.id.detailText);
        Button bookButton = rowView.findViewById(R.id.bookButton);

        CarpoolData item = data.get(position);

        itemText.setText("Name: " + item.getName() +
                "\nPrice: RM" + item.getPrice() +
                "\nDriver From: " + item.getDistance());
        detailText.setText("Phone No: +60" + item.getPhone() +
                "\nDate: " + item.getDate() +
                "\nTime: " + item.getTime() +
                "\nCar Model: " + item.getCarModel() +
                "\nCar Color: " + item.getCarColor() +
                "\nLicense Plate: " + item.getLicensePlate());

        dropdown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (detailText.getVisibility() == View.GONE) {
                    detailText.setVisibility(View.VISIBLE);
                    bookButton.setVisibility(View.VISIBLE);
                    dropdown.setImageResource(R.drawable.ic_collapse);
                } else {
                    detailText.setVisibility(View.GONE);
                    bookButton.setVisibility(View.GONE);
                    dropdown.setImageResource(R.drawable.ic_expand);
                }
            }
        });

        ll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (detailText.getVisibility() == View.GONE) {
                    detailText.setVisibility(View.VISIBLE);
                    bookButton.setVisibility(View.VISIBLE); // Also show the Book button
                } else {
                    detailText.setVisibility(View.GONE);
                    bookButton.setVisibility(View.GONE);   // Also hide the Book button
                }
            }
        });

        // Set on click listener for bookButton to show booking confirmation
        bookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // To show the booking confirmation, we'll initiate a callback to the activity
                if(context instanceof CarpoolOffer) {
                    ((CarpoolOffer) context).showBookingConfirmation(position);
                }

            }
        });

        return rowView;
    }

}
