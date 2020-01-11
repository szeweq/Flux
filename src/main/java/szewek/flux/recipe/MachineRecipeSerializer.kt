package szewek.flux.recipe

import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.ints.IntList
import it.unimi.dsi.fastutil.ints.IntLists
import net.minecraft.item.ItemStack
import net.minecraft.item.crafting.IRecipeSerializer
import net.minecraft.item.crafting.Ingredient
import net.minecraft.item.crafting.ShapedRecipe
import net.minecraft.network.PacketBuffer
import net.minecraft.util.JSONUtils
import net.minecraft.util.NonNullList
import net.minecraft.util.ResourceLocation
import net.minecraftforge.registries.ForgeRegistries
import net.minecraftforge.registries.ForgeRegistryEntry

class MachineRecipeSerializer<T : AbstractMachineRecipe>(
        private val factory: IFactory<T>,
        private val defaultProcess: Int
) : ForgeRegistryEntry<IRecipeSerializer<*>>(), IRecipeSerializer<T> {
    override fun read(recipeId: ResourceLocation, json: JsonObject): T {
        val s = JSONUtils.getString(json, "group", "")
        val arr = JSONUtils.getJsonArray(json, "ingredients")
        val b = Builder()
        for (elem in arr) {
            val ingredient = Ingredient.deserialize(elem)
            if (!ingredient.hasNoMatchingItems()) b.ingredients.add(ingredient)
        }
        if (!json.has("result")) throw JsonSyntaxException("Missing result, expected to find a string or object")
        if (json["result"].isJsonObject) b.result = ShapedRecipe.deserializeItem(JSONUtils.getJsonObject(json, "result")) else {
            val s1 = JSONUtils.getString(json, "result")
            val location = ResourceLocation(s1)
            val item = ForgeRegistries.ITEMS.getValue(location)
                    ?: throw IllegalStateException("Item: $s1 does not exist")
            b.result = ItemStack(item)
        }
        b.experience = JSONUtils.getFloat(json, "experience", 0.0f)
        b.process = JSONUtils.getInt(json, "processtime", defaultProcess)
        if (json.has("cost")) {
            val jc = json["cost"]
            if (jc.isJsonPrimitive) {
                b.itemCost = IntLists.singleton(jc.asInt)
            } else if (jc.isJsonArray) {
                val ja = jc.asJsonArray
                val cl: IntList = IntArrayList(ja.size())
                for (je in ja) {
                    cl.add(je.asInt)
                }
                b.itemCost = cl
            }
        } else {
            b.itemCost = DEFAULT_COST
        }
        return factory.create(recipeId, s, b)
    }

    override fun read(recipeId: ResourceLocation, buffer: PacketBuffer): T? {
        val s = buffer.readString(32767)
        val b = Builder()
        var size = buffer.readByte().toInt()
        for (i in 0 until size) {
            b.ingredients.add(Ingredient.read(buffer))
        }
        b.result = buffer.readItemStack()
        b.experience = buffer.readFloat()
        b.process = buffer.readVarInt()
        size = buffer.readByte().toInt()
        if (size > 0) {
            if (size == 1) b.itemCost = IntLists.singleton(buffer.readVarInt()) else {
                val itemCost = IntArrayList(size)
                for (i in 0 until size) {
                    itemCost.add(buffer.readVarInt())
                }
                b.itemCost = itemCost
            }
        }
        return factory.create(recipeId, s, b)
    }

    override fun write(buffer: PacketBuffer, recipe: T?) {
        buffer.writeString(recipe!!.group)
        buffer.writeByte(recipe.ingredientsList.size)
        for (ingredient in recipe.ingredientsList) ingredient.write(buffer)
        buffer.writeItemStack(recipe.result)
        buffer.writeFloat(recipe.experience)
        buffer.writeVarInt(recipe.processTime)
        buffer.writeByte(recipe.itemCost.size)
        recipe.itemCost.forEach {
            buffer.writeVarInt(it)
        }
    }

    @FunctionalInterface
    interface IFactory<T : AbstractMachineRecipe> {
        fun create(id: ResourceLocation, group: String, builder: Builder): T
    }

    class Builder {
        @JvmField
        var ingredients: NonNullList<Ingredient> = NonNullList.create()
        @JvmField
        var result: ItemStack? = null
        @JvmField
        var experience = 0f
        @JvmField
        var process = 0
        @JvmField
        var itemCost: IntList? = null
    }

    companion object {
        private val DEFAULT_COST = IntLists.singleton(1)

        inline fun <T : AbstractMachineRecipe> factory(crossinline fn: (ResourceLocation, String, Builder) -> T) = object : IFactory<T> {
            override fun create(id: ResourceLocation, group: String, builder: Builder) = fn(id, group, builder)
        }
    }
}