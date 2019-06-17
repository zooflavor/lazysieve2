package sievecompress;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;
import org.junit.Assert;

public class Segment {
	public static final int DELTA_BITS=11;
	public static final int DELTAS=1<<DELTA_BITS;
	public static final long UNCOMPRESSED_SIZE=8l+(1l<<26); //start+table

	public final List<Integer> deltas;
	public final long end;
	public final NavigableMap<Integer, Integer> frequencies;
	public final List<Long> primes;
	public final long start;

	public Segment(List<Integer> deltas, long end, NavigableMap<Integer, Integer> frequencies, List<Long> primes,
				   long start) {
		this.deltas=deltas;
		this.end=end;
		this.frequencies=frequencies;
		this.primes=primes;
		this.start=start;
	}

	public static void assertSegment(Segment expected, Segment actual) {
		Assert.assertEquals(expected.start, actual.start);
		Assert.assertEquals(expected.deltas, actual.deltas);
		Assert.assertEquals(expected.primes, actual.primes);
	}

	public static Columns<byte[]> columns() {
		return new Columns<byte[]>() {
			@Override
			public byte[] bits(byte[] compressed) {
				return null;
			}

			@Override
			public byte[] compress(Segment segment) throws Throwable {
				return null;
			}

			@Override
			public List<String> data(Segment segment, byte[] compressed) throws Throwable {
				return Arrays.asList(
						String.format("%1$016x", segment.start),
						String.format("%1$7.4f", segment.lnEnd()));
			}

			@Override
			public Segment decompress(byte[] bits) throws Throwable {
				return null;
			}

			@Override
			public List<String> header() throws Throwable {
				return Arrays.asList("segment start", "ln(end)");
			}
		};
	}

	public static Segment createDeltas(List<Integer> deltas, long start) {
		long next=start;
		List<Long> primes=new ArrayList<>(deltas.size()-1);
		for (int ii=0; deltas.size()-1>ii; ++ii) {
			next+=deltas.get(ii);
			primes.add(next);
		}
		return createPrimes(primes, start);
	}

	public static Segment createPrimes(Collection<Long> primes, long start) {
		long end=end(start);
		List<Integer> deltas=new ArrayList<>(primes.size()+1);
		long last=start;
		for (Long prime: primes) {
			deltas.add((int)(prime-last));
			last=prime;
		}
		deltas.add((int)(end-last));
		int[] frequenciesArray=new int[2048];
		for (Integer delta: deltas) {
			++frequenciesArray[delta];
		}
		NavigableMap<Integer, Integer> frequencies=new TreeMap<>();
		for (int ii=0; frequenciesArray.length>ii; ++ii) {
			if (0!=frequenciesArray[ii]) {
				frequencies.put(ii, frequenciesArray[ii]);
			}
		}
		return new Segment(
				Collections.unmodifiableList(deltas),
				end,
				Collections.unmodifiableNavigableMap(frequencies),
				Collections.unmodifiableList(new ArrayList<>(primes)),
				start);
	}

	public static long end(long start) {
		return start+(1l<<30);
	}

	public double lnEnd() {
		return 20*Math.log(2)+Math.log(end>>>20);
	}

	public static Segment read(Path path) throws IOException {
		long start=Long.parseUnsignedLong(path.getFileName().toString().substring(7), 16);
		List<Long> primes=new ArrayList<>();
		long next=start;
		try (InputStream is=Files.newInputStream(path);
			 InputStream bis=new BufferedInputStream(is)) {
			for (int ii=1<<26; 0<ii; --ii) {
				int rr=bis.read();
				if (0>rr) {
					throw new EOFException();
				}
				for (int jj=8; 0<jj; --jj, rr>>=1, next+=2) {
					if (0==(rr&1)) {
						primes.add(next);
					}
				}
			}
		}
		return createPrimes(primes, start);
	}
}
