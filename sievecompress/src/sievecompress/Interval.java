package sievecompress;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Interval {
	public static class Read {
		private Interval bits=Interval.UNIT;
		private final Map<BigInteger, Interval> cache=new HashMap<>();
		private Interval choices=Interval.UNIT;

		public BigInteger read(Distribution distribution, BitReader reader) {
			cache.clear();
			for (BigInteger choice: distribution.frequencies.keySet()) {
				cache.put(choice, choices.choice(choice, distribution));
			}
			while (true) {
				for (Map.Entry<BigInteger, Interval> entry: cache.entrySet()) {
					Interval choices2=entry.getValue();
					if (choices2.contains(bits)) {
						choices=choices2;
						BigInteger choice=entry.getKey();
						cache.clear();
						return choice;
					}
				}
				bits=bits.choice(reader.read()?BigInteger.ONE:BigInteger.ZERO, Distribution.BINARY);
			}
		}
	}

	public static final Interval UNIT=new Interval(BigInteger.ONE, BigInteger.ZERO, BigInteger.ONE);

	public final BigInteger denominator;
	public final BigInteger left;
	public final BigInteger right;

	public Interval(BigInteger denominator, BigInteger left, BigInteger right) {
		Objects.requireNonNull(denominator, "denominator");
		Objects.requireNonNull(left, "left");
		Objects.requireNonNull(right, "right");
		if (0>=denominator.signum()) {
			throw new IllegalArgumentException();
		}
		if (0>left.signum()) {
			throw new IllegalArgumentException();
		}
		if (0<right.compareTo(denominator)) {
			throw new IllegalArgumentException();
		}
		if (0<=left.compareTo(right)) {
			throw new IllegalArgumentException();
		}
		BigInteger gcd=denominator.gcd(left).gcd(right);
		if (!BigInteger.ONE.equals(gcd)) {
			denominator=denominator.divide(gcd);
			left=left.divide(gcd);
			right=right.divide(gcd);
		}
		this.denominator=denominator;
		this.left=left;
		this.right=right;
	}

	public Interval choice(BigInteger choice, Distribution distribution) {
		BigInteger denominator2=distribution.cumulative();
		BigInteger left2=distribution.cumulativeLess(choice);
		BigInteger right2=distribution.cumulativeLessOrEqual(choice);
		return new Interval(
				denominator.multiply(denominator2),
				left.multiply(denominator2.subtract(left2)).add(left2.multiply(right)),
				left.multiply(denominator2.subtract(right2)).add(right2.multiply(right)));
	}

	public boolean contains(Interval interval) {
		return (0>=left.multiply(interval.denominator).compareTo(interval.left.multiply(denominator)))
				&& (0>=interval.right.multiply(denominator).compareTo(right.multiply(interval.denominator)));
	}

	@Override
	public String toString() {
		return "Interval("+left+"-"+right+"/"+denominator+")";
	}

	public void write(BitWriter writer) {
		BigInteger left2=left;
		BigInteger right2=right;
		while ((0<left2.signum())
				|| (0>right2.compareTo(denominator))) {
			left2=left2.shiftLeft(1);
			right2=right2.shiftLeft(1);
			if (0<=left2.compareTo(denominator)) {
				writer.write(true);
				left2=left2.subtract(denominator);
				right2=right2.subtract(denominator);
			}
			else if (0>=right2.compareTo(denominator)) {
				writer.write(false);
			}
			else if (0<=denominator.subtract(left2).compareTo(right2.subtract(denominator))) {
				writer.write(false);
				right2=denominator;
			}
			else {
				writer.write(true);
				left2=BigInteger.ZERO;
				right2=right2.subtract(denominator);
			}
		}
	}
}
