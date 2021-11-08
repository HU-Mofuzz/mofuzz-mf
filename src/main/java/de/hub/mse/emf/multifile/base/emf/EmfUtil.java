package de.hub.mse.emf.multifile.base.emf;

import com.pholser.junit.quickcheck.random.SourceOfRandomness;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.ConstructorUtils;
import org.eclipse.emf.ecore.*;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class EmfUtil {

    private final int MAXIMUM_MANY_ATTRIBUTE_COUNT = 100;
    private final float EMPTY_STRING_CHANCE = 0.1f;

    public EClass getRandomEClassFromPackage(EPackage ePackage, SourceOfRandomness source) {
        var classifier = ePackage.getEClassifiers();
        Object clazz;
        do {
            clazz = classifier.get(source.nextInt(0, classifier.size() - 1));
        } while (!(clazz instanceof EClass));
        return (EClass) clazz;
    }

    public void setRandomValueForAttribute(EObject object, EAttribute attribute, SourceOfRandomness source) {
        if(attribute.isMany()) {
            getRandomManyAttribute(object, attribute, source);
        } else {
            object.eSet(attribute, getRandomSingularAttribute(attribute, source));
        }
    }

    private void getRandomManyAttribute(EObject object, EAttribute attribute, SourceOfRandomness source) {
        var resultingValues = (List<Object>)object.eGet(attribute);
        var count = getRandomCountForAttribute(attribute, source);
        for (int i = 0; i < count; i++) {
            var value = getRandomSingularAttribute(attribute, source);
            if(value != null) {
                resultingValues.add(value);
            }
        }
    }

    private int getRandomCountForAttribute(EAttribute attribute, SourceOfRandomness source) {
        int upperBound = attribute.getUpperBound() == EAttribute.UNBOUNDED_MULTIPLICITY ?
                MAXIMUM_MANY_ATTRIBUTE_COUNT : attribute.getUpperBound();

        return source.nextInt(attribute.getLowerBound(), upperBound);
    }

    private Object getRandomSingularAttribute(EAttribute attribute, SourceOfRandomness source) {
        Class<?> clazz = attribute.getEAttributeType().getInstanceClass();
        if(attribute.getEAttributeType() instanceof EEnum eEnum) {
            var size = eEnum.getELiterals().size();
            if (size > 0) {
            return eEnum.getELiterals().get(source.nextInt(size)).getInstance();
            } else {
                return null;
            }
        } else if(ClassUtils.isPrimitiveOrWrapper(clazz)) {
            if(ClassUtils.isPrimitiveWrapper(clazz)) {
                clazz = ClassUtils.wrapperToPrimitive(clazz);
            }
            if (clazz == boolean.class) {
                return source.nextBoolean();
            } else if (clazz == byte.class) {
                return source.nextByte(Byte.MIN_VALUE, Byte.MAX_VALUE);
            } else if (clazz == char.class) {
                return (char) source.nextInt(65_535);
            } else if (clazz == double.class) {
                return source.nextDouble();
            } else if (clazz == float.class) {
                return source.nextFloat();
            } else if (clazz == int.class) {
                return source.nextInt();
            } else if (clazz == long.class) {
                return source.nextLong();
            } else if (clazz == short.class) {
                return source.nextShort(Short.MIN_VALUE, Short.MAX_VALUE);
            } else {
                throw new IllegalArgumentException();
            }
        } else {
            // higher object
            if(clazz == String.class) {
                if(source.nextFloat() < EMPTY_STRING_CHANCE) {
                    return StringUtils.EMPTY;
                } else {
                    return  RandomStringUtils.random(source.nextByte((byte)1, Byte.MAX_VALUE), 0, 0, true, true, null,
                            source.toJDKRandom());
                }
            }
            return tryInstantiate(clazz);
        }
    }

    private static Object tryInstantiate(Class<?> clazz) {
        try {
            return ConstructorUtils.invokeConstructor(clazz);
        } catch (NullPointerException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException e) {
            return null;
        }
    }
}
