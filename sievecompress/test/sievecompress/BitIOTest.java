package sievecompress;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class BitIOTest {
	@Test
	public void test() throws Throwable {
		for (int length=0; 64>=length; ++length) {
			for (int base: new int[]{3, 5, 7}) {
				long value=base;
				for (int ii=length; 0<ii; --ii) {
					value*=base;
				}
				if (64!=length) {
					value&=(1l<<length)-1l;
				}
				BitWriter writer=new BitWriter();
				for (int ii=0; length>ii; ++ii) {
					writer.write(0l!=(value&(1l<<ii)));
				}
				byte[] bits=writer.toByteArray();
				BitReader reader=new BitReader(bits);
				assertEquals(8*(length/8)+((0==(length&7))?0:8), 8*bits.length);
				long value2=0l;
				for (int ii=0; length>ii; ++ii) {
					if (reader.read()) {
						value2|=1l<<ii;
					}
				}
				assertEquals(value, value2);
			}
		}
	}
}
