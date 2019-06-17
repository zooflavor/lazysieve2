package sievecompress;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class Variable {
	private static abstract class ColumnsImpl implements Columns<Variable> {
		private final int divisor;
		private final int name;

		public ColumnsImpl(int divisor, int name) {
			this.divisor=divisor;
			this.name=name;
		}

		@Override
		public byte[] bits(Variable compressed) {
			return compressed.bits;
		}

		@Override
		public List<String> data(Segment segment, Variable compressed) throws Throwable {
			return Arrays.asList(
					Columns.formatCompression(compressed.bits),
					compressed.lengths.toString()
							.replaceAll("\\[", "\"")
							.replaceAll("\\]", "\""),
					String.format("%1$6.4f",
							Math.log(segment.lnEnd()/divisor)/Math.log(2)));
		}

		@Override
		public List<String> header() throws Throwable {
			return Arrays.asList(
					"var"+name+"/uncompressed",
					"var"+name+" var0123",
					"log_2(ln(end)/"+divisor+")");
		}
	}

	public final byte[] bits;
	public final List<Integer> lengths;

	public Variable(byte[] bits, List<Integer> lengths) {
		this.bits=bits;
		this.lengths=lengths;
	}

	public static int bits(int number, int var0, int var1, int var2, int var3) {
		int bits=32-Integer.numberOfLeadingZeros(number)-var0;
		int result=var0+1;
		if (0<bits) {
			bits-=var1;
			result+=var1+1;
		}
		if (0<bits) {
			bits-=var2;
			result+=var2+1;
		}
		if (0<bits) {
			bits-=var3;
			result+=var3+1;
		}
		if (0<bits) {
			result+=2*bits;
		}
		return result;
	}

	public static Columns<Variable> columns1() {
		return new ColumnsImpl(2, 1) {
			@Override
			public Variable compress(Segment segment) throws Throwable {
				return compress1(segment);
			}

			@Override
			public Segment decompress(byte[] bits) throws Throwable {
				return decompress1(bits);
			}
		};
	}

	public static Columns<Variable> columns2() {
		return new ColumnsImpl(6, 2) {
			@Override
			public Variable compress(Segment segment) throws Throwable {
				return compress2(segment);
			}

			@Override
			public Segment decompress(byte[] bits) throws Throwable {
				return decompress2(bits);
			}
		};
	}

	public static Variable compress1(Segment segment) {
		int best0=0;
		int best1=0;
		int best2=0;
		int best3=0;
		long bestSize=Long.MAX_VALUE;
		for (int var0=1; 64>=var0; ++var0) {
			for (int var1=1; 8>=var1; ++var1) {
				for (int var2=1; 8>=var2; ++var2) {
					for (int var3=1; 8>=var3; ++var3) {
						long size=0l;
						for (Map.Entry<Integer, Integer> entry: segment.frequencies.entrySet()) {
							int delta=entry.getKey();
							int frequency=entry.getValue();
							int bits=Variable.bits(delta/2, var0, var1, var2, var3);
							size+=bits*frequency;
						}
						if (bestSize>size) {
							best0=var0;
							best1=var1;
							best2=var2;
							best3=var3;
							bestSize=size;
						}
					}
				}
			}
		}
		BitWriter writer=new BitWriter();
		writer.writeLong(segment.start, 64);
		writer.writeInt(best0, 8);
		writer.writeInt(best1, 8);
		writer.writeInt(best2, 8);
		writer.writeInt(best3, 8);
		for (int delta: segment.deltas) {
			write(delta/2, best0, best1, best2, best3, writer);
		}
		return new Variable(writer.toByteArray(), Arrays.asList(best0, best1, best2, best3));
	}

	public static Variable compress2(Segment segment) {
		int best0=0;
		int best1=0;
		int best2=0;
		int best3=0;
		long bestSize=Long.MAX_VALUE;
		for (int var0=1; 64>=var0; ++var0) {
			for (int var1=1; 8>=var1; ++var1) {
				for (int var2=1; 8>=var2; ++var2) {
					for (int var3=1; 8>=var3; ++var3) {
						long size=0l;
						for (Map.Entry<Integer, Integer> entry: segment.frequencies.entrySet()) {
							int delta=entry.getKey();
							int frequency=entry.getValue();
							int remainder;
							switch (delta%6) {
								case 0:
									remainder=1;
									break;
								case 2:
								case 4:
									remainder=2;
									break;
								default:
									throw new IllegalArgumentException(""+delta%6);
							}
							int bits=Variable.bits(delta/6, var0, var1, var2, var3);
							size+=(remainder+bits)*frequency;
						}
						if (bestSize>size) {
							best0=var0;
							best1=var1;
							best2=var2;
							best3=var3;
							bestSize=size;
						}
					}
				}
			}
		}
		BitWriter writer=new BitWriter();
		writer.writeLong(segment.start, 64);
		writer.writeInt(best0, 8);
		writer.writeInt(best1, 8);
		writer.writeInt(best2, 8);
		writer.writeInt(best3, 8);
		for (int delta: segment.deltas) {
			switch (delta%6) {
				case 0:
					writer.write(true);
					break;
				case 2:
					writer.write(false);
					writer.write(true);
					break;
				case 4:
					writer.write(false);
					writer.write(false);
					break;
				default:
					throw new IllegalArgumentException(""+delta%6);
			}
			write(delta/6, best0, best1, best2, best3, writer);
		}
		return new Variable(writer.toByteArray(), Arrays.asList(best0, best1, best2, best3));
	}

	public static Segment decompress1(byte[] bits) {
		BitReader reader=new BitReader(bits);
		long start=reader.readLong(64);
		int var0=reader.readInt(8);
		int var1=reader.readInt(8);
		int var2=reader.readInt(8);
		int var3=reader.readInt(8);
		long end=Segment.end(start);
		List<Integer> deltas=new ArrayList<>();
		long next=start;
		while (end>next) {
			int delta=2*read(var0, var1, var2, var3, reader);
			deltas.add(delta);
			next+=delta;
		}
		return Segment.createDeltas(deltas, start);
	}

	public static Segment decompress2(byte[] bits) {
		BitReader reader=new BitReader(bits);
		long start=reader.readLong(64);
		int var0=reader.readInt(8);
		int var1=reader.readInt(8);
		int var2=reader.readInt(8);
		int var3=reader.readInt(8);
		long end=Segment.end(start);
		List<Integer> deltas=new ArrayList<>();
		long next=start;
		while (end>next) {
			int remainder=reader.read()?0:(reader.read()?2:4);
			int delta=remainder+6*read(var0, var1, var2, var3, reader);
			deltas.add(delta);
			next+=delta;
		}
		return Segment.createDeltas(deltas, start);
	}

	public static int read(int var0, int var1, int var2, int var3, BitReader reader) {
		int number=reader.readInt(var0);
		int bits=var0;
		if (reader.read()) {
			number|=reader.readInt(var1)<<bits;
			bits+=var1;
			if (reader.read()) {
				number|=reader.readInt(var2)<<bits;
				bits+=var2;
				if (reader.read()) {
					number|=reader.readInt(var3)<<bits;
					bits+=var3;
					while (reader.read()) {
						number|=reader.readInt(1)<<bits;
						bits+=1;
					}
				}
			}
		}
		return number;
	}

	public static void write(int number, int var0, int var1, int var2, int var3, BitWriter writer) {
		writer.writeInt(number, var0);
		number>>=var0;
		if (0!=number) {
			writer.write(true);
			writer.writeInt(number, var1);
			number>>=var1;
		}
		if (0!=number) {
			writer.write(true);
			writer.writeInt(number, var2);
			number>>=var2;
		}
		if (0!=number) {
			writer.write(true);
			writer.writeInt(number, var3);
			number>>=var3;
		}
		while (0!=number) {
			writer.write(true);
			writer.writeInt(number, 1);
			number>>=1;
		}
		writer.write(false);
	}
}
