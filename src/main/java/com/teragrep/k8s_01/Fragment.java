/*
   Kubernetes log forwarder k8s_01
   Copyright (C) 2023  Suomen Kanuuna Oy

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/

package com.teragrep.k8s_01;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public final class Fragment implements Consumer<ByteStream>, Clearable, Matchable, Byteable {

    private final ByteBuffer buffer;
    private FragmentState fragmentState;

    final BiFunction<ByteStream, ByteBuffer, ByteBuffer> parseRule;

    public final boolean isStub;

    Fragment() {
        this.isStub = true;
        this.buffer = ByteBuffer.allocateDirect(0);
        this.parseRule = (byteStream, bytebuffer) -> ByteBuffer.allocateDirect(0);
        this.fragmentState = FragmentState.EMPTY;
    }

    Fragment(int bufferSize, BiFunction<ByteStream, ByteBuffer, ByteBuffer> parseRule) {
        this.buffer = ByteBuffer.allocateDirect(bufferSize);
        this.fragmentState = FragmentState.EMPTY;
        this.parseRule = parseRule;
        this.isStub = false;
    }

    @Override
    public void accept(ByteStream byteStream) {
        if (fragmentState != FragmentState.EMPTY) {
            throw new IllegalStateException("fragmentState != FragmentState.EMPTY");
        }
        if (isStub) {
            throw new IllegalStateException("Fragment isStub");
        }
        parseRule.apply(byteStream, buffer);
        fragmentState = FragmentState.WRITTEN;
    }

    @Override
    public void clear() {
        buffer.clear();
        fragmentState = FragmentState.EMPTY;
    }

    @Override
    public String toString() {
        if (fragmentState != FragmentState.WRITTEN) {
            throw new IllegalStateException("fragmentState != FragmentState.WRITTEN");
        }
        String string = StandardCharsets.UTF_8.decode(buffer).toString();
        buffer.rewind();
        return string;
    }

    @Override
    public boolean matches(ByteBuffer other) {
        if (fragmentState != FragmentState.WRITTEN) {
            throw new IllegalStateException("fragmentState != FragmentState.WRITTEN");
        }
        return buffer.equals(other);
    }

    @Override
    public byte[] toBytes() {
        if (fragmentState != FragmentState.WRITTEN) {
            throw new IllegalStateException("fragmentState != FragmentState.WRITTEN");
        }
        final byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        buffer.rewind();
        return bytes;
    }
}
