/*
 * Copyright 2014 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.vip.pallas.search.netty.http.handler;

import static com.vip.pallas.search.netty.http.handler.Lz4Constants.BLOCK_TYPE_COMPRESSED;
import static com.vip.pallas.search.netty.http.handler.Lz4Constants.BLOCK_TYPE_NON_COMPRESSED;
import static com.vip.pallas.search.netty.http.handler.Lz4Constants.COMPRESSED_LENGTH_OFFSET;
import static com.vip.pallas.search.netty.http.handler.Lz4Constants.COMPRESSION_LEVEL_BASE;
import static com.vip.pallas.search.netty.http.handler.Lz4Constants.DECOMPRESSED_LENGTH_OFFSET;
import static com.vip.pallas.search.netty.http.handler.Lz4Constants.DEFAULT_BLOCK_SIZE;
import static com.vip.pallas.search.netty.http.handler.Lz4Constants.HEADER_LENGTH;
import static com.vip.pallas.search.netty.http.handler.Lz4Constants.MAX_BLOCK_SIZE;
import static com.vip.pallas.search.netty.http.handler.Lz4Constants.MIN_BLOCK_SIZE;

import java.nio.ByteBuffer;
import java.util.concurrent.TimeUnit;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.ChannelPromise;
import io.netty.channel.ChannelPromiseNotifier;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.handler.codec.compression.CompressionException;
import io.netty.util.concurrent.EventExecutor;
import net.jpountz.lz4.LZ4Compressor;
import net.jpountz.lz4.LZ4Exception;
import net.jpountz.lz4.LZ4Factory;

/**
 * Compresses a {@link ByteBuf} using the LZ4 format.
 *
 * See original <a href="https://github.com/Cyan4973/lz4">LZ4 Github project</a>
 * and <a href="http://fastcompression.blogspot.ru/2011/05/lz4-explained.html">LZ4 block format</a>
 * for full description.
 *
 * Since the original LZ4 block format does not contains size of compressed block and size of original data
 * this encoder uses format like <a href="https://github.com/idelpivnitskiy/lz4-java">LZ4 Java</a> library
 * written by Adrien Grand and approved by Yann Collet (author of original LZ4 library).
 * *  * * * * * * * * * * * * * * * * * * * * * * * * * * * * *    
 * * Token *  compressed * Decompressed *  +  *  LZ4 compressed *
 * *       *    length   *    length    *     *      block      *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * 
 */
public class Lz4Encoder extends MessageToByteEncoder<ByteBuf> {
    private final int blockSize;

    /**
     * Underlying compressor in use.
     */
    private LZ4Compressor compressor;

    /**
     * Compression level of current LZ4 encoder (depends on {@link #compressedBlockSize}).
     */
    private final int compressionLevel;

    /**
     * Inner byte buffer for outgoing data.
     */
    private ByteBuf buffer;

    /**
     * Current length of buffered bytes in {@link #buffer}.
     */
    private int currentBlockLength;

    /**
     * Maximum size of compressed block with header.
     */
    private final int compressedBlockSize;

    /**
     * Indicates if the compressed stream has been finished.
     */
    private volatile boolean finished;

    /**
     * Used to interact with its {@link ChannelPipeline} and other handlers.
     */
    private volatile ChannelHandlerContext ctx;

    /**
     * Creates the fastest LZ4 encoder with default block size (64 KB)
     */
    public Lz4Encoder() {
        this(false);
    }

    /**
     * Creates a new LZ4 encoder with hight or fast compression, default block size (64 KB)
     * and xxhash hashing for Java, based on Yann Collet's work available at
     * <a href="http://code.google.com/p/xxhash/">Google Code</a>.
     *
     * @param highCompressor  if {@code true} codec will use compressor which requires more memory
     *                        and is slower but compresses more efficiently
     */
    public Lz4Encoder(boolean highCompressor) {
        this(LZ4Factory.fastestInstance(), highCompressor, DEFAULT_BLOCK_SIZE);
    }

    /**
     * Creates a new customizable LZ4 encoder.
     *
     * @param factory         user customizable {@link LZ4Factory} instance
     *                        which may be JNI bindings to the original C implementation, a pure Java implementation
     *                        or a Java implementation that uses the {@link sun.misc.Unsafe}
     * @param highCompressor  if {@code true} codec will use compressor which requires more memory
     *                        and is slower but compresses more efficiently
     * @param blockSize       the maximum number of bytes to try to compress at once,
     *                        must be >= 64 and <= 32 M
     */
    public Lz4Encoder(LZ4Factory factory, boolean highCompressor, int blockSize) {
        if (factory == null) {
            throw new IllegalArgumentException("factory can't be null.");
        }

        compressor = highCompressor ? factory.highCompressor() : factory.fastCompressor();

        compressionLevel = computeCompressionLevel(blockSize);
        this.blockSize = blockSize;
        currentBlockLength = 0;
        compressedBlockSize = HEADER_LENGTH + compressor.maxCompressedLength(blockSize);

        finished = false;
    }

