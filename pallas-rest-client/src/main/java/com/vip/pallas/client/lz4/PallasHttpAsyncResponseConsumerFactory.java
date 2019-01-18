/**
 * Copyright 2019 vip.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.vip.pallas.client.lz4;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.ContentTooLongException;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.entity.ContentBufferEntity;
import org.apache.http.nio.protocol.AbstractAsyncResponseConsumer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.nio.util.ByteBufferAllocator;
import org.apache.http.nio.util.HeapByteBufferAllocator;
import org.apache.http.nio.util.SimpleInputBuffer;
import org.apache.http.protocol.HttpContext;
import org.elasticsearch.client.HttpAsyncResponseConsumerFactory;

import net.jpountz.lz4.LZ4Factory;
import net.jpountz.lz4.LZ4FastDecompressor;

public class PallasHttpAsyncResponseConsumerFactory implements HttpAsyncResponseConsumerFactory {

    // default buffer limit is 100MB
    static final int DEFAULT_BUFFER_LIMIT = 100 * 1024 * 1024;    
    // Underlying decompressor in use.
    private static LZ4FastDecompressor decompressor = LZ4Factory.fastestInstance().fastDecompressor();
    
	@Override
	public HttpAsyncResponseConsumer<HttpResponse> createHttpAsyncResponseConsumer() {
		return new Lz4HeapBufferedAsyncResponseConsumer(DEFAULT_BUFFER_LIMIT);
	}

	static class Lz4HeapBufferedAsyncResponseConsumer extends AbstractAsyncResponseConsumer<HttpResponse> {

	    private final int bufferLimitBytes;
	    private volatile HttpResponse response;
	    private volatile SimpleInputBuffer buf;
		private volatile String encoding;

	    /**
	     * Creates a new instance of this consumer with the provided buffer limit
	     */
	    public Lz4HeapBufferedAsyncResponseConsumer(int bufferLimit) {
	        if (bufferLimit <= 0) {
	            throw new IllegalArgumentException("bufferLimit must be greater than 0");
	        }
	        this.bufferLimitBytes = bufferLimit;
	    }

	    /**
	     * Get the limit of the buffer.
	     */
	    public int getBufferLimit() {
	        return bufferLimitBytes;
	    }

	    @Override
	    protected void onResponseReceived(HttpResponse response) throws HttpException, IOException {
	        this.response = response;
	    }

	    @Override
	    protected void onEntityEnclosed(HttpEntity entity, ContentType contentType) throws IOException {
	        long len = entity.getContentLength();
	        if (len > bufferLimitBytes) {
	            throw new ContentTooLongException("entity content is too long [" + len +
	                    "] for the configured buffer limit [" + bufferLimitBytes + "]");
	        }
	        if (len < 0) {
	            len = 4096;
	        }
	        this.buf = new SimpleInputBuffer((int) len, getByteBufferAllocator());
	        this.response.setEntity(new ContentBufferEntity(entity, this.buf));
	        this.encoding = (entity.getContentEncoding() != null) ? entity.getContentEncoding().getValue():null;
	    }

	    /**
	     * Returns the instance of {@link ByteBufferAllocator} to use for content buffering.
	     * Allows to plug in any {@link ByteBufferAllocator} implementation.
	     */
	    protected ByteBufferAllocator getByteBufferAllocator() {
	        return HeapByteBufferAllocator.INSTANCE;
	    }

	    @Override
	    protected HttpResponse buildResult(HttpContext context) throws Exception {
	        if ("lz4".equalsIgnoreCase(encoding)) {
	        	decodeLz4Block();
	        }
	        return response;
	    }

	    @Override
	    protected void releaseResources() {
	        response = null;
	    }

	    @Override
	    protected void onContentReceived(ContentDecoder decoder, IOControl ioctrl) throws IOException {
	        this.buf.consumeContent(decoder);
	    }
	    
	    protected void decodeLz4Block() throws IOException {
	    	List<byte[]> decodedList = new ArrayList<>();
	    	int totalLength = 0;
	    	while (this.buf.hasData() && this.buf.length() > Lz4Constants.HEADER_LENGTH) {
		    		byte[] header = new byte[Lz4Constants.HEADER_LENGTH];
		    		this.buf.read(header);
					int compressedLength = makeInt(header[1], header[2], header[3], header[4]);

	                if (compressedLength < 0 || compressedLength > Lz4Constants.MAX_BLOCK_SIZE) {
	                    throw new IllegalStateException(String.format(
	                            "invalid compressedLength: %d (expected: 0-%d)",
	                            compressedLength, Lz4Constants.MAX_BLOCK_SIZE));
	                }

					int decompressedLength = makeInt(header[5], header[6], header[7], header[8]);

					int token = header[0];
					final int compressionLevel = (token & 0x0F) + Lz4Constants.COMPRESSION_LEVEL_BASE;
	                final int maxDecompressedLength = 1 << compressionLevel;
	                if (decompressedLength < 0 || decompressedLength > maxDecompressedLength) {
	                    throw new IllegalStateException(String.format(
	                            "invalid decompressedLength: %d (expected: 0-%d)",
	                            decompressedLength, maxDecompressedLength));
	                }

					int blockType = token & 0xF0;
	                boolean nonCompressedMismatch = blockType == Lz4Constants.BLOCK_TYPE_NON_COMPRESSED && decompressedLength != compressedLength;
	                boolean compressedLenthIsZero = decompressedLength != 0 && compressedLength == 0;
	                boolean decompressedLenthIsZero = decompressedLength == 0 && compressedLength != 0;
	                if (nonCompressedMismatch || compressedLenthIsZero || decompressedLenthIsZero) {
	                    throw new IllegalStateException(String.format(
	                            "stream corrupted: compressedLength(%d) and decompressedLength(%d) mismatch",
	                            compressedLength, decompressedLength));
	                }

                    switch (blockType) {
                        case Lz4Constants.BLOCK_TYPE_NON_COMPRESSED: 
                        	totalLength += decodeNonCompressed(decodedList, compressedLength, decompressedLength); 
                            break;
                        case Lz4Constants.BLOCK_TYPE_COMPRESSED: 
                        	totalLength += decodeCompressed(decodedList, compressedLength, decompressedLength); 
                            break;
                        default:
                            throw new IllegalStateException(String.format(
                                    "unexpected blockType: %d (expected: %d or %d)",
                                    blockType, Lz4Constants.BLOCK_TYPE_NON_COMPRESSED,Lz4Constants.BLOCK_TYPE_COMPRESSED));
                    }
	    	 }
	    	this.buf.reset();
	    	this.buf.shutdown();
	    	byte[] totalDecodeBlock = new byte[totalLength];
	    	int destPos = 0;
	    	for (byte[] block : decodedList) {
	    		System.arraycopy(block, 0, totalDecodeBlock, destPos, block.length);
	    		destPos += block.length;
			}
	    	this.response.setEntity(new ByteArrayEntity(totalDecodeBlock));
	    }

		public int decodeCompressed(List<byte[]> decodedList,  int compressedLength, int decompressedLength) throws IOException {
			byte[] toDecodeBlock = new byte[compressedLength];
			buf.read(toDecodeBlock, 0, compressedLength);
			byte[] decompressBlock = decompressor.decompress(toDecodeBlock, decompressedLength);
			decodedList.add(decompressBlock);
			return decompressedLength;
		}

		public int decodeNonCompressed(List<byte[]> decodedList, int compressedLength, int decompressedLength) throws IOException {
			byte[] toDecodeBlock = new byte[compressedLength];
			buf.read(toDecodeBlock, 0, compressedLength);
			decodedList.add(toDecodeBlock);
			return decompressedLength;
		}
		
	    private static int makeInt(int b3, int b2, int b1, int b0) {
	        return (((b3 & 0xff) << 24) |
	                ((b2 & 0xff) << 16) |
	                ((b1 & 0xff) <<  8) |
	                (b0 & 0xff      ));
	    }
	}

}