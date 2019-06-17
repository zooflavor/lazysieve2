package sievecompress;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;

public class Distribution {
	public static final Distribution BINARY;

	static {
		Map<BigInteger, BigInteger> binary=new TreeMap<>();
		binary.put(BigInteger.ZERO, BigInteger.ONE);
		binary.put(BigInteger.ONE, BigInteger.ONE);
		BINARY=new Distribution(binary);
	}

	public final NavigableMap<BigInteger, BigInteger> cumulative;
	public final NavigableMap<BigInteger, BigInteger> frequencies;

	public Distribution(Map<BigInteger, BigInteger> frequencies) {
		NavigableMap<BigInteger, BigInteger> frequencies2=new TreeMap<>();
		BigInteger gcd=null;
		for (Iterator<Map.Entry<BigInteger, BigInteger>> iterator=frequencies.entrySet().iterator();
			 	iterator.hasNext(); ) {
			Map.Entry<BigInteger, BigInteger> entry=iterator.next();
			BigInteger key=Objects.requireNonNull(entry.getKey(), "key");
			BigInteger value=Objects.requireNonNull(entry.getValue(), "value");
			if (0>value.signum()) {
				throw new IllegalArgumentException();
			}
			if (0!=value.signum()) {
				frequencies2.put(key, value);
				if (null==gcd) {
					gcd=value;
				}
				else {
					gcd=gcd.gcd(value);
				}
			}
		}
		if (null==gcd) {
			throw new IllegalArgumentException();
		}
		if (!BigInteger.ONE.equals(gcd)) {
			for (Map.Entry<BigInteger, BigInteger> entry: frequencies2.entrySet()) {
				entry.setValue(entry.getValue().divide(gcd));
			}
		}
		this.frequencies=Collections.unmodifiableNavigableMap(new TreeMap<>(frequencies2));
		NavigableMap<BigInteger, BigInteger> cumulative=new TreeMap<>(this.frequencies);
		BigInteger cumulative2=BigInteger.ZERO;
		for (Map.Entry<BigInteger, BigInteger> entry: cumulative.entrySet()) {
			cumulative2=cumulative2.add(entry.getValue());
			entry.setValue(cumulative2);
		}
		this.cumulative=Collections.unmodifiableNavigableMap(cumulative);
	}

	public BigInteger cumulative() {
		return cumulative.lastEntry().getValue();
	}

	public BigInteger cumulativeLess(BigInteger key) {
		Map.Entry<BigInteger, BigInteger> entry=cumulative.lowerEntry(key);
		return (null==entry)?BigInteger.ZERO:entry.getValue();
	}

	public BigInteger cumulativeLessOrEqual(BigInteger key) {
		return cumulative.get(key);
	}

	@Override
	public String toString() {
		return "Distribution(frequencies: "+frequencies+", cumulative: "+cumulative+")";
	}
}
