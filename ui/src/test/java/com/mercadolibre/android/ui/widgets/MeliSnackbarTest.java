package com.mercadolibre.android.ui.widgets;

import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mercadolibre.android.testing.AbstractRobolectricTest;
import com.mercadolibre.android.ui.R;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.util.ReflectionHelpers;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.shadows.support.v4.SupportFragmentTestUtil.startFragment;


/**
 * Test class for {@link MeliSnackbar}.
 *
 * @since 25/2/16
 */
@SuppressWarnings("CPD-START")
public class MeliSnackbarTest extends AbstractRobolectricTest {

    private static final String TEST_STRING = "This is a test";
    private static final String ACTION_STRING = "action";

    private View view;

    @Before
    public void setUp() {
        // We just need a view.
        final Fragment fragment = new DummyMeliDialog();
        startFragment(fragment);
        view = fragment.getView();
    }

    @Test
    public void testMakeWithoutAction() {
        final MeliSnackbar snackbar = MeliSnackbar.make(view, TEST_STRING, Snackbar.LENGTH_SHORT);

        Assert.assertNotNull(snackbar);
        Assert.assertEquals(TEST_STRING, getTextView(snackbar).getText());
        Assert.assertEquals(Snackbar.LENGTH_SHORT, getSnackbar(snackbar).getDuration());
    }

    @Test
    public void testSetText() {
        final MeliSnackbar snackbar = MeliSnackbar.make(view, TEST_STRING, Snackbar.LENGTH_INDEFINITE);

        Assert.assertNotNull(snackbar);
        Assert.assertEquals(TEST_STRING, getTextView(snackbar).getText());

        final String testString = "This is a test 2";
        snackbar.setText(testString);

        Assert.assertEquals(testString, getTextView(snackbar).getText());
    }

