package de.bluecolored.bluemap.entities.entity;

import de.bluecolored.bluenbt.NBTName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode(callSuper = true)
@ToString
@SuppressWarnings("FieldMayBeFinal")
public class AgeVariantEntity extends AgeEntity {

    @NBTName("variant") String variant;

    /**
     * Returns the raw variant string without any namespace prefix.
     * @return raw variant string
     */
    public String getRawVariant() {
        if (variant == null || variant.isEmpty()) return "";
        String[] parts = variant.split(":", 2);
        return parts[parts.length - 1];
    }
}
