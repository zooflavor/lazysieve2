package sievecompress;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class Huffman {
	private static class Tree {
		private final Integer delta;
		private final int frequency;
		private final Tree left;
		private final Tree right;

		public Tree(Integer delta, int frequency) {
			this.delta=delta;
			this.frequency=frequency;
			left=null;
			right=null;
		}

		public Tree(Tree left, Tree right) {
			this.left=left;
			this.right=right;
			delta=null;
			frequency=left.frequency+right.frequency;
		}

		public int readDelta(BitReader reader) {
			if (null==delta) {
				if (reader.read()) {
					return right.readDelta(reader);
				}
				else {
					return left.readDelta(reader);
				}
			}
			else {
				return delta;
			}
		}

		public static Tree readTree(BitReader reader) {
			if (reader.read()) {
				Tree left=readTree(reader);
				Tree right=readTree(reader);
				return new Tree(left, right);
			}
			else {
				int delta=reader.readInt(Segment.DELTA_BITS);
				return new Tree(delta, 0);
			}
		}

		public void write(long bits, int length, long[] deltaBits, int[] deltaLengths, BitWriter writer) {
			if (null==delta) {
				writer.write(true);
				left.write(bits, length+1, deltaBits, deltaLengths, writer);
				right.write(bits|(1l<<length), length+1, deltaBits, deltaLengths, writer);
			}
			else {
				writer.write(false);
				writer.writeInt(delta, Segment.DELTA_BITS);
				deltaBits[delta]=bits;
				deltaLengths[delta]=length;
			}
		}
	}

	private Huffman() {
	}

	public static Columns<byte[]> columns() {
		return new Columns<byte[]>() {
			@Override
			public byte[] bits(byte[] compressed) {
				return compressed;
			}

			@Override
			public byte[] compress(Segment segment) throws Throwable {
				return Huffman.compress(segment);
			}

			@Override
			public List<String> data(Segment segment, byte[] compressed) throws Throwable {
				return Arrays.asList(Columns.formatCompression(compressed));
			}

			@Override
			public Segment decompress(byte[] bits) throws Throwable {
				return Huffman.decompress(bits);
			}

			@Override
			public List<String> header() throws Throwable {
				return Arrays.asList("huffman/uncompressed");
			}
		};
	}

	public static byte[] compress(Segment segment) {
		BitWriter writer=new BitWriter();
		writer.writeLong(segment.start, 64);
		PriorityQueue<Tree> queue=new PriorityQueue<>(Comparator.comparingInt(tree->tree.frequency));
		segment.frequencies.forEach((delta, frequency)->queue.add(new Tree(delta, frequency)));
		while (1<queue.size()) {
			queue.add(new Tree(queue.poll(), queue.poll()));
		}
		Tree tree=queue.poll();
		long[] deltaBits=new long[Segment.DELTAS];
		int[] deltaLengths=new int[deltaBits.length];
		tree.write(0l, 0, deltaBits, deltaLengths, writer);
		segment.deltas.forEach((delta)->{
			writer.writeLong(deltaBits[delta], deltaLengths[delta]);
		});
		return writer.toByteArray();
	}

	public static Segment decompress(byte[] bits) {
		BitReader reader=new BitReader(bits);
		long start=reader.readLong(64);
		long end=Segment.end(start);
		Tree tree=Tree.readTree(reader);
		long next=start;
		List<Integer> deltas=new ArrayList<>();
		while (end>next) {
			int delta=tree.readDelta(reader);
			deltas.add(delta);
			next+=delta;
		}
		return Segment.createDeltas(deltas, start);
	}
}
