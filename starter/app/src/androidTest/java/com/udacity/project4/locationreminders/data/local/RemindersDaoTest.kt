package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.util.validDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RemindersDaoTest {

    @get:Rule
    val instantTaskExecRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase


    @Before
    fun initDb() {
        database = Room.inMemoryDatabaseBuilder(
            getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
    }

    @Test
    fun insertReminder() = runBlockingTest {
        database.reminderDao().saveReminder(validDataItem)

        val reminders = database.reminderDao().getReminders()
        assertThat<List<ReminderDTO>>(reminders, notNullValue())
        assertThat(reminders, contains(validDataItem))
    }

    @Test
    fun insertReminderAndGetById() = runBlockingTest {
        database.reminderDao().saveReminder(validDataItem)

        val reminder = database.reminderDao().getReminderById(validDataItem.id)

        assertThat<ReminderDTO>(reminder as ReminderDTO, notNullValue())
        assertThat(reminder.id, `is`(validDataItem.id))
        assertThat(reminder.title, `is`(validDataItem.title))
        assertThat(reminder.description, `is`(validDataItem.description))
        assertThat(reminder.location, `is`(validDataItem.location))
        assertThat(reminder.latitude, `is`(validDataItem.latitude))
        assertThat(reminder.longitude, `is`(validDataItem.longitude))
    }

    @Test
    fun deleteReminders() = runBlockingTest {
        database.reminderDao().saveReminder(validDataItem)

        assertThat(database.reminderDao().getReminders(), hasSize(1))

        database.reminderDao().deleteAllReminders()
        assertThat(database.reminderDao().getReminders(), empty())
    }

    @After
    fun closeDb() = database.close()

}