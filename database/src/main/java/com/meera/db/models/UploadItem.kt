package com.meera.db.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import java.io.Serializable
import java.util.UUID


@Entity(tableName = "upload_items")
data class UploadItem(
    @ColumnInfo(name = "type")
    val type: UploadType,

    @ColumnInfo(name = "upload_bundle")
    val uploadBundleStringify: String,

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Int = 0,

    @ColumnInfo(name = "upload_tries")
    var uploadTries: Int = 0
) {
    fun <Type : UploadBundle> getUploadBundle(explicitClass: Class<out UploadBundle>): Type {
        return uploadBundleStringify.fromJson(explicitClass)
    }

    private fun <Type : Serializable> String.fromJson(explicitClass:Class<out Serializable>): Type {
        return GsonBuilder().create().fromJson(this, explicitClass) as Type
    }
}

enum class UploadType {
    Post,
    EventPost,
    Moment,
    EditPost
}

/**
 * Модель для хранение произвольных данных о загрузке
 */
abstract class UploadBundle(
    @SerializedName("operationId")
    private var operationId: String? = null
) : Serializable {
    fun setUid(uuid: UUID?) {
        operationId = uuid?.toString()
    }

    fun getUid(): UUID? {
        return if (operationId != null) {
            UUID.fromString(operationId)
        } else {
            null
        }
    }
}
