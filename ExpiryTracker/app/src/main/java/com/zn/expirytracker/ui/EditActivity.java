package com.zn.expirytracker.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.zn.expirytracker.R;
import com.zn.expirytracker.data.model.InputType;

import java.util.List;

import butterknife.ButterKnife;

public class EditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        if (intent != null) {
            long id = intent.getLongExtra(EditFragment.ARG_ITEM_ID_LONG, 0);
            String barcode = intent.getStringExtra(EditFragment.ARG_BARCODE_STRING);
            InputType inputType = (InputType) intent.getSerializableExtra(
                    EditFragment.ARG_INPUT_TYPE);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container_edit_fragment,
                            EditFragment.newInstance(id, barcode, inputType),
                            EditFragment.class.getSimpleName())
                    .commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Emulate the back button when pressing the up button, to prevent parent activity from
            // getting recreated
            // https://stackoverflow.com/questions/22947713/make-the-up-button-behave-like-the-back-button-on-android
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments.size() == 0) {
            super.onBackPressed();
        } else {
            Fragment topFragment = fragments.get(fragments.size() - 1);
            if (topFragment instanceof EditFragment &&
                    ((EditFragment) topFragment).haveFieldsChanged()) {
                // don't do anything. at this point the EditFragment is showing the
                // FormChangedDialog
                return;
            }
            if (fragments.size() > 1) {
                // Handle cases where Glide somehow is the top fragment instead
                Fragment secondToTopFragment = fragments.get(fragments.size() - 2);
                if (secondToTopFragment instanceof EditFragment &&
                        ((EditFragment) secondToTopFragment).haveFieldsChanged()) {
                    return;
                }
            }
            super.onBackPressed();
        }
    }
}
