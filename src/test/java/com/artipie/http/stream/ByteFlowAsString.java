/*
 * MIT License
 *
 * Copyright (c) 2020 Artipie
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.artipie.http.stream;

import com.artipie.asto.Remaining;
import io.reactivex.Flowable;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import org.reactivestreams.Publisher;

/**
 * Turn a flow of ByteBuffer's into a string.
 *
 * @since 0.1
 */
public class ByteFlowAsString {

    /**
     * The flow of ByteBuffer's.
     */
    private final Publisher<ByteBuffer> publisher;

    /**
     * Ctor.
     * @param split The split.
     */
    public ByteFlowAsString(final ByteByByteSplit split) {
        this(Flowable.fromPublisher(split).flatMap(pub -> pub));
    }

    /**
     * Ctor.
     * @param publisher The flow
     */
    public ByteFlowAsString(final Publisher<ByteBuffer> publisher) {
        this.publisher = publisher;
    }

    /**
     * Fetch stream elements and turn them into a single string.
     * @return The string
     */
    public String value() {
        return new String(
            Flowable.fromPublisher(this.publisher)
                .toList()
                .blockingGet()
                .stream()
                .map(byteBuffer -> new Remaining(byteBuffer).bytes())
                .reduce(
                    (one, another) -> {
                        final byte[] res = new byte[one.length + another.length];
                        System.arraycopy(one, 0, res, 0, one.length);
                        System.arraycopy(another, 0, res, one.length, another.length);
                        return res;
                    }
                )
                .get(),
            StandardCharsets.UTF_8
        );
    }
}
