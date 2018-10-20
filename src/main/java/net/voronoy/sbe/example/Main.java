package net.voronoy.sbe.example;

import baseline.*;
import org.agrona.concurrent.UnsafeBuffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.nio.file.StandardOpenOption.CREATE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;

public class Main {

    private static final int CAPACITY = 64;

    public static void main(String[] args) throws IOException {
        MessageHeaderEncoder headerEncoder = new MessageHeaderEncoder();
        CarEncoder encoder = new CarEncoder();

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(CAPACITY);
        UnsafeBuffer unsafeBuffer = new UnsafeBuffer(byteBuffer);

        encoder.wrapAndApplyHeader(unsafeBuffer, 0, headerEncoder)
                .activationCode("123")
                .available(BooleanType.T)
                .code(Model.A)
                .modelYear(2016);

        encoder.engine()
                .capacity(2)
                .efficiency((byte) 2)
                .fuel(5);

        int encodedLength = headerEncoder.encodedLength() + encoder.encodedLength();

        System.out.println("Writing to file started...");

        Path path = Paths.get("resources/car.bin");
        try (FileChannel fileChannel = FileChannel.open(path, CREATE, WRITE)) {
            byteBuffer.limit(encodedLength);
            fileChannel.write(byteBuffer);
        }

        System.out.println("Written to " + path.toAbsolutePath());

        MessageHeaderDecoder headerDecoder = new MessageHeaderDecoder();
        CarDecoder decoder = new CarDecoder();

        ByteBuffer decodingBuffer = ByteBuffer.allocateDirect(CAPACITY);
        UnsafeBuffer decodingUnsafeBuffer = new UnsafeBuffer(decodingBuffer);

        System.out.println("Read binary file " + path.toAbsolutePath());

        try (FileChannel fileChannel = FileChannel.open(path, READ)) {
            fileChannel.read(decodingBuffer);
        }

        headerDecoder.wrap(decodingUnsafeBuffer, 0);

        int decodingOffset = headerDecoder.encodedLength();
        int blockLength = headerDecoder.blockLength();
        int version = headerDecoder.version();

        decoder.wrap(decodingUnsafeBuffer, decodingOffset, blockLength, version);

        System.out.println("Printing decoded object...");
        System.out.println(decoder);
    }

}
