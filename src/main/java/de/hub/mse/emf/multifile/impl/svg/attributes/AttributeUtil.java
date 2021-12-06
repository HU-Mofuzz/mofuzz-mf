package de.hub.mse.emf.multifile.impl.svg.attributes;

import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.NotImplementedException;

import java.util.HashMap;
import java.util.Map;

@UtilityClass
public class AttributeUtil {

    public static final String[] COLOR_NAMES = {
            "black", "silver", "gray", "white", "maroon", "red", "purple", "fuchsia", "green", "lime", "olive",
            "yellow", "navy", "blue", "teal", "aqua", "orange", "aliceblue", "antiquewhite", "aquamarine", "azure",
            "beige", "bisque", "blanchedalmond", "blueviolet", "brown", "burlywood", "cadetblue", "chartreuse",
            "chocolate", "coral", "cornflowerblue", "cornsilk", "crimson", "cyan", "darkblue", "darkcyan",
            "darkgoldenrod", "darkgray", "darkgreen", "darkgrey", "darkkhaki", "darkmagenta", "darkolivegreen",
            "darkorange", "darkorchid", "darkred", "darksalmon", "darkseagreen", "darkslateblue", "darkslategray",
            "darkslategrey", "darkturquoise", "darkviolet", "deeppink", "deepskyblue", "dimgray", "dimgrey",
            "dodgerblue", "firebrick", "floralwhite", "forestgreen", "gainsboro", "ghostwhite", "gold", "goldenrod",
            "greenyellow", "grey", "honeydew", "hotpink", "indianred", "indigo", "ivory", "khaki", "lavender",
            "lavenderblush", "lawngreen", "lemonchiffon", "lightblue", "lightcoral", "lightcyan",
            "lightgoldenrodyellow", "lightgray", "lightgreen", "lightgrey", "lightpink", "lightsalmon", "lightseagreen",
            "lightskyblue", "lightslategray", "lightslategrey", "lightsteelblue", "lightyellow", "limegreen", "linen",
            "magenta", "mediumaquamarine", "mediumblue", "mediumorchid", "mediumpurple", "mediumseagreen",
            "mediumslateblue", "mediumspringgreen", "mediumturquoise", "mediumvioletred", "midnightblue",
            "mintcream", "mistyrose", "moccasin", "navajowhite", "oldlace", "olivedrab", "orangered", "orchid",
            "palegoldenrod", "palegreen", "paleturquoise", "palevioletred", "papayawhip", "peachpuff", "peru", "pink",
            "plum", "powderblue", "rosybrown", "royalblue", "saddlebrown", "salmon", "sandybrown", "seagreen",
            "seashell", "sienna", "skyblue", "slateblue", "slategray", "slategrey", "snow", "springgreen", "steelblue",
            "tan", "thistle", "tomato", "turquoise", "violet", "wheat", "whitesmoke", "yellowgreen",
    };

    private final Map<String, SvgAttributeGenerator> GENERATOR_MAP = new AttributeGeneratorMap();

    private final SvgAttributeGenerator NULL_GENERATOR = source -> null;

    public String generateRandomValueForAttribute(String attributeName, SourceOfRandomness source) {
        if(attributeName == null) {
            return null;
        }
        return GENERATOR_MAP.getOrDefault(attributeName, NULL_GENERATOR).generateRandom(source);
    }
}
