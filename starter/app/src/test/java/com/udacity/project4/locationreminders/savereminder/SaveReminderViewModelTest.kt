package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.util.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@Config(sdk = [Build.VERSION_CODES.O_MR1])
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    private lateinit var fakeDataSource: FakeDataSource
    private lateinit var viewModel: SaveReminderViewModel

    @get:Rule
    val instantTaskExecRule = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    @Before
    fun setup() {
        stopKoin()
        fakeDataSource = FakeDataSource()
        viewModel = SaveReminderViewModel(
            getApplicationContext(),
            fakeDataSource
        )
    }

    @Test
    fun validateAndSaveReminder_check_loadinge() {
        mainCoroutineRule.pauseDispatcher()

        viewModel.validateAndSaveReminder(validDataItem)
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(true))

        mainCoroutineRule.resumeDispatcher()
        assertThat(viewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun viewModel_savingValidItemSucceeds() {
        val returnValue = viewModel.validateAndSaveReminder(validDataItem)
        assertThat(returnValue, `is`(true))
        assertThat(viewModel.showToast.getOrAwaitValue(), `is`(getString(R.string.reminder_saved)))
    }

    @Test
    fun validateAndSaveReminder_errEnterTitle() {
        val result = viewModel.validateAndSaveReminder(dataItemNullTitle)
        assertThat(result, `is`(false))
        assertThat(viewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))
    }

    @Test
    fun validateAndSaveReminder_errSelectLocation() {
        val result = viewModel.validateAndSaveReminder(dataItemNullLocation)
        assertThat(result, `is`(false))
        assertThat(viewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_select_location))
    }


}