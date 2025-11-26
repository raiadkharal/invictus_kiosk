package net.invictusmanagement.invictuskiosk.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import net.invictusmanagement.invictuskiosk.data.local.entities.BusinessPromotionEntity
import net.invictusmanagement.invictuskiosk.data.local.entities.CouponsCategoryEntity

@Dao
interface CouponsDao {

    @Query("SELECT * FROM promotion_categories")
    suspend fun getPromotionCategories(): List<CouponsCategoryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPromotionCategories(categories: List<CouponsCategoryEntity>)

    @Query("SELECT * FROM business_promotions WHERE type = :categoryId ORDER BY name ASC")
    suspend fun getPromotionsByCategory(categoryId: Int): List<BusinessPromotionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPromotions(promotions: List<BusinessPromotionEntity>)

    @Query("DELETE FROM business_promotions")
    suspend fun clearPromotions()

    @Query("DELETE FROM promotion_categories")
    suspend fun clearPromotionCategories()
}
