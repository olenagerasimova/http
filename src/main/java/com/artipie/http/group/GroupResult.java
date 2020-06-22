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
package com.artipie.http.group;

import com.artipie.http.Connection;
import com.artipie.http.Headers;
import com.artipie.http.rs.RsStatus;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletionStage;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Response result.
 * <p>
 * The result of {@link GroupResponse}, it's waiting in order for all previous responses
 * to be completed, and may be replied to connection or cancelled.
 * </p>
 * @since 0.11
 */
final class GroupResult {

    /**
     * Subscriber which cancel publisher subscription.
     * @checkstyle AnonInnerLengthCheck (25 lines)
     */
    private static final Subscriber<? super Object> CANCEL_SUB = new Subscriber<Object>() {
        @Override
        public void onSubscribe(final Subscription sub) {
            sub.cancel();
        }

        @Override
        public void onNext(final Object obj) {
            // nothing to do
        }

        @Override
        public void onError(final Throwable err) {
            // nothing to do
        }

        @Override
        public void onComplete() {
            // nothing to do
        }
    };

    /**
     * Response status.
     */
    private final RsStatus status;

    /**
     * Response headers.
     */
    private final Headers headers;

    /**
     * Body publisher.
     */
    private final Publisher<ByteBuffer> body;

    /**
     * New response result.
     * @param status Response status
     * @param headers Response headers
     * @param body Body publisher
     */
    GroupResult(final RsStatus status, final Headers headers,
        final Publisher<ByteBuffer> body) {
        this.status = status;
        this.headers = headers;
        this.body = body;
    }

    /**
     * Replay response to connection.
     * @param con Connection
     * @return Future
     */
    public CompletionStage<Void> replay(final Connection con) {
        return con.accept(this.status, this.headers, this.body);
    }

    /**
     * Check if response was successes.
     * @return True if success
     */
    public boolean success() {
        final int code = Integer.parseInt(this.status.code());
        // @checkstyle MagicNumberCheck (1 line)
        return code >= 200 && code < 300;
    }

    /**
     * Cancel response body stream.
     */
    void cancel() {
        this.body.subscribe(GroupResult.CANCEL_SUB);
    }
}
