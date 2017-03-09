/*
 * Justin Washburn and Michael Scott
 *  TCSS 450
 *  Swellest Reader version 1
 *
 *
 */

package washbj.uw.tacoma.edu.the_reader.functionality;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import washbj.uw.tacoma.edu.the_reader.R;

/**
 * Fragment to add a book
 */
public class AddBookFragment extends Fragment {
    /**
     * Fragment listener
     */
    private OnFragmentInteractionListener mListener;

    /**
     * Button for adding a book
     */
    private Button mButton;

    public AddBookFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment AddBookFragment.
     */
    public static AddBookFragment newInstance() {
        AddBookFragment fragment = new AddBookFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }


     // General onCreateView method

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_book, container, false);

        mButton = (Button) view.findViewById(R.id.button_add_book);
        mButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                ((ShelfActivity) getActivity()).selectFile();
            }

        });

        return view;

    }

  //General onAttach
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

    // General onDetach
    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
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
