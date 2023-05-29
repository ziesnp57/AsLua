package com.yongle.aslua.room

/**
 * Room 数据库
 */

/*

// 创建数据类来存储您的数据
@Entity(tableName = "content_typeu")
data class ContentTypeu(
    @PrimaryKey val typeId: Int
)

// 创建 DAO 接口
@Dao
interface ContentTypeDaou {
    // 查询所有数据
    @Query("SELECT * FROM content_types ORDER BY typeSort DESC")
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

fun datacachu(data: List<ContentTypeu>) {
    //转换为数据库对象
    val contentTypes = data.map {
        ContentType(it.typeId)
    }

    //删除数据库
    Db.instance.contentTypeDao().deleteAll()

    //插入数据库
    Db.instance.contentTypeDao().insertAll(contentTypes)
}
*/
