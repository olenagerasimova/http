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
package com.artipie.http.rt;

import com.artipie.http.Response;
import com.artipie.http.Slice;
import com.artipie.http.rs.RsStatus;
import com.artipie.http.rs.RsWithBody;
import com.artipie.http.rs.RsWithStatus;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.cactoos.list.ListOf;
import org.reactivestreams.Publisher;

/**
 * Routing slice.
 * <p>
 * {@link Slice} implementation which redirect requests to {@link Slice}
 * in {@link SliceRoute.Path} if {@link RtRule} matched.<br/>
 * Usage:
 * <pre><code>
 * new SliceRoute(
 *   new SliceRoute.Path(
 *     new RtRule.ByMethod("GET"), new DownloadSlice(storage)
 *   ),
 *   new SliceRoute.Path(
 *     new RtRule.ByMethod("PUT"), new UploadSlice(storage)
 *   )
 * );
 * </code></pre>
 * </p>
 * @since 0.5
 */
public final class SliceRoute implements Slice {

    /**
     * Routes.
     */
    private final List<Path> routes;

    /**
     * New slice route.
     * @param routes Routes
     */
    public SliceRoute(final Path... routes) {
        this(new ListOf<>(routes));
    }

    /**
     * New slice route.
     * @param routes Routes
     */
    public SliceRoute(final List<Path> routes) {
        this.routes = routes;
    }

    @Override
    public Response response(final String line,
        final Iterable<Map.Entry<String, String>> headers,
        final Publisher<ByteBuffer> body) {
        return this.routes.stream()
            .map(item -> item.response(line, headers, body))
            .filter(Optional::isPresent)
            .map(Optional::get)
            .findFirst()
            .orElse(
                new RsWithBody(
                    new RsWithStatus(RsStatus.NOT_FOUND),
                    "not found", StandardCharsets.UTF_8
                )
            );
    }

    /**
     * Route path.
     * @since 0.10
     */
    public interface Path {
        /**
         * Try respond.
         * @param line Request line
         * @param headers Headers
         * @param body Body
         * @return Response if passed routing rule
         */
        Optional<Response> response(
            String line,
            Iterable<Map.Entry<String, String>> headers,
            Publisher<ByteBuffer> body
        );
    }
}
