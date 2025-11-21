package net.invictusmanagement.invictuskiosk.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import net.invictusmanagement.invictuskiosk.domain.model.BusinessPromotion
import net.invictusmanagement.invictuskiosk.domain.model.Promotion

@Entity(tableName = "business_promotions")
data class BusinessPromotionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val type: Int,
    val name: String,
    val address1: String,
    val address2: String,
    val city: String,
    val phone: String,
    val promotions: List<Promotion>,
    val state: String,
    val zip: String
)


fun BusinessPromotionEntity.toBusinessPromotion() = BusinessPromotion(
    name = name,
    address1 = address1,
    address2 = address2,
    city = city,
    phone = phone,
    promotions = promotions,
    state = state,
    zip = zip
)
