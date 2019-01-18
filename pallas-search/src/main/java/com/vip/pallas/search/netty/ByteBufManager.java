package com.vip.pallas.search.netty;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.EmptyByteBuf;
import io.netty.buffer.PooledByteBufAllocator;

public class ByteBufManager {

	public static final int DEFAULT_BUF_SIZE = 8192;

	// Netty的PooledByteBufAllocator与Fiber不配，hack PooledByteBufAllocator，把FastThreadLocal改成继承TrueThreadLocal
	//public static ByteBufAllocator byteBufAllocator = OspSystemEnvProperties.USE_FIBER
	//		? new UnpooledByteBufAllocator(false) : new PooledByteBufAllocator(true);
	
	
	public static final ByteBufAllocator byteBufAllocator = new PooledByteBufAllocator(true);
			
	private static final Logger logger = LoggerFactory.getLogger(ByteBufManager.class);
	
	public static void initByteBufManager(){
		directBuffer(ByteBufManager.DEFAULT_BUF_SIZE).release();
	}

	/**
	 * 分配pooled direct ByteBuf
	 */
	public static ByteBuf directBuffer(int initialCapacity) {
		//如果要兼容堆内内存堆情况，使用buffer() 而不是directBuffer,让Netty自己多做一次判断
		int tempInitialCapacity = initialCapacity;
		if (tempInitialCapacity <= 0){
			tempInitialCapacity = 128;
		}
		return byteBufAllocator.directBuffer(tempInitialCapacity);
	}

	/**
	 * 克隆ByteBuf，共享同一段ByteBuf，引用数加1
	 */
	public static ByteBuf duplicateByteBuf(ByteBuf oldBuf) {
		return oldBuf.duplicate().retain();
	}

	/**
	 * 安全释放ByteBuf
	 */
	public static void release(ByteBuf buf) {
		if (buf != null && buf.refCnt() > 0) {
			buf.release();
		}
	}
	
	public static void deepRelease(ByteBuf buf) {
		
		if(buf == null || buf.refCnt()== 0 || buf instanceof EmptyByteBuf){ 
			return;
		} //#1110 EmptyByte的refCnt固定为1，无须release
		
		buf.release(buf.refCnt());
		
	}

	/**
	 * 安全释放ByteBuf, 成功时打印信息, 多用于在异常情况下的释放.
	 */
	public static void release(ByteBuf buf, String message) {
		if (buf != null && buf.refCnt() > 0) {
			buf.release();
			logger.info(message);
		}
	}
}