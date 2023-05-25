package zad1;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public class Futil {
    public static void processDir(String dirName, String resultFileName) {
        try {
            Path start = Paths.get(dirName);
            Path result = Paths.get(resultFileName);

            if(!Files.exists(result)) {
                Files.createFile(result);
            }

            Charset input = Charset.forName("Cp1250");
            Charset output = StandardCharsets.UTF_8;

            try (FileChannel outputFile = FileChannel.open(result, StandardOpenOption.WRITE)) {
                Files.walkFileTree(start, new SimpleFileVisitor<Path>(){
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) throws IOException {
                        try(FileChannel inputFileChannel = FileChannel.open(file,StandardOpenOption.READ)) {
                            ByteBuffer byteBuffer = ByteBuffer.allocate((int) inputFileChannel.size());
                            inputFileChannel.read(byteBuffer);
                            byteBuffer.flip();
                            String content = input.decode(byteBuffer).toString();
                            ByteBuffer buffer = output.encode(content);
                            outputFile.write(buffer);
                        }

                        return FileVisitResult.CONTINUE;
                    }
                });
            }
        }catch (IOException ignored) {
        }
    }
}
