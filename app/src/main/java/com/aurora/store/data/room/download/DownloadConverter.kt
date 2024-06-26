package com.aurora.store.data.room.download

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.aurora.gplayapi.data.models.File
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
@ProvidedTypeConverter
class DownloadConverter @Inject constructor(private val gson: Gson) {

    @TypeConverter
    fun toSharedLibList(string: String): List<SharedLib> {
        val listType = object : TypeToken<List<SharedLib>>() {}.type
        return gson.fromJson(string, listType)
    }

    @TypeConverter
    fun fromSharedLibList(list: List<SharedLib>): String {
        return gson.toJson(list)
    }

    @TypeConverter
    fun toGPlayFileList(string: String): List<File> {
        val listType = object : TypeToken<List<File>>() {}.type
        return gson.fromJson(string, listType)
    }

    @TypeConverter
    fun fromGPlayFileList(list: List<File>): String {
        return gson.toJson(list)
    }
}
