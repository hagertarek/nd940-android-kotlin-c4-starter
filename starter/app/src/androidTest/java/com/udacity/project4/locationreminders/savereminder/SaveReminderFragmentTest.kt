package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.espresso.Espresso.*
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.material.internal.ContextUtils.getActivity
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.util.monitorFragment
import com.udacity.project4.util.validDataItem
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.core.Is.`is`
import org.hamcrest.core.IsNot.not
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito.mock


@MediumTest
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderFragmentTest {

    private lateinit var viewModel: SaveReminderViewModel
    private lateinit var appContext: Application

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @Before
    fun registerIdlingResources(): Unit = IdlingRegistry.getInstance().run {
        register(EspressoIdlingResource.countingIdlingResource)
        register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource(): Unit = IdlingRegistry.getInstance().run {
        unregister(EspressoIdlingResource.countingIdlingResource)
        unregister(dataBindingIdlingResource)
    }

    @Before
    fun setup() {
        stopKoin()
        appContext = ApplicationProvider.getApplicationContext()

        val appModule = module {
            single {
                SaveReminderViewModel(
                    appContext,
                    get() as ReminderDataSource
                )
            }

            single<ReminderDataSource> { RemindersLocalRepository(get()) }
            single { LocalDB.createRemindersDao(appContext) }
        }

        startKoin {
            androidContext(appContext)
            modules(listOf(appModule))
        }

        viewModel = GlobalContext.get().koin.get()
    }

    @Test
    fun saveReminder_NullTitle() {
        val navController = mock(NavController::class.java)
        val scenario =
            launchFragmentInContainer<SaveReminderFragment>(Bundle.EMPTY, R.style.AppTheme)

        dataBindingIdlingResource.monitorFragment(scenario)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }



        onView(withId(R.id.saveReminder)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_enter_title)))
    }

    @Test
    fun saveReminder_NullLocation() {

        val navController = mock(NavController::class.java)
        val scenario =
            launchFragmentInContainer<SaveReminderFragment>(Bundle.EMPTY, R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.reminderTitle)).perform(typeText(validDataItem.title))
        closeSoftKeyboard()

        onView(withId(R.id.saveReminder)).perform(click())
        onView(withId(com.google.android.material.R.id.snackbar_text))
            .check(matches(withText(R.string.err_select_location)))
    }

    @Test
    fun saveReminder_validData_succeed() {
        val latlng = LatLng(validDataItem.latitude!!, validDataItem.longitude!!)
        viewModel.setSelectedLocation(PointOfInterest(latlng, validDataItem.location, null))

        val navController = mock(NavController::class.java)
        val scenario =
            launchFragmentInContainer<SaveReminderFragment>(Bundle.EMPTY, R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.reminderTitle)).perform(typeText(validDataItem.title))
        onView(withId(R.id.reminderDescription)).perform(typeText(validDataItem.description))
        closeSoftKeyboard()

        onView(withId(R.id.saveReminder)).perform(click())

        onView(withText(R.string.reminder_saved)).inRoot(
            withDecorView(
                not(
                    `is`(
                        getActivity(appContext)?.window?.decorView
                    )
                )
            )
        ).check(
            matches(
                isDisplayed()
            )
        )
    }
}