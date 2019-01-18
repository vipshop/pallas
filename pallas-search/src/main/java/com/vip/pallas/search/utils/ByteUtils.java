package com.vip.pallas.search.utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.EmptyByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

public class ByteUtils {
	public static byte[] toBytes(String str) {
		if (str == null) {
			return null;
		} else {
			return str.getBytes(CharsetUtil.UTF_8);
		}

	}

	public static ByteBuf toByteBuf(String str) {
		if (str == null) {
			return Unpooled.EMPTY_BUFFER;
		}
		return Unpooled.wrappedBuffer(ByteUtils.toBytes(str));
	}

	
	public static void safeRelease(ByteBuf buf) {
		if (buf != null && buf.refCnt() > 0) {
			ReferenceCountUtil.safeRelease(buf);
		}
	}
	
	
	public static void deepSafeRelease(ByteBuf buf) {
		//EmptyByte的refCnt固定为1，无须release
		if(buf == null || buf.refCnt()== 0 || buf instanceof EmptyByteBuf){ 
			return;
		}
		ReferenceCountUtil.safeRelease(buf,buf.refCnt());
		
	}
}
