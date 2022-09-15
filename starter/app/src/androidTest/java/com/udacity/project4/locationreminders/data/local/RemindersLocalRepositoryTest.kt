package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.util.validDataItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.hasSize
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    private lateinit var database: RemindersDatabase
    private lateinit var repository: RemindersLocalRepository

    @Before
    fun init() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        repository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
    }

    @Test
    fun insertReminder() = runBlocking {
        repository.saveReminder(validDataItem)

        val result = repository.getReminders()
        assertThat(result, instanceOf(Result.Success::class.java))

        result as Result.Success

        assertThat(result.data, not(empty()))
        assertThat(result.data, hasSize(1))
    }

    @Test
    fun reminderSaveAndGetById_success() = runBlocking {
        repository.saveReminder(validDataItem)

        val result = repository.getReminder(validDataItem.id)
        assertThat(result, instanceOf(Result.Success::class.java))

        result as Result.Success

        assertThat(result.data, notNullValue())
        assertThat(result.data.title, `is`(validDataItem.title))
        assertThat(result.data.description, `is`(validDataItem.description))
        assertThat(result.data.location, `is`(validDataItem.location))
        assertThat(result.data.latitude, `is`(validDataItem.latitude))
        assertThat(result.data.longitude, `is`(validDataItem.longitude))
    }

    @Test
    fun reminderSaveAndGetById_error() = runBlocking {
        val result = repository.getReminder(validDataItem.id)
        assertThat(result, instanceOf(Result.Error::class.java))

        result as Result.Error

        assertThat(result.message, `is`("Reminder not found!"))
    }

    @Test
    fun deleteReminders() = runBlocking {
        repository.saveReminder(validDataItem)
        repository.deleteAllReminders()

        val result = repository.getReminders()
        assertThat(result, instanceOf(Result.Success::class.java))

        result as Result.Success
        assertThat(result.data, empty())
    }

    @After
    fun closeDb() = database.close()

}