    @Test
    public void testSetAction() {
        final MeliSnackbar snackbar = MeliSnackbar.make(view, TEST_STRING, Snackbar.LENGTH_INDEFINITE);

        Assert.assertNotNull(snackbar);
        Assert.assertEquals("", getActionButton(snackbar).getText());

        snackbar.setAction(ACTION_STRING, new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                // Do nothing.
            }
        });

        Assert.assertEquals(ACTION_STRING, getActionButton(snackbar).getText());
    }

    @Test
    public void testSetTextColor() {
        final MeliSnackbar snackbar = MeliSnackbar.make(view, TEST_STRING, Snackbar.LENGTH_INDEFINITE);

        int color = ContextCompat.getColor(RuntimeEnvironment.application, R.color.ui_meli_red);

        Assert.assertNotNull(snackbar);
        Assert.assertNotSame(color, getTextView(snackbar).getTextColors().getDefaultColor());

        snackbar.setTextColor(R.color.ui_meli_red);

        Assert.assertEquals(color, getTextView(snackbar).getTextColors().getDefaultColor());
    }

    @Test
    public void testSetActionTextColor() {
        final MeliSnackbar snackbar = MeliSnackbar.make(view, TEST_STRING, Snackbar.LENGTH_INDEFINITE);

        int color = ContextCompat.getColor(RuntimeEnvironment.application, R.color.ui_meli_red);

        Assert.assertNotNull(snackbar);
        Assert.assertNotSame(color, getActionButton(snackbar).getTextColors().getDefaultColor());

        snackbar.setActionTextColor(R.color.ui_meli_red);

        Assert.assertEquals(color, getActionButton(snackbar).getTextColors().getDefaultColor());
    }

    @Test
    public void testSetTextSize() {
        final MeliSnackbar snackbar = MeliSnackbar.make(view, TEST_STRING, Snackbar.LENGTH_INDEFINITE);
        final float newSize = 18;

        Assert.assertNotSame(newSize, getTextView(snackbar).getTextSize());

        snackbar.setTextSize(newSize);

        Assert.assertEquals(newSize, getTextView(snackbar).getTextSize(), 0);
    }

    @Test
    public void testSetActionTextSize() {
        final MeliSnackbar snackbar = MeliSnackbar.make(view, TEST_STRING, Snackbar.LENGTH_INDEFINITE);
        final float newSize = 18;

        Assert.assertNotSame(newSize, getActionButton(snackbar).getTextSize());

        snackbar.setActionTextSize(newSize);

        Assert.assertEquals(newSize, getActionButton(snackbar).getTextSize(), 0);
    }

    @Test
    public void testSetBackground() {
        final MeliSnackbar snackbar = MeliSnackbar.make(view, TEST_STRING, Snackbar.LENGTH_INDEFINITE);

        final int color = ContextCompat.getColor(RuntimeEnvironment.application, R.color.ui_meli_red);

        Assert.assertNotNull(snackbar);
        Assert.assertNotSame(color, getBackgroundColor(snackbar));

        snackbar.setBackgroundColor(R.color.ui_meli_red);

        Assert.assertEquals(color, getBackgroundColor(snackbar));
    }

    @Test
    public void testMessageType() {
        final MeliSnackbar snackbar = MeliSnackbar.make(view, TEST_STRING, Snackbar.LENGTH_INDEFINITE, MeliSnackbar.Type.MESSAGE)
                                                  .setAction(ACTION_STRING,
                                                             new View.OnClickListener() {
                                                                 @Override
                                                                 public void onClick(View v) {
                                                                     // Do nothing.
                                                                 }
                                                             });

        Assert.assertNotNull(snackbar);
        Assert.assertEquals(TEST_STRING, getTextView(snackbar).getText());
        Assert.assertEquals(Snackbar.LENGTH_INDEFINITE, getSnackbar(snackbar).getDuration());
        // Test background color.
        Assert.assertEquals(ContextCompat.getColor(RuntimeEnvironment.application, R.color.ui_meli_black), getBackgroundColor(snackbar));
        // Test text color.
        Assert.assertEquals(ContextCompat.getColor(RuntimeEnvironment.application, R.color.ui_meli_white), getTextView(snackbar).getTextColors().getDefaultColor());
        // Test action text color.
        Assert.assertEquals(ContextCompat.getColor(RuntimeEnvironment.application, R.color.ui_meli_white), getActionButton(snackbar).getTextColors().getDefaultColor());
    }

    @Test
    public void testSuccessType() {
        final MeliSnackbar snackbar = MeliSnackbar.make(view, TEST_STRING, Snackbar.LENGTH_INDEFINITE, MeliSnackbar.Type.SUCCESS)
                                                  .setAction(ACTION_STRING,
                                                             new View.OnClickListener() {
                                                                 @Override
                                                                 public void onClick(View v) {
                                                                     // Do nothing.
                                                                 }
                                                             });

        Assert.assertNotNull(snackbar);
        Assert.assertEquals(TEST_STRING, getTextView(snackbar).getText());
        Assert.assertEquals(Snackbar.LENGTH_INDEFINITE, getSnackbar(snackbar).getDuration());
        // Test background color.
        Assert.assertEquals(ContextCompat.getColor(RuntimeEnvironment.application, R.color.ui_meli_success), getBackgroundColor(snackbar));
        // Test text color.
        Assert.assertEquals(ContextCompat.getColor(RuntimeEnvironment.application, R.color.ui_meli_white), getTextView(snackbar).getTextColors().getDefaultColor());
        // Test action text color.
        Assert.assertEquals(ContextCompat.getColor(RuntimeEnvironment.application, R.color.ui_meli_white), getActionButton(snackbar).getTextColors().getDefaultColor());
    }

    @Test
    public void testErrorType() {
        final MeliSnackbar snackbar = MeliSnackbar.make(view, TEST_STRING, Snackbar.LENGTH_INDEFINITE, MeliSnackbar.Type.ERROR)
                                                  .setAction(ACTION_STRING, new View.OnClickListener() {
                                                      @Override
                                                      public void onClick(View v) {
                                                          // Do nothing.
                                                      }
                                                  });

        Assert.assertNotNull(snackbar);
        Assert.assertEquals(TEST_STRING, getTextView(snackbar).getText());
        Assert.assertEquals(Snackbar.LENGTH_INDEFINITE, getSnackbar(snackbar).getDuration());
        // Test background color.
        Assert.assertEquals(ContextCompat.getColor(RuntimeEnvironment.application, R.color.ui_meli_error), getBackgroundColor(snackbar));
        // Test text color.
        Assert.assertEquals(ContextCompat.getColor(RuntimeEnvironment.application, R.color.ui_meli_white), getTextView(snackbar).getTextColors().getDefaultColor());
        // Test action text color.
        Assert.assertEquals(ContextCompat.getColor(RuntimeEnvironment.application, R.color.ui_meli_white), getActionButton(snackbar).getTextColors().getDefaultColor());
    }

    @Test
    public void testShowDismiss() {
        final MeliSnackbar meliSnackbar = MeliSnackbar.make(view, TEST_STRING, Snackbar.LENGTH_INDEFINITE);
        Snackbar snackbar = mock(Snackbar.class);
        ReflectionHelpers.setField(meliSnackbar, "snackbar", snackbar);

        meliSnackbar.show();
        verify(snackbar).show();
        meliSnackbar.dismiss();
        verify(snackbar).dismiss();
    }

    @Test
    public void testTag() {
        final MeliSnackbar snackbar = MeliSnackbar.make(view, TEST_STRING, Snackbar.LENGTH_INDEFINITE);
        Assert.assertNull(snackbar.getTag());
        snackbar.setTag(TEST_STRING);
        Assert.assertEquals(TEST_STRING, snackbar.getTag());
        snackbar.setTag(null);
        Assert.assertNotSame("", snackbar.getTag());
        Assert.assertNull(snackbar.getTag());
    }

    private TextView getTextView(MeliSnackbar meliSnackbar) {
        return (TextView) getSnackbar(meliSnackbar).getView().findViewById(R.id.snackbar_text);
    }

    private Button getActionButton(MeliSnackbar meliSnackbar) {
        return (Button) getSnackbar(meliSnackbar).getView().findViewById(R.id.snackbar_action);
    }

    private int getBackgroundColor(MeliSnackbar meliSnackbar) {
        return ((ColorDrawable) getSnackbar(meliSnackbar).getView().getBackground()).getColor();
    }

    private Snackbar getSnackbar(MeliSnackbar meliSnackbar) {
        return ReflectionHelpers.getField(meliSnackbar, "snackbar");
    }
}
