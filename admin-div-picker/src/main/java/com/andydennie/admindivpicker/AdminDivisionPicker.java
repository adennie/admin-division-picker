package com.andydennie.admindivpicker;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v7.app.AppCompatDialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.EditText;
import android.widget.ListView;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class AdminDivisionPicker extends AppCompatDialogFragment {

    private static final String ARG_DIALOG_TITLE = "dialogTitle";
    private static final String ARG_COUNTRY_CODE = "countryCode";

    private EditText searchEditText;
    private ListView AdminDivsListView;
    private AdminDivisionListAdapter adapter;
    private AdminDivisionPickerListener listener;
    private AdminDivision preselectedAdminDivision;
    private List<AdminDivision> allAdminDivsByName;
    private NameComparator nameComparator = new NameComparator();
    private String countryCode;

    // admin divs that matched user query
    private List<AdminDivision> selectedAdminDivsList;

    public static AdminDivisionPicker newInstance(String CountryCode) {
        AdminDivisionPicker picker = new AdminDivisionPicker();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_COUNTRY_CODE, CountryCode);
        picker.setArguments(bundle);
        return picker;
    }

    public static AdminDivisionPicker newInstance(String CountryCode, String dialogTitle) {
        AdminDivisionPicker picker = new AdminDivisionPicker();
        Bundle bundle = new Bundle();
        bundle.putString(ARG_DIALOG_TITLE, dialogTitle);
        bundle.putString(ARG_COUNTRY_CODE, CountryCode);
        picker.setArguments(bundle);
        return picker;
    }

    /**
     * Create view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        Bundle args = getArguments();
        if (args != null) {
            countryCode = args.getString(ARG_COUNTRY_CODE);
            if (countryCode == null) {
                throw new IllegalArgumentException("country code must be provided in Fragment's argument bundle");
            }

            // Set dialog title if title provided in args
            String dialogTitle = args.getString(ARG_DIALOG_TITLE);
            if (dialogTitle != null) {
                getDialog().setTitle(dialogTitle);
            }
        }

        // Get admin divs from the json
        loadAdminDivs();

        // Inflate view
        View view = inflater.inflate(R.layout.admin_division_picker, null);

        EditText search = (EditText) (view.findViewById(R.id.search));

        // tint the search icon if the theme specifies colorControlNormal
        final TypedValue value = new TypedValue();
        if (getContext().getTheme().resolveAttribute(R.attr.colorControlNormal, value, true)) {
            Drawable searchIcon = search.getCompoundDrawables()[0];
            DrawableCompat.setTint(searchIcon, value.data);
        }

        // Get view components
        searchEditText = (EditText) view.findViewById(R.id.search);
        AdminDivsListView = (ListView) view.findViewById(R.id.pickerListview);

        // Set adapter
        adapter = new AdminDivisionListAdapter(getActivity(), selectedAdminDivsList);
        AdminDivsListView.setAdapter(adapter);

        // if a preselected admin div was specified, select it
        preselectAdminDiv();

        // Inform listener
        AdminDivsListView.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                if (listener != null) {
                    AdminDivision adminDivision = selectedAdminDivsList.get(position);
                    listener.onAdminDivisionSelected(adminDivision);
                }
            }
        });

        // Search for which admin divs matched user query
        searchEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                search(s.toString());
            }
        });

        return view;
    }

    public void setListener(AdminDivisionPickerListener listener) {
        this.listener = listener;
    }

    /**
     * Specify an admin div to be preselected in the list.  The argument must have either a
     * non-null code or name; either is sufficient.
     *
     * @param adminDivision to preselect
     */
    public void setPreselectedAdminDivision(AdminDivision adminDivision) {
        preselectedAdminDivision = adminDivision;
    }

    private void loadAdminDivs() {
        try {
            allAdminDivsByName = new ArrayList<>();

            // Parse resource string containing data in JSON format
            JSONObject jsonObject = new JSONObject(getAdminDivsString());
            Iterator<?> keys = jsonObject.keys();

            // Add the data to all admin divs list
            while (keys.hasNext()) {
                String key = (String) keys.next();
                AdminDivision adminDivision = new AdminDivision(key, jsonObject.getString(key));
                allAdminDivsByName.add(adminDivision);
            }

            // Sort the lists
            Collections.sort(allAdminDivsByName, nameComparator);

            // Initialize selected admin divs with all admin divs
            selectedAdminDivsList = new ArrayList<>();
            selectedAdminDivsList.addAll(allAdminDivsByName);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getAdminDivsString() {
        // The string resources contain values named after country codes.  Each value is a json
        // string which is a Base64 encoded (to avoid special characters in XML) JSON structure
        // containing that country's administrative divisions.
        Resources resources = getActivity().getResources();
        String base64 = resources.getString(resources.getIdentifier(countryCode, "string",
                getActivity().getPackageName()));

        byte[] data = Base64.decode(base64, Base64.DEFAULT);
        try {
            return new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void preselectAdminDiv() {
        if (preselectedAdminDivision != null) {
            if (preselectedAdminDivision.getName() != null) {
                AdminDivsListView.setSelection(
                        Collections.binarySearch(allAdminDivsByName, preselectedAdminDivision, nameComparator));
            } else if (preselectedAdminDivision.getCode() != null) {
                // brute force, but still fast enough to not matter
                int pos = 0;
                for (AdminDivision adminDivision : allAdminDivsByName) {
                    if (adminDivision.getCode().equals(preselectedAdminDivision.getCode())) {
                        AdminDivsListView.setSelection(pos);
                        break;
                    }
                    pos++;
                }
            }
        }
    }

    @SuppressLint("DefaultLocale")
    private void search(String text) {
        selectedAdminDivsList.clear();

        for (AdminDivision adminDivision : allAdminDivsByName) {
            if (adminDivision.getName().toLowerCase(Locale.ENGLISH).contains(text.toLowerCase())) {
                selectedAdminDivsList.add(adminDivision);
            }
        }

        adapter.notifyDataSetChanged();
    }

    private static class NameComparator implements Comparator<AdminDivision> {

        @Override
        public int compare(AdminDivision lhs, AdminDivision rhs) {
            return lhs.getName().compareTo(rhs.getName());
        }
    }
}
