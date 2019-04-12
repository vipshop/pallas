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
