package de.hub.mse.emf.multifile.impl.svg.attributes;

import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import lombok.experimental.Delegate;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

/**
 * attributeName (??)
 * base (??)
 * clipPath  (??)
 * content (??)
 * contentScriptType (MIME-TYPE)
 * contentStyleType (MIME-TYPE
 * enable-background (text or coordinates)
 * keySplines (float point list with formatting)
 * preserveAspectRatio (enum value +  enum value)
 * requiredExtensions (??)
 * requiredFeatures (list of enums)
 * style (list of generator results)
 * systemLanguage (enum value)
 * type (must be solved in attrib util)
 * type1 (propably to avoid colision with type
 * viewTarget (Non existend anymore)
 */
public class AttributeGeneratorMap implements Map<String, SvgAttributeGenerator> {

    public static final float PREDEFINED_COLOR_CHANCE = 0.5f;
    public static final float INDEFINITE_CLOCK_CHANCE = 0.5f;

    public static final float NUMBER_MIN = -10_000f;
    public static final float NUMBER_MAX = 10_000f;

    public static final SvgAttributeGenerator EMPTY_STRING_GENERATOR = source -> StringUtils.EMPTY;

    public static final SvgAttributeGenerator ANGLE_GENERATOR = new AngleGenerator();
    public static final SvgAttributeGenerator ATTRIBUTE_TYPE_GENERATOR = new AttributeTypeGenerator();
    public static final SvgAttributeGenerator BASELINE_SHIFT_GENERATOR = new BaselineShiftGenerator();
    public static final SvgAttributeGenerator BASIC_SHAPE_GENERATOR = new BasicShapeGenerator();
    public static final SvgAttributeGenerator CLIP_GENERATOR = new ClipGenerator();
    public static final SvgAttributeGenerator CLOCK_GENERATOR = new ClockGenerator();
    public static final SvgAttributeGenerator COLOR_GENERATOR = new ColorGenerator();
    public static final SvgAttributeGenerator CURSOR_GENERATOR = new CursorGenerator();
    public static final SvgAttributeGenerator FREQUENCY_GENERATOR = new FrequencyGenerator();
    public static final SvgAttributeGenerator FONT_FAMILY_GENERATOR = new FontFamilyGenerator();
    public static final SvgAttributeGenerator IRI_GENERATOR = new IriGenerator();
    public static final SvgAttributeGenerator LENGTH_GENERATOR = new LengthGenerator();
    public static final SvgAttributeGenerator NAME_GENERATOR = new NameGenerator();
    public static final SvgAttributeGenerator NUMBER_GENERATOR = new NumberGenerator();
    public static final SvgAttributeGenerator NUMBER_LIST_GENERATOR = new NumberListGenerator();
    public static final SvgAttributeGenerator OPACITY_GENERATOR = new OpacityGenerator();
    public static final SvgAttributeGenerator ORIENT_GENERATOR = new OrientGenerator();
    public static final SvgAttributeGenerator PERCENT_GENERATOR = new PercentGenerator();
    public static final SvgAttributeGenerator POINT_GENERATOR = new PointGenerator();
    public static final SvgAttributeGenerator POLYGON_GENERATOR = new PolygonGenerator();
    public static final SvgAttributeGenerator PATH_GENERATOR = new PathGenerator();
    public static final SvgAttributeGenerator REF_X_GENERATOR = new RefXGenerator();
    public static final SvgAttributeGenerator SYSTEM_LANGUAGE_GENERATOR = new SystemLanguageGenerator();
    public static final SvgAttributeGenerator TEXT_DECORATION_GENERATOR = new TextDecorationGenerator();
    public static final SvgAttributeGenerator TIME_GENERATOR = new TimeGenerator();
    public static final SvgAttributeGenerator TRANSFORM_FUNCTION_GENERATOR = new TransformFunctionGenerator();
    public static final SvgAttributeGenerator VALUE_LIST_GENERATOR = new ValueListGenerator();
    public static final SvgAttributeGenerator VIEW_BOX_GENERATOR = new ViewBoxGenerator();

