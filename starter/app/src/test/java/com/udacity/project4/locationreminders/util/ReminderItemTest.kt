package com.udacity.project4.locationreminders.util

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

val validDataItem = ReminderDataItem(
    title = "test",
    description = "test",
    location = "test",
    latitude = 2.344234234,
    longitude = 1.34234234
)

val dataItemNullTitle = ReminderDataItem(
    title = null,
    description = "test",
    location = "test",
    latitude = 2.344234234,
    longitude = 1.34234234
)

val dataItemNullLocation = ReminderDataItem(
    title = "test",
    description = "test",
    location = null,
    latitude = 2.344234234,
    longitude = 1.34234234
)

fun ReminderDataItem.toDTO()= ReminderDTO(
    title = title,
    description = description,
    location = location,
    latitude = latitude,
    longitude = longitude
)