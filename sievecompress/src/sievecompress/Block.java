package sievecompress;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Block {
	public static interface Codec {
		void init(int length) throws Throwable;
		long read(BitReader reader) throws Throwable;
		void write(long bitmap, BitWriter writer) throws Throwable;
	}

	public static final Codec BITMAP=new Codec() {
		private int length;

		@Override
		public void init(int length) throws Throwable {
			this.length=length;
		}

		@Override
		public long read(BitReader reader) throws Throwable {
			return reader.read()
					?reader.readLong(length)
					:0l;
		}

		@Override
		public String toString() {
			return "bitmap";
		}

		@Override
		public void write(long bitmap, BitWriter writer) throws Throwable {
			if (0l!=bitmap) {
				writer.write(true);
				writer.writeLong(bitmap, length);
			}
			else {
				writer.write(false);
			}
		}
	};

	public final byte[] bits;
	public final int length;

	public Block(byte[] bits, int length) {
		this.bits=bits;
		this.length=length;
	}

	public static Columns<Block> columns(Codec codec) {
		return new Columns<Block>() {
			@Override
			public byte[] bits(Block compressed) {
				return compressed.bits;
			}

			@Override
			public Block compress(Segment segment) throws Throwable {
				return Block.compress(codec, segment);
			}

			@Override
			public List<String> data(Segment segment, Block compressed) throws Throwable {
				return Arrays.asList(
						Columns.formatCompression(compressed.bits),
						Integer.toString(compressed.length));
			}

			@Override
			public Segment decompress(byte[] bits) throws Throwable {
				return Block.decompress(bits, codec);
			}

			@Override
			public List<String> header() throws Throwable {
				return Arrays.asList(
						String.format("block(%1$s)/uncompressed", codec),
						String.format("block(%1$s) length", codec));
			}
		};
	}

	public static Block compress(Codec codec, Segment segment) throws Throwable {
		byte[] bestBits=null;
		int bestLength=1;
		for (int length=2; 32>=length; ++length) {
			codec.init(length);
			BitWriter writer=new BitWriter();
			writer.writeLong(segment.start, 64);
			writer.writeInt(length, 8);
			int index=0;
			for (long prime=segment.start; segment.end>prime; ) {
				long prime2=prime+2*length;
				long bitmap=0l;
				for (; segment.primes.size()>index; ++index) {
					long prime3=segment.primes.get(index);
					if (prime3>=prime2) {
						break;
					}
					bitmap|=1l<<((prime3-prime)/2);
				}
				prime=prime2;
				codec.write(bitmap, writer);
			}
			byte[] bits=writer.toByteArray();
			if ((null==bestBits)
					|| (bestBits.length>=bits.length)) {
				bestBits=bits;
				bestLength=length;
			}
		}
		return new Block(bestBits, bestLength);
	}

	public static Segment decompress(byte[] bits, Codec codec) throws Throwable {
		BitReader reader=new BitReader(bits);
		long start=reader.readLong(64);
		int length=reader.readInt(8);
		codec.init(length);
		long end=Segment.end(start);
		List<Long> primes=new ArrayList<>();
		for (long prime=start; end>prime; prime+=2*length) {
			long bitmap=codec.read(reader);
			long bitmap2=bitmap;
			for (int ii=0; (length>ii) && (0l!=bitmap2); bitmap2=bitmap2>>>1, ++ii) {
				if (0l!=(bitmap2&1l)) {
					primes.add(prime+2*ii);
				}
			}
		}
		return Segment.createPrimes(primes, start);
	}
}
