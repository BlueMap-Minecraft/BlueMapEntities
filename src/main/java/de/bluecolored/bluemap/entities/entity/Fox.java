/*
 * This file is part of BlueMap, licensed under the MIT License (MIT).
 *
 * Copyright (c) Blue (Lukas Rieger) <https://bluecolored.de>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.bluecolored.bluemap.entities.entity;

import de.bluecolored.bluenbt.NBTDeserializer;
import de.bluecolored.bluenbt.NBTName;
import de.bluecolored.bluenbt.NBTReader;
import de.bluecolored.bluenbt.TypeDeserializer;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.IOException;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString
@SuppressWarnings("FieldMayBeFinal")
public class Fox extends AgeEntity {

    @NBTName("Sitting") boolean sitting;
    @NBTName("Sleeping") boolean sleeping;
    @NBTName("Type") Type type;

    @NBTDeserializer(Fox.Deserializer.class)
    public enum Type {
        RED,
        SNOW
    }

    public static class Deserializer implements TypeDeserializer<Fox.Type> {

        @Override
        public Fox.Type read(NBTReader reader) throws IOException {
            return switch (reader.nextString()) {
                case "snow" -> Fox.Type.SNOW;
                default -> Fox.Type.RED;
            };
        }

    }

}
