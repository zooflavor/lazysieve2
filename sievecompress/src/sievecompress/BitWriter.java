package sievecompress;

import java.util.Arrays;

public class BitWriter {
	private byte[] bits=new byte[1];
	private int index;

	public byte[] toByteArray() {
		int size=index>>3;
		if (0!=(index&7)) {
			++size;
		}
		return Arrays.copyOf(bits, size);
	}

	public void write(boolean value) {
		int index2=index>>3;
		if (bits.length<=index2) {
			bits=Arrays.copyOf(bits, 2*bits.length);
		}
		if (value) {
			bits[index2]|=1<<(index&7);
		}
		++index;
	}

	public void writeInt(int value, int length) {
		for (int ii=0; length>ii; ++ii) {
			write(0!=(value&(1<<ii)));
		}
	}

	public void writeLong(long value, int length) {
		for (int ii=0; length>ii; ++ii) {
			write(0l!=(value&(1l<<ii)));
		}
	}
}
