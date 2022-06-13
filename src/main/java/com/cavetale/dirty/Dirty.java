package com.cavetale.dirty;

import com.google.gson.Gson;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.NonNull;
import net.minecraft.core.BlockPosition;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagLongArray;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.level.WorldServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.TileEntity;
import net.minecraft.world.level.chunk.Chunk;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureBoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import org.bukkit.craftbukkit.v1_19_R1.CraftChunk;
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_19_R1.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_19_R1.inventory.CraftItemStack;

/**
 * Utility class to get or set item, entity, or block NBT data.
 * Items may be accessed directly; for the other two one has to load,
 * modify, store.
 */
public final class Dirty {
    private static Field fieldCraftItemStackHandle = null;
    private Dirty() { }

    /**
     * Turn an NBT into a corresponding Container object.  Works
     * recursively on Compounds and Lists.
     *
     * @param value The NBT value.
     * @return value A raw value, such as Number, Boolean, String,
     * Array, List, Map.
     */
    public static Object fromTag(NBTBase value) {
        if (value == null) {
            return null;
        } else if (value instanceof NBTTagCompound nbtTagCompound) {
            Map<String, Object> result = new HashMap<>();
            for (Map.Entry<String, NBTBase> entry : nbtTagCompound.x.entrySet()) {
                result.put(entry.getKey(), fromTag(entry.getValue()));
            }
            return result;
        } else if (value instanceof NBTTagList nbtTagList) {
            List<Object> result = new ArrayList<>();
            for (int i = 0; i < nbtTagList.size(); i += 1) {
                result.add(fromTag(nbtTagList.get(i)));
            }
            return result;
        } else if (value instanceof NBTTagString nbtTagString) {
            return (String) nbtTagString.e_(); // asString
        } else if (value instanceof NBTTagInt nbtTagInt) {
            return (int) nbtTagInt.f(); // asInt
        } else if (value instanceof NBTTagLong nbtTagLong) {
            return (long) nbtTagLong.f(); // asLong
        } else if (value instanceof NBTTagShort nbtTagShort) {
            return (short) nbtTagShort.g(); // asShort
        } else if (value instanceof NBTTagFloat nbtTagFloat) {
            return (float) nbtTagFloat.j(); // asFloat
        } else if (value instanceof NBTTagDouble nbtTagDouble) {
            return (double) nbtTagDouble.i(); // asDouble
        } else if (value instanceof NBTTagByte nbtTagByte) {
            return (byte) nbtTagByte.h(); // asByte
        } else if (value instanceof NBTTagByteArray nbtTagByteArray) {
            return (byte[]) nbtTagByteArray.d(); // getBytes
        } else if (value instanceof NBTTagIntArray nbtTagIntArray) {
            return (int[]) nbtTagIntArray.f(); // getInts
        } else if (value instanceof NBTTagLongArray nbtTagLongArray) {
            return (long[]) nbtTagLongArray.f(); // getLongs
        } else {
            throw new IllegalArgumentException("TagWrapper.fromTag: Unsupported value type: "
                                               + value.getClass().getName());
        }
    }

