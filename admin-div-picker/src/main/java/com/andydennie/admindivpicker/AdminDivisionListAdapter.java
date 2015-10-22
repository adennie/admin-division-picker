package com.andydennie.admindivpicker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

public class AdminDivisionListAdapter extends BaseAdapter {

    private Context context;
    List<AdminDivision> adminDivisions;
    LayoutInflater inflater;

    public AdminDivisionListAdapter(Context context, List<AdminDivision> adminDivisions) {
        super();
        this.context = context;
        this.adminDivisions = adminDivisions;
        inflater = (LayoutInflater) this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return adminDivisions.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int arg0) {
        return 0;
    }

    /**
     * Return row for each admin div
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View cellView = convertView;
        Cell cell;
        AdminDivision adminDivision = adminDivisions.get(position);

        if (convertView == null) {
            cell = new Cell();
            cellView = inflater.inflate(R.layout.row, null);
            cell.textView = (TextView) cellView.findViewById(R.id.row_title);
            cellView.setTag(cell);
        } else {
            cell = (Cell) cellView.getTag();
        }

        cell.textView.setText(adminDivision.getName());

        return cellView;
    }

    /**
     * Holder for the cell
     */
    static class Cell {
        public TextView textView;
    }

}