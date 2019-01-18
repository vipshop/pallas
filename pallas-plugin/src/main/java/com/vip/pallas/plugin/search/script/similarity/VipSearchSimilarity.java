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

package com.vip.pallas.plugin.search.script.similarity;

import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.SmallFloat;
import org.elasticsearch.common.settings.Settings;

public class VipSearchSimilarity extends TFIDFSimilarity {

	/** Cache of decoded bytes. */
	private static final float[] NORM_TABLE = new float[256];

	static {
		for (int i = 0; i < 256; i++) {
			NORM_TABLE[i] = SmallFloat.byte315ToFloat((byte) i);
		}
	}

	/** Sole constructor: parameter-free */
	public VipSearchSimilarity() {
	}

	public VipSearchSimilarity(Settings settings) {
	}

	/** Implemented as <code>overlap / maxOverlap</code>. */
	@Override
	public float coord(int overlap, int maxOverlap) {
		return overlap / (float) maxOverlap;
	}

	/** Implemented as <code>1/sqrt(sumOfSquaredWeights)</code>. */
	@Override
	public float queryNorm(float sumOfSquaredWeights) {
		return (float) (1.0 / Math.sqrt(sumOfSquaredWeights));
	}

	/**
	 * Encodes a normalization factor for storage in an index.
	 * <p>
	 * The encoding uses a three-bit mantissa, a five-bit exponent, and the zero-exponent point at 15, thus representing
	 * values from around 7x10^9 to 2x10^-9 with about one significant decimal digit of accuracy. Zero is also
	 * represented. Negative numbers are rounded up to zero. Values too large to represent are rounded down to the
	 * largest representable value. Positive values too small to represent are rounded up to the smallest positive
	 * representable value.
	 * 
	 * @see org.apache.lucene.document.Field#setBoost(float)
	 * @see SmallFloat
	 */
	@Override
	public final long encodeNormValue(float f) {
		return SmallFloat.floatToByte315(f);
	}

	/**
	 * Decodes the norm value, assuming it is a single byte.
	 * 
	 * @see #encodeNormValue(float)
	 */
	@Override
	public final float decodeNormValue(long norm) {
		return NORM_TABLE[(int) (norm & 0xFF)]; // & 0xFF maps negative bytes to positive above 127
	}

	/**
	 * Implemented as <code>state.getBoost()*lengthNorm(numTerms)</code>, where <code>numTerms</code> is
	 * {@link FieldInvertState#getLength()} if {@link #setDiscountOverlaps} is false, else it's
	 * {@link FieldInvertState#getLength()} - {@link FieldInvertState#getNumOverlap()}.
	 *
	 * @lucene.experimental
	 */
	@Override
	public float lengthNorm(FieldInvertState state) {
		final int numTerms;
		if (discountOverlaps)
			numTerms = state.getLength() - state.getNumOverlap();
		else
			numTerms = state.getLength();
		return state.getBoost() * ((float) (1.0 / Math.sqrt(numTerms)));
	}

	/** Implemented as <code>sqrt(freq)</code>. */
	@Override
	public float tf(float freq) {
		// return (float)Math.sqrt(freq);
		return 1;
	}

	/** Implemented as <code>1 / (distance + 1)</code>. */
	@Override
	public float sloppyFreq(int distance) {
		return 1.0f / (distance + 1);
	}

	/** The default implementation returns <code>1</code> */
	@Override
	public float scorePayload(int doc, int start, int end, BytesRef payload) {
		return 1;
	}

	/** Implemented as <code>log(numDocs/(docFreq+1)) + 1</code>. */
	@Override
	public float idf(long docFreq, long numDocs) {
		return (float) (Math.log(numDocs / (double) (docFreq + 1)) + 1.0);
	}

	/**
	 * True if overlap tokens (tokens with a position of increment of zero) are discounted from the document's length.
	 */
	protected boolean discountOverlaps = true;

	/**
	 * Determines whether overlap tokens (Tokens with 0 position increment) are ignored when computing norm. By default
	 * this is true, meaning overlap tokens do not count when computing norms.
	 *
	 * @lucene.experimental
	 *
	 * @see #computeNorm
	 */
	public void setDiscountOverlaps(boolean v) {
		discountOverlaps = v;
	}

	/**
	 * Returns true if overlap tokens are discounted from the document's length.
	 * @see #setDiscountOverlaps
	 */
	public boolean getDiscountOverlaps() {
		return discountOverlaps;
	}

	@Override
	public String toString() {
		return "vipSearchSimilarity";
	}
}