    @Delegate
    private final Map<String, SvgAttributeGenerator> map = new HashMap<>();

    public AttributeGeneratorMap() {
        map.put("attributeType", ATTRIBUTE_TYPE_GENERATOR);
        map.put("begin", CLOCK_GENERATOR);
        map.put("baseline-shift", BASELINE_SHIFT_GENERATOR);
        map.put("by", NUMBER_GENERATOR);
        map.put("cx", LENGTH_GENERATOR);
        map.put("cy", LENGTH_GENERATOR);
        map.put("clip", CLIP_GENERATOR);
      //  map.put("clip-path", BASIC_SHAPE_GENERATOR);
        map.put("color", COLOR_GENERATOR);
        map.put("cursor", CURSOR_GENERATOR);
        map.put("d", PATH_GENERATOR);
        map.put("dur", CLOCK_GENERATOR);
        map.put("end", CLOCK_GENERATOR);
        map.put("fill", COLOR_GENERATOR);
        map.put("fill-opacity", OPACITY_GENERATOR);
        map.put("filter", IRI_GENERATOR);
        map.put("filterRes", NUMBER_GENERATOR);
        map.put("flood-color", COLOR_GENERATOR);
        map.put("flood-opacity", OPACITY_GENERATOR);
        map.put("font-family", FONT_FAMILY_GENERATOR);
        map.put("font-size", LENGTH_GENERATOR);
        map.put("font-size-adjust", NUMBER_GENERATOR);
        map.put("from", NUMBER_GENERATOR);
        map.put("fx", NUMBER_GENERATOR);
        map.put("fy", NUMBER_GENERATOR);
        map.put("gradientTransform", TRANSFORM_FUNCTION_GENERATOR);
        map.put("glyph-orientation-horizontal", ANGLE_GENERATOR);
        map.put("glyph-orientation-vertical", ANGLE_GENERATOR);
        map.put("height", LENGTH_GENERATOR);
        map.put("id", NAME_GENERATOR);
        map.put("keyTimes", VALUE_LIST_GENERATOR);
        map.put("letter-spacing", NUMBER_GENERATOR);
        map.put("markerHeight", LENGTH_GENERATOR);
        map.put("markerWidth", LENGTH_GENERATOR);
        map.put("lighting-color", COLOR_GENERATOR);
        map.put("marker-end", IRI_GENERATOR);
        map.put("marker-mind", IRI_GENERATOR);
        map.put("marker-start", IRI_GENERATOR);
        map.put("mask", IRI_GENERATOR);
        map.put("max", CLOCK_GENERATOR);
        map.put("media", EMPTY_STRING_GENERATOR);
        map.put("min", CLOCK_GENERATOR);
        map.put("onabort", EMPTY_STRING_GENERATOR);
        map.put("onactivate", EMPTY_STRING_GENERATOR);
        map.put("onbegin", EMPTY_STRING_GENERATOR);
        map.put("onclick", EMPTY_STRING_GENERATOR);
        map.put("onend", EMPTY_STRING_GENERATOR);
        map.put("onerror", EMPTY_STRING_GENERATOR);
        map.put("onfocusin", EMPTY_STRING_GENERATOR);
        map.put("onfocusout", EMPTY_STRING_GENERATOR);
        map.put("onload", EMPTY_STRING_GENERATOR);
        map.put("onmousedown", EMPTY_STRING_GENERATOR);
        map.put("onmousemove", EMPTY_STRING_GENERATOR);
        map.put("onmouseout", EMPTY_STRING_GENERATOR);
        map.put("onmouseover", EMPTY_STRING_GENERATOR);
        map.put("onmouseup", EMPTY_STRING_GENERATOR);
        map.put("onrepeat", EMPTY_STRING_GENERATOR);
        map.put("onresize", EMPTY_STRING_GENERATOR);
        map.put("onscroll", EMPTY_STRING_GENERATOR);
        map.put("onunload", EMPTY_STRING_GENERATOR);
        map.put("onzoom", EMPTY_STRING_GENERATOR);
        map.put("opacity", OPACITY_GENERATOR);
        map.put("orient", ORIENT_GENERATOR);
        map.put("patternTransform", TRANSFORM_FUNCTION_GENERATOR);
        map.put("points", POINT_GENERATOR);
        map.put("r", LENGTH_GENERATOR);
        map.put("refX", REF_X_GENERATOR);
        map.put("repeatCount", NUMBER_GENERATOR);
        map.put("repeatDur", CLOCK_GENERATOR);
        map.put("rx", LENGTH_GENERATOR);
        map.put("ry", LENGTH_GENERATOR);
        map.put("stop-opacity", OPACITY_GENERATOR);
        map.put("stop-color", COLOR_GENERATOR);
        map.put("stroke", COLOR_GENERATOR);
        map.put("stroke-dasharray", NUMBER_LIST_GENERATOR);
        map.put("stroke-dashoffset", NUMBER_GENERATOR);
        map.put("stroke-miterlimit", NUMBER_GENERATOR);
        map.put("stroke-opacity", OPACITY_GENERATOR);
        map.put("stroke-width", LENGTH_GENERATOR);
        map.put("systemLanguage", SYSTEM_LANGUAGE_GENERATOR);
        map.put("text-decoration", TEXT_DECORATION_GENERATOR);
        map.put("textLength", LENGTH_GENERATOR);
        map.put("to", NUMBER_GENERATOR);
        map.put("transform", TRANSFORM_FUNCTION_GENERATOR);
        map.put("values", VALUE_LIST_GENERATOR);
        map.put("viewBox", VIEW_BOX_GENERATOR);
        map.put("width", LENGTH_GENERATOR);
        map.put("word-spacing", LENGTH_GENERATOR);
        map.put("x", NUMBER_GENERATOR);
        map.put("x1", NUMBER_GENERATOR);
        map.put("x2", NUMBER_GENERATOR);
        map.put("y", NUMBER_GENERATOR);
        map.put("y1", NUMBER_GENERATOR);
        map.put("y2", NUMBER_GENERATOR);

        SvgAttributeGenerator STYLE_GENERATOR = new StyleGenerator();
        map.put("style", STYLE_GENERATOR);
    }


