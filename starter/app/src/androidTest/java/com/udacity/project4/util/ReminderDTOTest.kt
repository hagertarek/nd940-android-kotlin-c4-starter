package com.udacity.project4.util

import com.udacity.project4.locationreminders.data.dto.ReminderDTO

val validDataItem = ReminderDTO(
    title = "test title",
    description = "test description",
    location = "test location",
    latitude = 2.344234234,
    longitude = 1.34234234
)

val dataItemNullTitle = ReminderDTO(
    title = null,
    description = "test description",
    location = "test location",
    latitude = 2.344234234,
    longitude = 1.34234234
)

val dataItemNullLocation = ReminderDTO(
    title = "test title",
    description = "test description",
    location = null,
    latitude = 2.344234234,
    longitude = 1.34234234
)