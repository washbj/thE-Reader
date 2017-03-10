package washbj.uw.tacoma.edu.the_reader;

import org.junit.Test;

import washbj.uw.tacoma.edu.the_reader.authentication.NewUserActivity;

import static org.junit.Assert.assertEquals;


/**
 * Test class for the logic of the login class
 */
public class NewUserActivityTest {

    /**
     * Tests for leaving out a username
     */
    @Test
    public void testvalidateUserNoUsername() {
        assertEquals("Username is empty", NewUserActivity.USER_NO_USERNAME,
                NewUserActivity.validateUser("", "prettyokaypassword"));
    }

    /**
     * Tests for whitespace in username
     */
    @Test
    public void testvalidateUsernameWhitespace() {
        assertEquals("Username has whitespace", NewUserActivity.USER_USERNAME_WHITESPACE,
                NewUserActivity.validateUser("bad username", "prettyokaypassword"));
    }

    /**
     * Tests for a username that is too long
     */
    @Test
    public void testvalidateUsernameTooLong() {
        assertEquals("Username too long", NewUserActivity.USER_USERNAME_TOO_LONG,
                NewUserActivity.validateUser(
                        "wowthisisareallylongpasswordIWouldsayitisdefinitelymorethan32orsocharacterswoooweee",
                        "prettyokaypassword"));
    }

    /**
     * Tests for leaving out a password
     */
    @Test
    public void testvalidateUserNoPassword() {
        assertEquals("Password is empty", NewUserActivity.USER_NO_PASSWORD,
                NewUserActivity.validateUser("prettyokayusername", ""));
    }

    /**
     * Tests for whitespace in password
     */
    @Test
    public void testvalidatePasswordWhitespace() {
        assertEquals("Password has whitespace", NewUserActivity.USER_PASSWORD_WHITESPACE,
                NewUserActivity.validateUser("prettyokayusername", "bad password"));
    }

    /**
     * Tests for leaving out a username
     */
    @Test
    public void testvalidateSuccesfulUsername() {
        assertEquals("Username test succesful", NewUserActivity.USER_SUCCESS,
                NewUserActivity.validateUser("prettyokayusername", "prettyokaypassword"));
    }

}


