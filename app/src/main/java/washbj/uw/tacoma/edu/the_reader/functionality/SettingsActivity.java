package washbj.uw.tacoma.edu.the_reader.functionality;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.net.URLEncoder;

import washbj.uw.tacoma.edu.the_reader.R;

import static android.icu.util.ULocale.getName;

public class SettingsActivity extends AppCompatActivity {
    int mPosition;

    private SharedPreferences mShelfSharedPreferences;
    private SharedPreferences mVisualSharedPreferences;

    EditText mEditTitle;

    ImageButton mEditCover;
    String mImagePath;

    Button mButtonSave;
    Button mButtonDiscard;

    TextView mTestText;
    Spinner mSpinnerFontSize;
    Spinner mSpinnerTypeface;

    int mTextFontSize;
    int mTextTypeface;
    int mBackground;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mPosition = getIntent().getIntExtra("position", 0);

        mShelfSharedPreferences = getSharedPreferences(getString(R.string.BOOK_SHELF), Context.MODE_PRIVATE);
        mVisualSharedPreferences = getSharedPreferences(getString(R.string.VISUAL_PREFS), Context.MODE_PRIVATE);

        mEditTitle = (EditText) findViewById(R.id.edit_title);
        mEditTitle.setText(mShelfSharedPreferences.getString(getString(R.string.BOOK_TAG) + mPosition + "_title", "(No Title)"));

        mEditCover = (ImageButton) findViewById(R.id.edit_cover);
        mImagePath = mShelfSharedPreferences.getString(getString(R.string.BOOK_TAG) + mPosition + "_imagepath", "NO_IMAGE_PATH");
        if (!mImagePath.equals("NO_IMAGE_PATH")) { mEditCover.setImageURI(Uri.parse(mImagePath)); }

        mEditCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                fileIntent.setType("*/*");
                startActivityForResult(fileIntent, ShelfActivity.FILE_PICKED_RESULT);
            }
        });

        mButtonSave = (Button) findViewById(R.id.button_save_changes);
        mButtonSave.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                updateSettings();
                finish();
            }
        });

        mTestText = (TextView) findViewById(R.id.text_appearance);
        mTextFontSize = mVisualSharedPreferences.getInt(getString(R.string.VP_TEXTSIZE) + mPosition, 0);
        mTextTypeface = mVisualSharedPreferences.getInt(getString(R.string.VP_TYPEFACE) + mPosition, 0);
        mBackground = mVisualSharedPreferences.getInt(getString(R.string.VP_BACKGROUND) + mPosition, 0);
        updateText();
        final FrameLayout layout = (FrameLayout) findViewById(R.id.activity_settings);
        layout.setBackgroundResource(ReadActivity.BACKGROUNDS[mBackground]);

        mSpinnerFontSize = (Spinner) findViewById(R.id.spinner_font_size);
        mSpinnerFontSize.setAdapter(ArrayAdapter.createFromResource(this, R.array.spinarray_fontsize,
                                    R.layout.spinner_item));
        mSpinnerFontSize.setSelection(mTextFontSize);
        mSpinnerFontSize.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mTextFontSize = position;
                updateText();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });


        mSpinnerTypeface = (Spinner) findViewById(R.id.spinner_typeface);
        mSpinnerTypeface.setAdapter(ArrayAdapter.createFromResource(this, R.array.spinarray_typeface,
                R.layout.spinner_item));
        mSpinnerTypeface.setSelection(mTextTypeface);
        mSpinnerTypeface.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mTextTypeface = position;
                updateText();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

        mSpinnerTypeface = (Spinner) findViewById(R.id.spinner_background);
        mSpinnerTypeface.setAdapter(ArrayAdapter.createFromResource(this, R.array.spinarray_background,
                R.layout.spinner_item));
        mSpinnerTypeface.setSelection(mBackground);
        mSpinnerTypeface.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mBackground = position;
                layout.setBackgroundResource(ReadActivity.BACKGROUNDS[mBackground]);

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }

        });

    }


    private void updateText() {
        mTestText.setTextSize(ReadActivity.TEXT_SIZES[mTextFontSize]);
        mTestText.setTypeface(ReadActivity.TYPEFACES[mTextTypeface]);
    }


    private void updateSettings() {
        SharedPreferences.Editor spEditor = mShelfSharedPreferences.edit();
        spEditor.putString(getString(R.string.BOOK_TAG) + mPosition + "_title", mEditTitle.getText().toString());
        spEditor.putString(getString(R.string.BOOK_TAG) + mPosition + "_imagepath", mImagePath);
        spEditor.commit();

        spEditor = mVisualSharedPreferences.edit();
        spEditor.putInt(getString(R.string.VP_TEXTSIZE) + mPosition, mTextFontSize);
        spEditor.putInt(getString(R.string.VP_TYPEFACE) + mPosition, mTextTypeface);
        spEditor.putInt(getString(R.string.VP_BACKGROUND) + mPosition, mBackground);
        spEditor.commit();

        Toast.makeText(this, "Settings updated!", Toast.LENGTH_SHORT);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        String fullPath = "";
        Uri uri;
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ShelfActivity.FILE_PICKED_RESULT) {
            if (resultCode == RESULT_OK) {
                uri = data.getData();
                try {
                    mEditCover.setImageURI(uri);
                    mImagePath = uri.toString();
                } catch (Exception e) {
                    Log.e("FileLoad", e.getMessage());

                }

            }

        }

    }

}
