package de.bluecolored.bluemap.entities.data;

import de.bluecolored.bluemap.core.util.Key;
import lombok.Data;

@Data
public class ItemStack {
    private Key id;
    private int count;
}
