package com.example.encryptiondecryption;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Smoke tests for the main encryption screen on a real Android runtime. */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void launchShowsEncryptionControlsAndResult() {
        onView(withId(R.id.inputText)).check(matches(isDisplayed()));
        onView(withId(R.id.passwordInput)).check(matches(isDisplayed()));
        onView(withId(R.id.encryptButton)).check(matches(isDisplayed()));
        onView(withId(R.id.decryptButton)).check(matches(isDisplayed()));
        onView(withId(R.id.outputText)).check(matches(isDisplayed()));
    }

    @Test
    public void characterCounterCountsUnicodeCodePoints() {
        activityRule.getScenario().onActivity(activity ->
                activity.findViewById(R.id.inputText).setText("Hi 🔐"));
        onView(withId(R.id.inputCount)).check(matches(withText("4 characters")));
    }
}
