package net.invictusmanagement.invictuskiosk.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.invictusmanagement.invictuskiosk.domain.model.PromotionsCategory

@Entity(tableName = "promotion_categories")
data class CouponsCategoryEntity(
    @PrimaryKey val id: String,
    val name: String
)

fun CouponsCategoryEntity.toPromotionsCategory() = PromotionsCategory(
    id = id,
    name = name
)
