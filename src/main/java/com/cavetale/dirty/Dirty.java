package com.cavetale.dirty;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.server.v1_12_R1.Entity;
import net.minecraft.server.v1_12_R1.ItemStack;
import net.minecraft.server.v1_12_R1.NBTBase;
import net.minecraft.server.v1_12_R1.NBTTagByte;
import net.minecraft.server.v1_12_R1.NBTTagByteArray;
import net.minecraft.server.v1_12_R1.NBTTagCompound;
import net.minecraft.server.v1_12_R1.NBTTagDouble;
import net.minecraft.server.v1_12_R1.NBTTagFloat;
import net.minecraft.server.v1_12_R1.NBTTagInt;
import net.minecraft.server.v1_12_R1.NBTTagIntArray;
import net.minecraft.server.v1_12_R1.NBTTagList;
import net.minecraft.server.v1_12_R1.NBTTagLong;
import net.minecraft.server.v1_12_R1.NBTTagLongArray;
import net.minecraft.server.v1_12_R1.NBTTagShort;
import net.minecraft.server.v1_12_R1.NBTTagString;
import net.minecraft.server.v1_12_R1.TileEntity;
import org.bukkit.craftbukkit.v1_12_R1.CraftWorld;
import org.bukkit.craftbukkit.v1_12_R1.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_12_R1.inventory.CraftItemStack;

/**
 * Utility class to get or set item, entity, or block NBT data.
 * Items may be accessed directly; for the other two one has to load,
 * modify, store.
 */
public final class Dirty {
    private static Field fieldCraftItemStackHandle = null;
    private Dirty() { }

    // --- NBT-Container converstion

    /**
     * Turn an NBT Tag into a corresponding Container object.  Works
     * recursively on Compounds and Lists.
     */
    public static Object fromTag(NBTBase value) {
        if (value == null) {
            return null;
        } else if (value instanceof NBTTagCompound) {
            // Recursive
            NBTTagCompound tag = (NBTTagCompound)value;
            Map<String, Object> result = new HashMap<>();
            for (String key: tag.c()) {
                result.put(key, fromTag(tag.get(key)));
            }
            return result;
        } else if (value instanceof NBTTagList) {
            // Recursive
            NBTTagList tag = (NBTTagList)value;
            List<Object> result = new ArrayList<>();
            for (int i = 0; i < tag.size(); i += 1) {
                result.add(fromTag(tag.get(i)));
            }
            return result;
        } else if (value instanceof NBTTagString) {
            return (String)((NBTTagString)value).c_();
        } else if (value instanceof NBTTagInt) {
            return (int)((NBTTagInt)value).e();
        } else if (value instanceof NBTTagLong) {
            return (long)((NBTTagLong)value).d();
        } else if (value instanceof NBTTagShort) {
            return (short)((NBTTagShort)value).f();
        } else if (value instanceof NBTTagFloat) {
            return (float)((NBTTagFloat)value).i();
        } else if (value instanceof NBTTagDouble) {
            return (double)((NBTTagDouble)value).asDouble();
        } else if (value instanceof NBTTagByte) {
            return (byte)((NBTTagByte)value).g();
        } else if (value instanceof NBTTagByteArray) {
            byte[] l = (byte[])((NBTTagByteArray)value).c();
            List<Byte> ls = new ArrayList<>(l.length);
            for (byte b: l) ls.add(b);
            return ls;
        } else if (value instanceof NBTTagIntArray) {
            int[] l = (int[])((NBTTagIntArray)value).d();
            return Arrays.stream(l).boxed().collect(Collectors.toList());
        // } else if (value instanceof NBTTagLongArray) {
        //     long[] l = (long[])((NBTTagLongArray)value).c();
        //     return Arrays.stream(l).boxed().collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException("TagWrapper.fromTag: Unsupported value type: " + value.getClass().getName());
        }
    }

