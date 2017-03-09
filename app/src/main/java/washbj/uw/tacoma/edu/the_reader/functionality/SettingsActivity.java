package washbj.uw.tacoma.edu.the_reader.functionality;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import washbj.uw.tacoma.edu.the_reader.R;

/**
 * Displays various settings for some book. Lets the user edit its title,
 * cover image, background appearance, text size, and font.
 */
public class SettingsActivity extends AppCompatActivity {
    int mPosition;

    /**
     * SharedPreferences for storing technical information on the given book,
     * including its title and paths.
     */
    private SharedPreferences mShelfSharedPreferences;

    /**
     * SharedPreferences for storing visual information on the given book, including
     * its background, text size, and font.
     */
    private SharedPreferences mVisualSharedPreferences;

    /** EditText for changing the book's title. */
    EditText mEditTitle;

    /** Press this to pick an image file for the book's cover. */
    ImageButton mEditCover;

    /** The path to the cover image. */
    String mImagePath;

    /** Press this to save the changes. */
    Button mButtonSave;

    /** A TextView for displaying some example text, so the user can see their selected text size and font. */
    TextView mTestText;

    /** A Spinner for selecting the font size. */
    Spinner mSpinnerFontSize;

    /** A Spinner for selecting the typeface. */
    Spinner mSpinnerTypeface;

    /**
     * The font-size of the text. Not the actual size. Rather, should be used to select a size from ReadActivity's
     * TEXT_SIZES array.
     */
    int mTextFontSize;

    /**
     * The typeface of the text. Not the actual typeface. Rather, should be used to select a typeface
     * from ReadActivity's TYPEFACES array.
     */
    int mTextTypeface;

    /**
     * The background of the book. Like above, it isn't the actual background, but should be used to
     * select an entry in ReadActivity's BACKGROUNDS array.
     */
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

        // Open a file picker to pick a cover image.
        mEditCover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent fileIntent = new Intent(Intent.ACTION_GET_CONTENT);
                fileIntent.setType("*/*");
                startActivityForResult(fileIntent, ShelfActivity.FILE_PICKED_RESULT);
            }
        });

        // Save changes and close the Settings Activity.
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

        // Spinner for selecting the font size.
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


        // Spinner for selecting the typeface.
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


        // Spinner for selecting the background.
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


    /**
     * Update's the example text when the user selects a new size or font.
     */
    private void updateText() {
        mTestText.setTextSize(ReadActivity.TEXT_SIZES[mTextFontSize]);
        mTestText.setTypeface(ReadActivity.TYPEFACES[mTextTypeface]);
    }


    /**
     * Updates the SharedPreferences with all the selected settings.
     */
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


    /**
     * Gets the URI for the selected image, if the user happened to be picking files.
     *
     * {@inheritDoc}
     */
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