    /**
     * Turn any JSON object into an NBT.  Works recursively on
     * Maps and Lists.
     *
     * @param value The raw value, such as Number, Boolean, String, Array, List, Map.
     * @return An NBT structure.
     */
    public static Object toTag(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof Map) {
            NBTTagCompound tag = new NBTTagCompound();
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            for (Map.Entry<String, Object> e : map.entrySet()) {
                tag.a(e.getKey(), (NBTBase) toTag(e.getValue())); // set
            }
            return tag;
        } else if (value instanceof List) {
            NBTTagList tag = new NBTTagList();
            @SuppressWarnings("unchecked")
            List<Object> list = (List<Object>) value;
            for (Object e : (List<Object>) list) {
                tag.add((NBTBase) toTag(e));
            }
            return tag;
        } else if (value instanceof String string) {
            return NBTTagString.a(string);
        } else if (value instanceof Integer integer) {
            return NBTTagInt.a(integer);
        } else if (value instanceof Long longValue) {
            return NBTTagLong.a(longValue);
        } else if (value instanceof Short shortValue) {
            return NBTTagShort.a(shortValue);
        } else if (value instanceof Byte byteValue) {
            return NBTTagByte.a(byteValue);
        } else if (value instanceof Float floatValue) {
            return NBTTagFloat.a(floatValue);
        } else if (value instanceof Double doubleValue) {
            return NBTTagDouble.a(doubleValue);
        } else if (value instanceof Boolean booleanValue) {
            return NBTTagInt.a(booleanValue ? 1 : 0);
        } else if (value instanceof byte[] byteArray) {
            return new NBTTagByteArray(byteArray);
        } else if (value instanceof int[] intArray) {
            return new NBTTagIntArray(intArray);
        } else if (value instanceof long[] longArray) {
            return new NBTTagLongArray(longArray);
        } else {
            throw new IllegalArgumentException("TagWrapper.toTag: Unsupported value type: "
                                               + value.getClass().getName());
        }
    }

    private static Field getFieldCraftItemStackHandle() {
        if (fieldCraftItemStackHandle == null) {
            try {
                fieldCraftItemStackHandle = CraftItemStack.class.getDeclaredField("handle");
            } catch (NoSuchFieldException nsfe) {
                nsfe.printStackTrace();
                fieldCraftItemStackHandle = null;
            }
        }
        return fieldCraftItemStackHandle;
    }

    /**
     * Get an item's tag in container form, that is, transformed from
     * NBT to Java language objects, ready to be saved as JSON or
     * modified.
     */
    public static Map<String, Object> getItemTag(org.bukkit.inventory.ItemStack bukkitItem) {
        try {
            if (!(bukkitItem instanceof CraftItemStack)) return null;
            CraftItemStack obcItem = (CraftItemStack) bukkitItem;
            getFieldCraftItemStackHandle().setAccessible(true);
            ItemStack nmsItem = (ItemStack) fieldCraftItemStackHandle.get(obcItem);
            if (nmsItem == null) return null;
            NBTTagCompound tag = nmsItem.u(); // getTag
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) fromTag(tag);
            return map;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Completely serialize an item, including `id` and `Count`.  The
     * result will be ready to be saved to JSON or deserialized, see
     * below.
     */
    public static Map<String, Object> serializeItem(org.bukkit.inventory.ItemStack bukkitItem) {
        if (bukkitItem == null) throw new NullPointerException("bukkitItem cannot be null");
        try {
            ItemStack nmsItem;
            if (bukkitItem instanceof CraftItemStack) {
                CraftItemStack obcItem = (CraftItemStack) bukkitItem;
                nmsItem = (ItemStack) fieldCraftItemStackHandle.get(obcItem);
            } else {
                nmsItem = CraftItemStack.asNMSCopy(bukkitItem);
            }
            NBTTagCompound tag = new NBTTagCompound();
            nmsItem.b(tag); // save
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) fromTag(tag);
            return map;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Deserialize a full item.
     * @param json The JSON structure.
     * @return The item.
     */
    public static org.bukkit.inventory.ItemStack deserializeItem(Map<String, Object> json) {
        if (json == null) throw new NullPointerException("json cannot be null");
        return ItemStack.a((NBTTagCompound) toTag(json)).asBukkitMirror();
    }

    /**
     * Deserialize a full item.
     * @param json The JSON string.
     * @return The item.
     */
    public static org.bukkit.inventory.ItemStack deserializeItem(String json) {
        if (json == null) throw new NullPointerException("json cannot be null");
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) new Gson().fromJson(json, Map.class);
        return ItemStack.a((NBTTagCompound) toTag(map)).asBukkitMirror();
    }

    /**
     * This will return a new instance if `bukkitItem` is not an
     * instance of of CraftItemStack, and possibly for other reasons.
     *
     * To avoid that, create the ItemStack with one of the methods
     * below, or retrieve them from a running Minecraft server.  In
     * other words, do not just use the result of ItemStack::new.
     */
    public static org.bukkit.inventory.ItemStack
        setItemTag(org.bukkit.inventory.ItemStack bukkitItem, Map<String, Object> json) {
        try {
            CraftItemStack obcItem;
            ItemStack nmsItem;
            if (bukkitItem instanceof CraftItemStack) {
                obcItem = (CraftItemStack) bukkitItem;
                getFieldCraftItemStackHandle().setAccessible(true);
                nmsItem = (ItemStack) fieldCraftItemStackHandle.get(obcItem);
            } else {
                nmsItem = CraftItemStack.asNMSCopy(bukkitItem);
                obcItem = CraftItemStack.asCraftMirror(nmsItem);
            }
            // if (!nmsItem.hasTag()) {
            //     nmsItem.setTag(new NBTTagCompound());
            // }
            NBTTagCompound tag = (NBTTagCompound) toTag(json);
            nmsItem.c(tag); // setTag
            return obcItem;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Object> getBlockTag(org.bukkit.block.Block bukkitBlock) {
        CraftWorld craftWorld = (CraftWorld) bukkitBlock.getWorld();
        BlockPosition pos = new BlockPosition(bukkitBlock.getX(),
                                              bukkitBlock.getY(),
                                              bukkitBlock.getZ());
        WorldServer worldServer = craftWorld.getHandle();
        TileEntity tileEntity = worldServer.c_(pos); // getTileEntity
        if (tileEntity == null) return null;
        NBTTagCompound tag = tileEntity.o(); // saveWithoutMetadata
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) fromTag(tag);
        return map;
    }

    public static Map<String, Object> getBlockTag(org.bukkit.block.BlockState bukkitBlockState) {
        if (!(bukkitBlockState instanceof CraftBlockEntityState)) return null;
        CraftBlockEntityState cbes = (CraftBlockEntityState) bukkitBlockState;
        NBTTagCompound tag = cbes.getSnapshotNBT();
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) fromTag(tag);
        return map;
    }

    public static boolean setBlockTag(org.bukkit.block.Block bukkitBlock, Map<String, Object> json) {
        CraftWorld craftWorld = (CraftWorld) bukkitBlock.getWorld();
        BlockPosition pos = new BlockPosition(bukkitBlock.getX(),
                                              bukkitBlock.getY(),
                                              bukkitBlock.getZ());
        WorldServer worldServer = craftWorld.getHandle();
        TileEntity tileEntity = worldServer.c_(pos); // getTileEntity
        if (tileEntity == null) return false;
        NBTTagCompound tag = (NBTTagCompound) toTag(json);
        tileEntity.a(tag);
        return true;
    }

    public static Map<String, Object> getEntityTag(org.bukkit.entity.Entity entity) {
        Entity nmsEntity = ((CraftEntity) entity).getHandle();
        NBTTagCompound tag = new NBTTagCompound();
        nmsEntity.serializeEntity(tag);
        @SuppressWarnings("unchecked")
        Map<String, Object> map = (Map<String, Object>) fromTag(tag);
        return map;
    }

    public static void setEntityTag(org.bukkit.entity.Entity entity, Map<String, Object> json) {
        Entity nmsEntity = ((CraftEntity) entity).getHandle();
        NBTTagCompound tag = (NBTTagCompound) toTag(json);
        nmsEntity.g(tag); // load
    }

    public static org.bukkit.inventory.ItemStack newCraftItemStack(org.bukkit.Material bukkitMaterial) {
        return CraftItemStack.asCraftCopy(new org.bukkit.inventory.ItemStack(bukkitMaterial));
    }

    public static org.bukkit.inventory.ItemStack toCraftItemStack(org.bukkit.inventory.ItemStack bukkitItem) {
        if (bukkitItem instanceof CraftItemStack) return (CraftItemStack) bukkitItem;
        ItemStack nmsItem = CraftItemStack.asNMSCopy(bukkitItem);
        return CraftItemStack.asCraftMirror(nmsItem);
    }

    public static  org.bukkit.inventory.ItemStack makeSkull(@NonNull String id,
                                                            @NonNull String texture) {
        CraftItemStack result;
        result = (CraftItemStack) newCraftItemStack(org.bukkit.Material.PLAYER_HEAD);
        Map<String, Object> tag = new HashMap<>();
        Map<String, Object> skullOwnerTag = new HashMap<>();
        Map<String, Object> propertiesTag = new HashMap<>();
        List<Object> texturesList = new ArrayList<>();
        Map<String, Object> texturesMap = new HashMap<>();
        tag.put("SkullOwner", skullOwnerTag);
        skullOwnerTag.put("Id", id);
        skullOwnerTag.put("Properties", propertiesTag);
        propertiesTag.put("textures", texturesList);
        texturesList.add(texturesMap);
        texturesMap.put("Value", texture);
        return setItemTag(result, tag);
    }

    /**
     * Returns null or a list with the following format.
     * [
     *   {
     *     name: String,
     *     bb: {
     *       min: {x: Int, y: Int, z: Int},
     *       max: {x: Int, y: Int, z: Int}
     *       pieces: [
     *         {
     *           min: {x: Int, y: Int, z: Int},
     *           max: {x: Int, y: Int, z: Int}
     *         },
     *       ]
     *     },
     *   },
     *   ...
     * ]
     * So essentially a named bounding box.
     * In the future, consider adding sub-BBs.
     */
    public static List<com.cavetale.dirty.Structure> getStructures(org.bukkit.Chunk bukkitChunk) {
        CraftChunk craftChunk = (CraftChunk) bukkitChunk;
        Chunk nmsChunk = craftChunk.getHandle();
        Map<Structure, StructureStart> structureMap = nmsChunk.g();
        List<com.cavetale.dirty.Structure> result = new ArrayList<>();
        for (Map.Entry<Structure, StructureStart> entry : structureMap.entrySet()) {
            Structure structure = entry.getKey();
            StructureStart structureStart = entry.getValue();
            List<StructurePiece> structurePieceList = structureStart.i();
            if (structurePieceList == null || structurePieceList.isEmpty()) continue;
            String name = structure.getClass().getSimpleName();
            if (name.endsWith("Structure")) name = name.substring(0, name.length() - 9);
            name = Util.camelToSnakeCase(name);
            Box box = Box.of(structureStart.a());
            if (box == null) continue;
            List<Box> pieces = new ArrayList<>();
            for (StructurePiece structurePiece : structurePieceList) {
                StructureBoundingBox structurePieceBoundingBox = structurePiece.f();
                pieces.add(Box.of(structurePieceBoundingBox));
            }
            result.add(new com.cavetale.dirty.Structure(name, box, pieces));
        }
        return result;
    }
}