    /**
     * Turn any JSON object into an NBT tag.  Workd recursively on
     * Maps and Lists.
     */
    public static NBTBase toTag(Object value) {
        if (value == null) {
            return null;
        } else if (value instanceof Map) {
            // Recursive
            NBTTagCompound tag = new NBTTagCompound();
            for (Map.Entry<String, Object> e: ((Map<String, Object>)value).entrySet()) {
                tag.set(e.getKey(), toTag(e.getValue()));
            }
            return tag;
        } else if (value instanceof List) {
            // Recursive
            NBTTagList tag = new NBTTagList();
            for (Object e: (List<Object>)value) {
                tag.add(toTag(e));
            }
            return tag;
        } else if (value instanceof String) {
            return new NBTTagString((String)value);
        } else if (value instanceof Integer) {
            return new NBTTagInt((Integer)value);
        } else if (value instanceof Long) {
            return new NBTTagLong((Long)value);
        } else if (value instanceof Short) {
            return new NBTTagShort((Short)value);
        } else if (value instanceof Byte) {
            return new NBTTagByte((Byte)value);
        } else if (value instanceof Float) {
            return new NBTTagFloat((Float)value);
        } else if (value instanceof Double) {
            return new NBTTagDouble((Double)value);
        } else if (value instanceof Boolean) {
            return new NBTTagInt((Boolean)value ? 1 : 0);
        } else if (value instanceof byte[]) {
            return new NBTTagByteArray((byte[])value);
        } else if (value instanceof int[]) {
            return new NBTTagIntArray((int[])value);
        } else if (value instanceof long[]) {
            return new NBTTagLongArray((long[])value);
        } else {
            throw new IllegalArgumentException("TagWrapper.toTag: Unsupported value type: " + value.getClass().getName());
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

    // --- Container getters and setters

    public static Map<String, Object> getItemTag(org.bukkit.inventory.ItemStack bukkitItem) {
        try {
            if (!(bukkitItem instanceof CraftItemStack)) return null;
            CraftItemStack obcItem = (CraftItemStack)bukkitItem;
            getFieldCraftItemStackHandle().setAccessible(true);
            ItemStack nmsItem = (ItemStack)fieldCraftItemStackHandle.get(obcItem);
            if (nmsItem == null) return null;
            NBTTagCompound tag = nmsItem.getTag();
            return (Map<String, Object>)fromTag(tag);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * This will return a new instance if `bukkitItem` is an instance
     * of ItemStack instead of CraftItemStack, and possibly for other
     * reasons.
     */
    public static org.bukkit.inventory.ItemStack setItemTag(org.bukkit.inventory.ItemStack bukkitItem, Map<String, Object> json) {
        try {
            CraftItemStack obcItem;
            ItemStack nmsItem;
            if (bukkitItem instanceof CraftItemStack) {
                obcItem = (CraftItemStack)bukkitItem;
                getFieldCraftItemStackHandle().setAccessible(true);
                nmsItem = (ItemStack)fieldCraftItemStackHandle.get(obcItem);
            } else {
                nmsItem = CraftItemStack.asNMSCopy(bukkitItem);
                obcItem = CraftItemStack.asCraftMirror(nmsItem);
            }
            // if (!nmsItem.hasTag()) {
            //     nmsItem.setTag(new NBTTagCompound());
            // }
            NBTTagCompound tag = (NBTTagCompound)toTag(json);
            nmsItem.setTag(tag);
            return obcItem;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static Map<String, Object> getBlockTag(org.bukkit.block.Block bukkitBlock) {
        CraftWorld craftWorld = (CraftWorld)bukkitBlock.getWorld();
        TileEntity tileEntity = craftWorld.getTileEntityAt(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ());
        if (tileEntity == null) return null;
        NBTTagCompound tag = new NBTTagCompound();
        tileEntity.save(tag);
        return (Map<String, Object>)fromTag(tag);
    }

    public static Map<String, Object> getBlockTag(org.bukkit.block.BlockState bukkitBlockState) {
        if (!(bukkitBlockState instanceof CraftBlockEntityState)) return null;
        CraftBlockEntityState cbes = (CraftBlockEntityState)bukkitBlockState;
        NBTTagCompound tag = cbes.getSnapshotNBT();
        return (Map<String, Object>)fromTag(tag);
    }

    public static boolean setBlockTag(org.bukkit.block.Block bukkitBlock, Map<String, Object> json) {
        CraftWorld craftWorld = (CraftWorld)bukkitBlock.getWorld();
        TileEntity tileEntity = craftWorld.getTileEntityAt(bukkitBlock.getX(), bukkitBlock.getY(), bukkitBlock.getZ());
        if (tileEntity == null) return false;
        NBTTagCompound tag = (NBTTagCompound)toTag(json);
        tileEntity.load(tag);
        return true;
    }

    public static Map<String, Object> getEntityTag(org.bukkit.entity.Entity entity) {
        Entity nmsEntity = ((CraftEntity)entity).getHandle();
        NBTTagCompound tag = new NBTTagCompound();
        nmsEntity.save(tag);
        return (Map<String, Object>)fromTag(tag);
    }

    public static void setEntityTag(org.bukkit.entity.Entity entity, Map<String, Object> json) {
        Entity nmsEntity = ((CraftEntity)entity).getHandle();
        NBTTagCompound tag = (NBTTagCompound)toTag(json);
        nmsEntity.f(tag);
    }

    // --- Item NBT access operations

    public static Optional<Object> accessItemNBT(org.bukkit.inventory.ItemStack bukkitItem, boolean create) {
        try {
            if (!(bukkitItem instanceof CraftItemStack)) return Optional.empty();
            CraftItemStack obcItem = (CraftItemStack)bukkitItem;
            getFieldCraftItemStackHandle().setAccessible(true);
            ItemStack nmsItem = (ItemStack)fieldCraftItemStackHandle.get(obcItem);
            if (nmsItem == null) return Optional.empty();
            NBTTagCompound tag = nmsItem.getTag();
            if (tag == null) {
                if (create) {
                    tag = new NBTTagCompound();
                    nmsItem.setTag(tag);
                } else {
                    return Optional.empty();
                }
            }
            return Optional.of(tag);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static CraftItemStack newCraftItemStack(org.bukkit.Material bukkitMaterial) {
        return CraftItemStack.asCraftCopy(new org.bukkit.inventory.ItemStack(bukkitMaterial));
    }

    public static CraftItemStack toCraftItemStack(org.bukkit.inventory.ItemStack bukkitItem) {
        if (bukkitItem instanceof CraftItemStack) return (CraftItemStack)bukkitItem;
        ItemStack nmsItem = CraftItemStack.asNMSCopy(bukkitItem);
        return CraftItemStack.asCraftMirror(nmsItem);
    }

    // --- NBT modification

    public static void setNBT(Optional<Object> opt, String key, Object value) {
        if (!opt.isPresent()) throw new NullPointerException("Tag cannot be null");
        if (!(opt.get() instanceof NBTTagCompound)) throw new IllegalArgumentException("Expected tag compound: " + opt.get().getClass().getName());
        NBTTagCompound tag = (NBTTagCompound)opt.get();
        if (value == null) {
            tag.remove(key);
        } else {
            tag.set(key, toTag(value));
        }
    }

    public static Optional<Object> getNBT(Optional<Object> opt, String key) {
        if (!opt.isPresent()) throw new NullPointerException("Tag cannot be null");
        if (!(opt.get() instanceof NBTTagCompound)) throw new IllegalArgumentException("Expected tag compound: " + opt.get().getClass().getName());
        NBTTagCompound tag = (NBTTagCompound)opt.get();
        return Optional.ofNullable(tag.get(key));
    }

    public static Optional<Object> getNBT(Optional<Object> opt, int index) {
        if (!opt.isPresent()) throw new NullPointerException("Tag cannot be null");
        if (opt.get() instanceof NBTTagList) {
            NBTTagList tag = (NBTTagList)opt.get();
            return Optional.ofNullable(tag.get(index));
        } else if (opt.get() instanceof NBTTagList) {
            NBTTagList tag = (NBTTagList)opt.get();
            return Optional.ofNullable(tag.get(index));
        } else {
            throw new IllegalArgumentException("Expected list or tag list: " + opt.get().getClass().getName());
        }
    }

    public static Object fromNBT(Optional<Object> opt) {
        Object o = opt.orElse(null);
        if (o instanceof NBTBase) return fromTag((NBTBase)o);
        return null;
    }
}
