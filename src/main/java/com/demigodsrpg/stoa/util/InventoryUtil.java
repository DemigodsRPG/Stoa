package com.demigodsrpg.stoa.util;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class InventoryUtil
{
	private final static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

	public static String serializeItemStacks(ItemStack[] inv)
	{
		try
		{
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			BukkitObjectOutputStream bos = new BukkitObjectOutputStream(os);
			bos.writeObject(inv);

			String hex = byteArrayToHexString(os.toByteArray());

			bos.close();
			os.close();
			return hex;
		}
		catch(IOException ex)
		{
			ex.printStackTrace();
		}
		return null;
	}

	public static ItemStack[] deserializeItemStacks(String s)
	{
		try
		{
			byte[] b = hexStringToByteArray(s);
			ByteArrayInputStream bais = new ByteArrayInputStream(b);
			BukkitObjectInputStream bois = new BukkitObjectInputStream(bais);

			ItemStack[] items = (ItemStack[]) bois.readObject();

			bois.close();
			bais.close();
			return items;
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
		return null;
	}

	public static String byteArrayToHexString(byte[] b)
	{
		char[] hexChars = new char[b.length * 2];
		for(int j = 0; j < b.length; j++)
		{
			int v = b[j] & 0xFF;
			hexChars[j * 2] = HEX_ARRAY[v >>> 4];
			hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
		}
		return new String(hexChars);
	}

	public static byte[] hexStringToByteArray(String s)
	{
		int len = s.length();
		byte[] data = new byte[len / 2];
		for(int i = 0; i < len; i += 2)
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
		return data;
	}
}