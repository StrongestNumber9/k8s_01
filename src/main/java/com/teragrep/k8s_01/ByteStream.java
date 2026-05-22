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

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.function.Supplier;

final class ByteStream implements Supplier<Byte> {

    private final InputStream inputStream;

    private final byte[] buffer = new byte[128];
    private int pointer;
    private int bytesInBuffer;
    private byte b;

    ByteStream(InputStream inputStream) {
        this.pointer = -1;
        this.bytesInBuffer = -1;
        this.inputStream = inputStream;
    }

    public Byte get() {
        return b;
    }

    boolean next() {
        if (pointer == bytesInBuffer) {
            int read;
            try {
                read = inputStream.read(buffer, 0, buffer.length);
            }
            catch (IOException ioException) {
                throw new UncheckedIOException(ioException);
            }
            if (read <= 0) { // EOF
                pointer = bytesInBuffer;
                return false;
            }

            bytesInBuffer = read;
            pointer = 0;
        }
        b = buffer[pointer++];
        return true;
    }
}