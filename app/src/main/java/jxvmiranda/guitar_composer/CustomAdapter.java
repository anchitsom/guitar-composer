package jxvmiranda.guitar_composer;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import android.widget.Toast;

import java.util.ArrayList;

/**
 * Created by abhinavgoyal on 19/11/2017.
 */

public class CustomAdapter extends BaseAdapter {
    String[] names;
    Context context;
    LayoutInflater inflter;
    String value;
    ArrayList<String> file_list = new ArrayList<>();

    public CustomAdapter(Context context, String[] names) {
        this.context = context;
        this.names = names;
        inflter = (LayoutInflater.from(context));
    }

    @Override
    public int getCount() {
        return names.length;
    }

    @Override
    public Object getItem(int i) {
        return null;
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = inflter.inflate(R.layout.lister, null);
        final CheckedTextView simpleCheckedTextView = (CheckedTextView) view.findViewById(R.id.simpleCheckedTextView);
        simpleCheckedTextView.setText(names[position]);
        // perform on Click Event Listener on CheckedTextView
        simpleCheckedTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (simpleCheckedTextView.isChecked()) {
                    // set check mark drawable and set checked property to false
                    value = "un-Checked";
                    Log.d("a", "onRemove:" + file_list.indexOf((String) simpleCheckedTextView.getText()));
                    file_list.remove(file_list.indexOf((String) simpleCheckedTextView.getText()));
                    simpleCheckedTextView.setCheckMarkDrawable(0);
                    simpleCheckedTextView.setChecked(false);
                } else {
                    // set cheek mark drawable and set checked property to true
                    value = "Checked";
                    Log.d("a", "onAdd:" + simpleCheckedTextView.getText());
                    file_list.add((String) simpleCheckedTextView.getText());
                    simpleCheckedTextView.setCheckMarkDrawable(R.drawable.checked);
                    simpleCheckedTextView.setChecked(true);
                }
                Toast.makeText(context, value, Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }


}


