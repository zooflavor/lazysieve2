package sievecompress;

import java.util.Arrays;
import java.util.List;

public interface Columns<C> {
	byte[] bits(C compressed);

	C compress(Segment segment) throws Throwable;

	default List<String> data(Segment segment, boolean decompress) throws Throwable {
		C compressed=compress(segment);
		if (decompress
				&& (null!=compressed)) {
			byte[] bits=bits(compressed);
			if (null!=bits) {
				Segment segment2=decompress(bits);
				Segment.assertSegment(segment, segment2);
			}
		}
		return data(segment, compressed);
	}

	List<String> data(Segment segment, C compressed) throws Throwable;

	Segment decompress(byte[] bits) throws Throwable;

	static Columns<byte[]> empty() {
		return new Columns<byte[]>() {
			@Override
			public byte[] bits(byte[] compressed) {
				return compressed;
			}

			@Override
			public byte[] compress(Segment segment) throws Throwable {
				return null;
			}

			@Override
			public List<String> data(Segment segment, byte[] compressed) throws Throwable {
				return Arrays.asList();
			}

			@Override
			public Segment decompress(byte[] bits) throws Throwable {
				return null;
			}

			@Override
			public List<String> header() throws Throwable {
				return Arrays.asList();
			}
		};
	}

	static String formatCompression(byte[] bits) {
		return String.format("%1$6.4f", 1.0*bits.length/Segment.UNCOMPRESSED_SIZE);
	}

	List<String> header() throws Throwable;
}