    /**
     * Calculates compression level on the basis of block size.
     */
    private static int computeCompressionLevel(int blockSize) {
        if (blockSize < MIN_BLOCK_SIZE || blockSize > MAX_BLOCK_SIZE) {
            throw new IllegalArgumentException(String.format(
                    "blockSize: %d (expected: %d-%d)", blockSize, MIN_BLOCK_SIZE, MAX_BLOCK_SIZE));
        }
        int compressionLevel = 32 - Integer.numberOfLeadingZeros(blockSize - 1); // ceil of log2
        compressionLevel = Math.max(0, compressionLevel - COMPRESSION_LEVEL_BASE);
        return compressionLevel;
    }
    
    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) throws Exception {
        if (finished) {
            out.writeBytes(in);
            return;
        }

        int length = in.readableBytes();

        final ByteBuf thisBuffer = this.buffer;
        final int thisBlockSize = thisBuffer.capacity();
        while (currentBlockLength + length >= thisBlockSize) {
            final int tail = thisBlockSize - currentBlockLength;
            in.getBytes(in.readerIndex(), thisBuffer, currentBlockLength, tail);
            currentBlockLength = thisBlockSize;
            flushBufferedData(out);
            in.skipBytes(tail);
            length -= tail;
        }
        in.readBytes(thisBuffer, currentBlockLength, length);
        currentBlockLength += length;
    }

    private void flushBufferedData(ByteBuf out) {
        int thisCurrentBlockLength = this.currentBlockLength;
        if (thisCurrentBlockLength == 0) {
            return;
        }
        out.ensureWritable(compressedBlockSize);
        final int idx = out.writerIndex();
        int compressedLength;
        try {
            ByteBuffer outNioBuffer = out.internalNioBuffer(idx + HEADER_LENGTH, out.writableBytes() - HEADER_LENGTH);
            int pos = outNioBuffer.position();
            // We always want to start at position 0 as we take care of reusing the buffer in the encode(...) loop.
            compressor.compress(buffer.internalNioBuffer(0, thisCurrentBlockLength), outNioBuffer);
            compressedLength = outNioBuffer.position() - pos;
        } catch (LZ4Exception e) {
            throw new CompressionException(e);
        }
        final int blockType;
        if (compressedLength >= thisCurrentBlockLength) {
            blockType = BLOCK_TYPE_NON_COMPRESSED;
            compressedLength = thisCurrentBlockLength;
            out.setBytes(idx + HEADER_LENGTH, buffer, 0, thisCurrentBlockLength);
        } else {
            blockType = BLOCK_TYPE_COMPRESSED;
        }

        out.setByte(idx, (byte) (blockType | compressionLevel));
        out.setInt(idx + COMPRESSED_LENGTH_OFFSET, compressedLength);
        out.setInt(idx + DECOMPRESSED_LENGTH_OFFSET, thisCurrentBlockLength);
        out.writerIndex(idx + HEADER_LENGTH + compressedLength);
        thisCurrentBlockLength = 0;

        this.currentBlockLength = thisCurrentBlockLength;
    }

    private ChannelFuture finishEncode(final ChannelHandlerContext ctx, ChannelPromise promise) {
        if (finished) {
            promise.setSuccess();
            return promise;
        }
        finished = true;

        try {
            final ByteBuf footer = ctx.alloc().heapBuffer(
                    compressor.maxCompressedLength(currentBlockLength) + HEADER_LENGTH);
            flushBufferedData(footer);

            return ctx.writeAndFlush(footer, promise);
        } finally {
            cleanup();
        }
    }

    private void cleanup() {
        compressor = null;
        if (buffer != null) {
            buffer.release();
            buffer = null;
        }
    }

    /**
     * Returns {@code true} if and only if the compressed stream has been finished.
     */
    public boolean isClosed() {
        return finished;
    }

    /**
     * Close this {@link Lz4FrameEncoder} and so finish the encoding.
     *
     * The returned {@link ChannelFuture} will be notified once the operation completes.
     */
    public ChannelFuture close() {
        return close(getChannelHandlerContext().newPromise());
    }

    /**
     * Close this {@link Lz4FrameEncoder} and so finish the encoding.
     * The given {@link ChannelFuture} will be notified once the operation
     * completes and will also be returned.
     */
    public ChannelFuture close(final ChannelPromise promise) {
        ChannelHandlerContext thisCtx = getChannelHandlerContext();
        EventExecutor executor = thisCtx.executor();
        if (executor.inEventLoop()) {
            return finishEncode(thisCtx, promise);
        } else {
            executor.execute(() -> {
                ChannelFuture f = finishEncode(getChannelHandlerContext(), promise);
                f.addListener(new ChannelPromiseNotifier(promise));
            });
            return promise;
        }
    }

    @Override
    public void close(final ChannelHandlerContext ctx, final ChannelPromise promise) throws Exception {
        ChannelFuture f = finishEncode(ctx, ctx.newPromise());
        f.addListener((ChannelFutureListener) f1 -> ctx.close(promise));

        if (!f.isDone()) {
            // Ensure the channel is closed even if the write operation completes in time.
            ctx.executor().schedule((Runnable) () -> ctx.close(promise), 10, TimeUnit.SECONDS);
        }
    }

    private ChannelHandlerContext getChannelHandlerContext() {
        ChannelHandlerContext thisCtx = this.ctx;
        if (thisCtx == null) {
            throw new IllegalStateException("not added to a pipeline");
        }
        return thisCtx;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        this.ctx = ctx;
        // Ensure we use a heap based ByteBuf.
        buffer = Unpooled.wrappedBuffer(new byte[blockSize]);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        super.handlerRemoved(ctx);
        cleanup();
    }
}