    private class StyleGenerator implements SvgAttributeGenerator {

        @Override
        public String generateRandom(SourceOfRandomness source) {

            List<Entry<String, SvgAttributeGenerator>> list = new ArrayList<>(map.entrySet());
            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < source.nextInt(1, 10); i++) {
                String key = null;
                String value = null;
                do {
                    var entry = source.choose(list);
                    key = entry.getKey();
                    value = entry.getValue().generateRandom(source);
                } while (StringUtils.isAllBlank(key) || StringUtils.isAllBlank(value));

                builder.append(key)
                        .append(":")
                        .append(value)
                        .append((";"));

            }
            return builder.toString();
        }
    }

    private static class SystemLanguageGenerator implements SvgAttributeGenerator {

        @Override
        public String generateRandom(SourceOfRandomness source) {
            int additionalLanguages = source.nextInt(3);
            String[] langs = new String[additionalLanguages + 1];
            for (int i = 0; i < additionalLanguages; i++) {
                langs[i] = source.choose(Locale.getISOLanguages());
            }
            langs[langs.length - 1] = Locale.getDefault().getLanguage();
            return StringUtils.join(langs, ", ");
        }
    }

    private static class AngleGenerator implements SvgAttributeGenerator {
        private static final String[] ANGLE_UNITS = {"deg", "grad", "rad"};

        @Override
        public String generateRandom(SourceOfRandomness source) {
            return source.nextFloat(-360f, 360f) + source.choose(ANGLE_UNITS);
        }
    }

    private static class AttributeTypeGenerator implements SvgAttributeGenerator {
        private static final String[] TYPES = {"CSS", "XML", "auto"};

        @Override
        public String generateRandom(SourceOfRandomness source) {
            return source.choose(TYPES);
        }
    }

    private static class BaselineShiftGenerator implements SvgAttributeGenerator {

        @Override
        public String generateRandom(SourceOfRandomness source) {
            switch (source.nextInt(3)) {
                case 0: return "sub";
                case 1: return "super";
                case 2: return LENGTH_GENERATOR.generateRandom(source);
                default: return null;
            }
        }
    }


    /**
     *
     * THIS DOES NOT WORK PROPERLY YET
     *
     * Note: circle and ellipse are generated in center
     */
    private static class BasicShapeGenerator implements SvgAttributeGenerator {
        private static final String[] SHAPES = {"inset", "circle", "ellipse", "polygon", "path", "none"};
        private static final String[] BOXES = {"view-box", "fill-box", "stroke-box", "content-box", "margin-box", "border-box", "padding-box", ""};


        @Override
        public String generateRandom(SourceOfRandomness source) {
            var shape = source.choose(SHAPES);
            
            String inner;
            switch (shape) {
                case "inset": inner = LENGTH_GENERATOR.generateRandom(source)+" "+LENGTH_GENERATOR.generateRandom(source);
                    break;
                case "circle": inner = NUMBER_GENERATOR.generateRandom((source)) + LENGTH_GENERATOR.generateRandom(source);
                    break;
                case "ellipse": inner = NUMBER_GENERATOR.generateRandom((source)) + LENGTH_GENERATOR.generateRandom(source) +
                        " " + NUMBER_GENERATOR.generateRandom((source)) + LENGTH_GENERATOR.generateRandom(source);
                    break;
                case "polygon": inner = POLYGON_GENERATOR.generateRandom(source);
                    break;
                case "path": inner = PATH_GENERATOR.generateRandom(source);
                    break;
                case "none":
                default: inner = "";
            }
            return shape + "(" + inner + ")";
        }
    }


    private static class ClipGenerator implements SvgAttributeGenerator {

        @Override
        public String generateRandom(SourceOfRandomness source) {
            switch (source.nextInt(3)) {
                case 0: return "inherit";
                case 1: return "auto";
                case 2: return String.format("rect(%s %s %s %s)",
                        LENGTH_GENERATOR.generateRandom(source),
                        LENGTH_GENERATOR.generateRandom(source),
                        LENGTH_GENERATOR.generateRandom(source),
                        LENGTH_GENERATOR.generateRandom(source));
                default: return null;
            }
        }
    }

    private static class ClockGenerator implements SvgAttributeGenerator {
        private static final String[] METRICS = {"h", "min", "s", "ms"};

        @Override
        public String generateRandom(SourceOfRandomness source) {
            if (source.nextFloat() < INDEFINITE_CLOCK_CHANCE) {
                return "indefinite";
            } else {
                var format = source.nextInt(3);
                int hour;
                int minutes;
                float seconds;
                switch (format) {
                    case 0:
                        // full clock
                        hour = source.nextInt(60);
                        minutes = source.nextInt(60);
                        seconds = source.nextFloat(0f, 60f);
                        return String.format("%d:%d:%f", hour, minutes, seconds);
                    case 1:
                        // partial clock
                        minutes = source.nextInt(60);
                        seconds = source.nextFloat(0f, 60f);
                        return String.format("%d:%f", minutes, seconds);
                    case 2: // time count
                            return source.nextFloat(0f, 100f) + source.choose(METRICS);
                    default: return null;
                }
            }
        }
    }

    private static class ColorGenerator implements SvgAttributeGenerator {

        @Override
        public String generateRandom(SourceOfRandomness source) {
            if (source.nextFloat() < PREDEFINED_COLOR_CHANCE) {
                return String.format("#%s%s%s", Integer.toString(source.nextInt(256), 16),
                        Integer.toString(source.nextInt(256), 16),
                        Integer.toString(source.nextInt(256), 16));
            } else {
                return source.choose(AttributeUtil.COLOR_NAMES);
            }
        }
    }

    private static class CursorGenerator implements SvgAttributeGenerator {

        private static final String[] CURSORS = {
                // default
                "auto", "crosshair", "default", "pointer", "move", "e-resize", "ne-resize", "nw-resize", "n-resize",
                "se-resize", "sw-resize", "s-resize", "w-resize", "text", "wait", "help",
                // special
                "inherit"
        };

        @Override
        public String generateRandom(SourceOfRandomness source) {
            return source.choose(CURSORS);
        }
    }

    private static class FrequencyGenerator implements SvgAttributeGenerator {

        private static final String[] FREQ_UNITS = {"Hz", "kHz"};

        @Override
        public String generateRandom(SourceOfRandomness source) {
            return source.nextInt() + source.choose(FREQ_UNITS);
        }
    }

    private static class FontFamilyGenerator implements SvgAttributeGenerator {
        private static final String[] FONT_FAMILIES = {
                "serif", "sans-serif", "cursive", "fantasy", "monospace"
        };

        @Override
        public String generateRandom(SourceOfRandomness source) {
            return source.choose(FONT_FAMILIES);
        }
    }

    private static class IriGenerator implements SvgAttributeGenerator {
        //todo

        @Override
        public String generateRandom(SourceOfRandomness source) {
            return EMPTY_STRING_GENERATOR.generateRandom(source);
        }
    }

    private static class LengthGenerator implements SvgAttributeGenerator {

        private static final String[] SIZE_UNITS = {
                "",
                // absolute
                "cm", "mm", "in", "px", "pt", "pc",
                //relative
                "em", "ex", "ch", "rem", "vw", "vh", "vmin", "vmax", "%"
        };

        @Override
        public String generateRandom(SourceOfRandomness source) {
            return Math.abs(source.nextInt()) + source.choose(SIZE_UNITS);
        }
    }

    private static class NameGenerator implements SvgAttributeGenerator {
        @Override
        public String generateRandom(SourceOfRandomness source) {
            return UUID.randomUUID().toString()
                    .replace("-", "");
        }
    }

    private static class NumberGenerator implements SvgAttributeGenerator {
        @Override
        public String generateRandom(SourceOfRandomness source) {
            return Float.toString(source.nextFloat(NUMBER_MIN, NUMBER_MAX));
        }
    }


    private static class NumberListGenerator implements SvgAttributeGenerator {
        @Override
        public String generateRandom(SourceOfRandomness source) {

            StringBuilder numbers = new StringBuilder();
            for (int i = 0; i < source.nextInt(2, 10); i++) {
                numbers.append(source.nextInt()).append(" ");
            }
            return numbers.toString();
        }
    }

    private static class OpacityGenerator implements SvgAttributeGenerator {

        @Override
        public String generateRandom(SourceOfRandomness source) {
            return Float.toString(source.nextFloat());
        }
    }

    private static class OrientGenerator implements SvgAttributeGenerator {

        @Override
        public String generateRandom(SourceOfRandomness source) {
            switch (source.nextInt(4)) {
                case 0: return "auto";
                case 1: return "auto-start-reverse";
                case 2: return ANGLE_GENERATOR.generateRandom(source);
                case 3: return NUMBER_GENERATOR.generateRandom(source);
                default: return null;
            }
        }
    }

    private static class PercentGenerator implements SvgAttributeGenerator {

        @Override
        public String generateRandom(SourceOfRandomness source) {
            return source.nextFloat(-200f, 200f) + "%";
        }
    }

    private static class PointGenerator implements SvgAttributeGenerator {

        @Override
        public String generateRandom(SourceOfRandomness source) {
            StringBuilder numbers = new StringBuilder();
            for (int i = 0; i < source.nextInt(1, 10); i++) {
                numbers.append(source.nextInt()).append(",").append(source.nextInt()).append((" "));
            }
            return numbers.toString();
        }
    }

    private static class PolygonGenerator implements SvgAttributeGenerator {

        @Override
        public String generateRandom(SourceOfRandomness source) {
            StringBuilder numbers = new StringBuilder();
            for (int i = 0; i < source.nextInt(1, 10); i++) {
                numbers.append(source.nextInt()).append(LENGTH_GENERATOR.generateRandom(source)).append(" ").
                        append(source.nextInt()).append(LENGTH_GENERATOR.generateRandom(source)).append((","));
            }
            var positions = numbers.toString();
            return positions.substring(0, positions.length() - 2);
        }
    }

    private static class PathGenerator implements SvgAttributeGenerator {

        private static final String[] PATH_COMMANDS = {
                "M", "m", "L", "l", "H", "h", "V", "v", "C", "c", "S", "s", "Q", "q", "T", "t", "A", "a",
        };

        @Override
        public String generateRandom(SourceOfRandomness source) {
            String numbers = String.format("M %s,%s ", source.nextInt(100), source.nextInt(100));

            for (int i = 0; i < source.nextInt(1, 10); i++) {
                var command = source.choose(PATH_COMMANDS);
                numbers += command;

                if ("M,L,T".contains(command.toUpperCase())) {
                    numbers += String.format(" %s,%s ", source.nextInt(100), source.nextInt(100));
                } else if ("H,V".contains(command.toUpperCase())) {
                    numbers += String.format(" %s ", source.nextInt(100));
                } else if ("C".contains(command.toUpperCase())) {
                    numbers += String.format(" %s,%s %s,%s %s,%s ",
                            source.nextInt(100),
                            source.nextInt(100),
                            source.nextInt(100),
                            source.nextInt(100),
                            source.nextInt(100),
                            source.nextInt(100));
                } else if ("S,Q".contains(command.toUpperCase())) {
                    numbers += String.format(" %s,%s %s,%s ",
                            source.nextInt(100), source.nextInt(100), source.nextInt(100), source.nextInt(100));
                } else if ("A".contains(command.toUpperCase())) {
                    numbers += " ";
                    for (int j = 0; j < 7; j++) {
                        numbers += source.nextInt(100) + " ";
                    }
                }
            }

            numbers += "Z";

            return numbers;
        }
    }

    private static class RefXGenerator implements SvgAttributeGenerator {

        @Override
        public String generateRandom(SourceOfRandomness source) {
            switch (source.nextInt(5)) {
                case 0: return LENGTH_GENERATOR.generateRandom(source);
                case 1: return NUMBER_LIST_GENERATOR.generateRandom(source);
                case 2: return "left";
                case 3: return "center";
                case 4: return "right";
                default: return null;
            }
        }
    }

    private static class TextDecorationGenerator implements SvgAttributeGenerator {
        private static final String[] DECORATIONS = {
                "none", "inherit",
                "underline", "overline", "line-through", "blink"
        };

        @Override
        public String generateRandom(SourceOfRandomness source) {
            return source.choose(DECORATIONS);
        }
    }

    private static class TimeGenerator implements SvgAttributeGenerator {

        private static final String[] TIME_UNITS = {"ms", "s"};

        @Override
        public String generateRandom(SourceOfRandomness source) {
            return source.nextInt() + source.choose(TIME_UNITS);
        }
    }

    private static class TransformFunctionGenerator implements SvgAttributeGenerator {

        private String randomFunction(SourceOfRandomness source) {
            switch (source.nextInt(21)) {
                case 0: return "matrix("
                        + source.nextInt(100)
                        + (", " + source.nextInt(100)).repeat(5)
                        + ")";
                case 1: return "matrix3d("
                        + source.nextInt(100)
                        + (", " + source.nextInt(100)).repeat(8)
                        + ")";
                case 2: return "perspective("
                        + source.nextInt(1000)
                        + LENGTH_GENERATOR.generateRandom(source)
                        + ")";
                case 3: return "scale("
                        + source.nextFloat() * 100 + ")"
                        + ")";
                case 4: return "scale3d("
                        + String.join(", ", Collections.nCopies(3, (source.nextFloat() * 100) + ""))
                        + ")";
                case 5: return "scaleX(" + source.nextFloat() * 100
                        + ")";
                case 6: return "scaleY(" + source.nextFloat() * 100
                        + ")";
                case 7: return "scaleZ(" + source.nextFloat() * 100
                        + ")";
                case 8: return "translate("
                        + source.nextInt(1000)
                        + LENGTH_GENERATOR.generateRandom(source) + ", "
                        + source.nextInt(1000)
                        + LENGTH_GENERATOR.generateRandom(source)
                        + ")";
                case 9: return "translate3d("
                        + source.nextInt(1000)
                        + LENGTH_GENERATOR.generateRandom(source) + ", "
                        + source.nextInt(1000)
                        + LENGTH_GENERATOR.generateRandom(source) + ", "
                        + source.nextInt(1000)
                        + LENGTH_GENERATOR.generateRandom(source)
                        + ")";
                case 10: return "translateX("
                        + source.nextInt(1000)
                        + LENGTH_GENERATOR.generateRandom(source)
                        + ")";
                case 11: return "translateY("
                        + source.nextInt(1000)
                        + LENGTH_GENERATOR.generateRandom(source)
                        + ")";
                case 12: return "translateZ("
                        + source.nextInt(1000)
                        + LENGTH_GENERATOR.generateRandom(source)
                        + ")";
                case 13: return "rotate("
                        + source.nextInt(360) + "deg"
                        + ")";
                case 14: return "rotate3d("
                        + source.nextFloat() + ", "
                        + source.nextFloat() + ", "
                        + source.nextFloat() + ", "
                        + source.nextInt(360) + "deg"
                        + ")";
                case 15: return "rotateX("
                        + source.nextInt(360) + "deg"
                        + ")";
                case 16: return "rotateY("
                        + source.nextInt(360) + "deg"
                        + ")";
                case 17: return "rotateZ("
                        + source.nextInt(360) + "deg"
                        + ")";
                case 18: return "skew("
                        + source.nextInt(360) + "deg, "
                        + source.nextInt(360) + "deg"
                        + ")";
                case 19: return "skewX("
                        + source.nextInt(360) + "deg"
                        + ")";
                case 20: return "skewY("
                        + source.nextInt(360) + "deg"
                        + ")";
                default: return "";
            }
        }

        @Override
        public String generateRandom(SourceOfRandomness source) {
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < source.nextInt(0, 5); i++) {
                builder.append(randomFunction(source)).append(" ");
            }
            return builder.toString();
        }
    }

    private static class ViewBoxGenerator implements SvgAttributeGenerator {
        @Override
        public String generateRandom(SourceOfRandomness source) {

            return String.format("%s %s %s %s",
                    source.nextInt(1000),
                    source.nextInt(1000),
                    source.nextInt(1000),
                    source.nextInt(1000));
        }
    }

    private static class ValueListGenerator implements SvgAttributeGenerator {
        @Override
        public String generateRandom(SourceOfRandomness source) {
            StringBuilder numbers = new StringBuilder();
            for (int i = 0; i < source.nextInt(2, 10); i++) {
                numbers.append(source.nextInt()).append(";");
            }
            return numbers.toString();
        }
    }
}

