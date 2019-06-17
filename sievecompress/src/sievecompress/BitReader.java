package sievecompress;

public class BitReader {
	private final byte[] bits;
	private int index;

	public BitReader(byte[] bits) {
		this.bits=bits;
	}

	public boolean read() {
		boolean result=0!=(bits[index>>3]&(1<<(index&7)));
		++index;
		return result;
	}

	public int readInt(int length) {
		int result=0;
		for (int ii=0; length>ii; ++ii) {
			if (read()) {
				result|=1<<ii;
			}
		}
		return result;
	}

	public long readLong(int length) {
		long result=0l;
		for (int ii=0; length>ii; ++ii) {
			if (read()) {
				result|=1l<<ii;
			}
		}
		return result;
	}
}
