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

import de.bluecolored.bluenbt.NBTName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
@SuppressWarnings("FieldMayBeFinal")
public class VillagerData {

    public static final String DEFAULT_TYPE = "plains";
    public static final String PROFESSION_NONE = "none";
    public static final String PROFESSION_NITWIT = "nitwit";

    @NBTName("type") String type;
    @NBTName("profession") String profession;
    @NBTName("level") int level;

    /**
     * @return raw type string
     */
    public String getRawType() {
        return raw(type, DEFAULT_TYPE);
    }

    /**
     * @return raw profession string
     */
    public String getRawProfession() {
        return raw(profession, PROFESSION_NONE);
    }

    private static String raw(String value, String fallback) {
        if (value == null || value.isEmpty()) return fallback;
        String[] parts = value.split(":", 2);
        return parts[parts.length - 1];
    }

}
