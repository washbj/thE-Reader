package washbj.uw.tacoma.edu.the_reader.functionality;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import washbj.uw.tacoma.edu.the_reader.R;

/**
 * Displays opening information about some book within the parent Shelf Activity's ViewPager,
 * including the title and cover image (if there is one). Also provides buttons for opening
 * said book, or opening the related Settings Activity.
 */
public class ViewBookFragment extends Fragment {
    private OnFragmentInteractionListener mListener;

    /**
     * The Shelf SharedPreferences. Here is stored the technical information on
     * this particular book, including its paths and title.
     */
    private SharedPreferences mShelfSharedPreferences;

    /**
     * This book's position in Shelf Activity's list of books.
     * Important so it knows which entries to access in the
     * SharedPreferences database.
     */
    int mPosition;

    /** The title of the book. */
    TextView mTitle;

    /** The cover image of the book. */
    ImageView mImage;

    /** The path to the cover image. */
    String mImagePath;

    /** Clicking this will open the book in the Read Activity. */
    Button mButtonOpen;

    /** Clicking this will open the Settings Activity for the book. */
    Button mButtonSettings;

    public ViewBookFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ViewBookFragment.
     */
    public static ViewBookFragment newInstance() {
        ViewBookFragment fragment = new ViewBookFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_book, container, false);

        mShelfSharedPreferences = getActivity().getSharedPreferences(getString(R.string.BOOK_SHELF),
                                                                        Context.MODE_PRIVATE);

        Bundle bundle = this.getArguments();
        if (bundle != null) { mPosition = bundle.getInt("position"); }
        else { mPosition = 0; }

        // Gets the title of the book.
        mTitle = (TextView) view.findViewById(R.id.book_title);
        mTitle.setText(mShelfSharedPreferences.getString(getString(R.string.BOOK_TAG) + mPosition + "_title", "(No Title)"));

        // Gets the cover image from the provided path, if there is one, and displays it for the user.
        mImage = (ImageView) view.findViewById(R.id.book_cover);
        mImagePath = mShelfSharedPreferences.getString(getString(R.string.BOOK_TAG) + mPosition + "_imagepath", "NO_IMAGE_PATH");
        if (!mImagePath.equals("NO_IMAGE_PATH")) { mImage.setImageURI(Uri.parse(mImagePath)); }

        // Opens the displayed book by launching its Read Activity.
        mButtonOpen = (Button) view.findViewById(R.id.button_open_book);
        mButtonOpen.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intentRead = new Intent(getActivity(), ReadActivity.class);
                intentRead.putExtra("position", mPosition);
                startActivity(intentRead);
            }
        });

        // Opens the book's Settings Activity.
        mButtonSettings = (Button) view.findViewById(R.id.button_settings);
        mButtonSettings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((ShelfActivity) getActivity()).openSettings();
            }
        });

        return view;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity an,d potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
