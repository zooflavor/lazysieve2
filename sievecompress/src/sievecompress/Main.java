package sievecompress;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Collectors;

public class Main {
	private final Path dir;

	private Main(Path dir) {
		this.dir=dir;
	}

	private void main() throws Throwable {
		List<Columns<?>> columns=Arrays.asList(
				Segment.columns(),
				Huffman.columns(),
				Variable.columns1(),
				Variable.columns2(),
				Block.columns(Block.BITMAP),
				Columns.empty()
		);
		for (Columns<?> columns2: columns) {
			for (String header: columns2.header()) {
				System.out.print(header);
				System.out.print(",");
			}
		}
		System.out.println();
		boolean decompress=true;
		for (Path file: Files.list(dir).collect(Collectors.toCollection(TreeSet::new))) {
			if (!Files.isRegularFile(file)) {
				continue;
			}
			Segment segment=Segment.read(file);
			for (Columns<?> columns2: columns) {
				for (String data: columns2.data(segment, decompress)) {
					System.out.print(data);
					System.out.print(",");
				}
				System.out.flush();
			}
			System.out.println();
		}
	}

	public static void main(String[] args) throws Throwable {
		new Main(Paths.get(args[0])).main();
	}
}
