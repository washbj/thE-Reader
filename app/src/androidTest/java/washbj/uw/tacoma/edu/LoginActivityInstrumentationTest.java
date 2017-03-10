package washbj.uw.tacoma.edu;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Random;

import washbj.uw.tacoma.edu.the_reader.R;
import washbj.uw.tacoma.edu.the_reader.authentication.LoginActivity;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNot.not;


/**
 * Test class for ensuring logins work correctly with instrumentation.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class LoginActivityInstrumentationTest {
    /**
     * A JUnit {@link Rule @Rule} for launching a test copy of LoginActivity.
     */
    @Rule
    public ActivityTestRule<LoginActivity> mActivityRule = new ActivityTestRule<>(LoginActivity.class);


    /**
     * Ensure we're logged out before proceeding with this test.
     */
    @Before
    public void logout() {
        openActionBarOverflowOrOptionsMenu(InstrumentationRegistry.getTargetContext());
        onView(withContentDescription("Log Out"))
                .perform(click());

    }


    /**
     * Tests to ensure user cannot login while leaving the username blank.
     */
    @Test
    public void testLoginNoUsername() {
        String password = "passwordofsufficientlength";

        // Type text and then press the button.
        onView(withId(R.id.enter_username))
                .perform(replaceText(""));
        onView(withId(R.id.enter_password))
                .perform(typeText(password));
        onView(withId(R.id.button_login))
                .perform(click());

        onView(withText("Enter a Username!"))
                .inRoot(withDecorView(not(is(
                        mActivityRule.getActivity()
                                .getWindow()
                                .getDecorView()))))
                .check(matches(isDisplayed()));


    }


    /**
     * Tests to ensure user cannot login with a username that has whitespace in it.
     */
    @Test
    public void testLoginUsernameWhitespace() {
        String username = "user name of sufficent length";
        String password = "passwordofsufficientlength";

        // Type text and then press the button.
        onView(withId(R.id.enter_username))
                .perform(typeText(username));
        onView(withId(R.id.enter_password))
                .perform(typeText(password));
        onView(withId(R.id.button_login))
                .perform(click());

        onView(withText("Username cannot contain whitespace!"))
                .inRoot(withDecorView(not(is(
                        mActivityRule.getActivity()
                                .getWindow()
                                .getDecorView()))))
                .check(matches(isDisplayed()));

    }


    /**
     * Tests to ensure user cannot log in with a username more than 32 characters in length.
     */
    @Test
    public void testLoginUsernameTooLong() {
        String username = "thisusernameiswaytoolongforrealandseriousimeaniwantedtoreadbooksbutthisisjustsilly";
        String password = "passwordofsufficientlength";

        // Type text and then press the button.
        onView(withId(R.id.enter_username))
                .perform(typeText(username));
        onView(withId(R.id.enter_password))
                .perform(typeText(password));
        onView(withId(R.id.button_login))
                .perform(click());

        onView(withText("Username cannot be more than 32 characters in length!"))
                .inRoot(withDecorView(not(is(
                        mActivityRule.getActivity()
                                .getWindow()
                                .getDecorView()))))
                .check(matches(isDisplayed()));

    }


    /**
     * Tests to ensure user cannot log in with a username only. Must have password too.
     */
    @Test
    public void testLoginNoPassword() {
        String username = "usernameofsufficentlength";

        // Type text and then press the button.
        onView(withId(R.id.enter_username))
                .perform(typeText(username));
        onView(withId(R.id.enter_password))
                .perform(replaceText(""));
        onView(withId(R.id.button_login))
                .perform(click());

        onView(withText("Enter a Password!"))
                .inRoot(withDecorView(not(is(
                        mActivityRule.getActivity()
                                .getWindow()
                                .getDecorView()))))
                .check(matches(isDisplayed()));

    }


    /**
     * Tests to ensure user cannot log in with a username more than 32 characters in length.
     */
    @Test
    public void testLoginPasswordWhitespace() {
        Random random = new Random();
        //Generate a username
        String username = "usernameofsufficentlength";

        String password = "password of sufficient length";

        // Type text and then press the button.
        onView(withId(R.id.enter_username))
                .perform(typeText(username));
        onView(withId(R.id.enter_password))
                .perform(typeText(password));
        onView(withId(R.id.button_login))
                .perform(click());

        onView(withText("Password cannot contain whitespace!"))
                .inRoot(withDecorView(not(is(
                        mActivityRule.getActivity()
                                .getWindow()
                                .getDecorView()))))
                .check(matches(isDisplayed()));

    }

}
