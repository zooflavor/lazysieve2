package sievecompress;

import java.math.BigInteger;
import java.util.Map;
import java.util.TreeMap;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class ArithmeticTest {
	@Test
	public void test() {
		Map<BigInteger, BigInteger> frequencies=new TreeMap<>();
		frequencies.put(BigInteger.valueOf(2), BigInteger.valueOf(3));
		frequencies.put(BigInteger.valueOf(4), BigInteger.valueOf(7));
		frequencies.put(BigInteger.valueOf(6), BigInteger.valueOf(13));
		Distribution distribution=new Distribution(frequencies);
		BitWriter writer=new BitWriter();
		for (int ii=7; 0<ii; --ii) {
			Interval interval=Interval.UNIT;
			for (int jj=17; 0<jj; --jj) {
				interval=interval.choice(BigInteger.valueOf(2*(1+(ii*5+jj)%3)), distribution);
			}
			interval.write(writer);
		}
		BitReader reader=new BitReader(writer.toByteArray());
		for (int ii=7; 0<ii; --ii) {
			Interval.Read read=new Interval.Read();
			for (int jj=17; 0<jj; --jj) {
				assertEquals(BigInteger.valueOf(2*(1+(ii*5+jj)%3)), read.read(distribution, reader));
			}
		}
	}
}
