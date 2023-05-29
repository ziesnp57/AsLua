package com.yongle.aslua.room

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import com.yongle.aslua.MainActivity.Companion.Db
import com.yongle.aslua.data.ContentTypes

/**
 * Room 数据库
 */


// 创建数据类来存储您的数据
@Entity(tableName = "content_types")
data class ContentType(
    @PrimaryKey val typeId: Int,
    val typeName: String
)

// 创建 DAO 接口
@Dao
interface ContentTypeDao {
    // 查询所有数据
    @Query("SELECT * FROM content_types ORDER BY typeId DESC")
    fun getAll(): List<ContentType>

    // 插入数据
    @Insert
    fun insertAll(contentTypes: List<ContentType>)

    // 删除数据
    @Query("DELETE FROM content_types")
    fun deleteAll()

    // 更新数据
    @Query("UPDATE content_types SET typeName = :typeName WHERE typeId = :typeId")
    fun update(typeId: Int, typeName: String)

    // 查询数据
    @Query("SELECT * FROM content_types WHERE typeId = :typeId")
    fun getContentType(typeId: Int): ContentType
}

// 创建数据库实例
@Database(entities = [ContentType::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contentTypeDao(): ContentTypeDao
}

fun datacache(data: List<ContentTypes>) {
        //转换为数据库对象
        val contentTypes = data.map {
            ContentType(it.typeId, it.typeName)
        }

        //删除数据库
        Db.instance.contentTypeDao().deleteAll()

        //插入数据库
        Db.instance.contentTypeDao().insertAll(contentTypes)
}



